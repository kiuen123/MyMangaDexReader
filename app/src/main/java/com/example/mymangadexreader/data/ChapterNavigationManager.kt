package com.example.mymangadexreader.data

/** Lưu danh sách chương của manga hiện tại để hỗ trợ chuyển chương khi đọc */
object ChapterNavigationManager {
    data class ChapterInfo(val id: String, val title: String)

    private var chapters: List<ChapterInfo> = emptyList()
    private var currentIndex: Int = -1

    /** Gọi từ MangaDetailScreen khi click vào chương */
    fun setChapterList(list: List<ChapterInfo>, clickedChapterId: String) {
        chapters = list
        currentIndex = list.indexOfFirst { it.id == clickedChapterId }
    }

    /** Cập nhật index khi ReaderScreen tải một chương (để sync khi navigate) */
    fun setCurrentChapter(chapterId: String) {
        val idx = chapters.indexOfFirst { it.id == chapterId }
        if (idx >= 0) currentIndex = idx
    }

    val nextChapter: ChapterInfo? get() = chapters.getOrNull(currentIndex + 1)
    val prevChapter: ChapterInfo? get() = chapters.getOrNull(currentIndex - 1)
    val hasNext: Boolean get() = currentIndex in 0 until chapters.size - 1
    val hasPrev: Boolean get() = currentIndex > 0
}

