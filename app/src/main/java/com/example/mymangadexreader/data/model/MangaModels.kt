package com.example.mymangadexreader.data.model

// ───── Manga list response ─────
data class MangaListResponse(
    val result: String,
    val data: List<MangaData>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class MangaData(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: List<Relationship>
)

data class MangaAttributes(
    val title: Map<String, String>,
    val description: Map<String, String>?,
    val status: String?,
    val contentRating: String?,
    val tags: List<Tag>?,
    val year: Int?,
    val state: String?
) {
    fun getTitle(): String =
        title["en"] ?: title.values.firstOrNull() ?: "Unknown Title"

    fun getDescription(): String =
        description?.get("en") ?: description?.values?.firstOrNull() ?: "No description."
}

data class Tag(
    val id: String,
    val attributes: TagAttributes?
)

data class TagAttributes(
    val name: Map<String, String>
) {
    fun getName(): String = name["en"] ?: name.values.firstOrNull() ?: ""
}

// ───── Relationships ─────
data class Relationship(
    val id: String,
    val type: String,
    val attributes: RelationshipAttributes?
)

data class RelationshipAttributes(
    // cover_art
    val fileName: String?,
    // author / artist
    val name: String?
)

// ───── Chapter feed ─────
data class ChapterListResponse(
    val result: String,
    val data: List<ChapterData>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class ChapterData(
    val id: String,
    val attributes: ChapterAttributes
)

data class ChapterAttributes(
    val title: String?,
    val chapter: String?,
    val volume: String?,
    val translatedLanguage: String?,
    val pages: Int,
    val publishAt: String?
) {
    fun getDisplayTitle(): String {
        val chNum = chapter?.let { "Ch.$it" } ?: ""
        val vol = volume?.let { " Vol.$it" } ?: ""
        val ttl = if (!title.isNullOrBlank()) " – $title" else ""
        return "$chNum$vol$ttl".ifBlank { "Oneshot" }
    }
}

// ───── At-home (page URLs) ─────
data class AtHomeResponse(
    val result: String,
    val baseUrl: String,
    val chapter: AtHomeChapter
)

data class AtHomeChapter(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>
)

// Helper to build cover URL
fun buildCoverUrl(mangaId: String, fileName: String, size: Int = 256): String =
    "https://uploads.mangadex.org/covers/$mangaId/$fileName.$size.jpg"

// ───── Manga reading status ─────
data class MangaStatusResponse(
    val result: String,
    val statuses: Map<String, String>   // mangaId -> status code
)

data class SetStatusRequest(val status: String?)

data class SimpleResponse(val result: String)
