package com.compose.smarthome.app

import android.app.Application
import android.os.StrictMode
import com.compose.smarthome.BuildConfig
import com.compose.smarthome.di.appModule
import com.compose.smarthome.di.datastoreModule
import com.compose.smarthome.di.repositoryModule
import com.compose.smarthome.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SmartHomeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configKoin()
        setupStrictMode()
    }

    private fun configKoin() {
        startKoin {
            androidLogger()
            androidContext(this@SmartHomeApplication)
            modules(
                appModule,
                datastoreModule,
                repositoryModule,
                viewModelModule
            )
        }
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll()
                    .penaltyLog() // Log violations to logcat
                    .penaltyFlashScreen() // Flash the screen on violation
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}