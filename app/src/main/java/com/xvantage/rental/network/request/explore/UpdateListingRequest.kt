package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore-module/validation/listing.validation.js -> listingUpdate.
 * Every field is optional/nullable - only send what actually changed.
 * NOTE: editing an already-APPROVED (live) listing sends it back to
 * pending_approval on the backend automatically (re-review safety net).
 */
data class UpdateListingRequest(

    @SerializedName("category_fk") val categoryFk: String? = null,
    @SerializedName("property_fk") val propertyFk: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("occupancy_for") val occupancyFor: String? = null,
    @SerializedName("gender_preference") val genderPreference: String? = null,
    @SerializedName("food_included") val foodIncluded: Boolean? = null,
    @SerializedName("food_type") val foodType: String? = null,
    @SerializedName("meal_count") val mealCount: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("landmark") val landmark: String? = null,
    @SerializedName("location_link") val locationLink: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("contact_person_name") val contactPersonName: String? = null,
    @SerializedName("contact_number") val contactNumber: String? = null,
    @SerializedName("whatsapp_number") val whatsappNumber: String? = null,
    @SerializedName("alternate_number") val alternateNumber: String? = null,
    @SerializedName("total_beds") val totalBeds: Int? = null,
    @SerializedName("available_beds") val availableBeds: Int? = null,
    @SerializedName("min_stay_months") val minStayMonths: Int? = null,
    @SerializedName("booking_amount") val bookingAmount: Double? = null,
    @SerializedName("curfew_time") val curfewTime: String? = null,
    @SerializedName("guest_policy") val guestPolicy: String? = null,
    @SerializedName("house_rules") val houseRules: String? = null,
    @SerializedName("nearby_services") val nearbyServices: List<String>? = null,

    @SerializedName("is_ac") val isAc: Boolean? = null,
    @SerializedName("is_attached_washroom") val isAttachedWashroom: Boolean? = null,
    @SerializedName("has_wifi") val hasWifi: Boolean? = null,
    @SerializedName("has_laundry") val hasLaundry: Boolean? = null,
    @SerializedName("has_housekeeping") val hasHousekeeping: Boolean? = null,
    @SerializedName("has_cctv") val hasCctv: Boolean? = null,
    @SerializedName("has_biometric_entry") val hasBiometricEntry: Boolean? = null,
    @SerializedName("has_power_backup") val hasPowerBackup: Boolean? = null,
    @SerializedName("has_ro_water") val hasRoWater: Boolean? = null,
    @SerializedName("has_parking") val hasParking: Boolean? = null,
    @SerializedName("has_gym") val hasGym: Boolean? = null,
    @SerializedName("has_lift") val hasLift: Boolean? = null,
    @SerializedName("has_study_table") val hasStudyTable: Boolean? = null,
    @SerializedName("has_wardrobe") val hasWardrobe: Boolean? = null,

    @SerializedName("documents_required") val documentsRequired: List<String>? = null,
    @SerializedName("sharing_prices") val sharingPrices: List<SharingPriceItem>? = null,
    @SerializedName("field_values") val fieldValues: List<FieldValueItem>? = null
)