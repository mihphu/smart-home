# DI with Koin (preferred)

Koin is this project's preferred DI (see `docs/ARCHITECTURE.md`). All DataStore wiring lives in `di/DataStoreModule.kt`; the repository binding goes into `di/RepositoryModule.kt`.

## Rules

- The `Context` needed by `AppConfigImpl` is resolved from Koin's `androidContext()` — never pass `Application` manually.
- The `DataStore` instance is **not** registered as a separate Koin binding — it is an internal implementation detail of `AppConfigImpl` (created via the `preferencesDataStore` delegate). Koin only registers `AppConfig` (the interface).
- If two preference groups share no keys, each gets its own `*Impl` (and its own DataStore delegate) and its own Koin binding — **never one shared DataStore for unrelated groups**.
- Repositories inject the **interface**, not the implementation: `single<AppConfig> { AppConfigImpl(androidContext()) }`.

## Template

```kotlin
// di/DataStoreModule.kt
package <root>.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import <root>.data.local.prefs.AppConfig
import <root>.data.local.prefs.AppConfigImpl

val dataStoreModule = module {

    // AppConfig — prefs accessor (DataStore is internal to AppConfigImpl)
    single<AppConfig> { AppConfigImpl(androidContext()) }

    // Add more preference groups here as the app grows:
    // single<UserPrefs> { UserPrefsImpl(androidContext()) }
}
```

```kotlin
// di/RepositoryModule.kt  (add the DataStore-backed repository binding here)
package <root>.di

import org.koin.dsl.module
import <root>.data.repository.SettingsRepositoryImpl
import <root>.domain.repository.SettingsRepository

val repositoryModule = module {

    // SettingsRepository — backed by AppConfig (DataStore)
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Other repositories …
}
```

```kotlin
// di/AppModule.kt  (register the ViewModel)
package <root>.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import <root>.ui.settings.SettingsViewModel

val appModule = module {

    viewModel { SettingsViewModel(get()) }
}
```

### Registering all modules in the Application class

```kotlin
// app/MyApplication.kt
package <root>.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import <root>.di.appModule
import <root>.di.dataStoreModule
import <root>.di.repositoryModule

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule,
                dataStoreModule,
                repositoryModule,
                // networkModule, databaseModule, …
            )
        }
    }
}
```

## Adding a second preference group — checklist

1. Create `data/local/datastore/UserPrefsKeys.kt` with the new key constants.
2. Create `data/local/prefs/UserPrefs.kt` (interface) and `data/local/prefs/UserPrefsImpl.kt` (implementation with its own top-level `preferencesDataStore` delegate and its own datastore name).
3. Add `single<UserPrefs> { UserPrefsImpl(androidContext()) }` to `DataStoreModule.kt`.
4. Create / update the repository in `domain/repository/` + `data/repository/`; bind in `RepositoryModule.kt`.

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| `single { AppConfigImpl(get()) }` without `<AppConfig>` interface type | Koin resolves the concrete class, not the interface — ViewModel can't inject it | Always bind to the interface: `single<AppConfig> { ... }` |
| Reusing the same `preferencesDataStore` name for two groups | Both groups share one DataStore file — keys can collide | Give each group a unique `APP_CONFIG_DATASTORE_NAME` |
| Registering `DataStore<Preferences>` as a Koin `single` instead of the accessor interface | Exposes internal type; multiple DataStores can't share a single binding cleanly | Register only the accessor interface; DataStore is an impl detail |
| Forgetting `dataStoreModule` in `startKoin { modules(...) }` | `NoBeanDefinitionFoundException` at runtime | Always include every module in `startKoin` |
