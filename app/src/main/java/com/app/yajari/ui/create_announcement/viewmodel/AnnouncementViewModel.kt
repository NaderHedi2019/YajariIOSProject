package com.app.yajari.ui.create_announcement.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.yajari.data.AnnouncementDetailsResponse
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.CommonResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.NotificationResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.data.repository.AnnouncementRepository
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

class AnnouncementViewModel (private val announcementRepository: AnnouncementRepository, private val networkHelper: NetworkHelper):
    ViewModel() {
    private val _createAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val createAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _createAnnouncement

    private val _editAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val editAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _editAnnouncement


    private val _deletePhoto = SingleLiveEvent<Resource<CommonResponse>>()
    val deletePhotoResponse: LiveData<Resource<CommonResponse>> get() = _deletePhoto

    private val _category = SingleLiveEvent<Resource<CategoryResponse>>()
    val categoryResponse: LiveData<Resource<CategoryResponse>> get() = _category

    private val _objectDonation = SingleLiveEvent<Resource<AnnouncementResponse>>()
    val objectDonationResponse: LiveData<Resource<AnnouncementResponse>> get() = _objectDonation

    private val _objectRequest = SingleLiveEvent<Resource<AnnouncementResponse>>()
    val objectRequestResponse: LiveData<Resource<AnnouncementResponse>> get() = _objectRequest

    private val _foodRequest = SingleLiveEvent<Resource<AnnouncementResponse>>()
    val foodRequestResponse: LiveData<Resource<AnnouncementResponse>> get() = _foodRequest

    private val _foodDonation = SingleLiveEvent<Resource<AnnouncementResponse>>()
    val foodDonationResponse: LiveData<Resource<AnnouncementResponse>> get() = _foodDonation

    private val _favAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val favouriteAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _favAnnouncement

    private val _announcementDetails = SingleLiveEvent<Resource<AnnouncementDetailsResponse>>()
    val announcementDetailsResponse: LiveData<Resource<AnnouncementDetailsResponse>> get() = _announcementDetails

    private val _userFavourite = SingleLiveEvent<Resource<CommonResponse>>()
    val userFavouriteResponse: LiveData<Resource<CommonResponse>> get() = _userFavourite

    private val _sendRequest = SingleLiveEvent<Resource<CommonResponse>>()
    val sendRequestResponse: LiveData<Resource<CommonResponse>> get() = _sendRequest

    private val _notification = SingleLiveEvent<Resource<NotificationResponse>>()
    val notificationResponse: LiveData<Resource<NotificationResponse>> get() = _notification

    private val _logout = MutableLiveData<Boolean>()
    val logout: LiveData<Boolean>
        get() = _logout


    private val _userData = SingleLiveEvent<LoginResponse.Data>()
    val userDataResponse: LiveData<LoginResponse.Data> get() = _userData

    private val _deleteAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val deleteAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _deleteAnnouncement

    private val _reportAnnouncement = SingleLiveEvent<Resource<CommonResponse>>()
    val reportAnnouncementResponse: LiveData<Resource<CommonResponse>> get() = _reportAnnouncement

    fun getUserFromPref() {
        announcementRepository.getUserData().let { _userData.postValue(it) }
    }

    fun logoutUser() {
        try {
            announcementRepository.clearData()
            _logout.postValue(true)
        } catch (e: Exception) {
            _logout.postValue(false)
            e.printStackTrace()
        }
    }
    fun userFavourite(map: Map<String,String>)
    {
        viewModelScope.launch {
            _userFavourite.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.userFavourite(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _userFavourite.postValue(Resource.success(response.body()))
                                }
                                else -> _userFavourite.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _userFavourite.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _userFavourite.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _userFavourite.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _userFavourite.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _userFavourite.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _userFavourite.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _userFavourite.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun announcementDetails(map: Map<String,String>)
    {
        viewModelScope.launch {
            _announcementDetails.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.announcementDetails(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _announcementDetails.postValue(Resource.success(response.body()))
                                }
                                else -> _announcementDetails.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _announcementDetails.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _announcementDetails.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _announcementDetails.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _announcementDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _announcementDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _announcementDetails.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _announcementDetails.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun favouriteAnnouncement(map: Map<String,String>)
    {
        viewModelScope.launch {
            _favAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.favouriteAnnouncement(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _favAnnouncement.postValue(Resource.success(response.body()))
                                }
                                else -> _favAnnouncement.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _favAnnouncement.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _favAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _favAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _favAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _favAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }
    fun getFoodDonation(map: Map<String,String>)
    {
        viewModelScope.launch {
            _foodDonation.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.getFoodDonation(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _foodDonation.postValue(Resource.success(response.body()))
                                }
                                else -> _foodDonation.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _foodDonation.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _foodDonation.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _foodDonation.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _foodDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _foodDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _foodDonation.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _foodDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }
    fun getFoodRequest(map: Map<String,String>)
    {
        viewModelScope.launch {
            _foodRequest.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.getFoodRequest(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _foodRequest.postValue(Resource.success(response.body()))
                                }
                                else -> _foodRequest.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _foodRequest.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _foodRequest.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _foodRequest.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _foodRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _foodRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _foodRequest.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _foodRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun getObjectRequest(map: Map<String,String>)
    {
        viewModelScope.launch {
            _objectRequest.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.getObjectRequest(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _objectRequest.postValue(Resource.success(response.body()))
                                }
                                else -> _objectRequest.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _objectRequest.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _objectRequest.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _objectRequest.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _objectRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _objectRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _objectRequest.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _objectRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

        fun getObjectDonation(map: Map<String,String>)
        {
            viewModelScope.launch {
                _objectDonation.postValue(Resource.loading(null))
                if (networkHelper.isNetworkConnected()){
                    try {
                        announcementRepository.getObjectDonation(map).let { response ->
                            if (response.isSuccessful){
                                when(response.code()){
                                    Constant.SUCCESS_CODE -> {
                                        _objectDonation.postValue(Resource.success(response.body()))
                                    }
                                    else -> _objectDonation.postValue(response.body()?.message?.let {
                                            errorMessage -> Resource.error(errorMessage, null) })
                                }
                            }else{
                                val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                    CommonResponse::class.java).lenient().fromJson(it1) }
                                return@let errorResponse?.let { errorMessage ->
                                    val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                    when {
                                        response.code() == Constant.ERROR_CODE -> _objectDonation.postValue(
                                            Resource.error(errorMsg, null))
                                        response.code() == Constant.UNAUTHORIZED_TOKEN -> _objectDonation.postValue(Resource.unAuthorized(errorMsg, null))
                                        else -> _objectDonation.postValue(Resource.error(errorMsg, null))
                                    }
                                }
                            }
                        }
                    }catch (e: UnknownHostException) {
                        _objectDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                    } catch (e: SocketTimeoutException) {
                        _objectDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                    } catch (e: Exception) {
                        _objectDonation.postValue(Resource.error(e.message.toString(), null))
                    }
                }else{
                    _objectDonation.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                }
            }

        }




    fun deletePhoto(map: Map<String,String>)
    {
        viewModelScope.launch {
            _deletePhoto.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.deletePhoto(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _deletePhoto.postValue(Resource.success(response.body()))
                                }
                                else -> _deletePhoto.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _deletePhoto.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _deletePhoto.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _deletePhoto.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _deletePhoto.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _deletePhoto.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _deletePhoto.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _deletePhoto.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }



    fun createAnnouncement(params: Map<String, RequestBody>, image : MutableList<MultipartBody.Part>?){
        viewModelScope.launch {
            _createAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.createAnnouncement(params,image).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _createAnnouncement.postValue(Resource.success(response.body()))
                                }
                                else -> _createAnnouncement.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _createAnnouncement.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _createAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _createAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _createAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _createAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _createAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _createAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun editAnnouncement(params: Map<String, RequestBody>, image : MutableList<MultipartBody.Part>?){
        viewModelScope.launch {
            _editAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.editAnnouncement(params,image).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _editAnnouncement.postValue(Resource.success(response.body()))
                                }
                                else -> _editAnnouncement.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _editAnnouncement.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _editAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _editAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _editAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _editAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _editAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _editAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun getCategory()
    {
        viewModelScope.launch {
            _category.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.getCategory().let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _category.postValue(Resource.success(response.body()))
                                }
                                else -> _category.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _category.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _category.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _category.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _category.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _category.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _category.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _category.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }
    }

    fun sendRequest(map: Map<String,String>)
    {
        viewModelScope.launch {
            _sendRequest.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.sendRequest(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _sendRequest.postValue(Resource.success(response.body()))
                                }
                                else -> _sendRequest.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _sendRequest.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _sendRequest.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _sendRequest.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _sendRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _sendRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _sendRequest.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _sendRequest.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }
    fun deleteAnnouncement(map: Map<String,String>)
    {
        viewModelScope.launch {
            _deleteAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.deleteAnnouncement(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _deleteAnnouncement.postValue(Resource.success(response.body()))
                                }
                                else -> _deleteAnnouncement.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _deleteAnnouncement.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _deleteAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _deleteAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _deleteAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _deleteAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun notifications(map: Map<String,String>)
    {
        viewModelScope.launch {
            _notification.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.notifications(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _notification.postValue(Resource.success(response.body()))
                                }
                                else -> _notification.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _notification.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _notification.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _notification.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _notification.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _notification.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _notification.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _notification.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

    fun reportAnnouncement(map: Map<String,String>)
    {
        viewModelScope.launch {
            _reportAnnouncement.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()){
                try {
                    announcementRepository.reportAnnouncement(map).let { response ->
                        if (response.isSuccessful){
                            when(response.code()){
                                Constant.SUCCESS_CODE -> {
                                    _reportAnnouncement.postValue(Resource.success(response.body()))
                                }
                                else -> _reportAnnouncement.postValue(response.body()?.message?.let {
                                        errorMessage -> Resource.error(errorMessage, null) })
                            }
                        }else{
                            val errorResponse = response.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                CommonResponse::class.java).lenient().fromJson(it1) }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _reportAnnouncement.postValue(
                                        Resource.error(errorMsg, null))
                                    response.code() == Constant.UNAUTHORIZED_TOKEN -> _reportAnnouncement.postValue(Resource.unAuthorized(errorMsg, null))
                                    else -> _reportAnnouncement.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                }catch (e: UnknownHostException) {
                    _reportAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _reportAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _reportAnnouncement.postValue(Resource.error(e.message.toString(), null))
                }
            }else{
                _reportAnnouncement.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }
        }

    }

}