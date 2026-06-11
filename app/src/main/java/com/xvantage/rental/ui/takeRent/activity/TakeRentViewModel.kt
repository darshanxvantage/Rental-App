package com.xvantage.rental.ui.takeRent.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TakeRentViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyList =
        MutableStateFlow<List<PropertyItem>>(
            emptyList()
        )

    fun loadProperties() {

        viewModelScope.launch {

            when (
                val response =
                    repository.getPropertyList()
            ) {

                is ResultWrapper.Success -> {

                    propertyList.value =
                        response.value.data.rows
                }

                else -> {}
            }
        }
    }
}