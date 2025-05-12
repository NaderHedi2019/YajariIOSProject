package com.app.yajari.ui.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.yajari.data.ChatDetailResponse
import com.app.yajari.data.ChatListResponse
import com.app.yajari.data.CommonResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.UserChatListResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.data.repository.ChatRepository
import com.app.yajari.utils.Constant
import com.app.yajari.utils.NetworkHelper
import com.app.yajari.utils.Resource
import com.app.yajari.utils.SingleLiveEvent
import com.app.yajari.utils.ifNotNullOrElse
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {
    private val _logout = MutableLiveData<Boolean>()
    val logout: LiveData<Boolean>
        get() = _logout

    private val _userData = SingleLiveEvent<LoginResponse.Data>()
    val userDataResponse: LiveData<LoginResponse.Data> get() = _userData

    private val _requestChatList = SingleLiveEvent<Resource<ChatListResponse>>()
    val chatRequestListResponse: LiveData<Resource<ChatListResponse>> get() = _requestChatList

    private val _donationChatList = SingleLiveEvent<Resource<ChatListResponse>>()
    val chatDonationListResponse: LiveData<Resource<ChatListResponse>> get() = _donationChatList

    private val _chatDetails = SingleLiveEvent<Resource<ChatDetailResponse>>()
    val chatDetailsResponse: LiveData<Resource<ChatDetailResponse>> get() = _chatDetails

    private val _sendMessage = SingleLiveEvent<Resource<CommonResponse>>()
    val sendMessageResponse: LiveData<Resource<CommonResponse>> get() = _sendMessage

    private val _changeStatus = SingleLiveEvent<Resource<CommonResponse>>()
    val changeStatusResponse: LiveData<Resource<CommonResponse>> get() = _changeStatus


    private val _chatUser = SingleLiveEvent<Resource<UserChatListResponse>>()
    val chatUserResponse: LiveData<Resource<UserChatListResponse>> get() = _chatUser

    private val _submitReview = SingleLiveEvent<Resource<CommonResponse>>()
    val submitRateReviewResponse: LiveData<Resource<CommonResponse>> get() = _submitReview

    fun getUserFromPref() {
        chatRepository.getUserData().let { _userData.postValue(it) }
    }
    fun logoutUser() {
        try {
            chatRepository.clearData()
            _logout.postValue(true)
        } catch (e: Exception) {
            _logout.postValue(false)
            e.printStackTrace()
        }
    }
    fun requestChatList(map: Map<String,String>)
    {
        viewModelScope.launch {
            _requestChatList.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.requestChatList(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _requestChatList.postValue(Resource.success(response.body()))
                                }
                                else -> _requestChatList.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _requestChatList.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _requestChatList.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _requestChatList.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _requestChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _requestChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _requestChatList.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _requestChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun donationChatList(map: Map<String,String>)
    {
        viewModelScope.launch {
            _donationChatList.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.donationChatList(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _donationChatList.postValue(Resource.success(response.body()))
                                }
                                else -> _donationChatList.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _donationChatList.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _donationChatList.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _donationChatList.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _donationChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _donationChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _donationChatList.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _donationChatList.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun chatDetails(map: Map<String,String>)
    {
        viewModelScope.launch {
            _chatDetails.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.chatDetails(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _chatDetails.postValue(Resource.success(response.body()))
                                }
                                else -> _chatDetails.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _chatDetails.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _chatDetails.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _chatDetails.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _chatDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _chatDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _chatDetails.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _chatDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun sendMessage(map: Map<String,String>)
    {
        viewModelScope.launch {
            _sendMessage.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.sendMessage(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _sendMessage.postValue(Resource.success(response.body()))
                                }
                                else -> _sendMessage.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _sendMessage.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _sendMessage.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _sendMessage.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _sendMessage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _sendMessage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _sendMessage.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _sendMessage.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun userChatList(map: Map<String,String>)
    {
        viewModelScope.launch {
            _chatUser.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.chatUserList(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _chatUser.postValue(Resource.success(response.body()))
                                }
                                else -> _chatUser.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _chatUser.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _chatUser.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _chatUser.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _chatUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _chatUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _chatUser.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _chatUser.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun changeStatus(map: Map<String,String>)
    {
        viewModelScope.launch {
            _changeStatus.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.changeStatus(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _changeStatus.postValue(Resource.success(response.body()))
                                }
                                else -> _changeStatus.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _changeStatus.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _changeStatus.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _changeStatus.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _changeStatus.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _changeStatus.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _changeStatus.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _changeStatus.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun submitRateReview(map: Map<String,String>,endPoint:String)
    {
        viewModelScope.launch {
            _submitReview.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    chatRepository.submitRateReview(map,endPoint).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _submitReview.postValue(Resource.success(response.body()))
                                }
                                else -> _submitReview.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _submitReview.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _submitReview.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _submitReview.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _submitReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _submitReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _submitReview.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _submitReview.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }
}