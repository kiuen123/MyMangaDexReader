package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.LanguageOption
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.model.ChapterData
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaStatus
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MangaDetailUiState(
    val manga: MangaData? = null,
    val chapters: List<ChapterData> = emptyList(),
    val isLoading: Boolean = false,
    val isChaptersLoading: Boolean = false,
    val error: String? = null,
    val selectedLanguage: LanguageOption = LanguagePreference.selectedLanguage,
    val availableLanguages: List<LanguageOption> = emptyList(),
    val mangaStatus: MangaStatus? = null,          // current user reading status
    val isSettingStatus: Boolean = false
)

class MangaDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MangaDetailUiState())
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    private var currentMangaId: String? = null

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

    fun setLanguage(lang: LanguageOption) {
        val mangaId = currentMangaId ?: return
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
        loadChapters(mangaId, lang.code)
    }

    private fun loadChapters(mangaId: String, langCode: String) {
        _uiState.value = _uiState.value.copy(isChaptersLoading = true)
        viewModelScope.launch {
            val chaptersResult = MangaRepository.getMangaChapters(mangaId, langCode)
            val chapters = when (chaptersResult) {
                is Result.Success -> chaptersResult.data.data
                else -> emptyList()
            }
            _uiState.value = _uiState.value.copy(
                chapters = chapters,
                isChaptersLoading = false
            )
        }
    }
}
