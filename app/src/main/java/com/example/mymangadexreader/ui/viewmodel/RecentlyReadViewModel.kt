package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.ChapterNavigationManager
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.ReadingHistoryEntry
import com.example.mymangadexreader.data.ReadingHistoryManager
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecentlyReadViewModel : ViewModel() {
    val entries: StateFlow<List<ReadingHistoryEntry>> = ReadingHistoryManager.entriesFlow

    private val _loadingMangaIds = MutableStateFlow<Set<String>>(emptySet())
    val loadingMangaIds: StateFlow<Set<String>> = _loadingMangaIds.asStateFlow()

    /**
     * Tải danh sách chương của manga, thiết lập ChapterNavigationManager,
     * sau đó gọi [onNavigate] để điều hướng vào reader.
     */
    fun continueReading(
        entry: ReadingHistoryEntry,
        onNavigate: (chapterId: String, chapterTitle: String) -> Unit
    ) {
        if (entry.mangaId in _loadingMangaIds.value) return
        viewModelScope.launch {
            _loadingMangaIds.value = _loadingMangaIds.value + entry.mangaId
            try {
                val langCode = LanguagePreference.code
                val result = MangaRepository.getMangaChapters(entry.mangaId, langCode)
                if (result is Result.Success) {
                    val sortedChapters = result.data.data
                        .sortedBy { it.attributes.chapter?.toFloatOrNull() ?: Float.MAX_VALUE }
                    val chapterInfos = sortedChapters.map {
                        ChapterNavigationManager.ChapterInfo(it.id, it.attributes.getDisplayTitle())
                    }
                    ChapterNavigationManager.setChapterList(chapterInfos, entry.chapterId)
                }
            } finally {
                _loadingMangaIds.value = _loadingMangaIds.value - entry.mangaId
            }
            onNavigate(entry.chapterId, entry.chapterTitle)
        }
    }
}
