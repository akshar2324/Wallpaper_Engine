package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.ScheduleDao
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val scheduleCount: Flow<Int> = scheduleDao.getScheduleCount()

    val conflictingSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules().map { list ->
        val enabled = list.filter { it.isEnabled }
        val conflicts = mutableListOf<ScheduleEntity>()
        val timeMap = mutableMapOf<Pair<Int, Int>, MutableList<ScheduleEntity>>()

        enabled.forEach { schedule ->
            val key = Pair(schedule.timeHour, schedule.timeMinute)
            timeMap.getOrPut(key) { mutableListOf() }.add(schedule)
        }

        timeMap.values.filter { it.size > 1 }.forEach { conflicts.addAll(it) }
        conflicts
    }

    suspend fun getScheduleById(id: Long): ScheduleEntity? = scheduleDao.getScheduleById(id)

    suspend fun saveSchedule(schedule: ScheduleEntity): Long {
        return scheduleDao.insertSchedule(schedule)
    }

    suspend fun toggleScheduleEnabled(id: Long, isEnabled: Boolean) {
        scheduleDao.updateScheduleEnabled(id, isEnabled)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }
}
