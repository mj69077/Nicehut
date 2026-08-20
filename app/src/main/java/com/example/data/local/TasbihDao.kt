package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TasbihRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters ORDER BY id ASC")
    fun getAllCounters(): Flow<List<TasbihRecord>>

    @Query("SELECT * FROM tasbih_counters WHERE id = :id LIMIT 1")
    fun getCounterById(id: Long): Flow<TasbihRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(record: TasbihRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounters(records: List<TasbihRecord>)

    @Update
    suspend fun updateCounter(record: TasbihRecord)

    @Query("UPDATE tasbih_counters SET currentCount = :currentCount, totalRounds = :rounds, totalAllTime = :total WHERE id = :id")
    suspend fun updateCounts(id: Long, currentCount: Int, rounds: Int, total: Long)

    @Query("UPDATE tasbih_counters SET currentCount = 0 WHERE id = :id")
    suspend fun resetCounter(id: Long)

    @Query("SELECT COUNT(*) FROM tasbih_counters")
    suspend fun getCountersCount(): Int
}
