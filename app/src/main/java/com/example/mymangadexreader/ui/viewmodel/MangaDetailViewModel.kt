package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.LanguageOption
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.ReadingHistoryManager
import com.example.mymangadexreader.data.model.ChapterData
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaStatus
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MangaDetailUiState(
    val manga: MangaData? = null,
    val chapters: List<ChapterData> = emptyList(),
    val isLoading: Boolean = false,
    val isChaptersLoading: Boolean = false,
    val error: String? = null,
    val chaptersError: String? = null,             // lỗi riêng khi tải danh sách chương
    val selectedLanguage: LanguageOption = LanguagePreference.selectedLanguage,
    val availableLanguages: List<LanguageOption> = emptyList(),
    val mangaStatus: MangaStatus? = null,          // current user reading status
    val isSettingStatus: Boolean = false,
    val readChapterIds: Set<String> = emptySet(),   // chapters already read
    val lastReadChapterId: String? = null,           // for "Continue Reading" button
    val lastReadChapterTitle: String? = null,
    val sortDescending: Boolean = true              // true = mới nhất trên đầu
)

class MangaDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MangaDetailUiState())
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    private var currentMangaId: String? = null

    init {
        // Observe read chapters updates
        ReadingHistoryManager.readChapterIdsFlow
            .onEach { readIds ->
                _uiState.value = _uiState.value.copy(readChapterIds = readIds)
                updateLastReadChapter()
            }
            .launchIn(viewModelScope)
    }

    private fun updateLastReadChapter() {
        val chapters = _uiState.value.chapters
        if (chapters.isEmpty()) return
        val readIds = _uiState.value.readChapterIds
        // Find the last read chapter from the chapter list (highest index read)
        val lastReadIndex = chapters.indexOfLast { it.id in readIds }
        val lastRead = if (lastReadIndex >= 0) chapters[lastReadIndex] else null
        _uiState.value = _uiState.value.copy(
            lastReadChapterId = lastRead?.id,
            lastReadChapterTitle = lastRead?.attributes?.getDisplayTitle()
        )
    }

    fun loadManga(mangaId: String) {
        if (currentMangaId == mangaId && _uiState.value.manga != null) return
        currentMangaId = mangaId
        _uiState.value = MangaDetailUiState(isLoading = true)
        viewModelScope.launch {
            val detailResult = MangaRepository.getMangaDetail(mangaId)
            val manga = when (detailResult) {
                is Result.Success -> detailResult.data.data
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = detailResult.message)
                    return@launch
                }
                else -> return@launch
            }
            _uiState.value = _uiState.value.copy(manga = manga, isLoading = false)
            loadChapters(mangaId, _uiState.value.selectedLanguage.code)
            loadMangaStatus(mangaId)
        }
    }

    private fun loadMangaStatus(mangaId: String) {
        viewModelScope.launch {
            when (val result = MangaRepository.getMangaStatuses()) {
                is Result.Success -> {
                    val code = result.data[mangaId]
                    _uiState.value = _uiState.value.copy(mangaStatus = MangaStatus.fromCode(code))
                }
                else -> {}
            }
        }
    }

    fun setMangaStatus(status: MangaStatus?) {
        val mangaId = currentMangaId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSettingStatus = true)
            when (MangaRepository.setMangaStatus(mangaId, status?.code)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(mangaStatus = status, isSettingStatus = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isSettingStatus = false)
                }
                else -> {}
            }
        }
    }

    fun toggleSortOrder() {
        _uiState.value = _uiState.value.copy(sortDescending = !_uiState.value.sortDescending)
    }

    fun setLanguage(lang: LanguageOption) {
        val mangaId = currentMangaId ?: return
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
        loadChapters(mangaId, lang.code)
    }

    private fun loadChapters(mangaId: String, langCode: String) {
        _uiState.value = _uiState.value.copy(isChaptersLoading = true, chaptersError = null)
        viewModelScope.launch {
            val chaptersResult = MangaRepository.getMangaChapters(mangaId, langCode)
            when (chaptersResult) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        chapters = chaptersResult.data.data,
                        isChaptersLoading = false,
                        chaptersError = null
                    )
                    updateLastReadChapter()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        chapters = emptyList(),
                        isChaptersLoading = false,
                        chaptersError = chaptersResult.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isChaptersLoading = false)
                }
            }
        }
    }
}
