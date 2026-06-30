package com.xvantage.rental.ui.takeRent.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.request.tenant.TenantPaymentRequest
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.network.response.PaymentSuccessResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceivePaymentViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val isLoading = MutableStateFlow(false)

    val paymentResult =
        MutableStateFlow<ResultWrapper<PaymentSuccessResponse>?>(null)

    fun receivePayment(
        request: TenantPaymentRequest
    ) {

        viewModelScope.launch {

            isLoading.value = true

            paymentResult.value =
                repository.receivePayment(request)

            isLoading.value = false

        }

    }

}