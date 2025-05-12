package com.app.yajari.data.repository

import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper

class ChatRepository (
    private val preferencesHelper: PreferencesHelper,
    private val apiService: ApiService
)
{
    suspend fun requestChatList(map: Map<String,String>)=apiService.requestChatList(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun donationChatList(map: Map<String,String>)=apiService.donationChatList(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun chatDetails(map: Map<String,String>)=apiService.chatDetails(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun sendMessage(map: Map<String,String>)=apiService.sendMessage(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun changeStatus(map: Map<String,String>)=apiService.changeStatus(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun submitRateReview(map: Map<String,String>,endPoint:String)=apiService.submitRateReview(preferencesHelper.getLanguage(),getAccessToken(),endPoint,map)
    suspend fun chatUserList(map: Map<String,String>)=apiService.chatUserList(preferencesHelper.getLanguage(),getAccessToken(),map)

    private fun getAccessToken(): String {
        return preferencesHelper.getToken().let {
            "Bearer $it"
        }
    }
    fun clearData() = preferencesHelper.clear()
    fun getUserData() = preferencesHelper.getUser()
}