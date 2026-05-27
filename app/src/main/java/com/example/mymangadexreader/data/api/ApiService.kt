package com.example.mymangadexreader.data.api

import com.example.mymangadexreader.data.model.AtHomeResponse
import com.example.mymangadexreader.data.model.ChapterListResponse
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaListResponse
import com.example.mymangadexreader.data.model.MangaStatusResponse
import com.example.mymangadexreader.data.model.MdListResponse
import com.example.mymangadexreader.data.model.SetStatusRequest
import com.example.mymangadexreader.data.model.SimpleResponse
import com.example.mymangadexreader.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApiService {
    @FormUrlEncoded
    @POST("realms/mangadex/protocol/openid-connect/token")
    suspend fun login(
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<com.example.mymangadexreader.data.model.AuthResponse>

    @FormUrlEncoded
    @POST("realms/mangadex/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<com.example.mymangadexreader.data.model.AuthResponse>
}

interface MangaDexApiService {
    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author"),
        @Query("order[followedCount]") orderFollowed: String = "desc",
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive", "erotica"),
        @Query("availableTranslatedLanguage[]") availableTranslatedLanguage: List<String> = listOf("vi")
    ): Response<MangaListResponse>

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive", "erotica"),
        @Query("availableTranslatedLanguage[]") availableTranslatedLanguage: List<String> = listOf("vi")
    ): Response<MangaListResponse>

    @GET("manga")
    suspend fun getMangaByIds(
        @Query("ids[]") ids: List<String>,
        @Query("limit") limit: Int = 100,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): Response<MangaListResponse>

    @GET("manga/{id}")
    suspend fun getMangaDetail(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist")
    ): Response<MangaDetailResponse>

    @GET("manga/{id}/feed")
    suspend fun getMangaFeed(
        @Path("id") id: String,
        @Query("limit") limit: Int = 500,
        @Query("offset") offset: Int = 0,
        @Query("translatedLanguage[]") languages: List<String> = listOf("vi"),
        @Query("order[chapter]") order: String = "asc",
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive", "erotica")
    ): Response<ChapterListResponse>

    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): Response<AtHomeResponse>

    @GET("user/me")
    suspend fun getUserProfile(): Response<UserResponse>

    @GET("user/list")
    suspend fun getUserLists(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<MdListResponse>

    @GET("manga/status")
    suspend fun getMangaStatuses(): Response<MangaStatusResponse>

    @POST("manga/{id}/status")
    suspend fun setMangaStatus(
        @Path("id") mangaId: String,
        @Body body: SetStatusRequest
    ): Response<SimpleResponse>
}

data class MangaDetailResponse(
    val result: String,
    val data: MangaData
)

