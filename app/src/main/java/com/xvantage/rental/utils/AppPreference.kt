
package com.xvantage.rental.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreference(context: Context) {

    private var appSharedPrefs: SharedPreferences =
        context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )

    private val editor: SharedPreferences.Editor =
        appSharedPrefs.edit()

    companion object {

        private const val KEY_JWT_TOKEN =
            "jwt_token"

        private const val KEY_USER_NAME =
            "user_name"

        private const val KEY_PHONE =
            "phone"

        private const val KEY_EMAIL =
            "email"

        private const val KEY_CITY =
            "city"

        private const val KEY_STATE =
            "state"

        private const val KEY_AGE =
            "age"

        private const val KEY_PROFILE_IMAGE =
            "profile_image"
    }

    fun isUserLoginFirstTime(): Boolean {

        return appSharedPrefs.getBoolean(
            "isUserLoginFirstTime",
            false
        )
    }

    fun setUserLoginFirstTime(value: Boolean?) {

        editor.putBoolean(
            "isUserLoginFirstTime",
            value!!
        )

        editor.commit()
    }

    fun isFirstTimePreview(): Boolean {

        return appSharedPrefs.getBoolean(
            "isFirstTimePreview",
            false
        )
    }

    fun setFirstTimePreview(value: Boolean?) {

        editor.putBoolean(
            "isFirstTimePreview",
            value!!
        )

        editor.commit()
    }

    fun isRemove(): Boolean {

        return appSharedPrefs.getBoolean(
            "isRemove",
            false
        )
    }

    fun setRemove(value: Boolean?) {

        editor.putBoolean(
            "isRemove",
            value!!
        )

        editor.commit()
    }

    fun isFirst(): Boolean {

        return appSharedPrefs.getBoolean(
            "isFirst",
            false
        )
    }

    fun setFirst(value: Boolean?) {

        editor.putBoolean(
            "isFirst",
            value!!
        )

        editor.commit()
    }

    fun isUserLogin(): Boolean {

        return appSharedPrefs.getBoolean(
            "isUserLogin",
            false
        )
    }

    fun setUId(data: String?) {

        editor.putString(
            "UId",
            data
        )

        editor.commit()
    }

    fun getUId(): String? {

        return appSharedPrefs.getString(
            "UId",
            ""
        )
    }

    // JWT TOKEN

    fun setToken(token: String?) {

        editor.putString(
            KEY_JWT_TOKEN,
            token
        )

        editor.commit()
    }

    fun getToken(): String? {

        return appSharedPrefs.getString(
            KEY_JWT_TOKEN,
            null
        )
    }

    // USER NAME

    fun setUserName(name: String) {

        editor.putString(
            KEY_USER_NAME,
            name
        )

        editor.apply()
    }

    fun getUserName(): String? {

        return appSharedPrefs.getString(
            KEY_USER_NAME,
            ""
        )
    }

    // PHONE

    fun setPhone(phone: String) {

        editor.putString(
            KEY_PHONE,
            phone
        )

        editor.apply()
    }

    fun getPhone(): String? {

        return appSharedPrefs.getString(
            KEY_PHONE,
            ""
        )
    }

    // EMAIL

    fun setEmail(email: String) {

        editor.putString(
            KEY_EMAIL,
            email
        )

        editor.apply()
    }

    fun getEmail(): String? {

        return appSharedPrefs.getString(
            KEY_EMAIL,
            ""
        )
    }

    // CITY

    fun setCity(city: String) {

        editor.putString(
            KEY_CITY,
            city
        )

        editor.apply()
    }

    fun getCity(): String? {

        return appSharedPrefs.getString(
            KEY_CITY,
            ""
        )
    }

    // STATE

    fun setState(state: String) {

        editor.putString(
            KEY_STATE,
            state
        )

        editor.apply()
    }

    fun getState(): String? {

        return appSharedPrefs.getString(
            KEY_STATE,
            ""
        )
    }

    // AGE

    fun setAge(age: String) {

        editor.putString(
            KEY_AGE,
            age
        )

        editor.apply()
    }

    fun getAge(): String? {

        return appSharedPrefs.getString(
            KEY_AGE,
            ""
        )
    }

    // PROFILE IMAGE

    fun setProfileImage(path: String) {

        editor.putString(
            KEY_PROFILE_IMAGE,
            path
        )

        editor.apply()
    }

    fun getProfileImage(): String? {

        return appSharedPrefs.getString(
            KEY_PROFILE_IMAGE,
            ""
        )
    }

    // CLEAR ALL

    fun logoutUser() {

        editor.remove("jwt_token")

        editor.apply()
    }

}

