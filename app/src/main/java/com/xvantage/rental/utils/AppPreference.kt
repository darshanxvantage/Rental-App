
package com.xvantage.rental.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppPreference @Inject constructor(
    @ApplicationContext context: Context
) {

    private var appSharedPrefs: SharedPreferences =
        context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )

    private val editor: SharedPreferences.Editor =
        appSharedPrefs.edit()

    companion object {

        private const val KEY_LISTED_COUNT =
            "listed_count"
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


        private const val KEY_RENTED_COUNT =
            "rented_count"

        private const val KEY_RATING =
            "rating"


        private const val KEY_PROFILE_IMAGE =
            "profile_image"

        private const val KEY_THEME =
            "theme"

        private const val KEY_FONT_SIZE        = "font_size"
        private const val KEY_LANGUAGE         = "language"
        private const val KEY_PUSH_NOTIF       = "push_notif"
        private const val KEY_EMAIL_ALERTS     = "email_alerts"
        private const val KEY_WHATSAPP_ALERTS  = "whatsapp_alerts"
        private const val KEY_DND              = "dnd_enabled"
        private const val KEY_AUTO_INVOICE     = "auto_invoice"
        private const val KEY_RENT_DUE_DAY     = "rent_due_day"
        private const val KEY_BIOMETRIC        = "biometric_enabled"
        private const val KEY_TWO_STEP         = "two_step_enabled"
        private const val KEY_CLOUD_BACKUP     = "cloud_backup"
    }

    fun setFcmToken(token: String) {
        editor.putString("fcm_token", token)
        editor.apply()
    }
    fun getFcmToken(): String {
        return appSharedPrefs.getString("fcm_token", "") ?: ""
    }

    fun setListedCount(count: Int) {

        editor.putInt(
            KEY_LISTED_COUNT,
            count
        )

        editor.apply()
    }

    fun getListedCount(): Int {

        return appSharedPrefs.getInt(
            KEY_LISTED_COUNT,
            0
        )
    }


    fun setRentedCount(count: Int) {

        editor.putInt(
            KEY_RENTED_COUNT,
            count
        )

        editor.apply()
    }

    fun getRentedCount(): Int {

        return appSharedPrefs.getInt(
            KEY_RENTED_COUNT,
            0
        )
    }

    fun setRating(rating: String) {

        editor.putString(
            KEY_RATING,
            rating
        )

        editor.apply()
    }

    fun getRating(): String {

        return appSharedPrefs.getString(
            KEY_RATING,
            "4.5"
        ) ?: "4.5"
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

    // GENDER
    fun setGender(gender: String) {
        editor.putString("gender", gender)
        editor.apply()
    }

    fun getGender(): String {
        return appSharedPrefs.getString("gender", "") ?: ""
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
    // THEME

    fun setTheme(theme: String) {

        editor.putString(
            KEY_THEME,
            theme
        )

        editor.apply()
    }

    fun getTheme(): String {

        return appSharedPrefs.getString(
            KEY_THEME,
            "LIGHT"
        ) ?: "LIGHT"
    }

    // CLEAR ALL






    // ─── FONT SIZE ──────────────────────────────────────────────────────────────
    fun setFontSize(size: String) {
        editor.putString("font_size", size)
        editor.apply()
    }
    fun getFontSize(): String {
        return appSharedPrefs.getString("font_size", "Medium") ?: "Medium"
    }

    // ─── LANGUAGE ───────────────────────────────────────────────────────────────
    fun setLanguage(language: String) {
        editor.putString("language", language)
        editor.apply()
    }
    fun getLanguage(): String {
        return appSharedPrefs.getString("language", "English") ?: "English"
    }

    // ─── PUSH NOTIFICATIONS ─────────────────────────────────────────────────────
    fun setPushNotifEnabled(enabled: Boolean) {
        editor.putBoolean("push_notif", enabled)
        editor.apply()
    }
    fun isPushNotifEnabled(): Boolean {
        return appSharedPrefs.getBoolean("push_notif", true)
    }

    // ─── EMAIL ALERTS ───────────────────────────────────────────────────────────
    fun setEmailAlertsEnabled(enabled: Boolean) {
        editor.putBoolean("email_alerts", enabled)
        editor.apply()
    }
    fun isEmailAlertsEnabled(): Boolean {
        return appSharedPrefs.getBoolean("email_alerts", true)
    }

    // ─── WHATSAPP ALERTS ────────────────────────────────────────────────────────
    fun setWhatsAppAlertsEnabled(enabled: Boolean) {
        editor.putBoolean("whatsapp_alerts", enabled)
        editor.apply()
    }
    fun isWhatsAppAlertsEnabled(): Boolean {
        return appSharedPrefs.getBoolean("whatsapp_alerts", false)
    }

    // ─── DO NOT DISTURB ─────────────────────────────────────────────────────────
    fun setDNDEnabled(enabled: Boolean) {
        editor.putBoolean("dnd_enabled", enabled)
        editor.apply()
    }
    fun isDNDEnabled(): Boolean {
        return appSharedPrefs.getBoolean("dnd_enabled", false)
    }

    // ─── AUTO INVOICE ───────────────────────────────────────────────────────────
    fun setAutoInvoiceEnabled(enabled: Boolean) {
        editor.putBoolean("auto_invoice", enabled)
        editor.apply()
    }
    fun isAutoInvoiceEnabled(): Boolean {
        return appSharedPrefs.getBoolean("auto_invoice", true)
    }

    // ─── RENT DUE DAY ───────────────────────────────────────────────────────────
    fun setRentDueDay(day: Int) {
        editor.putInt("rent_due_day", day)
        editor.apply()
    }
    fun getRentDueDay(): Int {
        return appSharedPrefs.getInt("rent_due_day", 1)
    }

    // ─── BIOMETRIC ──────────────────────────────────────────────────────────────
    fun setBiometricEnabled(enabled: Boolean) {
        editor.putBoolean("biometric_enabled", enabled)
        editor.apply()
    }
    fun isBiometricEnabled(): Boolean {
        return appSharedPrefs.getBoolean("biometric_enabled", false)
    }

    // ─── TWO STEP VERIFY ────────────────────────────────────────────────────────
    fun setTwoStepEnabled(enabled: Boolean) {
        editor.putBoolean("two_step_enabled", enabled)
        editor.apply()
    }
    fun isTwoStepEnabled(): Boolean {
        return appSharedPrefs.getBoolean("two_step_enabled", true)
    }

    // ─── CLOUD BACKUP ───────────────────────────────────────────────────────────
    fun setCloudBackupEnabled(enabled: Boolean) {
        editor.putBoolean("cloud_backup", enabled)
        editor.apply()
    }
    fun isCloudBackupEnabled(): Boolean {
        return appSharedPrefs.getBoolean("cloud_backup", false)
    }

    fun logoutUser() {

        editor.remove("jwt_token")

        editor.apply()
    }

}

