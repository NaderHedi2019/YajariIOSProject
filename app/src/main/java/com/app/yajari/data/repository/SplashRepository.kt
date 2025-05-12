package com.app.yajari.data.repository

import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper

class SplashRepository (private val preferencesHelper: PreferencesHelper, private val apiService: ApiService) {

    fun getIsLoggedIn() = preferencesHelper.isLoggedIn()
    fun setLanguage(language:String) = preferencesHelper.setLanguage(language)
    fun getLanguage() = preferencesHelper.getLanguage()
    fun getGuest()=preferencesHelper.getGuestUser()
    suspend fun versionCheck(map: Map<String, String>) = apiService.versionCheck(params = map)
}