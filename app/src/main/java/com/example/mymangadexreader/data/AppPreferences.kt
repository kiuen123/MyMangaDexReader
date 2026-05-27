package com.example.mymangadexreader.data

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_READING_MODE = "reading_mode"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var readingMode: String
        get() = if (::prefs.isInitialized) prefs.getString(KEY_READING_MODE, "SCROLL") ?: "SCROLL" else "SCROLL"
        set(value) { if (::prefs.isInitialized) prefs.edit().putString(KEY_READING_MODE, value).apply() }
}

