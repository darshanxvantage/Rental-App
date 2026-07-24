package com.xvantage.rental.ui.explore.common

import android.content.Context
import android.content.SharedPreferences

/**
 * Tiny, self-contained preference wrapper - ONLY for the Explore feature's
 * "Student" vs "Property Owner" choice made on RoleSelectionActivity.
 *
 * Deliberately NOT added to the existing AppPreference.kt so that file
 * stays 100% untouched, per the "don't touch old flow" requirement.
 */
object ExploreRoleManager {

    private const val PREF_NAME = "explore_role_prefs"
    private const val KEY_SELECTED_ROLE = "selected_role"

    const val ROLE_STUDENT = "student"
    const val ROLE_OWNER = "owner"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveRole(context: Context, role: String) {
        prefs(context).edit().putString(KEY_SELECTED_ROLE, role).apply()
    }

    fun getRole(context: Context): String? {
        return prefs(context).getString(KEY_SELECTED_ROLE, null)
    }

    fun isOwner(context: Context): Boolean = getRole(context) == ROLE_OWNER

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_SELECTED_ROLE).apply()
    }
}