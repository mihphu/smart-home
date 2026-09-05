package com.compose.smarthome.di

import com.compose.smarthome.data.local.datastore.LightConfig
import com.compose.smarthome.data.local.datastore.LightConfigImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single<LightConfig> { LightConfigImpl(androidContext()) }
}
