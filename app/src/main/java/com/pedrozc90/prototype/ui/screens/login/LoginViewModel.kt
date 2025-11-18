package com.pedrozc90.prototype.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.data.web.ApiRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException

class LoginViewModel(
    private val preferences: PreferencesRepository,
    private val repository: ApiRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = preferences.getLoginUiState()
                .filterNotNull()
                .first()
        }
    }

    private fun validate(state: LoginUiState): Boolean {
        return with(state) {
            username.isNotBlank() && password.isNotBlank()
        }
    }

    suspend fun persist(state: LoginUiState) {
        if (state.isValid()) {
            preferences.update(state)
        }
    }

    fun update(state: LoginUiState) {
        uiState = state.copy(touched = true)
    }

    suspend fun onLogin() {
        try {
            val state = uiState
            repository.login(state.username, state.password).let { res ->
                persist(state.copy(token = res.token))
            }
        } catch (e: IOException) {
            Log.e("LoginViewModel", "IOException during login", e)
        } catch (e: HttpException) {
            Log.e("LoginViewModel", "HttpException during login", e)
        }
    }

}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val token: String? = null,
    val touched: Boolean = false
) {

    fun isValid(): Boolean {
        return with(this) {
            isUsernameValid() && isPasswordValid()
        }
    }

    fun isUsernameValid(): Boolean {
        return with(this) {
            username.isNotBlank() && username.length <= 32
        }
    }

    fun isPasswordValid(): Boolean {
        return with(this) {
            password.isNotBlank() && password.length <= 32
        }
    }

}
