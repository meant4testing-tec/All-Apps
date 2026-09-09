package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM log_calculations ORDER BY timestamp DESC LIMIT 100")
    fun getAll(): Flow<List<LogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogEntry)

    @Query("DELETE FROM log_calculations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM log_calculations")
    suspend fun clearAll()
}
