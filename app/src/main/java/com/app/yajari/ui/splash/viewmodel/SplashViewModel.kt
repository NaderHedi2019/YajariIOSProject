package com.app.yajari.ui.splash.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.yajari.data.VersionResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.data.repository.SplashRepository
import com.app.yajari.utils.Constant.Companion.COMMON_ERROR
import com.app.yajari.utils.Constant.Companion.ERROR_CODE
import com.app.yajari.utils.Constant.Companion.INTERNET_ERROR
import com.app.yajari.utils.Constant.Companion.SUCCESS_CODE
import com.app.yajari.utils.Constant.Companion.UNAUTHORIZED_TOKEN
import com.app.yajari.utils.NetworkHelper
import com.app.yajari.utils.Resource
import com.app.yajari.utils.Resource.Companion.loading
import com.app.yajari.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SplashViewModel (
    private val splashRepository: SplashRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn


    private val _isVersionCheck = SingleLiveEvent<Resource<VersionResponse>>()
    val versionCheckResponse: LiveData<Resource<VersionResponse>> get() = _isVersionCheck


    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language


    init {
        decideRoute()
    }

    fun setLanguage(lang: String) {
        splashRepository.setLanguage(lang)
    }
    fun getLanguage(){
        _language.postValue(splashRepository.getLanguage())
    }
    fun getSelectedLanguage():String{
        return splashRepository.getLanguage()
    }

    fun getGuestUser():Boolean{
        return splashRepository.getGuest()
    }

    private fun decideRoute() {
        val isLoggedIn = splashRepository.getIsLoggedIn()
        if (isLoggedIn) {
            _isLoggedIn.postValue(true)
        } else {
            _isLoggedIn.postValue(false)
        }
    }
    fun versionCheck(params: Map<String, String>) {
        viewModelScope.launch {
            _isVersionCheck.postValue(loading(null))
            if (networkHelper.isNetworkConnected()) {
                try {
                    splashRepository.versionCheck(params).let {
                        if (it.isSuccessful) {
                            when (it.body()?.status) {
                                SUCCESS_CODE -> {
                                    _isVersionCheck.postValue(Resource.success(it.body()))
                                }
                                else -> {
                                    _isVersionCheck.postValue(it.body()?.message?.let { errorMessage ->
                                        Resource.error(
                                            errorMessage,
                                            null
                                        )
                                    })
                                }
                            }
                        } else {
                            val errorResponse = it.errorBody()?.source()?.let { it1 -> moshiBuilder.adapter(
                                VersionResponse::class.java).lenient().fromJson(it1) }
                            when {
                                it.code() == UNAUTHORIZED_TOKEN -> {
                                    _isVersionCheck.postValue(
                                        Resource.unAuthorized(
                                            it.message(),
                                            null
                                        )
                                    )
                                }
                                it.code() == ERROR_CODE -> {
                                    _isVersionCheck.postValue(errorResponse?.message?.let { errorMessage ->
                                        Resource.error(
                                            errorMessage,
                                            errorResponse
                                        )
                                    })
                                }
                                else -> {
                                    _isVersionCheck.postValue(Resource.error(COMMON_ERROR, null))
                                }
                            }
                        }
                    }
                } catch (e: UnknownHostException) {
                    _isVersionCheck.postValue(Resource.error(e.message.toString(), null))
                } catch (e: SocketTimeoutException) {
                    _isVersionCheck.postValue(Resource.error(e.message.toString(), null))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isVersionCheck.postValue(Resource.error(COMMON_ERROR, null))
                }
            } else {
                _isVersionCheck.postValue(Resource.networkError(INTERNET_ERROR, null))
            }
        }
    }


}