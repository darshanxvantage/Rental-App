package com.xvantage.rental.ui.addProperty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val data: JsonObject) : State()
        data class Error(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    fun createRoom(
        propertyId: String,
        propertyTypeId: String,
        roomNo: String,
        roomTypeId: String,
        roomTypeText: String,
        address: String,
        rent: String,
        meterReading: String,
        meterReadingLastDate: String,
        roomImage: MultipartBody.Part?,
        // PG: sharing type + bed count. Row House: which floor this unit is.
        // Left null/blank for property types that don't use them.
        sharingType: String? = null,
        bedCount: String? = null,
        floorLabel: String? = null
    ) {

        viewModelScope.launch {

            _state.value = State.Loading

            android.util.Log.e("ROOM_API", "Calling Create Room API")

            when (
                val result = repository.createRoom(
                    propertyId,
                    propertyTypeId,
                    roomNo,
                    roomTypeId,
                    roomTypeText,
                    address,
                    rent,
                    meterReading,
                    meterReadingLastDate,
                    roomImage,
                    sharingType,
                    bedCount,
                    floorLabel
                )
            ) {


                is ResultWrapper.Success ->
                    _state.value = State.Success(result.value)

                is ResultWrapper.Error ->
                    _state.value = State.Error(result.message)

                else -> {}
            }
        }
    }
}