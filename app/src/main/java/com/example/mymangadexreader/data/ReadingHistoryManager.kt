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
    private const val KEY_READ_CHAPTERS = "read_chapters"
    private const val MAX_ENTRIES = 50

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private val _entries = MutableStateFlow<List<ReadingHistoryEntry>>(emptyList())
    val entriesFlow: StateFlow<List<ReadingHistoryEntry>> = _entries.asStateFlow()

    /** Set of chapter IDs that the user has opened/started reading */
    private val _readChapterIds = MutableStateFlow<Set<String>>(emptySet())
    val readChapterIdsFlow: StateFlow<Set<String>> = _readChapterIds.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
        loadReadChaptersFromPrefs()
    }

    fun isChapterRead(chapterId: String): Boolean = chapterId in _readChapterIds.value

    fun markChapterAsRead(chapterId: String) {
        if (!::prefs.isInitialized) return
        val current = _readChapterIds.value.toMutableSet()
        if (current.add(chapterId)) {
            _readChapterIds.value = current
            prefs.edit().putString(KEY_READ_CHAPTERS, gson.toJson(current.toList())).apply()
        }
    }

    fun getReadChapterIdsForManga(mangaChapterIds: List<String>): Set<String> =
        _readChapterIds.value.intersect(mangaChapterIds.toSet())

    /** Lưu entry mới (hoặc cập nhật nếu cùng mangaId để nhóm theo manga) */
    fun saveEntry(entry: ReadingHistoryEntry) {
        markChapterAsRead(entry.chapterId)
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
        markChapterAsRead(chapterId)
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

    private fun loadReadChaptersFromPrefs() {
        val json = prefs.getString(KEY_READ_CHAPTERS, null) ?: return
        val type = object : TypeToken<List<String>>() {}.type
        _readChapterIds.value = try {
            (gson.fromJson<List<String>>(json, type) ?: emptyList()).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveToPrefs(entries: List<ReadingHistoryEntry>) {
        prefs.edit().putString(KEY_ENTRIES, gson.toJson(entries)).apply()
    }
}

