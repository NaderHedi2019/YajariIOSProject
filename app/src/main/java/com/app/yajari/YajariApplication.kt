package com.app.yajari

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.app.yajari.data.di.appModule
import com.app.yajari.data.di.repoModule
import com.app.yajari.data.di.viewModelModule
import com.app.yajari.utils.Constant
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import java.util.Locale

class YajariApplication :  Application() {

    companion object {
        lateinit var instance: YajariApplication
    }

    override fun onCreate() {
        super.onCreate()
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        GlobalContext.startKoin {
            androidContext(this@YajariApplication)
            modules(listOf(appModule, viewModelModule, repoModule))
        }

        AppLocale.supportedLocales = Constant.languageList().map { Locale(it.countryCode, it.language) }
        AppLocale.appLocaleRepository = SharedPrefsAppLocaleRepository(this)
        ViewPump.init(RewordInterceptor)

        instance = this@YajariApplication


    }
}
