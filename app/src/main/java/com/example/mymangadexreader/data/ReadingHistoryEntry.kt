package com.example.mymangadexreader.data

data class ReadingHistoryEntry(
    val mangaId: String = "",
    val mangaTitle: String = "",
    val coverUrl: String? = null,
    val chapterId: String = "",
    val chapterTitle: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val lastReadAt: Long = System.currentTimeMillis()
)

