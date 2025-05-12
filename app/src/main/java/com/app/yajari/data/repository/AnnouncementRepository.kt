package com.app.yajari.data.repository

import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AnnouncementRepository (
    private val preferencesHelper: PreferencesHelper,
    private val apiService: ApiService
)
{
    suspend fun getCategory()=apiService.getCategory(preferencesHelper.getLanguage(),getAccessToken())
    suspend fun favouriteAnnouncement(map: Map<String,String>)=apiService.favouriteAnnouncement(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun announcementDetails(map: Map<String,String>)=apiService.announcementDetails(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun userFavourite(map: Map<String,String>)=apiService.userFavourite(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun getObjectDonation(map: Map<String,String>)=apiService.getObjectDonation(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun getFoodDonation(map: Map<String,String>)=apiService.getFoodDonation(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun getObjectRequest(map: Map<String,String>)=apiService.getObjectRequest(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun getFoodRequest(map: Map<String,String>)=apiService.getFoodRequest(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun deletePhoto(map: Map<String,String>)=apiService.deletePhoto(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun sendRequest(map: Map<String,String>)=apiService.sendRequest(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun notifications(map: Map<String,String>)=apiService.notifications(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun createAnnouncement(params: Map<String, RequestBody>, image : MutableList<MultipartBody.Part>?) = apiService.createAnnouncement(preferencesHelper.getLanguage(),getAccessToken(),image,params)
    suspend fun editAnnouncement(params: Map<String, RequestBody>, image : MutableList<MultipartBody.Part>?) = apiService.editAnnouncement(preferencesHelper.getLanguage(),getAccessToken(),image,params)
    suspend fun deleteAnnouncement(map: Map<String,String>)=apiService.deleteAnnouncement(preferencesHelper.getLanguage(),getAccessToken(),map)
    suspend fun reportAnnouncement(map: Map<String,String>)=apiService.reportAnnouncement(preferencesHelper.getLanguage(),getAccessToken(),map)

    private fun getAccessToken(): String {
        return preferencesHelper.getToken().let {
            "Bearer $it"
        }
    }
    fun clearData() = preferencesHelper.clear()
    fun getUserData() = preferencesHelper.getUser()


}