package com.compose.smarthome.di

import com.compose.smarthome.app.SmartHomeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single<SmartHomeApplication> { androidApplication() as SmartHomeApplication }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
