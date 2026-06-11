package com.xvantage.rental.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RentalNotificationHelper.createChannels(this)
        Logger.d("BaseApplication", "Application started")
        instance = this
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: Context

        fun get(): Context {
            return instance
        }
    }
}
