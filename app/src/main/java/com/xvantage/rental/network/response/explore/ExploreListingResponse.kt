package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore-module/models/exploreListing.model.js field-for-field.
 * Used for: discover list, discover details, my-listings (owner), favorites, similar.
 */
data class ExploreListingResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("user_fk")
    val userFk: String? = null,

    @SerializedName("property_fk")
    val propertyFk: String? = null,

    @SerializedName("category_fk")
    val categoryFk: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("slug")
    val slug: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("occupancy_for")
    val occupancyFor: String, // student | working_professional | any

    @SerializedName("gender_preference")
    val genderPreference: String, // male | female | unisex

    @SerializedName("food_included")
    val foodIncluded: Boolean = false,

    @SerializedName("food_type")
    val foodType: String = "none", // veg | non_veg | both | none

    @SerializedName("meal_count")
    val mealCount: String = "none", // breakfast_only | two_meals | three_meals | none

    @SerializedName("city")
    val city: String,

    @SerializedName("locality")
    val locality: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("landmark")
    val landmark: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("contact_number")
    val contactNumber: String,

    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null,

    @SerializedName("alternate_number")
    val alternateNumber: String? = null,

    @SerializedName("total_beds")
    val totalBeds: Int = 0,

    @SerializedName("available_beds")
    val availableBeds: Int = 0,

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

    @SerializedName("location_link")
    val locationLink: String? = null,

    @SerializedName("nearby_services")
    val nearbyServices: List<String>? = null,

    @SerializedName("owner_id_proof")
    val ownerIdProof: String? = null,

    @SerializedName("contact_person_name")
    val contactPersonName: String? = null,

    // ---- fixed amenity flags (fast-filterable, mirrors backend columns) ----
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

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,

    @SerializedName("status")
    val status: String = "draft", // draft | pending_approval | approved | rejected | inactive

    @SerializedName("rejection_reason")
    val rejectionReason: String? = null,

    @SerializedName("view_count")
    val viewCount: Int = 0,

    @SerializedName("lead_count")
    val leadCount: Int = 0,

    @SerializedName("favorite_count")
    val favoriteCount: Int = 0,

    @SerializedName("published_at")
    val publishedAt: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    // ---- relations (present depending on which endpoint was called) ----
    @SerializedName("category")
    val category: ExploreCategoryResponse? = null,

    @SerializedName("images")
    val images: List<ExploreImageResponse>? = null,

    @SerializedName("sharing_prices")
    val sharingPrices: List<SharingPriceResponse>? = null,

    @SerializedName("field_values")
    val fieldValues: List<ExploreFieldValueResponse>? = null,

    // ---- computed / added by the controller layer ----
    @SerializedName("price_range")
    val priceRange: PriceRangeResponse? = null,

    @SerializedName("is_favorited")
    val isFavorited: Boolean = false,

    @SerializedName("distance_km")
    val distanceKm: Double? = null // only present on /discover/nearby
)