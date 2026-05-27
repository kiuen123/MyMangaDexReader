package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.AppPreferences
import com.example.mymangadexreader.data.ChapterNavigationManager
import com.example.mymangadexreader.data.ReadingHistoryManager
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReadingMode { SCROLL, PAGE }

data class ReaderUiState(
    val chapterId: String = "",
    val pages: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chapterTitle: String = "",
    val currentPage: Int = 0,
    val readingMode: ReadingMode = ReadingMode.valueOf(AppPreferences.readingMode),
    val hasNextChapter: Boolean = false,
    val nextChapterTitle: String = "",
    val hasPrevChapter: Boolean = false
)

class ReaderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadChapter(chapterId: String, chapterTitle: String) {
        val currentMode = _uiState.value.readingMode
        ChapterNavigationManager.setCurrentChapter(chapterId)
        val next = ChapterNavigationManager.nextChapter
        val prev = ChapterNavigationManager.prevChapter
        _uiState.value = ReaderUiState(
            chapterId = chapterId,
            isLoading = true,
            chapterTitle = chapterTitle,
            readingMode = currentMode,
            hasNextChapter = next != null,
            nextChapterTitle = next?.title ?: "",
            hasPrevChapter = prev != null
        )
        viewModelScope.launch {
            when (val result = MangaRepository.getChapterPages(chapterId)) {
                is Result.Success -> {
                    val atHome = result.data
                    val pageUrls = atHome.chapter.data.map { fileName ->
                        "${atHome.baseUrl}/data/${atHome.chapter.hash}/$fileName"
                    }
                    _uiState.value = _uiState.value.copy(
                        pages = pageUrls,
                        isLoading = false
                    )
                    ReadingHistoryManager.updateProgress(chapterId, 0, pageUrls.size)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun setCurrentPage(page: Int) {
        if (_uiState.value.currentPage != page) {
            _uiState.value = _uiState.value.copy(currentPage = page)
            ReadingHistoryManager.updateProgress(
                _uiState.value.chapterId,
                page,
                _uiState.value.pages.size
            )
        }
    }

    fun toggleReadingMode() {
        val newMode = if (_uiState.value.readingMode == ReadingMode.SCROLL)
            ReadingMode.PAGE else ReadingMode.SCROLL
        _uiState.value = _uiState.value.copy(readingMode = newMode)
        AppPreferences.readingMode = newMode.name   // persist
    }

    fun goToNextPage() {
        val state = _uiState.value
        if (state.currentPage < state.pages.size - 1) {
            setCurrentPage(state.currentPage + 1)
        }
    }

    fun goToPrevPage() {
        val state = _uiState.value
        if (state.currentPage > 0) {
            setCurrentPage(state.currentPage - 1)
        }
    }
}
