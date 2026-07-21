package com.xvantage.rental.ui.explore.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.network.response.explore.LeadResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingDetailsViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val listing = MutableStateFlow<ExploreListingResponse?>(null)
    val similarListings = MutableStateFlow<List<ExploreListingResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val isFavorited = MutableStateFlow(false)

    /** Result of the last "Call Now"/"WhatsApp" tap - Activity launches the actual intent from this. */
    val leadReadyToContact = MutableStateFlow<Pair<String, LeadResponse>?>(null) // Pair(leadType, response)

    val reportSubmitted = MutableStateFlow(false)

    fun loadListingDetails(listingId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            when (val result = repository.discoverDetails(listingId)) {
                is ResultWrapper.Success -> {
                    listing.value = result.value.data
                    isFavorited.value = result.value.data?.isFavorited ?: false
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = result.message
                }
                else -> {}
            }

            isLoading.value = false
            loadSimilarListings(listingId)
        }
    }

    private fun loadSimilarListings(listingId: String) {
        viewModelScope.launch {
            when (val result = repository.discoverSimilar(listingId)) {
                is ResultWrapper.Success -> {
                    similarListings.value = result.value.data ?: emptyList()
                }
                else -> { /* similar listings are a nice-to-have, fail silently */ }
            }
        }
    }

    fun toggleFavorite() {
        val currentListing = listing.value ?: return
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(currentListing.id)) {
                is ResultWrapper.Success -> {
                    isFavorited.value = result.value.data?.isFavorited ?: !isFavorited.value
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Records the lead (silently, fire-and-forget style) and then hands the
     * returned contact_number/whatsapp_number back to the Activity so it can
     * launch the dialer/WhatsApp intent right away - never blocks the tap.
     */
    fun recordLeadAndGetContact(leadType: String) {
        val currentListing = listing.value ?: return
        viewModelScope.launch {
            when (val result = repository.createLead(currentListing.id, leadType)) {
                is ResultWrapper.Success -> {
                    result.value.data?.let { leadReadyToContact.value = leadType to it }
                }
                is ResultWrapper.Error -> {
                    // Even if lead-logging fails, still let the student contact the owner directly.
                    leadReadyToContact.value = leadType to LeadResponse(
                        contactNumber = currentListing.contactNumber,
                        whatsappNumber = currentListing.whatsappNumber
                    )
                }
                else -> {}
            }
        }
    }

    fun submitReport(reason: String, description: String?) {
        val currentListing = listing.value ?: return
        viewModelScope.launch {
            when (repository.createReport(currentListing.id, reason, description)) {
                is ResultWrapper.Success -> reportSubmitted.value = true
                is ResultWrapper.Error -> errorMessage.value = "Could not submit report. Please try again."
                else -> {}
            }
        }
    }
}