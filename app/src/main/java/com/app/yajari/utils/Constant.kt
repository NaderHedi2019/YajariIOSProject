package com.app.yajari.utils
import android.content.Context
import com.app.yajari.R
import com.app.yajari.data.AnnouncementType
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.DispenseData
import com.app.yajari.data.LanguageData
import com.app.yajari.data.ReportData
import com.app.yajari.data.StatusType


enum class Status { SUCCESS, ERROR, LOADING, NETWORK_ERROR, UNAUTHORIZED }

class Constant {
    companion object {
        const val APP_SHARED_PREFERENCE_NAME = "yajari"
        //const val BASE_URL = "https://hexeros.com/dev/yajari/api/V1/"
        const val BASE_URL = "https://yajary.com/api/V1/"
        const val APP_TYPE = "android"
        var sDeviceId = ""
        var sFcmKey = ""
        var DEFAULT_LANGUAGE = "en"
        const val SUCCESS_CODE = 200
        const val UNAUTHORIZED_TOKEN = 401
        const val ERROR_CODE = 412
        const val LIMIT="10"
        /*--------- Common Message ------------------------*/
        const val INTERNET_ERROR = "No Internet Connection"
        const val COMMON_ERROR = "Something went wrong, Please try again."
        const val INTERNET_ERROR_COMMON = "Check your internet connection"

        //Type
        const val FOOD="food"
        const val OBJECT="object"


        // Intent key
        const val FROM="from"
        const val OBJECT_ID="object_id"
        const val PUSH_TYPE="push_type"
        const val KEY="key"
        const val NAME="name"
        const val DOB="dob"
        const val COUNTRY_CODE="country_code"
        const val ADDRESS="address"
        const val LAT="lat"
        const val LNG="lng"
        const val IsFILTER="filter"
        const val EMAIL="email"
        const val PASSWORD="password"
        const val EDIT="edit"
        const val CREATE="create"
        const val ANNOUNCEMENT_DATA="create"
        const val ANNOUNCEMENT_LIKE="like"
        const val CHAT_REQUEST_DATA="chat_request_data"
        const val FULL_DETAILS="full_details"
        const val DIRECT_CHAT="direct_chat"
        const val END_PONT="end_point"

        //API Parameter
        const val CHECK_TYPE="checking_type"
        const val MOBILE="mobile"
        const val BOD="bod"
        const val PUSH_TOKEN="push_token"
        const val DEVICE_TYPE="device_type"
        const val DEVICE_ID="device_id"
        const val OTP="otp"
        const val VALUE="value"
        const val OLD_PASS="old_password"
        const val NEW_PASS="new_password"
        const val CONF_NEW_PASS="conf_new_password"
        const val MESSAGE="message"
        const val YES="Yes"
        const val NO="No"
        const val LIMIT_PARAM="limit"
        const val OFFSET_PARAM="offset"
        const val ANNOUNCEMENT_ID="announcement_id"
        const val FRIEND_ID="friend_id"
        const val STATUS="status"
        const val USER_ID="user_id"
        const val GALLERY_ID="gallery_id"
        const val DISTANCE="distance"
        const val CONDITION="condition"
        const val CATEGORY="category"
        const val SEARCH="search"
        const val THREAD_ID="thread_id"
        const val RECEIVER_ID="receiver_id"
        const val MORE_REQ="more_request"
        const val COMMENT="comment"
        const val COMMUNICATION_RATE="communication_rating"
        const val PUNCTUAL_RATE="punctuality_rating"
        const val REASON="reason"
        const val OTHER_USER_ID="other_user_id"
        const val NOTIFICATION_C="notification_count"
        const val NOTIFICATION="notification"
        const val MSG_C="message_count"


        const val OBJECT_CATEGORY="object_category"
        const val OBJECT_DURATION="object_duration"
        const val FOOD_DURATION="food_duration"


        const val DEEPLINK_URL = "https://www.betteryouandfriends.com/"

        const val PRIVACY_URL="${BASE_URL}content/privacy_policy"
        const val ABOUT_US_URL="${BASE_URL}content/about_us"
        const val TERMS_URL="${BASE_URL}content/term_condition"
        const val PRIVACY="privacy"
        const val TERMS="terms"
        const val ABOUT="about"
        const val DONATION="donation"
        const val REQUEST="request"
        const val TYPE="type"
        const val OBJECT_TYPE="object_type"
        const val PENDING="pending"
        const val AVAILABLE="available"
        const val RESERVED="reserved"
        const val REJECTED="rejected"
        const val FINALIZED="finalized"
        const val ALL=""
        const val PUSH="push"
        const val SPLASH="splash"
        var IS_IN_CHAT=false

        //BroadCast Receiver action
        const val CHAT="chat"
         var CHAT_COUNT=0
         var NOTIFICATION_COUNT=0

        //Filter
        /* Object Donation Filter */
        var objectDonationStatus= ""
        var objectDonationCondition=""
        var objectDonationDistance=""
        var objectDonationDistanceAddress=""
        var objectDonationLatitude=0.0
        var objectDonationLongitude=0.0
        var objectDonationIsFilterApply=false

        /* Object Request Filter*/
        var objectRequestStatus= ""
        var objectRequestDistance=""
        var objectRequestDistanceAddress=""
        var objectRequestLatitude=0.0
        var objectRequestLongitude=0.0
        var objectRequestIsFilterApply=false

        /* Food Donation Filter */
        var foodDonationStatus= ""
        var foodDonationDistance=""
        var foodDonationDistanceAddress=""
        var foodDonationLatitude=0.0
        var foodDonationLongitude=0.0
        var foodDonationIsFilterApply=false

        /* Food Request Filter */
        var foodRequestStatus= ""
        var foodRequestDistance=""
        var foodRequestDistanceAddress=""
        var foodRequestLatitude=0.0
        var foodRequestLongitude=0.0
        var foodRequestIsFilterApply=false

        //Search
        /* Object Donation Filter */
        var searchObjectDonationStatus= ""
        var searchObjectDonationCondition=""
        var searchObjectDonationDistance=""
        var searchObjectDonationDistanceAddress=""
        var searchObjectDonationLatitude=0.0
        var searchObjectDonationLongitude=0.0
        var searchObjectDonationIsFilterApply=false

        /* Object Request Filter*/
        var searchObjectRequestStatus= ""
        var searchObjectRequestDistance=""
        var searchObjectRequestDistanceAddress=""
        var searchObjectRequestLatitude=0.0
        var searchObjectRequestLongitude=0.0
        var searchObjectRequestIsFilterApply=false

        /* Food Donation Filter */
        var searchFoodDonationStatus= ""
        var searchFoodDonationDistance=""
        var searchFoodDonationDistanceAddress=""
        var searchFoodDonationLatitude=0.0
        var searchFoodDonationLongitude=0.0
        var searchFoodDonationIsFilterApply=false

        /* Food Request Filter */
        var searchFoodRequestStatus= ""
        var searchFoodRequestDistance=""
        var searchFoodRequestDistanceAddress=""
        var searchFoodRequestLatitude=0.0
        var searchFoodRequestLongitude=0.0
        var searchFoodRequestIsFilterApply=false

        const val displayFormat="dd/MM/yyyy"
        const val apiFormat="yyyy-MM-dd"
        const val backEndUTCFormat="yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
        const val chatDisplayFormat="dd-MM-yyyy hh:mm a"
        var categoryList= mutableListOf<CategoryResponse.Category>()
        var conditionList= mutableListOf<CategoryResponse.Condition>()
        fun languageList():MutableList<LanguageData>{
            val languageList= mutableListOf<LanguageData>().apply {
                add(LanguageData("English","en",R.drawable.united_states,true))
                add(LanguageData("French","fr",R.drawable.france,false))
            }
            return languageList
        }

    }
    object Singleton {

