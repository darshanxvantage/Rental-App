package com.xvantage.rental.ui.explore.createListing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.request.explore.CreateListingRequest
import com.xvantage.rental.network.request.explore.FieldValueItem
import com.xvantage.rental.network.request.explore.SharingPriceItem
import com.xvantage.rental.network.request.explore.UpdateListingRequest
import com.xvantage.rental.network.response.explore.ExploreCategoryFieldResponse
import com.xvantage.rental.network.response.explore.ExploreCategoryResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Everything the 6-step wizard collects, in one place. Fragments read/update
 * this via `updateForm { copy(...) }` - simple and avoids passing data
 * between fragments manually.
 */
data class CreateListingFormState(
    val editingListingId: String? = null, // null = creating a brand-new listing

    // step 1 - category
    val categoryFk: String? = null,
    val categoryFields: List<ExploreCategoryFieldResponse> = emptyList(),

    // step 2 - basic details
    val title: String = "",
    val description: String = "",
    val occupancyFor: String = "any",
    val genderPreference: String = "unisex",
    val city: String = "",
    val locality: String = "",
    val address: String = "",
    val landmark: String = "",
    val locationLink: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactPersonName: String = "",
    val contactNumber: String = "",
    val whatsappNumber: String = "",
    val alternateNumber: String = "",
    val totalBeds: Int = 0,
    val availableBeds: Int = 0,
    val minStayMonths: Int? = null,
    val bookingAmount: Double? = null,

    // step 3 - amenities (fixed flags + dynamic field values + rules + food)
    val foodIncluded: Boolean = false,
    val foodType: String = "none",
    val mealCount: String = "none",
    val curfewTime: String = "",
    val guestPolicy: String = "",
    val houseRules: String = "",
    val isAc: Boolean = false,
    val isAttachedWashroom: Boolean = false,
    val hasWifi: Boolean = false,
    val hasLaundry: Boolean = false,
    val hasHousekeeping: Boolean = false,
    val hasCctv: Boolean = false,
    val hasBiometricEntry: Boolean = false,
    val hasPowerBackup: Boolean = false,
    val hasRoWater: Boolean = false,
    val hasParking: Boolean = false,
    val hasGym: Boolean = false,
    val hasLift: Boolean = false,
    val hasStudyTable: Boolean = false,
    val hasWardrobe: Boolean = false,
    val documentsRequired: List<String> = emptyList(),
    val dynamicFieldValues: Map<String, String> = emptyMap(), // category_field_fk -> value

    // step 4 - sharing prices
    val sharingPrices: List<SharingPriceItem> = emptyList(),

    // step 5 - images
    val newImageUris: List<Uri> = emptyList(),
    val existingImageCount: Int = 0 // when editing, how many images already exist on the server
)

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val formState = MutableStateFlow(CreateListingFormState())
    val currentStep = MutableStateFlow(0) // 0..5

    val categories = MutableStateFlow<List<ExploreCategoryResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val submitSuccess = MutableStateFlow(false)

    fun updateForm(transform: CreateListingFormState.() -> CreateListingFormState) {
        formState.value = formState.value.transform()
    }

    fun goToStep(step: Int) {
        currentStep.value = step.coerceIn(0, TOTAL_STEPS - 1)
    }

    fun nextStep() {
        if (currentStep.value < TOTAL_STEPS - 1) currentStep.value += 1
    }

    fun previousStep() {
        if (currentStep.value > 0) currentStep.value -= 1
    }

    fun loadCategories() {
        viewModelScope.launch {
            when (val result = repository.getCategoryList()) {
                is ResultWrapper.Success -> categories.value = result.value.data ?: emptyList()
                is ResultWrapper.Error -> errorMessage.value = result.message
                else -> {}
            }
        }
    }

    fun loadCategoryFields(categoryId: String) {
        viewModelScope.launch {
            when (val result = repository.getCategoryFields(categoryId)) {
                is ResultWrapper.Success -> {
                    updateForm { copy(categoryFields = result.value.data?.fields ?: emptyList()) }
                }
                is ResultWrapper.Error -> errorMessage.value = result.message
                else -> {}
            }
        }
    }

    /** Pre-fills the whole form when the wizard is opened in edit mode. */
    fun loadListingForEdit(listingId: String) {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.myListingDetails(listingId)) {
                is ResultWrapper.Success -> {
                    val listing = result.value.data
                    if (listing != null) {
                        updateForm {
                            copy(
                                editingListingId = listing.id,
                                categoryFk = listing.categoryFk,
                                title = listing.title,
                                description = listing.description ?: "",
                                occupancyFor = listing.occupancyFor,
                                genderPreference = listing.genderPreference,
                                city = listing.city,
                                locality = listing.locality,
                                address = listing.address,
                                landmark = listing.landmark ?: "",
                                latitude = listing.latitude,
                                longitude = listing.longitude,
                                contactNumber = listing.contactNumber,
                                whatsappNumber = listing.whatsappNumber ?: "",
                                alternateNumber = listing.alternateNumber ?: "",
                                totalBeds = listing.totalBeds,
                                availableBeds = listing.availableBeds,
                                minStayMonths = listing.minStayMonths,
                                bookingAmount = listing.bookingAmount,
                                foodIncluded = listing.foodIncluded,
                                foodType = listing.foodType,
                                mealCount = listing.mealCount,
                                curfewTime = listing.curfewTime ?: "",
                                guestPolicy = listing.guestPolicy ?: "",
                                isAc = listing.isAc,
                                isAttachedWashroom = listing.isAttachedWashroom,
                                hasWifi = listing.hasWifi,
                                hasLaundry = listing.hasLaundry,
                                hasHousekeeping = listing.hasHousekeeping,
                                hasCctv = listing.hasCctv,
                                hasBiometricEntry = listing.hasBiometricEntry,
                                hasPowerBackup = listing.hasPowerBackup,
                                hasRoWater = listing.hasRoWater,
                                hasParking = listing.hasParking,
                                hasGym = listing.hasGym,
                                hasLift = listing.hasLift,
                                hasStudyTable = listing.hasStudyTable,
                                hasWardrobe = listing.hasWardrobe,
                                documentsRequired = listing.documentsRequired ?: emptyList(),
                                sharingPrices = listing.sharingPrices?.map {
                                    SharingPriceItem(it.sharingType, it.price, it.securityDeposit, it.maintenanceCharge, it.availableBeds)
                                } ?: emptyList(),
                                existingImageCount = listing.images?.size ?: 0
                            )
                        }
                        listing.categoryFk.let { loadCategoryFields(it) }
                    }
                }
                is ResultWrapper.Error -> errorMessage.value = result.message
                else -> {}
            }
            isLoading.value = false
        }
    }

    fun submit() {
        val form = formState.value

        if (form.categoryFk.isNullOrBlank()) {
            errorMessage.value = "Please select a category"
            return
        }
        if (form.title.isBlank() || form.city.isBlank() || form.address.isBlank() || form.contactNumber.isBlank()) {
            errorMessage.value = "Please fill in all required basic details"
            return
        }
        if (form.sharingPrices.isEmpty()) {
            errorMessage.value = "Please add at least one sharing-type price"
            return
        }

        val fieldValues = form.dynamicFieldValues.map { (fieldFk, value) -> FieldValueItem(fieldFk, value) }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            val listingId: String? = if (form.editingListingId == null) {
                createNewListing(form, fieldValues)
            } else {
                updateExistingListing(form, fieldValues)
            }

            if (listingId != null && form.newImageUris.isNotEmpty()) {
                repository.uploadListingImages(listingId, form.newImageUris)
            }

            isLoading.value = false
        }
    }

    private suspend fun createNewListing(form: CreateListingFormState, fieldValues: List<FieldValueItem>): String? {
        val request = CreateListingRequest(
            categoryFk = form.categoryFk!!,
            title = form.title,
            description = form.description,
            occupancyFor = form.occupancyFor,
            genderPreference = form.genderPreference,
            foodIncluded = form.foodIncluded,
            foodType = form.foodType,
            mealCount = form.mealCount,
            city = form.city,
            locality = form.locality,
            address = form.address,
            landmark = form.landmark.ifBlank { null },
            locationLink = form.locationLink.ifBlank { null },
            latitude = form.latitude,
            longitude = form.longitude,
            contactPersonName = form.contactPersonName.ifBlank { null },
            contactNumber = form.contactNumber,
            whatsappNumber = form.whatsappNumber.ifBlank { null },
            alternateNumber = form.alternateNumber.ifBlank { null },
            totalBeds = form.totalBeds,
            availableBeds = form.availableBeds,
            minStayMonths = form.minStayMonths,
            bookingAmount = form.bookingAmount,
            curfewTime = form.curfewTime.ifBlank { null },
            guestPolicy = form.guestPolicy.ifBlank { null },
            houseRules = form.houseRules.ifBlank { null },
            isAc = form.isAc,
            isAttachedWashroom = form.isAttachedWashroom,
            hasWifi = form.hasWifi,
            hasLaundry = form.hasLaundry,
            hasHousekeeping = form.hasHousekeeping,
            hasCctv = form.hasCctv,
            hasBiometricEntry = form.hasBiometricEntry,
            hasPowerBackup = form.hasPowerBackup,
            hasRoWater = form.hasRoWater,
            hasParking = form.hasParking,
            hasGym = form.hasGym,
            hasLift = form.hasLift,
            hasStudyTable = form.hasStudyTable,
            hasWardrobe = form.hasWardrobe,
            documentsRequired = form.documentsRequired.ifEmpty { null },
            sharingPrices = form.sharingPrices,
            fieldValues = fieldValues.ifEmpty { null }
        )

        return when (val result = repository.createListing(request)) {
            is ResultWrapper.Success -> {
                submitSuccess.value = true
                result.value.data?.id
            }
            is ResultWrapper.Error -> {
                errorMessage.value = result.message
                null
            }
            else -> null
        }
    }

    private suspend fun updateExistingListing(form: CreateListingFormState, fieldValues: List<FieldValueItem>): String? {
        val listingId = form.editingListingId ?: return null
        val request = UpdateListingRequest(
            categoryFk = form.categoryFk,
            title = form.title,
            description = form.description,
            occupancyFor = form.occupancyFor,
            genderPreference = form.genderPreference,
            foodIncluded = form.foodIncluded,
            foodType = form.foodType,
            mealCount = form.mealCount,
            city = form.city,
            locality = form.locality,
            address = form.address,
            landmark = form.landmark.ifBlank { null },
            locationLink = form.locationLink.ifBlank { null },
            latitude = form.latitude,
            longitude = form.longitude,
            contactPersonName = form.contactPersonName.ifBlank { null },
            contactNumber = form.contactNumber,
            whatsappNumber = form.whatsappNumber.ifBlank { null },
            alternateNumber = form.alternateNumber.ifBlank { null },
            totalBeds = form.totalBeds,
            availableBeds = form.availableBeds,
            minStayMonths = form.minStayMonths,
            bookingAmount = form.bookingAmount,
            curfewTime = form.curfewTime.ifBlank { null },
            guestPolicy = form.guestPolicy.ifBlank { null },
            houseRules = form.houseRules.ifBlank { null },
            isAc = form.isAc,
            isAttachedWashroom = form.isAttachedWashroom,
            hasWifi = form.hasWifi,
            hasLaundry = form.hasLaundry,
            hasHousekeeping = form.hasHousekeeping,
            hasCctv = form.hasCctv,
            hasBiometricEntry = form.hasBiometricEntry,
            hasPowerBackup = form.hasPowerBackup,
            hasRoWater = form.hasRoWater,
            hasParking = form.hasParking,
            hasGym = form.hasGym,
            hasLift = form.hasLift,
            hasStudyTable = form.hasStudyTable,
            hasWardrobe = form.hasWardrobe,
            documentsRequired = form.documentsRequired.ifEmpty { null },
            sharingPrices = form.sharingPrices.ifEmpty { null },
            fieldValues = fieldValues.ifEmpty { null }
        )

        return when (val result = repository.editListing(listingId, request)) {
            is ResultWrapper.Success -> {
                submitSuccess.value = true
                result.value.data?.id
            }
            is ResultWrapper.Error -> {
                errorMessage.value = result.message
                null
            }
            else -> null
        }
    }

    companion object {
        const val TOTAL_STEPS = 6
    }
}