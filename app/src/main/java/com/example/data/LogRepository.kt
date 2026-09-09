package com.example.data

import kotlinx.coroutines.flow.Flow

class LogRepository(private val dao: LogDao) {
    val allEntries: Flow<List<LogEntry>> = dao.getAll()

    suspend fun insert(entry: LogEntry) {
        dao.insert(entry)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
