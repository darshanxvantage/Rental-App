package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_category_fields table.
 * field_type drives which widget AmenityFieldAdapter renders:
 *   "boolean"     -> checkbox / switch
 *   "text"        -> single-line EditText
 *   "textarea"    -> multi-line EditText
 *   "number"      -> numeric EditText
 *   "select"      -> single-choice dropdown (uses `options`)
 *   "multiselect" -> multi-choice chips (uses `options`)
 */
data class ExploreCategoryFieldResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("category_fk")
    val categoryFk: String,

    @SerializedName("field_key")
    val fieldKey: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("field_type")
    val fieldType: String, // text | number | boolean | select | multiselect | textarea

    @SerializedName("field_group")
    val fieldGroup: String, // basic | amenities | rules | food | documents

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("is_required")
    val isRequired: Boolean = false,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("status")
    val status: Boolean = true
)