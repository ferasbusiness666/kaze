package com.kaze.browser.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntry::class, DownloadEntry::class],
    version = 1,
    exportSchema = false, // single local DB, no migration history to track
)
abstract class KazeDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
}
