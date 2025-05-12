package com.app.yajari.shared.di

import com.app.yajari.shared.data.repository.UserRepositoryImpl
import com.app.yajari.shared.data.repository.CategoryRepositoryImpl
import com.app.yajari.shared.domain.repository.UserRepository
import com.app.yajari.shared.domain.repository.CategoryRepository
import com.app.yajari.shared.network.ApiServiceFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule: Module = module {
    single { ApiServiceFactory().createApiService() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
} 