package com.example.mymangadexreader.data.model

enum class MangaStatus(val code: String, val display: String, val emoji: String) {
    READING("reading", "Đang đọc", "📖"),
    PLAN_TO_READ("plan_to_read", "Kế hoạch đọc", "📋"),
    COMPLETED("completed", "Hoàn thành", "✅"),
    ON_HOLD("on_hold", "Tạm dừng", "⏸"),
    RE_READING("re_reading", "Đọc lại", "🔄"),
    DROPPED("dropped", "Đã bỏ", "❌");

    companion object {
        fun fromCode(code: String?): MangaStatus? = entries.firstOrNull { it.code == code }
        val all get() = entries.toList()
    }
}

