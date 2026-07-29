package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore-module/validation/listing.validation.js -> listingCreate EXACTLY.
 * Sent as @Body (raw JSON) to POST /explore/landlord/listing/create.
 */
data class CreateListingRequest(

    @SerializedName("category_fk")
    val categoryFk: String,

    @SerializedName("property_fk")
    val propertyFk: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("occupancy_for")
    val occupancyFor: String, // student | working_professional | any

    @SerializedName("gender_preference")
    val genderPreference: String, // male | female | unisex

    @SerializedName("food_included")
    val foodIncluded: Boolean = false,

    @SerializedName("food_type")
    val foodType: String = "none",

    @SerializedName("meal_count")
    val mealCount: String = "none",

    @SerializedName("city")
    val city: String,

    @SerializedName("locality")
    val locality: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("landmark")
    val landmark: String? = null,

    @SerializedName("location_link")
    val locationLink: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("contact_person_name")
    val contactPersonName: String? = null,

    @SerializedName("contact_number")
    val contactNumber: String,

    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null,

    @SerializedName("alternate_number")
    val alternateNumber: String? = null,

    @SerializedName("total_beds")
    val totalBeds: Int,

    @SerializedName("available_beds")
    val availableBeds: Int,

    @SerializedName("min_stay_months")
    val minStayMonths: Int? = null,

    @SerializedName("booking_amount")
    val bookingAmount: Double? = null,

    @SerializedName("curfew_time")
    val curfewTime: String? = null,

    @SerializedName("guest_policy")
    val guestPolicy: String? = null,

    @SerializedName("house_rules")
    val houseRules: String? = null,

    @SerializedName("is_ac") val isAc: Boolean = false,
    @SerializedName("is_attached_washroom") val isAttachedWashroom: Boolean = false,
    @SerializedName("has_wifi") val hasWifi: Boolean = false,
    @SerializedName("has_laundry") val hasLaundry: Boolean = false,
    @SerializedName("has_housekeeping") val hasHousekeeping: Boolean = false,
    @SerializedName("has_cctv") val hasCctv: Boolean = false,
    @SerializedName("has_biometric_entry") val hasBiometricEntry: Boolean = false,
    @SerializedName("has_power_backup") val hasPowerBackup: Boolean = false,
    @SerializedName("has_ro_water") val hasRoWater: Boolean = false,
    @SerializedName("has_parking") val hasParking: Boolean = false,
    @SerializedName("has_gym") val hasGym: Boolean = false,
    @SerializedName("has_lift") val hasLift: Boolean = false,
    @SerializedName("has_study_table") val hasStudyTable: Boolean = false,
    @SerializedName("has_wardrobe") val hasWardrobe: Boolean = false,

    @SerializedName("documents_required")
    val documentsRequired: List<String>? = null,

    @SerializedName("sharing_prices")
    val sharingPrices: List<SharingPriceItem>,

    @SerializedName("field_values")
    val fieldValues: List<FieldValueItem>? = null
)