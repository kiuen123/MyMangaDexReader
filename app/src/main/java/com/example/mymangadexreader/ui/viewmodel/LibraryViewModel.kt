package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaStatus
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val statusMap: Map<String, String> = emptyMap(),      // mangaId -> statusCode
    val selectedStatus: MangaStatus = MangaStatus.READING,
    val mangaList: List<MangaData> = emptyList(),
    val isLoadingStatuses: Boolean = false,
    val isLoadingManga: Boolean = false,
    val error: String? = null
) {
    /** IDs for the currently selected status */
    val selectedIds: List<String>
        get() = statusMap.entries
            .filter { it.value == selectedStatus.code }
            .map { it.key }

    /** Count per status for badge display */
    fun countFor(status: MangaStatus) = statusMap.values.count { it == status.code }
}

class LibraryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatuses = true, error = null)
            when (val result = MangaRepository.getMangaStatuses()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        statusMap = result.data,
                        isLoadingStatuses = false
                    )
                    loadMangaForCurrentStatus()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStatuses = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun selectStatus(status: MangaStatus) {
        if (_uiState.value.selectedStatus == status) return
        _uiState.value = _uiState.value.copy(
            selectedStatus = status,
            mangaList = emptyList()
        )
        loadMangaForCurrentStatus()
    }

    private fun loadMangaForCurrentStatus() {
        val ids = _uiState.value.selectedIds
        if (ids.isEmpty()) {
            _uiState.value = _uiState.value.copy(mangaList = emptyList(), isLoadingManga = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingManga = true)
            when (val result = MangaRepository.getMangaByIds(ids)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        mangaList = result.data.data,
                        isLoadingManga = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingManga = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
}

