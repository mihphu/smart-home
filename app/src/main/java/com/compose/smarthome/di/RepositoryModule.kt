package com.compose.smarthome.di

import com.compose.smarthome.data.repository.LightRepositoryImpl
import com.compose.smarthome.domain.repository.LightRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<LightRepository> { LightRepositoryImpl(get()) }
}
