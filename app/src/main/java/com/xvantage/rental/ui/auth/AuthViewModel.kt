package com.xvantage.rental.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.AuthRepository

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
import java.io.File

@HiltViewModel
class
AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val appPreference: AppPreference

) : ViewModel() {


    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Idle)
    fun resetAuthState() {

        authStateFlow.value =
            AuthState.Idle
    }
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


    fun signIn(phone: String) {

        viewModelScope.launch {

            authStateFlow.value =
                AuthState.Loading

            when (
                val response =
                    repository.login(phone)
            ) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success("OTP Sent")

                    currentScreenFlow.value =
                        AuthScreen.VerifyOtp(
                            phone = phone,
                            isFromLogin = true)
                }

                is ResultWrapper.Error -> {

                    authStateFlow.value =
                        AuthState.Error(
                            response.message
                                ?: "User not found"
                        )
                }

                ResultWrapper.Loading -> Unit
            }
        }
    }

    fun signUp(phone: String) {

        viewModelScope.launch {

            authStateFlow.value =
                AuthState.Loading

            when (
                val response =
                    repository.signUp(phone)
            ) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success("OTP Sent")

                    currentScreenFlow.value =
                        AuthScreen.VerifyOtp(
                            phone = phone,
                            isFromLogin = false
                        )
                }

                is ResultWrapper.Error -> {

                    authStateFlow.value =
                        AuthState.Error(
                            response.message
                                ?: "Signup Failed"
                        )
                }

                ResultWrapper.Loading -> Unit
            }
        }
    }
    fun verifyOtp(
        phone: String,
        otp: String,
        isFromLogin: Boolean
    ) {

        viewModelScope.launch {

            authStateFlow.value =
                AuthState.Loading

            val response =

                if (isFromLogin) {

                    repository.verifyLoginOtp(
                        phone,
                        otp
                    )

                } else {

                    repository.verifyOtp(
                        phone,
                        otp
                    )
                }

            when (response) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success(
                            "OTP Verified"
                        )

                    storeJwtToken(
                        response.value.data?.token
                            ?: ""
                    )

                    if (isFromLogin) {

                        currentScreenFlow.value =
                            AuthScreen.Dashboard

                    } else {

                        currentScreenFlow.value =
                            AuthScreen.CreateProfile
                    }
                }

                is ResultWrapper.Error -> {

                    authStateFlow.value =
                        AuthState.Error(
                            response.message
                                ?: "Invalid OTP"
                        )
                }

                ResultWrapper.Loading -> Unit
            }
        }
    }
    fun createProfile(
        firstName: String,
        lastName: String,
        email: String,
        state: String,
        city: String,
        age: Int
    )
    {

        viewModelScope.launch {

            authStateFlow.value =
                AuthState.Loading

            when (

                val response =
                    repository.createProfile(
                        firstName,
                        lastName,
                        email,
                        state,
                        city,
                        age
                    )

            ) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success(
                            "Profile Created"
                        )

                    currentScreenFlow.value =
                        AuthScreen.Dashboard
                }

                is ResultWrapper.Error -> {

                    authStateFlow.value =
                        AuthState.Error(
                            response.message
                                ?: "Profile Create Failed"
                        )
                }

                ResultWrapper.Loading -> Unit
            }
        }

    }
    fun updateProfileImage(
        firstName: String,
        imageFile: File
    ) {

        viewModelScope.launch {

            when(
                val response =
                    repository.updateProfileImage(
                        firstName,
                        imageFile
                    )
            ) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success(
                            "Profile Image Updated"
                        )
                }

                is ResultWrapper.Error -> {

                    authStateFlow.value =
                        AuthState.Error(
                            response.message ?: "Upload Failed"
                        )
                }

                ResultWrapper.Loading -> Unit
            }
        }
    }
}










