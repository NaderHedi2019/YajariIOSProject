package com.app.yajari.data.pref
import android.content.Context
import android.content.SharedPreferences
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.di.moshiBuilder
import com.app.yajari.utils.Constant.Companion.APP_SHARED_PREFERENCE_NAME


class PreferencesHelperImpl constructor(context: Context) : PreferencesHelper {

    private var mPrefs: SharedPreferences =
        context.getSharedPreferences(APP_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

    companion object {
        const val LOGIN = "is_logged_in"
        const val TOKEN = "token"
        const val USER = "user"
        const val LANGUAGE = "language"
        const val GUEST_USER = "guest_user"
    }

    override fun isLoggedIn(): Boolean {
        return mPrefs.getBoolean(LOGIN, false)
    }


    override fun setIsLoggedIn(isLogin:Boolean) {
        mPrefs.edit().putBoolean(LOGIN, isLogin).apply()
    }

    override fun setToken(token: String) {
        mPrefs.edit().putString(TOKEN, token).apply()
    }


    override fun getToken(): String {
        return mPrefs.getString(TOKEN, "").toString()
    }


    override fun setUser(user: String) {
        mPrefs.edit().putString(USER, user).apply()
    }

    override fun getUser(): LoginResponse.Data {
        val userJson = mPrefs.getString(
            USER, moshiBuilder.adapter(LoginResponse.Data::class.java).toJson(
            LoginResponse.Data()))
        return userJson?.let { moshiBuilder.adapter(LoginResponse.Data::class.java).fromJson(it) }!!
    }

    override fun setGuestUser(isGuest: Boolean) {
        mPrefs.edit().putBoolean(GUEST_USER, isGuest).apply()
    }

    override fun getGuestUser(): Boolean {
        return mPrefs.getBoolean(GUEST_USER, false)
    }


    override fun clear() {
        mPrefs.edit().remove(LOGIN).apply()
        mPrefs.edit().remove(USER).apply()
        mPrefs.edit().remove(TOKEN).apply()
    }

    override fun setLanguage(language: String) {
        mPrefs.edit().putString(LANGUAGE, language).apply()
    }

    override fun getLanguage(): String {
        return mPrefs.getString(LANGUAGE, "").toString()
    }

}