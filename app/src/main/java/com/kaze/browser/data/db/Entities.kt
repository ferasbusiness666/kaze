package com.kaze.browser.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long,
)

@Entity(tableName = "downloads")
data class DownloadEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val downloadedAt: Long,
)
