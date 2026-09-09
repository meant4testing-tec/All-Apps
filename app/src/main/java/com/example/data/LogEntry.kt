package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_calculations")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "LOG" or "ANTILOG"
    val base: Double,
    val baseDisplay: String, // e.g. "10", "e", "2", "3.5"
    val argument: Double,
    val result: Double,
    val expression: String, // e.g. "log₁₀(100) = 2"
    val timestamp: Long = System.currentTimeMillis()
)
