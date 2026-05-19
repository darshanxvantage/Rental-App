package com.xvantage.rental.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.AuthRepository
import com.xvantage.rental.network.request.auth.GoogleLoginRequest
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.ui.auth.fragment.sealed.AuthScreen
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val appPreference: AppPreference

) : ViewModel() {
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = authStateFlow.asStateFlow()

    private val currentScreenFlow = MutableStateFlow<AuthScreen>(AuthScreen.SignIn)
    val currentScreen: StateFlow<AuthScreen> = currentScreenFlow.asStateFlow()

    fun setCurrentScreen(screen: AuthScreen) {
        currentScreenFlow.value = screen
    }
    fun storeJwtToken(token: String) {
        appPreference.setToken(token)
    }

    fun isUserLoggedIn(): Boolean {
        return !appPreference.getToken().isNullOrEmpty()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading
            when (val response = repository.login(email, password)) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success(response.value.data?.token)
                    storeJwtToken(response.value.data?.token ?: "")
                    currentScreenFlow.value = AuthScreen.Dashboard
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(response.message ?: "Login failed")
                }
                ResultWrapper.Loading -> Unit
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading
            when (val response = repository.signUp(email, password)) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success("Signup successful")

                    currentScreenFlow.value = AuthScreen.VerifyOtp(email)
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(
                        response.message ?: "Signup failed"
                    )
                }
                ResultWrapper.Loading -> Unit
            }
        }
    }
    fun signUpWithGoogle(googleData: GoogleLoginRequest) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading
            when (val response = repository.signUpWithGoogle(googleData)) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success(response.value.data?.token ?: "Signup successful")
                    storeJwtToken(response.value.data?.token ?: "")
                    currentScreenFlow.value = AuthScreen.Dashboard
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(response.message ?: "Google Signup failed")
                }
                ResultWrapper.Loading -> Unit
            }
        }
    }

    fun verifyOtp(email: String, otp: String, type: String) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading
            when (val response = repository.verifyOtp(email, otp, type)) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success("OTP Verified")
                    storeJwtToken(response.value.data?.token ?: "")

                    currentScreenFlow.value = AuthScreen.Dashboard
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(response.message ?: "Invalid OTP")
                }
                ResultWrapper.Loading -> Unit
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading
            when (val response = repository.forgotPassword(email)) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success("Password reset link sent")
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(response.message ?: "Invalid email")
                }
                ResultWrapper.Loading -> Unit
            }
        }
    }
}
