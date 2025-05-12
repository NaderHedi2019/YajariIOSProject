package com.app.yajari.shared.utils

expect class Platform() {
    val os: OS
    val osVersion: String
    val deviceModel: String
}

enum class OS {
    ANDROID,
    IOS
} 