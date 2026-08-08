package com.xvantage.rental.utils

object UnitLabelProvider {

    data class UnitLabel(
        val singular: String,   // "Room", "Flat", "Shop", "Office", "Floor"
        val plural: String      // "Rooms", "Flats", "Shops", "Offices", "Floors"
    )

    /**
     * Returns the correct unit word for a given property type name.
     * Falls back to "Room" / "Rooms" if the type is unrecognised or null,
     * so existing behaviour is preserved for any type not yet mapped.
     */
    fun forPropertyType(propertyTypeName: String?): UnitLabel {
        val name = propertyTypeName?.trim()?.lowercase().orEmpty()

        return when {
            name.contains("apartment") || name.contains("flat") ->
                UnitLabel("Flat", "Flats")

            name.contains("pg") ->
                UnitLabel("Room", "Rooms")

            name.contains("commerc") || name.contains("shop") ->
                UnitLabel("Shop", "Shops")

            name.contains("office") ->
                UnitLabel("Office", "Offices")

            name.contains("row house") ->
                UnitLabel("Floor", "Floors")

            name.contains("bhada") ->
                UnitLabel("Room", "Rooms")

            else ->
                UnitLabel("Room", "Rooms")
        }
    }

    // ── Convenience helpers for common UI strings, so call sites don't
    //    each have to re-build the same sentence around .singular/.plural ──

    fun addUnitLabel(propertyTypeName: String?): String =
        "Add ${forPropertyType(propertyTypeName).singular}"

    fun unitNumberFieldLabel(propertyTypeName: String?): String =
        "${forPropertyType(propertyTypeName).singular} Number/Name"

    fun editUnitTitle(propertyTypeName: String?, number: String): String =
        "Edit ${forPropertyType(propertyTypeName).singular} $number"

    fun deleteUnitTitle(propertyTypeName: String?, number: String): String =
        "Delete ${forPropertyType(propertyTypeName).singular} $number?"

    fun deleteUnitMessage(propertyTypeName: String?, number: String): String {
        val label = forPropertyType(propertyTypeName).singular
        return "This will permanently delete $label $number. This cannot be undone."
    }

    fun unitOccupiedMessage(propertyTypeName: String?, number: String): String {
        val label = forPropertyType(propertyTypeName).singular
        return "$label $number is currently occupied. Please remove the tenant before deleting this $label."
    }

    fun unitDeletedMessage(propertyTypeName: String?): String =
        "${forPropertyType(propertyTypeName).singular} deleted successfully"

    fun unitUpdatedMessage(propertyTypeName: String?): String =
        "${forPropertyType(propertyTypeName).singular} updated successfully"

    fun unitNumberRequiredMessage(propertyTypeName: String?): String =
        "${forPropertyType(propertyTypeName).singular} number required"

    fun selectUnitHint(propertyTypeName: String?): String =
        "Select ${forPropertyType(propertyTypeName).singular}"

    fun searchUnitHint(propertyTypeName: String?): String =
        "Search ${forPropertyType(propertyTypeName).singular.lowercase()}..."
}