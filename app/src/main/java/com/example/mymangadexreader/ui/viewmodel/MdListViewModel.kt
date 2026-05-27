package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MdListData
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MdListUiState(
    val lists: List<MdListData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // opened list content
    val selectedList: MdListData? = null,
    val listManga: List<MangaData> = emptyList(),
    val isMangaLoading: Boolean = false,
    val mangaError: String? = null
)

class MdListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MdListUiState())
    val uiState: StateFlow<MdListUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    fun loadLists() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = MangaRepository.getUserLists()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        lists = result.data.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun selectList(list: MdListData) {
        _uiState.value = _uiState.value.copy(
            selectedList = list,
            listManga = emptyList(),
            isMangaLoading = true,
            mangaError = null
        )
        viewModelScope.launch {
            val ids = list.getMangaIds()
            when (val result = MangaRepository.getMangaByIds(ids)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        listManga = result.data.data,
                        isMangaLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isMangaLoading = false,
                        mangaError = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun clearSelectedList() {
        _uiState.value = _uiState.value.copy(
            selectedList = null,
            listManga = emptyList()
        )
    }
}

