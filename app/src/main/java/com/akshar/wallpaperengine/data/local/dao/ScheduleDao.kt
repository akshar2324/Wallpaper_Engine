package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY isEnabled DESC, name ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules ORDER BY id ASC")
    suspend fun getAllSchedulesList(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1")
    suspend fun getEnabledSchedules(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE triggerType = :triggerType AND isEnabled = 1 ORDER BY priority DESC")
    suspend fun getEnabledSchedulesByTrigger(triggerType: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query("SELECT COUNT(*) FROM schedules")
    fun getScheduleCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateScheduleEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE schedules SET lastExecution = :lastExecution, nextExecution = :nextExecution WHERE id = :id")
    suspend fun updateScheduleExecutionTime(id: Long, lastExecution: Long, nextExecution: Long)
}
