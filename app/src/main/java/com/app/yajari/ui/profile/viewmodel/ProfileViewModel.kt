package com.app.yajari.ui.profile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.CommonResponse
import com.app.yajari.data.CommunityImpactResponse
import com.app.yajari.data.FaqResponse
import com.app.yajari.data.FavouriteUserResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.data.OtherUserResponse
import com.app.yajari.data.PartnerResponse
import com.app.yajari.data.RateReviewResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.data.repository.ProfileRepository
import com.app.yajari.utils.Constant
import com.app.yajari.utils.NetworkHelper
import com.app.yajari.utils.Resource
import com.app.yajari.utils.SingleLiveEvent
import com.app.yajari.utils.ifNotNullOrElse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProfileViewModel(private val profileRepository: ProfileRepository, private val networkHelper: NetworkHelper) :
    ViewModel() {
    private val _logout = MutableLiveData<Boolean>()
    val logout: LiveData<Boolean>
        get() = _logout

    private val _logoutResponse = SingleLiveEvent<Resource<CommonResponse>>()
    val logoutResponse: LiveData<Resource<CommonResponse>> get() = _logoutResponse


    private val _changePassword = SingleLiveEvent<Resource<CommonResponse>>()
    val changePasswordResponse: LiveData<Resource<CommonResponse>> get() = _changePassword

    private val _helpSupport = SingleLiveEvent<Resource<CommonResponse>>()
    val helpSupportResponse: LiveData<Resource<CommonResponse>> get() = _helpSupport

    private val _deleteAccount = SingleLiveEvent<Resource<CommonResponse>>()
    val deleteAccountResponse: LiveData<Resource<CommonResponse>> get() = _deleteAccount

    private val _faq = SingleLiveEvent<Resource<FaqResponse>>()
    val faqResponse: LiveData<Resource<FaqResponse>> get() = _faq

    private val _getProfile = SingleLiveEvent<Resource<LoginResponse>>()
    val profileResponse: LiveData<Resource<LoginResponse>> get() = _getProfile

    private val _userData = SingleLiveEvent<LoginResponse.Data>()
    val userDataResponse: LiveData<LoginResponse.Data> get() = _userData


    private val _editProfile = SingleLiveEvent<Resource<LoginResponse>>()
    val editProfileResponse: LiveData<Resource<LoginResponse>> get() = _editProfile

    private val _partners = SingleLiveEvent<Resource<PartnerResponse>>()
    val partnerResponse: LiveData<Resource<PartnerResponse>> get() = _partners

    private val _myAnnouncement = SingleLiveEvent<Resource<MyAnnouncementResponse>>()
    val myAnnouncementResponse: LiveData<Resource<MyAnnouncementResponse>> get() = _myAnnouncement

    private val _favAnnouncementList = SingleLiveEvent<Resource<AnnouncementResponse>>()
    val favAnnouncementListResponse: LiveData<Resource<AnnouncementResponse>> get() = _favAnnouncementList

    private val _favUser = SingleLiveEvent<Resource<FavouriteUserResponse>>()
    val favUserResponse: LiveData<Resource<FavouriteUserResponse>> get() = _favUser

    private val _unFavUser = SingleLiveEvent<Resource<CommonResponse>>()
    val favResponse: LiveData<Resource<CommonResponse>> get() = _unFavUser


    private val _deleteAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val deleteAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _deleteAnnouncement

    private val _favAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val favouriteAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _favAnnouncement

    private val _otherUserProfile = SingleLiveEvent<Resource<OtherUserResponse>>()
    val otherUserProfileResponse: LiveData<Resource<OtherUserResponse>> get() = _otherUserProfile

    private val _changeLanguage = SingleLiveEvent<Resource<CommonResponse>>()
    val changeLanguageResponse: LiveData<Resource<CommonResponse>> get() = _changeLanguage

    private val _reportUser = SingleLiveEvent<Resource<CommonResponse>>()
    val reportUserResponse: LiveData<Resource<CommonResponse>> get() = _reportUser

    private val _rateReview = SingleLiveEvent<Resource<RateReviewResponse>>()
    val rateReviewResponse: LiveData<Resource<RateReviewResponse>> get() = _rateReview

    private val _communityImpact = SingleLiveEvent<Resource<CommunityImpactResponse>>()
    val communityImpactResponse: LiveData<Resource<CommunityImpactResponse>> get() = _communityImpact


    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language
    fun logoutUser() {
        try {
            profileRepository.clearData()
            _logout.postValue(true)
        } catch (e: Exception) {
            _logout.postValue(false)
            e.printStackTrace()
        }
    }

    fun getUserFromPref() {
        profileRepository.getUserData().let { _userData.postValue(it) }
    }

    fun setLanguage(lang: String) {
        profileRepository.setLanguage(lang)
    }

    fun getLanguage() {
        _language.postValue(profileRepository.getLanguage())
    }

    fun logout() {
        viewModelScope.launch {
            _logoutResponse.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.logout().let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    val userJson = moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(LoginResponse.Data())
                                    profileRepository.setUserToPref(userJson)
                                    _logoutResponse.postValue(Resource.success(response.body()))
                                }

                                else -> _logoutResponse.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _logoutResponse.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _logoutResponse.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _logoutResponse.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _logoutResponse.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _logoutResponse.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _logoutResponse.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _logoutResponse.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun deleteAccount(map: Map<String, String>) {
        viewModelScope.launch {
            _deleteAccount.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.deleteAccount(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _deleteAccount.postValue(Resource.success(response.body()))
                                }

                                else -> _deleteAccount.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _deleteAccount.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _deleteAccount.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _deleteAccount.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _deleteAccount.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _deleteAccount.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _deleteAccount.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _deleteAccount.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun changePassword(map: Map<String, String>) {
        viewModelScope.launch {
            _changePassword.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.changePassword(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _changePassword.postValue(Resource.success(response.body()))
                                }

                                else -> _changePassword.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _changePassword.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _changePassword.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _changePassword.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _changePassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _changePassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _changePassword.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _changePassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun helpSupport(map: Map<String, String>) {
        viewModelScope.launch {
            _helpSupport.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.support(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _helpSupport.postValue(Resource.success(response.body()))
                                }

                                else -> _helpSupport.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _helpSupport.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _helpSupport.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _helpSupport.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _helpSupport.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _helpSupport.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _helpSupport.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _helpSupport.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun faq() {
        viewModelScope.launch {
            _faq.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.faq().let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _faq.postValue(Resource.success(response.body()))
                                }

                                else -> _faq.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _faq.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _faq.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _faq.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _faq.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _faq.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _faq.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _faq.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun getProfile() {
        viewModelScope.launch {
            _getProfile.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.getProfile().let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    response.body()?.data.let { user ->
                                        val userJson = moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(user)
                                        profileRepository.setUserToPref(userJson)
                                        profileRepository.setLanguage(user?.language.toString())
                                    }
                                    _getProfile.postValue(Resource.success(response.body()))
                                }

                                else -> _getProfile.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _getProfile.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _getProfile.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _getProfile.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _getProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _getProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _getProfile.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _getProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun editProfile(params: Map<String, RequestBody>, image: MultipartBody.Part?) {
        viewModelScope.launch {
            _editProfile.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.editProfile(params, image).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    response.body()?.data.let { user ->
                                        val userJson = moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(user)
                                        profileRepository.setUserToPref(userJson)
                                    }
                                    _editProfile.postValue(Resource.success(response.body()))
                                }

                                else -> _editProfile.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _editProfile.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _editProfile.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _editProfile.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _editProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _editProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _editProfile.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _editProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun getPartners() {
        viewModelScope.launch {
            _partners.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.getPartners().let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _partners.postValue(Resource.success(response.body()))
                                }

                                else -> _partners.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _partners.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _partners.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _partners.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _partners.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _partners.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _partners.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _partners.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun myAnnouncement(map: Map<String, String>) {
        viewModelScope.launch {
            _myAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.myAnnouncement(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _myAnnouncement.postValue(Resource.success(response.body()))
                                }

                                else -> _myAnnouncement.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _myAnnouncement.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _myAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _myAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _myAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _myAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _myAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _myAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun deleteAnnouncement(map: Map<String, String>) {
        viewModelScope.launch {
            _deleteAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.deleteAnnouncement(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _deleteAnnouncement.postValue(Resource.success(response.body()))
                                }

                                else -> _deleteAnnouncement.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _deleteAnnouncement.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _deleteAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _deleteAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _deleteAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun favAnnouncementList(map: Map<String, String>) {
        viewModelScope.launch {
            _favAnnouncementList.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.favouriteAnnouncementList(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _favAnnouncementList.postValue(Resource.success(response.body()))
                                }

                                else -> _favAnnouncementList.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _favAnnouncementList.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _favAnnouncementList.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _favAnnouncementList.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _favAnnouncementList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _favAnnouncementList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _favAnnouncementList.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _favAnnouncementList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun favouriteAnnouncement(map: Map<String, String>) {
        viewModelScope.launch {
            _favAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.favouriteAnnouncement(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _favAnnouncement.postValue(Resource.success(response.body()))
                                }

                                else -> _favAnnouncement.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _favAnnouncement.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _favAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _favAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _favAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun favouriteUser(map: Map<String, String>) {
        viewModelScope.launch {
            _favUser.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.favouriteUser(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _favUser.postValue(Resource.success(response.body()))
                                }

                                else -> _favUser.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _favUser.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _favUser.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _favUser.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _favUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _favUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _favUser.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _favUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun unFavouriteUser(map: Map<String, String>) {
        viewModelScope.launch {
            _unFavUser.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.unFavouriteUser(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _unFavUser.postValue(Resource.success(response.body()))
                                }

                                else -> _unFavUser.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _unFavUser.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _unFavUser.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _unFavUser.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _unFavUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _unFavUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _unFavUser.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _unFavUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun otherUserProfile(map: Map<String, String>) {
        viewModelScope.launch {
            _otherUserProfile.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.otherUserProfile(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _otherUserProfile.postValue(Resource.success(response.body()))
                                }

                                else -> _otherUserProfile.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _otherUserProfile.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _otherUserProfile.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _otherUserProfile.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _otherUserProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _otherUserProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _otherUserProfile.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _otherUserProfile.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun changeLanguage(map: Map<String, String>) {
        viewModelScope.launch {
            _changeLanguage.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.changeLanguage(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _changeLanguage.postValue(Resource.success(response.body()))
                                }

                                else -> _changeLanguage.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _changeLanguage.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _changeLanguage.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _changeLanguage.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _changeLanguage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _changeLanguage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _changeLanguage.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _changeLanguage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun reportUser(map: Map<String, String>) {
        viewModelScope.launch {
            _reportUser.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.reportUser(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _reportUser.postValue(Resource.success(response.body()))
                                }

                                else -> _reportUser.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _reportUser.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _reportUser.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _reportUser.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _reportUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _reportUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _reportUser.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _reportUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun rateReview(map: Map<String, String>) {
        viewModelScope.launch {
            _rateReview.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.rateReview(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _rateReview.postValue(Resource.success(response.body()))
                                }

                                else -> _rateReview.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _rateReview.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _rateReview.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _rateReview.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _rateReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _rateReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _rateReview.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _rateReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun communityImpact() {
        viewModelScope.launch {
            _communityImpact.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.communityImpact().let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _communityImpact.postValue(Resource.success(response.body()))
                                }

                                else -> _communityImpact.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _communityImpact.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _communityImpact.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _communityImpact.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _communityImpact.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _communityImpact.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _communityImpact.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _communityImpact.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }


    private val _alertSetting = SingleLiveEvent<Resource<CommonResponse>>()
    val alertSettingResponse: LiveData<Resource<CommonResponse>> get() = _alertSetting
    fun alertSetting(map: Map<String, String>) {
        viewModelScope.launch {
            _alertSetting.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    profileRepository.alertSetting(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _alertSetting.postValue(Resource.success(response.body()))
                                }

                                else -> _alertSetting.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _alertSetting.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _alertSetting.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _alertSetting.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _alertSetting.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _alertSetting.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _alertSetting.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _alertSetting.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }


}