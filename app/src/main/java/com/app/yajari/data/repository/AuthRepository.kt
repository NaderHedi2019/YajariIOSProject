package com.app.yajari.data.repository

import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper

class AuthRepository (private val preferencesHelper: PreferencesHelper, private val apiService: ApiService)
{
    fun setIsLoggedIn(isLogin : Boolean) = preferencesHelper.setIsLoggedIn(isLogin)
    fun setUserPref(user: String) = preferencesHelper.setUser(user)
    fun setLanguage(language : String) = preferencesHelper.setLanguage(language)
    fun getUser() = preferencesHelper.getUser()
    fun setToken(token:String)=preferencesHelper.setToken(token)
    fun setGuest(isGuest:Boolean)=preferencesHelper.setGuestUser(isGuest)
    suspend fun signup(params : Map<String,String>) = apiService.signup(preferencesHelper.getLanguage(),params)
    suspend fun login(params : Map<String,String>) = apiService.login(preferencesHelper.getLanguage(),params)
    suspend fun sendOTP(params : Map<String,String>) = apiService.sendOTP(preferencesHelper.getLanguage(),params)
    suspend fun checkAvailability(params : Map<String,String>) = apiService.checkAvailability(preferencesHelper.getLanguage(),params)
    suspend fun forgotPassword(params : Map<String,String>) = apiService.forgotPassword(preferencesHelper.getLanguage(),params)

}