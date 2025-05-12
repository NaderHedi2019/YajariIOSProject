package com.app.yajari.data.repository

import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileRepository(
    private val preferencesHelper: PreferencesHelper,
    private val apiService: ApiService
) {
    fun setLanguage(language: String) = preferencesHelper.setLanguage(language)
    fun getLanguage() = preferencesHelper.getLanguage()
    fun setUserToPref(user: String) = preferencesHelper.setUser(user)
    fun clearData() = preferencesHelper.clear()
    fun getUserData() = preferencesHelper.getUser()
    suspend fun getProfile() = getAccessToken().let { apiService.getProfile(preferencesHelper.getLanguage(), it) }
    suspend fun changeLanguage(map: Map<String, String>) = getAccessToken().let { apiService.changeLanguage(it, map) }
    suspend fun reportUser(map: Map<String, String>) = getAccessToken().let { apiService.reportUser(preferencesHelper.getLanguage(), it, map) }
    suspend fun rateReview(map: Map<String, String>) = getAccessToken().let { apiService.reviewList(preferencesHelper.getLanguage(), it, map) }
    suspend fun communityImpact() = getAccessToken().let { apiService.communityImpact(preferencesHelper.getLanguage(), it) }
    suspend fun getPartners() = getAccessToken().let { apiService.getPartners(preferencesHelper.getLanguage(), it) }
    suspend fun editProfile(params: Map<String, RequestBody>, file: MultipartBody.Part?) = apiService.editProfile(preferencesHelper.getLanguage(), getAccessToken(), params, file)
    suspend fun deleteAnnouncement(map: Map<String, String>) = apiService.deleteAnnouncement(preferencesHelper.getLanguage(), getAccessToken(), map)
    suspend fun favouriteAnnouncement(map: Map<String, String>) = apiService.favouriteAnnouncement(preferencesHelper.getLanguage(), getAccessToken(), map)

    suspend fun logout() = getAccessToken().let { apiService.logout(preferencesHelper.getLanguage(), it) }
    suspend fun faq() = getAccessToken().let { apiService.faq(preferencesHelper.getLanguage(), it) }
    suspend fun deleteAccount(map: Map<String, String>) = getAccessToken().let { apiService.deleteAccount(preferencesHelper.getLanguage(), it, map) }
    suspend fun changePassword(map: Map<String, String>) = getAccessToken().let { apiService.changePassword(preferencesHelper.getLanguage(), it, map) }
    suspend fun support(map: Map<String, String>) = getAccessToken().let { apiService.helpSupport(preferencesHelper.getLanguage(), it, map) }
    suspend fun myAnnouncement(map: Map<String, String>) = getAccessToken().let { apiService.myAnnouncement(preferencesHelper.getLanguage(), it, map) }
    suspend fun favouriteAnnouncementList(map: Map<String, String>) = getAccessToken().let { apiService.favouriteAnnouncementList(preferencesHelper.getLanguage(), it, map) }
    suspend fun favouriteUser(map: Map<String, String>) = getAccessToken().let { apiService.favouriteUser(preferencesHelper.getLanguage(), it, map) }
    suspend fun otherUserProfile(map: Map<String, String>) = getAccessToken().let { apiService.otherUserProfile(preferencesHelper.getLanguage(), it, map) }
    suspend fun unFavouriteUser(map: Map<String, String>) = getAccessToken().let { apiService.userFavourite(preferencesHelper.getLanguage(), it, map) }
    suspend fun alertSetting(map: Map<String, String>) = getAccessToken().let { apiService.alertSetting(preferencesHelper.getLanguage(), it, map) }
    private fun getAccessToken(): String {
        return preferencesHelper.getToken().let {
            "Bearer $it"
        }
    }
}