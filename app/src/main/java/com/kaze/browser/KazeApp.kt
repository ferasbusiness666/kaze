package com.kaze.browser

import android.app.Application
import androidx.room.Room
import com.kaze.browser.data.db.KazeDatabase
import com.kaze.browser.web.AdBlocker

class KazeApp : Application() {

    /** Single Room instance for the whole app. */
    val db: KazeDatabase by lazy {
        Room.databaseBuilder(this, KazeDatabase::class.java, "kaze.db").build()
    }

    override fun onCreate() {
        super.onCreate()
        // Parse the ad-block filter list off the main thread at startup.
        AdBlocker.init(this)
    }
}
