package com.xvantage.rental.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.request.feedback.FeedbackRequest
import com.xvantage.rental.network.response.FeedbackResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val isSubmitting = MutableStateFlow(false)
    val submitSuccess = MutableStateFlow<String?>(null)
    val submitError = MutableStateFlow<String?>(null)

    fun submitFeedback(category: String, rating: Int, message: String, appVersion: String?) {
        viewModelScope.launch {
            isSubmitting.value = true
            submitError.value = null

            val request = FeedbackRequest(
                category = category,
                rating = rating,
                message = message,
                appVersion = appVersion
            )

            when (val result = repository.submitFeedback(request)) {
                is ResultWrapper.Success -> submitSuccess.value = result.value.message
                is ResultWrapper.Error   -> submitError.value = result.message
                else -> {}
            }

            isSubmitting.value = false
        }
    }

    fun resetState() {
        submitSuccess.value = null
        submitError.value = null
    }
}