package com.app.yajari.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(platformModule: Module) = startKoin {
    modules(sharedModule, platformModule)
} 