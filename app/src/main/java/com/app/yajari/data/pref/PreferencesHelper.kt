package com.app.yajari.data.pref

import com.app.yajari.data.LoginResponse


interface PreferencesHelper {


    fun setIsLoggedIn(isLogin : Boolean)
    fun isLoggedIn(): Boolean
    fun setLanguage(language: String)
    fun getLanguage(): String
    fun setToken(token: String)
    fun getToken(): String

    fun setUser(user: String)
    fun getUser(): LoginResponse.Data
    fun setGuestUser(isGuest : Boolean)
    fun getGuestUser(): Boolean
    fun clear()
}