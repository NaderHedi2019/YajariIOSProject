package com.app.yajari.data.api

import com.app.yajari.data.AnnouncementDetailsResponse
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.ChatDetailResponse
import com.app.yajari.data.ChatListResponse
import com.app.yajari.data.CommonResponse
import com.app.yajari.data.CommunityImpactResponse
import com.app.yajari.data.FaqResponse
import com.app.yajari.data.FavouriteUserResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.data.NotificationResponse
import com.app.yajari.data.OtherUserResponse
import com.app.yajari.data.PartnerResponse
import com.app.yajari.data.RateReviewResponse
import com.app.yajari.data.UserChatListResponse
import com.app.yajari.data.VersionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    /**
     * Version checker
     */
    @POST("version_checker")
    suspend fun versionCheck(@Body params: Map<String, String>): Response<VersionResponse>

    /**
     * Register
     */
    @POST("signup")
    suspend fun signup(
        @Header("localization") language: String,
        @Body params: Map<String, String>,
    ): Response<LoginResponse>

    /**
     * Send OTP
     */
    @POST("send_otp")
    suspend fun sendOTP(
        @Header("localization") language: String,
        @Body params: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Check Availability
     */
    @POST("check_ability")
    suspend fun checkAvailability(
        @Header("localization") language: String,
        @Body params: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Forgot Password
     */
    @POST("forgot_password")
    suspend fun forgotPassword(
        @Header("localization") language: String,
        @Body params: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Login
     */
    @POST("login")
    suspend fun login(
        @Header("localization") language: String,
        @Body params: Map<String, String>,
    ): Response<LoginResponse>

    /**
     * Logout
     */
    @GET("user/logout")
    suspend fun logout(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
    ): Response<CommonResponse>

    /**
     * Change Password
     */
    @POST("user/password_update")
    suspend fun changePassword(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Help & Support
     */
    @POST("user/help_support")
    suspend fun helpSupport(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /***
     * User Delete
     */
    @POST("user/deleteAccount")
    suspend fun deleteAccount(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * FAQ
     */
    @POST("user/faqs")
    suspend fun faq(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
    ): Response<FaqResponse>

    /**
     * Get Profile
     */
    @GET("user/getProfile")
    suspend fun getProfile(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
    ): Response<LoginResponse>

    /**
     * Edit Profile
     */
    @Multipart
    @JvmSuppressWildcards
    @POST("user/edit_profile")
    suspend fun editProfile(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @PartMap params: Map<String, RequestBody>,
        @Part part: MultipartBody.Part?,
    ): Response<LoginResponse>

    /**
     * Partners
     */
    @POST("user/partners")
    suspend fun getPartners(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
    ): Response<PartnerResponse>

    /**
     * create Announcement
     */
    @Multipart
    @JvmSuppressWildcards
    @POST("announcement/create")
    suspend fun createAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Part image: MutableList<MultipartBody.Part>?,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
    ): Response<CommonResponse>

    /**
     * Get Category
     */
    @POST("user/category")
    suspend fun getCategory(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
    ): Response<CategoryResponse>

    /**
     * edit Announcement
     */
    @Multipart
    @JvmSuppressWildcards
    @POST("announcement/edit")
    suspend fun editAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Part image: MutableList<MultipartBody.Part>?,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
    ): Response<CommonResponse>

    /**
     * Delete Announcement
     */
    @POST("announcement/delete")
    suspend fun deleteAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Delete Announcement Photo
     */
    @POST("announcement/gallery_remove")
    suspend fun deletePhoto(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Object Donation list
     */
    @POST("home/object/donation")
    suspend fun getObjectDonation(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementResponse>

    /**
     * Object Request list
     */
    @POST("home/object/request")
    suspend fun getObjectRequest(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementResponse>

    /**
     * Food Donation list
     */
    @POST("home/food/donation")
    suspend fun getFoodDonation(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementResponse>

    /**
     * Food Request list
     */
    @POST("home/food/request")
    suspend fun getFoodRequest(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementResponse>

    /**
     * Announcement Favourite
     */
    @POST("announcement/bookmark")
    suspend fun favouriteAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Announcement Details
     */
    @POST("announcement/details")
    suspend fun announcementDetails(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementDetailsResponse>

    /**
     * User Favourite
     */
    @POST("user/bookmark")
    suspend fun userFavourite(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    @POST("user/alert_details")
    suspend fun alertSetting(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * My Announcement
     */
    @POST("user/my_announcement")
    suspend fun myAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<MyAnnouncementResponse>

    /**
     * favourite announcements
     */
    @POST("user/favorite_announcement")
    suspend fun favouriteAnnouncementList(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<AnnouncementResponse>

    /**
     * favourite user list
     */
    @POST("user/favorite_user")
    suspend fun favouriteUser(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<FavouriteUserResponse>

    /**
     * Other User profile
     */
    @POST("user/other_user_profile")
    suspend fun otherUserProfile(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<OtherUserResponse>

    /**
     * Change language
     */
    @POST("user/change_language")
    suspend fun changeLanguage(
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Send Request
     */
    @POST("chat/send_request")
    suspend fun sendRequest(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Request Chat List
     */
    @POST("chat/list")
    suspend fun requestChatList(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<ChatListResponse>

    /**
     * Donation chat list
     */
    @POST("chat/list_donation")
    suspend fun donationChatList(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<ChatListResponse>

    /**
     * Chat details
     */
    @POST("chat/details")
    suspend fun chatDetails(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<ChatDetailResponse>

    /**
     * Send message
     */
    @POST("chat/send_message")
    suspend fun sendMessage(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Chat user list
     */
    @POST("chat/user_list")
    suspend fun chatUserList(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<UserChatListResponse>

    /**
     * Change status
     */
    @POST("chat/change_status")
    suspend fun changeStatus(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Notification
     */
    @POST("user/notification")
    suspend fun notifications(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<NotificationResponse>

    /**
     * Submit rate & review
     */
    //rate_review
    //rate_review_user
    @POST("review/{endPoint}")
    suspend fun submitRateReview(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Path(value = "endPoint", encoded = true) endPoint:String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>



    /**
     * Report Announcement
     */
    @POST("announcement/report")
    suspend fun reportAnnouncement(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Report User
     */
    @POST("user/report")
    suspend fun reportUser(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<CommonResponse>

    /**
     * Review list
     */
    @POST("review/list")
    suspend fun reviewList(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String,
        @Body map: Map<String, String>,
    ): Response<RateReviewResponse>

    /**
     * Community impact
     */
    @GET("user/community_impact")
    suspend fun communityImpact(
        @Header("localization") language: String,
        @Header("VAuthorization") authorization: String
    ): Response<CommunityImpactResponse>
}