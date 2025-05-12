package com.app.yajari.data.di

import com.app.yajari.data.repository.AnnouncementRepository
import com.app.yajari.data.repository.AuthRepository
import com.app.yajari.data.repository.ChatRepository
import com.app.yajari.data.repository.ProfileRepository
import com.app.yajari.data.repository.SplashRepository
import org.koin.dsl.module

val repoModule = module {
    single { SplashRepository(get(), get()) }
    single { AuthRepository(get(), get()) }
    single { ProfileRepository(get(), get()) }
    single { AnnouncementRepository(get(),get()) }
    single { ChatRepository(get(),get()) }
}
