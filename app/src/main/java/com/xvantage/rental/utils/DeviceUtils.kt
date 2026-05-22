
package com.xvantage.rental.utils

import android.os.Build

object DeviceUtils {

    fun getDeviceName(): String {

        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun getAndroidVersion(): String {

        return Build.VERSION.RELEASE
    }
}
