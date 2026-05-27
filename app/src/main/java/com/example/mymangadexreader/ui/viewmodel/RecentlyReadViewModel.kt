package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mymangadexreader.data.ReadingHistoryEntry
import com.example.mymangadexreader.data.ReadingHistoryManager
import kotlinx.coroutines.flow.StateFlow

class RecentlyReadViewModel : ViewModel() {
    val entries: StateFlow<List<ReadingHistoryEntry>> = ReadingHistoryManager.entriesFlow
}

