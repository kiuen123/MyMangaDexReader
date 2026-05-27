package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.LanguageOption
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeUiState(
    val mangaList: List<MangaData> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val hasMore: Boolean = true,
    val currentOffset: Int = 0,
    val selectedLanguage: LanguageOption = LanguagePreference.selectedLanguage
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentQuery: String = ""

    init {
        // Reload when language changes globally
        LanguagePreference.selectedLanguageFlow
            .onEach { lang ->
                _uiState.value = _uiState.value.copy(selectedLanguage = lang)
                loadInitial()
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            fetchManga(reset = true)
        }
    }

    private suspend fun fetchManga(reset: Boolean) {
        val offset = if (reset) 0 else _uiState.value.currentOffset
        val result = if (currentQuery.isBlank()) {
            MangaRepository.getMangaList(offset)
        } else {
            MangaRepository.searchManga(currentQuery, offset)
        }
        when (result) {
            is Result.Success -> {
                val newList = if (reset) result.data.data
                else _uiState.value.mangaList + result.data.data
                _uiState.value = _uiState.value.copy(
                    mangaList = newList,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    currentOffset = offset + result.data.data.size,
                    hasMore = (offset + result.data.data.size) < result.data.total
                )
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = result.message
                )
            }
            else -> {}
        }
    }

    fun onSearchQueryChange(query: String) {
        currentQuery = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            fetchManga(reset = true)
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch { fetchManga(reset = false) }
    }

    fun retry() { loadInitial() }
}
