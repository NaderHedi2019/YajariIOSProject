package com.app.yajari.shared.utils

import platform.UIKit.UIDevice

actual class Platform {
    actual val os: OS = OS.IOS
    actual val osVersion: String = UIDevice.currentDevice.systemVersion
    actual val deviceModel: String = UIDevice.currentDevice.model
} 