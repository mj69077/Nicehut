package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.OfflineData
import com.example.data.model.*
import com.example.data.network.PrayerCalculationEngine
import com.example.data.network.QuranApiService
import com.example.widget.DailyWirdAppWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val taskDao = database.taskDao()
    private val quranDao = database.quranDao()
    private val duaDao = database.duaDao()
    private val athkarDao = database.athkarDao()
    private val tasbihDao = database.tasbihDao()
    private val fatwaDao = database.fatwaDao()

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Initialize & Seed Database if needed
    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // 1. Seed Duas
        if (duaDao.getDuasCount() == 0) {
            duaDao.insertDuas(OfflineData.allDuasSeed)
        }

        // 2. Seed Athkar
        if (athkarDao.getAthkarCount() == 0) {
            athkarDao.insertAthkar(OfflineData.allAthkarSeed)
        }

        // 3. Seed Tasbih
        if (tasbihDao.getCountersCount() == 0) {
            tasbihDao.insertCounters(OfflineData.allTasbihSeed)
        }

        // 4. Seed Fatwas
        if (fatwaDao.getCount() == 0) {
            fatwaDao.insertFatwas(com.example.data.local.OfflineFatwasData.fatwasList)
        }

        // 4. Seed Quran Progress if empty
        val progress = quranDao.getQuranProgress().first()
        if (progress == null) {
            quranDao.saveQuranProgress(
                QuranProgress(
                    currentSurahId = 1,
                    currentSurahName = "الفاتحة",
                    currentAyahNumber = 1,
                    currentJuz = 1,
                    currentPage = 1,
                    dailyTargetPages = 4,
                    pagesReadToday = 0,
                    lastReadDate = todayDateString
                )
            )
        }

        // 5. Seed Daily Tasks for today if none exist
        ensureTodayTasksExist()
    }

    suspend fun ensureTodayTasksExist() = withContext(Dispatchers.IO) {
        val todayTasks = taskDao.getTasksForDate(todayDateString).first()
        if (todayTasks.isEmpty()) {
            val defaults = OfflineData.getDefaultTasks(todayDateString)
            taskDao.insertTasks(defaults)
            notifyWidgetUpdate()
        }
    }

    // Daily Tasks
    fun getTasksForDate(date: String = todayDateString): Flow<List<DailyTask>> =
        taskDao.getTasksForDate(date)

    suspend fun toggleTaskCompleted(task: DailyTask) = withContext(Dispatchers.IO) {
        val newStatus = !task.isCompleted
        val newCount = if (newStatus) task.targetCount else 0
        taskDao.updateTaskStatus(task.id, newStatus, newCount)

        // If it's a Quran task and marked completed, update today's read pages
        if (task.category == TaskCategory.QURAN && newStatus) {
            val prog = quranDao.getQuranProgress().first() ?: QuranProgress()
            quranDao.saveQuranProgress(
                prog.copy(
                    pagesReadToday = prog.dailyTargetPages,
                    lastReadDate = todayDateString
                )
            )
        }

        notifyWidgetUpdate()
    }

    suspend fun incrementTaskCount(task: DailyTask) = withContext(Dispatchers.IO) {
        val nextCount = (task.currentCount + 1).coerceAtMost(task.targetCount)
        val isDone = nextCount >= task.targetCount
        taskDao.updateTaskStatus(task.id, isDone, nextCount)
        notifyWidgetUpdate()
    }

    suspend fun addNewTask(task: DailyTask) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
        notifyWidgetUpdate()
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
        notifyWidgetUpdate()
    }

    // Quran Progress & Bookmarks
    fun getQuranProgress(): Flow<QuranProgress?> = quranDao.getQuranProgress()

    suspend fun updateQuranProgress(
        surahId: Int,
        surahName: String,
        ayahNum: Int,
        juz: Int,
        page: Int,
        pagesReadIncrement: Int = 1
    ) = withContext(Dispatchers.IO) {
        val current = quranDao.getQuranProgress().first() ?: QuranProgress()
        val isNewDay = current.lastReadDate != todayDateString
        val todayRead = if (isNewDay) pagesReadIncrement else current.pagesReadToday + pagesReadIncrement

        val updated = current.copy(
            currentSurahId = surahId,
            currentSurahName = surahName,
            currentAyahNumber = ayahNum,
            currentJuz = juz,
            currentPage = page,
            pagesReadToday = todayRead,
            lastReadDate = todayDateString
        )
        quranDao.saveQuranProgress(updated)

        // Check if daily target completed
        if (todayRead >= current.dailyTargetPages) {
            val tasks = taskDao.getTasksForDate(todayDateString).first()
            val quranTask = tasks.find { it.category == TaskCategory.QURAN }
            if (quranTask != null && !quranTask.isCompleted) {
                taskDao.updateTaskStatus(quranTask.id, true, quranTask.targetCount)
            }
        }

        notifyWidgetUpdate()
    }

    suspend fun updateKhatmahPlan(targetDays: Int, dailyPages: Int) = withContext(Dispatchers.IO) {
        val current = quranDao.getQuranProgress().first() ?: QuranProgress()
        quranDao.saveQuranProgress(
            current.copy(
                khatmahTargetDays = targetDays,
                dailyTargetPages = dailyPages
            )
        )
        notifyWidgetUpdate()
    }

    fun getAllBookmarks(): Flow<List<Bookmark>> = quranDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        quranDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(id: Long) = withContext(Dispatchers.IO) {
        quranDao.deleteBookmarkById(id)
    }

    // Duas
    fun getAllDuas(): Flow<List<Dua>> = duaDao.getAllDuas()

    fun getDuasByCategory(category: DuaCategory): Flow<List<Dua>> =
        duaDao.getDuasByCategory(category)

    fun getFavoriteDuas(): Flow<List<Dua>> = duaDao.getFavoriteDuas()

    fun searchDuas(query: String): Flow<List<Dua>> = duaDao.searchDuas(query)

    suspend fun toggleDuaFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        duaDao.updateFavoriteStatus(id, !isFavorite)
    }

    // Athkar
    fun getAllAthkar(): Flow<List<AthkarItem>> = athkarDao.getAllAthkar()

    fun getAthkarByCategory(category: AthkarCategory): Flow<List<AthkarItem>> =
        athkarDao.getAthkarByCategory(category)

    suspend fun incrementAthkarCount(item: AthkarItem) = withContext(Dispatchers.IO) {
        val newCount = (item.currentCount + 1).coerceAtMost(item.countTarget)
        val completed = newCount >= item.countTarget
        athkarDao.updateCount(item.id, newCount, completed)
    }

    suspend fun resetAthkarCategory(category: AthkarCategory) = withContext(Dispatchers.IO) {
        athkarDao.resetCategory(category)
    }

    // Tasbih
    fun getAllTasbihCounters(): Flow<List<TasbihRecord>> = tasbihDao.getAllCounters()

    suspend fun incrementTasbih(record: TasbihRecord) = withContext(Dispatchers.IO) {
        val nextCount = record.currentCount + 1
        val rounds = if (nextCount >= record.targetCount) record.totalRounds + 1 else record.totalRounds
        val current = if (nextCount >= record.targetCount) 0 else nextCount
        val total = record.totalAllTime + 1
        tasbihDao.updateCounts(record.id, current, rounds, total)
    }

    suspend fun resetTasbihCounter(id: Long) = withContext(Dispatchers.IO) {
        tasbihDao.resetCounter(id)
    }

    suspend fun addNewTasbih(title: String, target: Int) = withContext(Dispatchers.IO) {
        tasbihDao.insertCounter(
            TasbihRecord(
                title = title,
                targetCount = target
            )
        )
    }

    // Fatwas & Rulings
    fun getAllFatwas(): Flow<List<Fatwa>> = fatwaDao.getAllFatwas()

    fun getFatwasByCategory(category: FatwaCategory): Flow<List<Fatwa>> =
        if (category == FatwaCategory.ALL) fatwaDao.getAllFatwas()
        else fatwaDao.getFatwasByCategory(category)

    fun getFavoriteFatwas(): Flow<List<Fatwa>> = fatwaDao.getFavoriteFatwas()

    fun searchFatwas(query: String): Flow<List<Fatwa>> = fatwaDao.searchFatwas(query)

    suspend fun toggleFatwaFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        fatwaDao.updateFavorite(id, !isFavorite)
    }

    // Online Verses & Tafsir
    suspend fun fetchVersesForSurah(surahId: Int): List<Ayah> =
        QuranApiService.fetchVersesForSurah(surahId)

    suspend fun fetchTafsirForSurah(surahId: Int): Map<Int, String> =
        QuranApiService.fetchTafsirForSurah(surahId)

    // AppWidget update trigger
    fun notifyWidgetUpdate() {
        DailyWirdAppWidgetProvider.updateAllWidgets(context)
    }
}
