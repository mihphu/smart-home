# Repository — Interface & Implementation over DataStore

The repository bridges the DataStore accessor (`data/local/prefs/`) and the ViewModel. ViewModels inject only the repository interface from `domain/repository/`.

## Rules

- **Interface** in `domain/repository/` — pure Kotlin, no Android/DataStore/prefs imports.
- **Implementation** in `data/repository/` — injects the prefs accessor (e.g. `AppConfig`), delegates all DataStore mechanics to it, and returns domain types.
- The repository never exposes `Preferences` or raw `DataStore` types — only domain values (`Boolean`, `String`, domain model, etc.).
- One repository can combine DataStore prefs **and** Room DAOs (e.g. `SettingsRepositoryImpl` reads a preference and a cached entity).
- Bound in `RepositoryModule.kt` via Koin/Hilt — never instantiated manually.

## Templates

### Domain repository interface

```kotlin
// domain/repository/SettingsRepository.kt
package <root>.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkModeOnFlow: Flow<Boolean>
    val userTokenFlow: Flow<String>
    val selectedThemeFlow: Flow<Int>
    val lastSyncTimeFlow: Flow<Long>
    val fontScaleFlow: Flow<Float>
    val sortOrderFlow: Flow<String>

    suspend fun setDarkMode(isDarkModeOn: Boolean)
    suspend fun setUserToken(token: String)
    suspend fun setSelectedTheme(themeIndex: Int)
    suspend fun setLastSyncTime(timestamp: Long)
    suspend fun setFontScale(scale: Float)
    suspend fun setSortOrder(order: String)
}
```

### Repository implementation

```kotlin
// data/repository/SettingsRepositoryImpl.kt
package <root>.data.repository

import kotlinx.coroutines.flow.Flow
import <root>.data.local.prefs.AppConfig
import <root>.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val appConfig: AppConfig
) : SettingsRepository {

    override val isDarkModeOnFlow: Flow<Boolean>
        get() { return appConfig.isDarkModeOnFlow }

    override val userTokenFlow: Flow<String>
        get() { return appConfig.userTokenFlow }

    override val selectedThemeFlow: Flow<Int>
        get() { return appConfig.selectedThemeFlow }

    override val lastSyncTimeFlow: Flow<Long>
        get() { return appConfig.lastSyncTimeFlow }

    override val fontScaleFlow: Flow<Float>
        get() { return appConfig.fontScaleFlow }

    override val sortOrderFlow: Flow<String>
        get() { return appConfig.sortOrderFlow }

    override suspend fun setDarkMode(isDarkModeOn: Boolean) {
        appConfig.setDarkMode(isDarkModeOn)
    }

    override suspend fun setUserToken(token: String) {
        appConfig.setUserToken(token)
    }

    override suspend fun setSelectedTheme(themeIndex: Int) {
        appConfig.setSelectedTheme(themeIndex)
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        appConfig.setLastSyncTime(timestamp)
    }

    override suspend fun setFontScale(scale: Float) {
        appConfig.setFontScale(scale)
    }

    override suspend fun setSortOrder(order: String) {
        appConfig.setSortOrder(order)
    }
}
```

### ViewModel consuming the repository

```kotlin
// ui/settings/SettingsViewModel.kt
package <root>.ui.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import <root>.common.base.viewmodel.BaseViewModel
import <root>.domain.repository.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

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

## Combining DataStore + Room in one repository

```kotlin
class SettingsRepositoryImpl(
    private val appConfig: AppConfig,
    private val userDao: UserDao          // Room DAO also injected
) : SettingsRepository {

    // DataStore preference
    override val isDarkModeOnFlow: Flow<Boolean>
        get() { return appConfig.isDarkModeOnFlow }

    // Room query alongside DataStore pref — both exposed as Flow
    override val cachedUserFlow: Flow<User?> = userDao.getUserFlow()
        .map { entity -> entity?.toDomain() }
}
```

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Injecting `AppConfig` or `DataStore` directly into ViewModel | Leaks DataStore into UI layer | Always go through the repository interface |
| Exposing `Preferences` or `DataStore<Preferences>` from the repository | Data-layer type crosses into domain/UI | Map to plain Kotlin types before returning |
| Calling prefs setters from a `Dispatchers.Main` scope | ANR or coroutine exception | The `withContext(IO)` is already in the prefs `*Impl`; no extra switch needed in the repository |
