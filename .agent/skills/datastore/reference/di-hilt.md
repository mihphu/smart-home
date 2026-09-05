# DI with Hilt

Use this reference only if the project already uses Hilt. **Never mix Koin and Hilt in the same project.**

All DataStore wiring lives in `di/DataStoreModule.kt` annotated with `@Module` + `@InstallIn(SingletonComponent::class)`.

## Rules

- Provide `AppConfig` (the interface) as a `@Singleton`, **not** the concrete `AppConfigImpl`.
- `Context` is injected via `@ApplicationContext` — never pass `Activity` context.
- The `DataStore<Preferences>` instance may be provided as a `@Singleton` `@Provides` binding so it can be injected into `AppConfigImpl` — or you can rely on the `preferencesDataStore` delegate inside the impl (both approaches work; the delegate approach avoids registering a `DataStore` binding).
- Repository bindings live in a separate `@Module` using `@Binds`.

## Template — delegate approach (recommended, matches the Koin approach)

```kotlin
// di/DataStoreModule.kt
package <root>.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import <root>.data.local.prefs.AppConfig
import <root>.data.local.prefs.AppConfigImpl

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppConfig(
        @ApplicationContext context: Context
    ): AppConfig {
        return AppConfigImpl(context)
    }

    // Add more preference groups here as the app grows:
    // @Provides @Singleton
    // fun provideUserPrefs(@ApplicationContext context: Context): UserPrefs =
    //     UserPrefsImpl(context)
}
```

```kotlin
// di/RepositoryModule.kt
package <root>.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import <root>.data.repository.SettingsRepositoryImpl
import <root>.domain.repository.SettingsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
```

### `SettingsRepositoryImpl` with Hilt constructor injection

```kotlin
// data/repository/SettingsRepositoryImpl.kt
package <root>.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import <root>.data.local.prefs.AppConfig
import <root>.domain.repository.SettingsRepository

class SettingsRepositoryImpl @Inject constructor(
    private val appConfig: AppConfig
) : SettingsRepository {

    override val isDarkModeOnFlow: Flow<Boolean>
        get() { return appConfig.isDarkModeOnFlow }

    override suspend fun setDarkMode(isDarkModeOn: Boolean) {
        appConfig.setDarkMode(isDarkModeOn)
    }

    // … other delegating overrides …
}
```

### ViewModel with Hilt injection

```kotlin
// ui/settings/SettingsViewModel.kt
package <root>.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import <root>.domain.repository.SettingsRepository

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkModeOn: StateFlow<Boolean> = settingsRepository.isDarkModeOnFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }
}
```

## Template — explicit DataStore binding approach

Use this only when you need to inject the raw `DataStore<Preferences>` into multiple classes (rare):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppConfigDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("app_config") }
        )
    }

    @Provides
    @Singleton
    fun provideAppConfig(
        dataStore: DataStore<Preferences>
    ): AppConfig {
        return AppConfigImpl(dataStore)   // impl takes DataStore directly
    }
}
```

> **Note:** when using the explicit binding approach, `AppConfigImpl` takes `DataStore<Preferences>` as a constructor parameter instead of `Context`, and does NOT use the `preferencesDataStore` delegate.

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| `@Provides` returning `AppConfigImpl` (concrete) instead of `AppConfig` (interface) | Hilt can't satisfy `AppConfig` injection points | Return the interface type |
| Using `@ActivityContext` instead of `@ApplicationContext` | DataStore scoped to Activity lifetime — crashes after rotation | Always use `@ApplicationContext` for DataStore |
| Forgetting `@Singleton` on the `@Provides` method | New `AppConfigImpl` (and new DataStore) created per injection | Always add `@Singleton` |
| Mixing `@Binds` (abstract module) with `@Provides` (object module) in the same class | Compilation error | Use `abstract class` for `@Binds`, `object` for `@Provides`, or use a companion object |
