package com.app.yajari.shared.utils

import android.os.Build

actual class Platform {
    actual val os: OS = OS.ANDROID
    actual val osVersion: String = Build.VERSION.RELEASE
    actual val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"
} 