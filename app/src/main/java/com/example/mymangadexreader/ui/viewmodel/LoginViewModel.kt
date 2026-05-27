package com.example.mymangadexreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymangadexreader.data.api.TokenManager
import com.example.mymangadexreader.data.repository.MangaRepository
import com.example.mymangadexreader.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    // Pre-fill from last login (username always shown, password only if remember=true)
    val username: String = TokenManager.savedUsername ?: "",
    val password: String = if (TokenManager.rememberLogin) TokenManager.savedPassword ?: "" else "",
    val rememberLogin: Boolean = TokenManager.rememberLogin,
    val isLoading: Boolean = false,
    val isAutoLogin: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Auto-login only when remember-me is on and full credentials are saved
        if (TokenManager.hasSavedCredentials()) {
            autoLogin()
        }
    }

    private fun autoLogin() {
        val username = TokenManager.savedUsername ?: return
        val password = TokenManager.savedPassword ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isAutoLogin = true, error = null)
            when (val result = MangaRepository.authenticate(username, password, remember = true)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isAutoLogin = false, loginSuccess = true
                    )
                }
                is Result.Error -> {
                    // Auto-login failed → show form with prefilled username
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isAutoLogin = false, error = null
                    )
                }
                else -> {}
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onRememberLoginChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberLogin = value)
    }

    fun login() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password
        val remember = _uiState.value.rememberLogin
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập tên đăng nhập")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập mật khẩu")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = MangaRepository.authenticate(username, password, remember)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }
}
