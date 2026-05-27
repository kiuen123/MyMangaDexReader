package com.example.mymangadexreader.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER = "remember_login"

    private lateinit var prefs: SharedPreferences

    // In-memory only (access token expires ~15 min, no need to persist)
    var accessToken: String? = null
    var refreshToken: String? = null

    // Persisted across app restarts
    var savedUsername: String? = null
        private set
    var savedPassword: String? = null
        private set
    var rememberLogin: Boolean = false
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        rememberLogin = prefs.getBoolean(KEY_REMEMBER, false)
        savedUsername = prefs.getString(KEY_USERNAME, null)
        savedPassword = if (rememberLogin) prefs.getString(KEY_PASSWORD, null) else null
    }

    /** Persist credentials and remember-me flag after successful login */
    fun saveCredentials(username: String, password: String, remember: Boolean) {
        savedUsername = username
        savedPassword = if (remember) password else null
        rememberLogin = remember
        if (::prefs.isInitialized) {
            prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, if (remember) password else null)
                .putBoolean(KEY_REMEMBER, remember)
                .apply()
        }
    }

    fun isLoggedIn() = accessToken != null

    /** True if remember-me is on and we have full credentials for auto-login */
    fun hasSavedCredentials() = rememberLogin && savedUsername != null && savedPassword != null

    fun clear() {
        accessToken = null
        refreshToken = null
        savedUsername = null
        savedPassword = null
        rememberLogin = false
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }
}

object RetrofitInstance {
    private const val AUTH_BASE_URL = "https://auth.mangadex.org/"
    private const val API_BASE_URL = "https://api.mangadex.org/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val authOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val token = TokenManager.accessToken
            val request = if (token != null) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else original
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    val mangaDexApiService: MangaDexApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(apiOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaDexApiService::class.java)
    }
}
