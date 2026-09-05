package com.compose.smarthome.di

import com.compose.smarthome.presentation.lightscreen.LightScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LightScreenViewModel(get(), get()) }
}
