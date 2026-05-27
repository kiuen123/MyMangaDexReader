package com.example.mymangadexreader.data.model

// ───── User ─────
data class UserResponse(
    val result: String,
    val data: UserData
)

data class UserData(
    val id: String,
    val type: String,
    val attributes: UserAttributes
)

data class UserAttributes(
    val username: String,
    val roles: List<String>?,
    val version: Int?
) {
    fun isAdmin() = roles?.contains("ROLE_ADMIN") == true
    fun isModerator() = roles?.contains("ROLE_MOD") == true
    fun displayRoles(): String = roles
        ?.filter { it.startsWith("ROLE_") }
        ?.map { it.removePrefix("ROLE_").lowercase().replaceFirstChar { c -> c.uppercase() } }
        ?.joinToString(", ")
        ?: "Member"
}

// ───── Custom Lists (MDList) ─────
data class MdListResponse(
    val result: String,
    val data: List<MdListData>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class MdListData(
    val id: String,
    val type: String,
    val attributes: MdListAttributes,
    val relationships: List<Relationship>
) {
    fun getMangaIds(): List<String> =
        relationships.filter { it.type == "manga" }.map { it.id }

    fun getMangaCount(): Int =
        relationships.count { it.type == "manga" }
}

data class MdListAttributes(
    val name: String,
    val visibility: String,
    val version: Int?
)

