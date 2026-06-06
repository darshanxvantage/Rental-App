package com.xvantage.rental.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log


@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyList =
        MutableStateFlow<List<PropertyItem>>(emptyList())

    fun loadProperties() {

        viewModelScope.launch {

            when(
                val response =
                    repository.getPropertyList()
            ) {

                is ResultWrapper.Success -> {

                    Log.d(
                        "PROPERTY_DEBUG",
                        "API Success Count = ${response.value.data.rows.size}"
                    )

                    propertyList.value =
                        response.value.data.rows
                }

                is ResultWrapper.Error -> {

                    Log.e(
                        "PROPERTY_DEBUG",
                        "API Error = ${response.message}"
                    )
                }

                else -> {}
            }
        }
    }
}