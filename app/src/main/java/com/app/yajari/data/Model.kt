package com.app.yajari.data

import android.net.Uri
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File
import java.io.Serializable

data class HomeData(
    val tag: String,
    var isLike: Boolean,
)

data class MediaModel(
    val media: File? = null,
    val imageUri: Uri? = null,
    var imageUrl: String? = null,
    var type: String? = null,
    var id: String? = null,
)

data class AnnouncementType(var name: String, var value: String) {
    override fun toString(): String {
        return name
    }
}

data class StatusType(var name: String, var value: String) {
    override fun toString(): String {
        return name
    }
}

data class PlanData(
    var price: String,
    var planDuration: String,
    var title: String,
    var planDesc: MutableList<String>,
    var selected: Boolean,
)

data class DispenseData(var name: String, var value:String,var selected: Boolean)
data class AskData(
    var title: String,
    var optionName1: String,
    var icon1: Int,
    var optionName2: String,
    var icon2: Int,
    var selected1: Boolean,
    var selected2: Boolean,
)

data class LanguageData(
    var language: String,
    var countryCode: String,
    var flag: Int,
    var isSelected: Boolean,
)

data class PartnerData(var icon: Int, var name: String)

data class ReportData(var name: String,var value: String, var selected: Boolean)

data class DrawerModel(
    val menuIcon: Int,
    val menuName: String,
)

data class ProfileOptionData(
    val menuIcon: Int,
    val menuName: String,
)

//Common
@JsonClass(generateAdapter = true)
@Keep
data class CommonResponse(
    @Json(name = "data")
    val data: Any? = null,
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = null,
)

//Version
@JsonClass(generateAdapter = true)
data class VersionResponse(
    val data: Data? = null,
    val message: String? = null,
    val status: Int? = 0,
) {
    @JsonClass(generateAdapter = true)
    data class Data(
        @Json(name = "is_force_update")
        val isForceUpdate: Int? = null,
        @Json(name = "total_user")
        val totalUser: Int? = null,
        @Json(name = "total_donation")
        val totalDonation: Int? = null,
    )
}

//Login

data class LoginResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "address")
        val address: String? = "",
        @Json(name = "bod")
        val bod: String? = "",
        @Json(name = "country_code")
        val countryCode: String? = "",
        @Json(name = "email")
        val email: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "mobile")
        val mobile: String? = "",
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "token")
        val token: String? = "",
        @Json(name = "avg_rating")
        val avgRating: String? = "",
        @Json(name = "total_review")
        val totalReview: String? = "",
        @Json(name = "donation_count")
        val donationCount: Int? = 0,
        @Json(name = "collection_count")
        val collectionCount: Int? = 0,
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "object_category")
        val objectCategory: String? = "",
        @Json(name = "object_duration")
        val objectDuration: String? = "",
        @Json(name = "food_duration")
        val foodDuration: String? = "",
        @Json(name = "language")
        val language : String? = ""
    )
}

/**
 * Faq Response
 */
data class FaqResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "description")
        val description: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "title")
        val title: String? = "",
        var isExpand: Boolean = false,
    )
}

/**
 * Partners
 */
data class PartnerResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "image")
        val image: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "title")
        val title: String? = "",
    )
}

/**
 * Category Response
 */
data class CategoryResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "category")
        val category: MutableList<Category>? = mutableListOf(),
        @Json(name = "condition")
        val condition: MutableList<Condition>? = mutableListOf(),
    )

    data class Category(
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lang")
        val lang: String? = "",
        @Json(name = "title")
        val title: String? = "",
        @Json(name = "unique_id")
        val uniqueId: String? = "",
        var isSelected: Boolean = false,
    )

    data class Condition(
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lang")
        val lang: String? = "",
        @Json(name = "title")
        val title: String? = "",
        @Json(name = "unique_id")
        val uniqueId: String? = "",
    ) {
        override fun toString(): String {
            return title.toString()
        }
    }
}

/**
 * Announcement Response
 */
