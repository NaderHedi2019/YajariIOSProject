package com.app.yajari.ui.signup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.yajari.data.CommonResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.data.repository.AuthRepository
import com.app.yajari.utils.Constant
import com.app.yajari.utils.NetworkHelper
import com.app.yajari.utils.Resource
import com.app.yajari.utils.SingleLiveEvent
import com.app.yajari.utils.ifNotNullOrElse
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel(private val authRepository: AuthRepository, private val networkHelper: NetworkHelper) : ViewModel() {
    private val _signup = SingleLiveEvent<Resource<LoginResponse>>()
    val signUpResponse: LiveData<Resource<LoginResponse>> get() = _signup

    private val _sendOTP = SingleLiveEvent<Resource<CommonResponse>>()
    val sendOTPResponse: LiveData<Resource<CommonResponse>> get() = _sendOTP


    private val _emailMobileExists = SingleLiveEvent<Resource<CommonResponse>>()
    val emailMobileExistsResponse: LiveData<Resource<CommonResponse>> get() = _emailMobileExists

    private val _forgotPassword = SingleLiveEvent<Resource<CommonResponse>>()
    val forgotPasswordResponse: LiveData<Resource<CommonResponse>> get() = _forgotPassword

    private val _login = SingleLiveEvent<Resource<LoginResponse>>()
    val loginResponse: LiveData<Resource<LoginResponse>> get() = _login
    private val _userData = SingleLiveEvent<LoginResponse.Data>()
    val userDataResponse: LiveData<LoginResponse.Data> get() = _userData
    fun setGuest(isGuest: Boolean) {
        authRepository.setGuest(isGuest)
    }

    fun getUserFromPref() {
        authRepository.getUser().let { _userData.postValue(it) }
    }

    fun login(map: Map<String, String>) {
        viewModelScope.launch {
            _login.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    authRepository.login(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    response.body()?.data.let { user ->
                                        val userJson = moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(user)
                                        authRepository.setIsLoggedIn(true)
                                        authRepository.setUserPref(userJson)
                                        authRepository.setLanguage(user?.language.toString())
                                        authRepository.setToken(user!!.token.toString())
                                    }
                                    _login.postValue(Resource.success(response.body()))
                                }

                                else -> _login.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ _it -> _it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _login.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    else -> _login.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _login.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _login.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _login.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _login.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }

        }
    }

    fun signup(map: Map<String, String>) {
        viewModelScope.launch {
            _signup.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    authRepository.signup(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    response.body()?.data.let { user ->
                                        val userJson = moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(user)
                                        authRepository.setIsLoggedIn(true)
                                        authRepository.setUserPref(userJson)
                                        authRepository.setToken(user!!.token.toString())
                                    }
                                    _signup.postValue(Resource.success(response.body()))
                                }

                                else -> _signup.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ _it -> _it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _signup.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    else -> _signup.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _signup.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _signup.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _signup.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _signup.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }

        }
    }

    fun sendOTP(map: Map<String, String>) {
        viewModelScope.launch {
            _sendOTP.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    authRepository.sendOTP(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _sendOTP.postValue(Resource.success(response.body()))
                                }

                                else -> _sendOTP.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ _it -> _it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _sendOTP.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    else -> _sendOTP.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _sendOTP.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _sendOTP.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _sendOTP.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _sendOTP.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }

        }
    }

    fun checkMobile(map: Map<String, String>) {
        viewModelScope.launch {
            _emailMobileExists.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    authRepository.checkAvailability(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _emailMobileExists.postValue(Resource.success(response.body()))
                                }

                                else -> _emailMobileExists.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ _it -> _it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _emailMobileExists.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    else -> _emailMobileExists.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _emailMobileExists.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _emailMobileExists.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _emailMobileExists.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _emailMobileExists.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }

        }
    }

    fun forgotPassword(map: Map<String, String>) {
        viewModelScope.launch {
            _forgotPassword.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    authRepository.forgotPassword(map).let { response ->
                        if (response.isSuccessful) {
                            when (response.code()) {
                                Constant.SUCCESS_CODE -> {
                                    _forgotPassword.postValue(Resource.success(response.body()))
                                }

                                else -> _forgotPassword.postValue(response.body()?.message?.let { errorMessage -> Resource.error(errorMessage, null) })
                            }
                        } else {
                            val errorResponse = response.errorBody()?.source()?.let { it1 ->
                                moshiBuilder.adapter(
                                    CommonResponse::class.java
                                ).lenient().fromJson(it1)
                            }
                            return@let errorResponse?.let { errorMessage ->
                                val errorMsg = errorMessage.message.ifNotNullOrElse({ _it -> _it }, { "" })
                                when {
                                    response.code() == Constant.ERROR_CODE -> _forgotPassword.postValue(
                                        Resource.error(errorMsg, null)
                                    )

                                    else -> _forgotPassword.postValue(Resource.error(errorMsg, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _forgotPassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: SocketTimeoutException) {
                    _forgotPassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
                } catch (e: Exception) {
                    _forgotPassword.postValue(Resource.error(e.message.toString(), null))
                }
            } else {
                _forgotPassword.postValue(Resource.networkError(Constant.INTERNET_ERROR_COMMON, null))
            }

        }
    }

}