        fun typeList(mContext: Context):MutableList<AnnouncementType>{
            val typeList= mutableListOf<AnnouncementType>().apply {
                add(AnnouncementType(mContext.getStr(R.string.`object`), OBJECT))
                add(AnnouncementType(mContext.getStr(R.string.food), FOOD))
            }
            return typeList
        }

        fun statusList(mContext: Context):MutableList<StatusType>
        {
            val statusList= mutableListOf<StatusType>().apply {
                add(StatusType(mContext.getStr(R.string.all), ALL))
                add(StatusType(mContext.getStr(R.string.available), AVAILABLE))
                add(StatusType(mContext.getStr(R.string.reserved), RESERVED))
                add(StatusType(mContext.getStr(R.string.finalized), FINALIZED))
                add(StatusType(mContext.getStr(R.string.pending), PENDING))
                add(StatusType(mContext.getStr(R.string.rejected), REJECTED))
            }
            return statusList
        }
        fun otherStatusList(mContext: Context):MutableList<StatusType>
        {
            val statusList= mutableListOf<StatusType>().apply {
                add(StatusType(mContext.getStr(R.string.available), AVAILABLE))
                add(StatusType(mContext.getStr(R.string.reserved), RESERVED))
                add(StatusType(mContext.getStr(R.string.finalized), FINALIZED))
            }
            return statusList
        }


        fun dispenseList(mContext: Context):MutableList<DispenseData>{
            val dispenseList= mutableListOf<DispenseData>().apply {
                add(DispenseData(mContext.getStr(R.string.available_announcement), AVAILABLE,false))
                add(DispenseData(mContext.getStr(R.string.reserved_announcement), RESERVED,false))
                add(DispenseData(mContext.getStr(R.string.all), ALL,true))
            }
            return dispenseList
        }



        fun reportAnnouncementList(mContext:Context):MutableList<ReportData>{
            val reportList= mutableListOf<ReportData>().apply {
                add(ReportData(mContext.getStr(R.string.its_spam),"It’s spam",false))
                add(ReportData(mContext.getStr(R.string.nudity_sexual),"Nudity or sexual activity",false))
                add(ReportData(mContext.getStr(R.string.do_not_like),"I just don’t like it",false))
                add(ReportData(mContext.getStr(R.string.scam_fraud),"Scam or fraud",false))
                add(ReportData(mContext.getStr(R.string.false_info),"False information",false))
                add(ReportData(mContext.getStr(R.string.hate_speech),"Hate speech or symbols",false))
            }
            return reportList
        }
    }

}