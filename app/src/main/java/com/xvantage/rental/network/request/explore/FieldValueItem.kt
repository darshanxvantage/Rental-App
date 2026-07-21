package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches listing.validation.js -> fieldValueItemSchema
 * `value` is sent as String always (booleans as "true"/"false", numbers as
 * their string form) - the backend's Joi `convert:true` coerces it back.
 */
data class FieldValueItem(

    @SerializedName("category_field_fk")
    val categoryFieldFk: String,

    @SerializedName("value")
    val value: String
)