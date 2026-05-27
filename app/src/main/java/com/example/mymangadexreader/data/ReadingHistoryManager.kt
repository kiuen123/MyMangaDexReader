package com.example.mymangadexreader.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ReadingHistoryManager {

    private const val PREFS_NAME = "reading_history"
    private const val KEY_ENTRIES = "entries"
    private const val MAX_ENTRIES = 50

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private val _entries = MutableStateFlow<List<ReadingHistoryEntry>>(emptyList())
    val entriesFlow: StateFlow<List<ReadingHistoryEntry>> = _entries.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    /** Lưu entry mới (hoặc cập nhật nếu cùng mangaId để nhóm theo manga) */
    fun saveEntry(entry: ReadingHistoryEntry) {
        if (!::prefs.isInitialized) return
        val current = _entries.value.toMutableList()
        // Remove old entry for same manga (keep only latest chapter per manga)
        current.removeAll { it.mangaId == entry.mangaId }
        current.add(0, entry)
        if (current.size > MAX_ENTRIES) current.subList(MAX_ENTRIES, current.size).clear()
        _entries.value = current
        saveToPrefs(current)
    }

    /** Cập nhật tiến trình đọc */
    fun updateProgress(chapterId: String, currentPage: Int, totalPages: Int) {
        if (!::prefs.isInitialized) return
        val current = _entries.value.toMutableList()
        val idx = current.indexOfFirst { it.chapterId == chapterId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(
                currentPage = currentPage,
                totalPages = totalPages,
                lastReadAt = System.currentTimeMillis()
            )
            _entries.value = current
            saveToPrefs(current)
        }
    }

    private fun loadFromPrefs() {
        val json = prefs.getString(KEY_ENTRIES, null) ?: return
        val type = object : TypeToken<List<ReadingHistoryEntry>>() {}.type
        _entries.value = try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToPrefs(entries: List<ReadingHistoryEntry>) {
        prefs.edit().putString(KEY_ENTRIES, gson.toJson(entries)).apply()
    }
}