data class AnnouncementResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
    @Json(name = "notification_count")
    val notificationCount: Int? = 0,
    @Json(name = "message_count")
    val messageCount: Int? = 0,
) {
    data class Data(
        @Json(name = "condition")
        val condition: String? = "",
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "distance")
        val distance: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "image")
        val image: String? = "",
        @Json(name = "is_bookmark")
        var isBookmark: Int? = 0,
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "title")
        val title: String? = "",
    )
}

/**
 * Announcement Details
 */
data class AnnouncementDetailsResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "finalized")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "category")
        val category: String? = "",
        @Json(name = "condition")
        val condition: String? = "",
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "description")
        val description: String? = "",
        @Json(name = "distance")
        val distance: String? = "",
        @Json(name = "finalized")
        val finalized: Finalized? = null,
        @Json(name = "gallery")
        val gallery: MutableList<Gallery>? = mutableListOf(),
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "image")
        val image: String? = "",
        @Json(name = "is_bookmark")
        var isBookmark: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "location")
        val location: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "title")
        val title: String? = "",
        @Json(name = "object_type")
        val objectType: String? = "",
        @Json(name = "type")
        val type: String? = "",
        @Json(name = "is_thread_id")
        val isThreadId: Int? = 0,
        @Json(name = "expiration_date")
        var expirationDate: String? = "",
        @Json(name = "commercial")
        var commercial: String? = "",
        @Json(name = "condition_id")
        var conditionId: String? = "",
        @Json(name = "category_id")
        val categoryId: String? = "",
        @Json(name = "user")
        val user: User? = User(),
    )

    data class Finalized(
        @Json(name = "id")
        val id: Int? = null,
        @Json(name = "name")
        val name: String? = null,
        @Json(name = "email")
        val email: String? = null,
        @Json(name = "profile_image")
        val profile_image: String? = null,
        @Json(name = "address")
        val address: String? = null,
        @Json(name = "lat")
        val lat: String? = null,
        @Json(name = "lng")
        val lng: String? = null,
        @Json(name = "avg_rating")
        val avg_rating: String? = null,
        @Json(name = "total_review")
        val total_review: Int? = null,
        @Json(name = "donation_count")
        val donation_count: Int? = null,
        @Json(name = "collection_count")
        val collection_count: Int? = null,
        @Json(name = "created_at")
        val created_at: String? = null
    )


    data class Gallery(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "file")
        val `file`: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
    )

    data class User(
        @Json(name = "address")
        val address: String? = "",
        @Json(name = "avg_rating")
        val avgRating: String? = "",
        @Json(name = "collection_count")
        val collectionCount: Int? = 0,
        @Json(name = "donation_count")
        val donationCount: Int? = 0,
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "total_review")
        val totalReview: String? = "",
        @Json(name = "is_bookmark")
        var isBookmark: Int? = 0,
        @Json(name = "distance")
        val distance: String? = "",
    )
}


//Favourite User
data class FavouriteUserResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "address")
        val address: String? = "",
        @Json(name = "avg_rating")
        val avgRating: String? = "",
        @Json(name = "collection_count")
        val collectionCount: Int? = 0,
        @Json(name = "distance")
        val distance: String? = "",
        @Json(name = "donation_count")
        val donationCount: Int? = 0,
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "total_review")
        val totalReview: Int? = 0,
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "is_bookmark")
        var isBookmark: Int? = 0
    )
}

// OtherUser Response
data class OtherUserResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
) {
    data class Data(
        @Json(name = "announcement")
        val announcement: MutableList<AnnouncementResponse.Data>? = mutableListOf(),
        @Json(name = "user")
        val user: FavouriteUserResponse.Data? = FavouriteUserResponse.Data(),
    )

}

//My Announcement

