package com.example.nityaandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nityaandroid.data.remote.models.LoginRequest
import com.example.nityaandroid.data.remote.models.RegisterRequest
import com.example.nityaandroid.data.remote.models.UserDto
import com.example.nityaandroid.data.repository.AuthRepository
import com.example.nityaandroid.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<UserDto>?>(null)
    val loginState: StateFlow<Resource<UserDto>?> = _loginState

    private val _logoutState = MutableStateFlow<Resource<Unit>?>(null)
    val logoutState: StateFlow<Resource<Unit>?> = _logoutState

    private val _registerState = MutableStateFlow<Resource<UserDto>?>(null)
    val registerState: StateFlow<Resource<UserDto>?> = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(LoginRequest(email, password)).collect { result ->
                _loginState.value = result
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(RegisterRequest(name, email, password)).collect { result ->
                _registerState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearLocalSyncData()

            repository.logout().collect { result ->
                _logoutState.value = result
            }
        }
    }

    suspend fun checkUnsyncedData(): Boolean {
        return repository.hasUnsyncedData()
    }
}