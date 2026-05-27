package com.example.mymangadexreader.data.repository

import com.example.mymangadexreader.data.api.MangaDetailResponse
import com.example.mymangadexreader.data.api.RetrofitInstance
import com.example.mymangadexreader.data.api.TokenManager
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.model.AtHomeResponse
import com.example.mymangadexreader.data.model.ChapterListResponse
import com.example.mymangadexreader.data.model.MangaListResponse
import com.example.mymangadexreader.data.model.MdListResponse
import com.example.mymangadexreader.data.model.SetStatusRequest
import com.example.mymangadexreader.data.model.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val CLIENT_ID = "personal-client-39c56d18-cf28-419e-a317-7b807a371a0a-26e7bf13"
private const val CLIENT_SECRET = "Bao92gQPEGkGskfRVMS4lzV87NA8danR"

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

object MangaRepository {

    private val authApi = RetrofitInstance.authApiService
    private val api = RetrofitInstance.mangaDexApiService
    private val authMutex = Mutex()

    /** Login with explicit credentials – saves them for re-auth */
    suspend fun authenticate(username: String, password: String, remember: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(
                grantType = "password",
                username = username,
                password = password,
                clientId = CLIENT_ID,
                clientSecret = CLIENT_SECRET
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                TokenManager.accessToken = body.accessToken
                TokenManager.refreshToken = body.refreshToken
                TokenManager.saveCredentials(username, password, remember)
                Result.Success(Unit)
            } else {
                Result.Error("Đăng nhập thất bại: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Lỗi kết nối: ${e.localizedMessage}")
        }
    }

    suspend fun getMangaList(offset: Int = 0): Result<MangaListResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        val lang = if (LanguagePreference.code == "all") emptyList() else listOf(LanguagePreference.code)
        try {
            val response = api.getMangaList(offset = offset, availableTranslatedLanguage = lang)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun searchManga(query: String, offset: Int = 0): Result<MangaListResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        val lang = if (LanguagePreference.code == "all") emptyList() else listOf(LanguagePreference.code)
        try {
            val response = api.searchManga(title = query, offset = offset, availableTranslatedLanguage = lang)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMangaDetail(mangaId: String): Result<MangaDetailResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.getMangaDetail(mangaId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMangaChapters(mangaId: String, language: String? = null): Result<ChapterListResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        val lang = if (language == null || language == "all") emptyList() else listOf(language)
        try {
            val response = api.getMangaFeed(mangaId, languages = lang)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getChapterPages(chapterId: String): Result<AtHomeResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.getChapterPages(chapterId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getUserProfile(): Result<UserResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getUserLists(): Result<MdListResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.getUserLists()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMangaByIds(ids: List<String>): Result<MangaListResponse> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        if (ids.isEmpty()) return@withContext Result.Success(
            MangaListResponse("ok", emptyList(), 0, 0, 0)
        )
        try {
            val response = api.getMangaByIds(ids = ids.take(100))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getMangaStatuses(): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.getMangaStatuses()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.statuses)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun setMangaStatus(mangaId: String, status: String?): Result<Unit> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val response = api.setMangaStatus(mangaId, SetStatusRequest(status))
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Network error")
        }
    }

    private suspend fun ensureAuthenticated() {
        if (TokenManager.accessToken == null) {
            authMutex.withLock {
                if (TokenManager.accessToken == null) {
                    val u = TokenManager.savedUsername ?: return
                    val p = TokenManager.savedPassword ?: return
                    authenticate(u, p, remember = TokenManager.rememberLogin)
                }
            }
        }
    }
}
