package com.app.yajari.data.di
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.ui.splash.viewmodel.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { SplashViewModel(get(), get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { AnnouncementViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get()) }

}