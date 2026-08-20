package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.QuranAudioPlayer
import com.example.data.local.AppDatabase
import com.example.data.local.OfflineData
import com.example.data.model.*
import com.example.data.network.PrayerCalculationEngine
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab(val title: String, val iconName: String) {
    DAILY_TASKS("الورد والمهام", "CheckCircle"),
    QURAN("المصحف", "MenuBook"),
    DUAS("الأدعية", "VolunteerActivism"),
    ATHKAR("الأذكار", "SelfImprovement"),
    FATWAS("الفتاوى والأحكام", "HelpOutline"),
    PRAYER("الصلاة والقبلة", "Compass")
}

data class UiNotification(
    val title: String,
    val message: String,
    val durationMs: Long = 2500
)

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(application, db)
    val audioPlayer = QuranAudioPlayer(application)

    // Active Tab
    private val _currentTab = MutableStateFlow(AppTab.DAILY_TASKS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Notification toast in UI
    private val _uiNotification = MutableStateFlow<UiNotification?>(null)
    val uiNotification: StateFlow<UiNotification?> = _uiNotification.asStateFlow()

    // --- Tasks State ---
    val todayTasks: StateFlow<List<DailyTask>> = repository.getTasksForDate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasksProgress: StateFlow<Float> = todayTasks.map { tasks ->
        if (tasks.isEmpty()) 0f
        else tasks.count { it.isCompleted }.toFloat() / tasks.size.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Quran State ---
    val quranProgress: StateFlow<QuranProgress> = repository.getQuranProgress()
        .map { it ?: QuranProgress() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranProgress())

    val bookmarks: StateFlow<List<Bookmark>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _surahSearchQuery = MutableStateFlow("")
    val surahSearchQuery: StateFlow<String> = _surahSearchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _selectedSurahVerses = MutableStateFlow<List<Ayah>>(emptyList())
    val selectedSurahVerses: StateFlow<List<Ayah>> = _selectedSurahVerses.asStateFlow()

    private val _selectedSurahTafsir = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedSurahTafsir: StateFlow<Map<Int, String>> = _selectedSurahTafsir.asStateFlow()

    private val _isSurahLoading = MutableStateFlow(false)
    val isSurahLoading: StateFlow<Boolean> = _isSurahLoading.asStateFlow()

    private val _quranFontSize = MutableStateFlow(24) // sp
    val quranFontSize: StateFlow<Int> = _quranFontSize.asStateFlow()

    // --- Duas State ---
    private val _selectedDuaCategory = MutableStateFlow<DuaCategory?>(null)
    val selectedDuaCategory: StateFlow<DuaCategory?> = _selectedDuaCategory.asStateFlow()

    private val _duaSearchQuery = MutableStateFlow("")
    val duaSearchQuery: StateFlow<String> = _duaSearchQuery.asStateFlow()

    val allDuas: StateFlow<List<Dua>> = repository.getAllDuas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Fatwas & Rulings State ---
    private val _selectedFatwaCategory = MutableStateFlow(FatwaCategory.ALL)
    val selectedFatwaCategory: StateFlow<FatwaCategory> = _selectedFatwaCategory.asStateFlow()

    private val _fatwaSearchQuery = MutableStateFlow("")
    val fatwaSearchQuery: StateFlow<String> = _fatwaSearchQuery.asStateFlow()

    private val _selectedScholarFilter = MutableStateFlow<String?>(null)
    val selectedScholarFilter: StateFlow<String?> = _selectedScholarFilter.asStateFlow()

    private val _selectedRulingFilter = MutableStateFlow<RulingType?>(null)
    val selectedRulingFilter: StateFlow<RulingType?> = _selectedRulingFilter.asStateFlow()

    private val _fatwasOnlyFavorites = MutableStateFlow(false)
    val fatwasOnlyFavorites: StateFlow<Boolean> = _fatwasOnlyFavorites.asStateFlow()

    val allFatwas: StateFlow<List<Fatwa>> = repository.getAllFatwas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredFatwas: StateFlow<List<Fatwa>> = combine(
        allFatwas,
        _selectedFatwaCategory,
        _fatwaSearchQuery,
        combine(
            _selectedScholarFilter,
            _selectedRulingFilter,
            _fatwasOnlyFavorites
        ) { scholar, ruling, onlyFavs ->
            Triple(scholar, ruling, onlyFavs)
        }
    ) { fatwas: List<Fatwa>, category: FatwaCategory, query: String, filters: Triple<String?, RulingType?, Boolean> ->
        val (scholar, ruling, onlyFavs) = filters
        fatwas.filter { fatwa ->
            val matchesCategory = (category == FatwaCategory.ALL || fatwa.category == category)
            val matchesQuery = query.isBlank() || (
                fatwa.question.contains(query, ignoreCase = true) ||
                fatwa.answer.contains(query, ignoreCase = true) ||
                fatwa.tags.contains(query, ignoreCase = true) ||
                fatwa.scholar.contains(query, ignoreCase = true)
            )
            val matchesScholar = scholar == null || fatwa.scholar.contains(scholar)
            val matchesRuling = ruling == null || fatwa.rulingType == ruling
            val matchesFav = !onlyFavs || fatwa.isFavorite

            matchesCategory && matchesQuery && matchesScholar && matchesRuling && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFatwa: StateFlow<Fatwa?> = allFatwas.map { list ->
        if (list.isEmpty()) null
        else {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            list[dayOfYear % list.size]
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Athkar State ---
    private val _selectedAthkarCategory = MutableStateFlow(AthkarCategory.MORNING)
    val selectedAthkarCategory: StateFlow<AthkarCategory> = _selectedAthkarCategory.asStateFlow()

    val allAthkar: StateFlow<List<AthkarItem>> = repository.getAllAthkar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Tasbih State ---
    val tasbihCounters: StateFlow<List<TasbihRecord>> = repository.getAllTasbihCounters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTasbihId = MutableStateFlow<Long?>(null)
    val activeTasbihId: StateFlow<Long?> = _activeTasbihId.asStateFlow()

    // --- Prayer Times & Qibla ---
    private val _prayerTimes = MutableStateFlow(PrayerCalculationEngine.calculatePrayerTimes())
    val prayerTimes: StateFlow<PrayerTimesData> = _prayerTimes.asStateFlow()

    private val _calculationMethod = MutableStateFlow(CalculationMethod.UMM_AL_QURA)
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _currentCity = MutableStateFlow("مكة المكرمة")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _userLat = MutableStateFlow(PrayerCalculationEngine.MAKKAH_LAT)
    private val _userLng = MutableStateFlow(PrayerCalculationEngine.MAKKAH_LNG)

    // Qibla Compass
    private val _deviceHeading = MutableStateFlow(0f)
    val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

    val qiblaAngle: StateFlow<Float> = combine(_userLat, _userLng) { lat, lng ->
        PrayerCalculationEngine.calculateQiblaAngle(lat, lng).toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Compass Sensor Manager
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null

    // Daily Hadith
    private val _todayHadith = MutableStateFlow(OfflineData.dailyHadiths.first())
    val todayHadith: StateFlow<HadithWisdom> = _todayHadith.asStateFlow()

    private var timeTickerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            // Pick a daily hadith based on day of year
            val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val hadithIndex = dayOfYear % OfflineData.dailyHadiths.size
            _todayHadith.value = OfflineData.dailyHadiths[hadithIndex]
        }

        startTimeTicker()
        initCompassSensor()
    }

    private fun startTimeTicker() {
        timeTickerJob?.cancel()
        timeTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _prayerTimes.value = PrayerCalculationEngine.calculatePrayerTimes(
                    latitude = _userLat.value,
                    longitude = _userLng.value,
                    method = _calculationMethod.value,
                    locationName = _currentCity.value
                )
            }
        }
    }

    private fun initCompassSensor() {
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            rotationSensor?.let { sensor ->
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            val azimuthDegrees = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            _deviceHeading.value = (azimuthDegrees + 360f) % 360f
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            _deviceHeading.value = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun showNotification(title: String, message: String) {
        _uiNotification.value = UiNotification(title, message)
        viewModelScope.launch {
            delay(2500)
            _uiNotification.value = null
        }
    }

    // --- Task Actions ---
    fun toggleTask(task: DailyTask) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            vibrate(50)
            if (!task.isCompleted) {
                showNotification("تقبل الله طاعتكم", "تم إتمام المهمة: ${task.title}")
            }
        }
    }

    fun incrementTask(task: DailyTask) {
        viewModelScope.launch {
            repository.incrementTaskCount(task)
            vibrate(40)
        }
    }

    fun addNewTask(title: String, category: TaskCategory, target: Int, description: String = "") {
        viewModelScope.launch {
            repository.addNewTask(
                DailyTask(
                    title = title,
                    description = description,
                    category = category,
                    targetCount = target,
                    dateString = repository.todayDateString,
                    isDefault = false
                )
            )
            showNotification("تمت الإضافة", "تمت إضافة المهمة اليومية بنجاح")
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            showNotification("تم الحذف", "تم حذف المهمة")
        }
    }

    // --- Quran Actions ---
    fun setSurahSearchQuery(query: String) {
        _surahSearchQuery.value = query
    }

    fun openSurah(surah: Surah) {
        _selectedSurah.value = surah
        _isSurahLoading.value = true
        viewModelScope.launch {
            val verses = repository.fetchVersesForSurah(surah.id)
            val tafsir = repository.fetchTafsirForSurah(surah.id)
            _selectedSurahVerses.value = verses
            _selectedSurahTafsir.value = tafsir
            _isSurahLoading.value = false

            // Update reading position
            repository.updateQuranProgress(
                surahId = surah.id,
                surahName = surah.nameArabic,
                ayahNum = 1,
                juz = surah.juzNumber,
                page = surah.startPage,
                pagesReadIncrement = 1
            )
        }
    }

    fun closeSurahReader() {
        _selectedSurah.value = null
        _selectedSurahVerses.value = emptyList()
        _selectedSurahTafsir.value = emptyMap()
    }

    fun changeQuranFontSize(delta: Int) {
        _quranFontSize.value = (_quranFontSize.value + delta).coerceIn(18, 40)
    }

    fun addBookmark(surah: Surah, ayah: Ayah) {
        viewModelScope.launch {
            repository.addBookmark(
                Bookmark(
                    surahId = surah.id,
                    surahName = surah.nameArabic,
                    ayahNumber = ayah.numberInSurah,
                    ayahText = ayah.textUthmani,
                    pageNumber = ayah.page,
                    juzNumber = ayah.juz
                )
            )
            showNotification("تم الحفظ", "تمت إضافة علامة مرجعية عند سورة ${surah.nameArabic} آية ${ayah.numberInSurah}")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
        }
    }

    fun updateKhatmahPlan(targetDays: Int, dailyPages: Int) {
        viewModelScope.launch {
            repository.updateKhatmahPlan(targetDays, dailyPages)
            showNotification("تم تحديث الخطة", "الهدف: $dailyPages صفحات يومياً لختم القرآن في $targetDays يوماً")
        }
    }

    // --- Dua Actions ---
    fun setDuaCategory(category: DuaCategory?) {
        _selectedDuaCategory.value = category
    }

    fun setDuaSearchQuery(query: String) {
        _duaSearchQuery.value = query
    }

    fun toggleDuaFavorite(dua: Dua) {
        viewModelScope.launch {
            repository.toggleDuaFavorite(dua.id, dua.isFavorite)
            vibrate(30)
        }
    }

    // --- Fatwa Actions ---
    fun setFatwaCategory(category: FatwaCategory) {
        _selectedFatwaCategory.value = category
    }

    fun setFatwaSearchQuery(query: String) {
        _fatwaSearchQuery.value = query
    }

    fun setScholarFilter(scholar: String?) {
        _selectedScholarFilter.value = scholar
    }

    fun setRulingFilter(ruling: RulingType?) {
        _selectedRulingFilter.value = ruling
    }

    fun toggleFatwasOnlyFavorites() {
        _fatwasOnlyFavorites.value = !_fatwasOnlyFavorites.value
    }

    fun toggleFatwaFavorite(fatwa: Fatwa) {
        viewModelScope.launch {
            repository.toggleFatwaFavorite(fatwa.id, fatwa.isFavorite)
            vibrate(35)
            if (!fatwa.isFavorite) {
                showNotification("المفضلة", "تمت إضافة الفتوى إلى قائمة المفضلة")
            }
        }
    }

    // --- Athkar Actions ---
    fun setAthkarCategory(category: AthkarCategory) {
        _selectedAthkarCategory.value = category
    }

    fun getAthkarForCategory(category: AthkarCategory): Flow<List<AthkarItem>> {
        return repository.getAthkarByCategory(category)
    }

    fun incrementAthkar(item: AthkarItem) {
        viewModelScope.launch {
            repository.incrementAthkarCount(item)
            vibrate(40)
            if (item.currentCount + 1 >= item.countTarget) {
                vibrate(100)
                showNotification("تم الذكر", "أتممت هذا الذكر المبارك")
            }
        }
    }

    fun resetAthkarCategory(category: AthkarCategory) {
        viewModelScope.launch {
            repository.resetAthkarCategory(category)
            showNotification("إعادة التعيين", "تمت إعادة تعيين أذكار ${category.displayName}")
        }
    }

    // --- Tasbih Actions ---
    fun selectTasbih(id: Long) {
        _activeTasbihId.value = id
    }

    fun incrementActiveTasbih(record: TasbihRecord) {
        viewModelScope.launch {
            repository.incrementTasbih(record)
            vibrate(35)
            if (record.currentCount + 1 >= record.targetCount) {
                vibrate(120)
                showNotification("مبارك!", "أتممت دورة تسبيح (${record.targetCount}) لـ ${record.title}")
            }
        }
    }

    fun resetTasbih(id: Long) {
        viewModelScope.launch {
            repository.resetTasbihCounter(id)
            showNotification("إعادة تعيين", "تمت إعادة تعيين العداد")
        }
    }

    fun addNewTasbih(title: String, target: Int) {
        viewModelScope.launch {
            repository.addNewTasbih(title, target)
            showNotification("تمت الإضافة", "تمت إضافة ذكر جديد للمسبحة")
        }
    }

    // --- Prayer Settings ---
    fun setCalculationMethod(method: CalculationMethod) {
        _calculationMethod.value = method
        _prayerTimes.value = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = _userLat.value,
            longitude = _userLng.value,
            method = method,
            locationName = _currentCity.value
        )
        showNotification("تم التغيير", "تم تحديث طريقة الحساب إلى ${method.titleArabic}")
    }

    fun setLocation(city: String, lat: Double, lng: Double) {
        _currentCity.value = city
        _userLat.value = lat
        _userLng.value = lng
        _prayerTimes.value = PrayerCalculationEngine.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            method = _calculationMethod.value,
            locationName = city
        )
        showNotification("تم تحديد الموقع", "تم ضبط الموقع على $city")
    }

    // Haptic feedback helper
    fun vibrate(durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        timeTickerJob?.cancel()
        sensorManager?.unregisterListener(this)
    }
}
