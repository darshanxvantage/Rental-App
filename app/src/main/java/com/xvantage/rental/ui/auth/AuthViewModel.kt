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

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            authStateFlow.value = AuthState.Loading

            val response = repository.verifyLoginOtp(phone, otp)

            when (response) {
                is ResultWrapper.Success -> {
                    authStateFlow.value = AuthState.Success("OTP Verified")

                    val data = response.value.data
                    val newPhone = data?.phone_number ?: ""

                    // Only wipe locally cached data (photo, city, state, etc.)
                    // when the number logging in is DIFFERENT from the one
                    // previously stored on this device — i.e. a genuine
                    // account switch. The same user logging back in must
                    // keep their cached data, since it's never re-fetched
                    // from the server after login.
                    val previousPhone = appPreference.getPhone()
                    if (!previousPhone.isNullOrEmpty() && previousPhone != newPhone) {
                        appPreference.logoutUser()
                    }

                    storeJwtToken(data?.token ?: "")

                    appPreference.setPhone(newPhone)
                    appPreference.setEmail(data?.email ?: "")
                    appPreference.setUserName(
                        "${data?.first_name ?: ""} ${data?.last_name ?: ""}".trim()
                    )
                    appPreference.setCity(data?.city ?: "")
                    appPreference.setState(data?.state ?: "")
                    appPreference.setIsProfileComplete(data?.is_profile_complete == true)

                    // Server is the source of truth for the photo. If the
                    // account already has one saved there (e.g. after a
                    // logout + re-login, or a different device), use that
                    // URL so the avatar isn't stuck blank until the user
                    // re-uploads it locally.
                    if (!data?.profile_pic.isNullOrEmpty()) {
                        appPreference.setRemoteProfileImageUrl(data?.profile_pic ?: "")
                    }


                    if (data?.is_profile_complete == true) {

                        currentScreenFlow.value = AuthScreen.Dashboard

                    } else {

                        currentScreenFlow.value = AuthScreen.CreateProfile

                    }
                }
                is ResultWrapper.Error -> {
                    authStateFlow.value = AuthState.Error(
                        response.message ?: "Invalid OTP"
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
        gender: String
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
                        gender
                    )

            ) {

                is ResultWrapper.Success -> {

                    authStateFlow.value =
                        AuthState.Success(
                            "Profile Created"
                        )

                    // Profile setup is now finished on the server — persist
                    // that locally so Splash is allowed to open Dashboard
                    // directly on the next app launch.
                    appPreference.setIsProfileComplete(true)

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