data class MyAnnouncementResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0
)
{
    data class Data(
        @Json(name = "category")
        var category: String? = "",
        @Json(name = "condition")
        var condition: String? = "",
        @Json(name = "created_at")
        var createdAt: String? = "",
        @Json(name = "distance")
        var distance: String? = "",
        @Json(name = "gallery")
        var gallery: MutableList<Gallery>? = mutableListOf(),
        @Json(name = "id")
        var id: Int? = 0,
        @Json(name = "image")
        var image: String? = "",
        @Json(name = "is_bookmark")
        var isBookmark: Int? = 0,
        @Json(name = "object_type")
        var objectType: String? = "",
        @Json(name = "status")
        var status: String? = "",
        @Json(name = "title")
        var title: String? = "",
        @Json(name = "type")
        var type: String? = "",
        @Json(name = "expiration_date")
        var expirationDate: String? = "",
        @Json(name = "commercial")
        var commercial: String? = "",
        @Json(name = "description")
        var description: String? = "",
        @Json(name = "location")
        var location: String? = "",
        @Json(name = "lat")
        var lat: String? = "",
        @Json(name = "lng")
        var lng: String? = "",
        @Json(name = "condition_id")
        var conditionId: String? = "",
        @Json(name = "category_id")
        var categoryId: String? = "",
    ):Serializable
    data class Gallery(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "file")
        val `file`: String? = "",
        @Json(name = "id")
        val id: Int? = 0
    ):Serializable
}

//Chat List

data class ChatListResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
    @Json(name = "notification_count")
    val notificationCount: Int? = 0,
    @Json(name = "message_count")
    val messageCount: Int? = 0,

)
{
    data class Data(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "object_id")
        val objectId: String? = "",
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "friend_request")
        val friendRequest: MutableList<FriendRequest>? = mutableListOf(),
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "thumb_image")
        val thumbImage: ThumbImage? = ThumbImage(),
        @Json(name = "title")
        val title: String? = "",
        @Json(name = "type")
        val type: String? = "",
        @Json(name = "friend_request_count")
        val friendRequestCount:Int=0,
        @Json(name = "unread_message")
        val unreadMsgCount:Int=0,
    ):Serializable
    data class FriendRequest(
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "last_msg_update_time")
        val lastMsgUpdateTime: String? = "",
        @Json(name = "message_latest")
        val messageLatest: MessageLatest? = MessageLatest(),
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "thread_id")
        val threadId: Int? = 0,
        @Json(name = "unread_message")
        val unreadMessage: Int? = 0
    ):Serializable
    data class MessageLatest(
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "message")
        val message: String? = "",
        @Json(name = "receiver_id")
        val receiverId: Int? = 0,
        @Json(name = "receiver_status")
        val receiverStatus: String? = "",
        @Json(name = "sender_id")
        val senderId: Int? = 0,
        @Json(name = "sender_status")
        val senderStatus: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "threads_id")
        val threadsId: Int? = 0,
        @Json(name = "type")
        val type: String? = ""
    ):Serializable
    data class ThumbImage(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "file")
        val `file`: String? = "",
        @Json(name = "id")
        val id: Int? = 0
    ):Serializable
}

//User chat List
data class UserChatListResponse(
    @Json(name = "data")
    val `data`: MutableList<ChatListResponse.FriendRequest>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0
)

// Chat detail
data class ChatDetailResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0,
    @Json(name = "announcement")
    val announcement: Announcement? = Announcement(),
    @Json(name = "is_review")
    val isReview:Int?=0
)
{
    data class Data(
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "message")
        val message: String? = "",
        @Json(name = "receiver_id")
        val receiverId: Int? = 0,
        @Json(name = "receiver_img")
        val receiverImg: String? = "",
        @Json(name = "receiver_name")
        val receiverName: String? = "",
        @Json(name = "receiver_reaction")
        val receiverReaction: String? = "",
        @Json(name = "receiver_status")
        val receiverStatus: String? = "",
        @Json(name = "sender_id")
        val senderId: Int? = 0,
        @Json(name = "sender_img")
        val senderImg: String? = "",
        @Json(name = "sender_name")
        val senderName: String? = "",
        @Json(name = "sender_reaction")
        val senderReaction: String? = "",
        @Json(name = "sender_status")
        val senderStatus: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "threads_id")
        val threadsId: Int? = 0,
        @Json(name = "type")
        val type: String? = "",
        @Json(name = "announcement_type")
        val announcementType:String="",
    )
    data class Announcement(
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "object_id")
        val objectId: Int? = null,
        @Json(name = "object_type")
        val objectType: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "thumb_image")
        val thumbImage: ThumbImage? = ThumbImage(),
        @Json(name = "title")
        val title: String? = "",
        @Json(name = "type")
        val type: String? = "",
        @Json(name = "user_id")
        val userId: Int? = 0
    )
    data class ThumbImage(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "file")
        val `file`: String? = "",
        @Json(name = "id")
        val id: Int? = 0
    )
}

