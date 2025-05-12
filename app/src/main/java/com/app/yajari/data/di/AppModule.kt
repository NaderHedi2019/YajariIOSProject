package com.app.yajari.data.di

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import android.content.Context
import com.app.yajari.utils.Constant.Companion.BASE_URL
import com.app.yajari.utils.NetworkHelper
import com.app.yajari.data.api.ApiService
import com.app.yajari.data.pref.PreferencesHelper
import com.app.yajari.data.pref.PreferencesHelperImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()


val appModule  = module {
    single<PreferencesHelper> { providePrefHelper(androidContext()) }
    single { provideOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }
    single { provideNetworkHelper(androidContext()) }
}


private fun providePrefHelper(context: Context) = PreferencesHelperImpl(context)
private fun provideOkHttpClient() = httpRequest()
private fun provideRetrofit(okHttpClient: OkHttpClient) =
    Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshiBuilder))
        .baseUrl(BASE_URL)
        .client(okHttpClient).build()

private fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
private fun provideNetworkHelper(context: Context) = NetworkHelper(context)

fun httpRequest(): OkHttpClient {
    val intervalTimeOut = 200L
    val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    return OkHttpClient.Builder().connectTimeout(intervalTimeOut, TimeUnit.SECONDS).readTimeout(intervalTimeOut, TimeUnit.SECONDS).writeTimeout(intervalTimeOut, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor).addInterceptor(headerInterceptor()).build()
}

private fun headerInterceptor() = Interceptor { chain ->
    val host = setHost(BASE_URL)
    val request = chain.request()
    val newUrl = request.url.newBuilder().host(host).build()
    val newRequest: Request = chain.request().newBuilder().url(newUrl).addHeader("Accept", "application/json").build()
    chain.proceed(newRequest)
}

fun setHost(host: String?): String {
    return host!!.toHttpUrlOrNull()!!.host
}