//Notification Response

data class NotificationResponse(
    @Json(name = "data")
    val `data`: MutableList<Data>? = mutableListOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0
)
{
    data class Data(
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "from_user_id")
        val fromUserId: Int? = 0,
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "object_id")
        val objectId: String? = "",
        @Json(name = "object_type")
        val objectType: String? = "",
        @Json(name = "push_message")
        val pushMessage: String? = "",
        @Json(name = "push_title")
        val pushTitle: String? = "",
        @Json(name = "push_type")
        val pushType: Int? = 0,
        @Json(name = "updated_at")
        val updatedAt: String? = "",
        @Json(name = "user_id")
        val userId: Int? = 0
    )
}

//Rate Review
data class RateReviewResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0
)
{
    data class Data(
        @Json(name = "list")
        val list: MutableList<RateReview>? = mutableListOf(),
        @Json(name = "review")
        val review: Review? = Review()
    )
    data class RateReview(
        @Json(name = "announcement_id")
        val announcementId: Int? = 0,
        @Json(name = "auth_id")
        val authId: Int? = 0,
        @Json(name = "comment")
        val comment: String? = "",
        @Json(name = "communication_rating")
        val communicationRating: String? = "",
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "punctuality_rating")
        val punctualityRating: String? = "",
        @Json(name = "rating")
        val rating: String? = "",
        @Json(name = "status")
        val status: String? = "",
        @Json(name = "updated_at")
        val updatedAt: String? = "",
        @Json(name = "user")
        val user: User? = User(),
        @Json(name = "user_id")
        val userId: Int? = 0
    )
    data class Review(
        @Json(name = "avg_rating")
        val avgRating: String? = "0",
        @Json(name = "rating_1_count")
        val rating1Count: String? = "0",
        @Json(name = "rating_2_count")
        val rating2Count: String? = "0",
        @Json(name = "rating_3_count")
        val rating3Count: String? = "0",
        @Json(name = "rating_4_count")
        val rating4Count: String? = "0",
        @Json(name = "rating_5_count")
        val rating5Count: String? = "0",
        @Json(name = "total_review")
        val totalReview: String? = "0"
    )
    data class User(
        @Json(name = "address")
        val address: String? = "",
        @Json(name = "avg_rating")
        val avgRating: String? ="",
        @Json(name = "collection_count")
        val collectionCount: Int? = 0,
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "donation_count")
        val donationCount: Int? = 0,
        @Json(name = "email")
        val email: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "total_review")
        val totalReview: String? = ""
    )
}

//Community Impact
data class CommunityImpactResponse(
    @Json(name = "data")
    val `data`: Data? = Data(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "status")
    val status: Int? = 0
)
{
    data class Data(
        @Json(name = "total_donation")
        val totalDonation: Int? = 0,
        @Json(name = "total_user")
        val totalUser: Int? = 0,
        @Json(name = "user")
        val user: MutableList<User>? = mutableListOf()
    )
    data class User(
        @Json(name = "address")
        val address: String? = "",
        @Json(name = "avg_rating")
        val avgRating: String? = "",
        @Json(name = "collection_count")
        val collectionCount: Int? = 0,
        @Json(name = "created_at")
        val createdAt: String? = "",
        @Json(name = "donation_count")
        val donationCount: Int? = 0,
        @Json(name = "email")
        val email: String? = "",
        @Json(name = "id")
        val id: Int? = 0,
        @Json(name = "lat")
        val lat: String? = "",
        @Json(name = "lng")
        val lng: String? = "",
        @Json(name = "name")
        val name: String? = "",
        @Json(name = "profile_image")
        val profileImage: String? = "",
        @Json(name = "total_review")
        val totalReview: String? = ""
    )
}