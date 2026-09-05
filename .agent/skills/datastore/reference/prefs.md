# Typed Accessor (Prefs Interface + Implementation)

The accessor is the only class that holds a reference to `DataStore<Preferences>`. The interface (pure Kotlin) is imported by the repository; the implementation handles all DataStore mechanics.

## Rules

- **Interface** (`data/local/prefs/AppConfig.kt`) — pure Kotlin, no Android/DataStore imports. Exposes `val …: Flow<T>` for reads and `suspend fun set…(value: T)` for writes.
- **Implementation** (`data/local/prefs/AppConfigImpl.kt`) — holds the `DataStore<Preferences>` reference (constructor-injected). Declares the `preferencesDataStore` delegate as a **top-level** `private` extension on `Context`.
- Every read flow **must** have `.catch { IOException → emptyPreferences() }` and `.distinctUntilChanged()`.
- Every write **must** use `withContext(Dispatchers.IO)`.
- The implementation is bound to its interface in `DataStoreModule.kt` — never instantiated directly.

## Templates

### Interface

```kotlin
// data/local/prefs/AppConfig.kt
package <root>.data.local.prefs

import kotlinx.coroutines.flow.Flow

interface AppConfig {
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

### Implementation

```kotlin
// data/local/prefs/AppConfigImpl.kt
package <root>.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import <root>.data.local.datastore.AppConfigKeys

// Top-level delegate — ONE per file, never inside a class
private const val APP_CONFIG_DATASTORE_NAME = "app_config"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_CONFIG_DATASTORE_NAME
)

class AppConfigImpl(private val context: Context) : AppConfig {

    // ---- Reads ----

    override val isDarkModeOnFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_IS_DARK_MODE_ON] ?: false
        }
        .distinctUntilChanged()

    override val userTokenFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_USER_TOKEN].orEmpty()
        }
        .distinctUntilChanged()

    override val selectedThemeFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_SELECTED_THEME] ?: 0
        }
        .distinctUntilChanged()

    override val lastSyncTimeFlow: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_LAST_SYNC_TIME] ?: 0L
        }
        .distinctUntilChanged()

    override val fontScaleFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_FONT_SCALE] ?: 1.0f
        }
        .distinctUntilChanged()

    // Enum stored as String — read back with enumValueOf<SortOrder>()
    override val sortOrderFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AppConfigKeys.KEY_SORT_ORDER] ?: SortOrder.DEFAULT.name
        }
        .distinctUntilChanged()

    // ---- Writes ----

    override suspend fun setDarkMode(isDarkModeOn: Boolean) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_IS_DARK_MODE_ON] = isDarkModeOn
            }
        }
    }

    override suspend fun setUserToken(token: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_USER_TOKEN] = token
            }
        }
    }

    override suspend fun setSelectedTheme(themeIndex: Int) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_SELECTED_THEME] = themeIndex
            }
        }
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_LAST_SYNC_TIME] = timestamp
            }
        }
    }

    override suspend fun setFontScale(scale: Float) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_FONT_SCALE] = scale
            }
        }
    }

    override suspend fun setSortOrder(order: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[AppConfigKeys.KEY_SORT_ORDER] = order
            }
        }
    }
}
```

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| `preferencesDataStore` inside a class body | `IllegalStateException` at runtime — delegate must be top-level | Move to file-level |
| Missing `.catch { IOException → emptyPreferences() }` | App crashes on first cold-start if DataStore file is corrupted | Always add the catch block |
| Missing `.distinctUntilChanged()` | Downstream collectors triggered for every read even if value didn't change | Always add after `.map { }` |
| `dataStore.edit` on Main thread | `NetworkOnMainThreadException` (or ANR) | Always wrap with `withContext(Dispatchers.IO)` |
| Defining keys inside `AppConfigImpl` | Keys are hidden, can't be reused or unit-tested independently | Keys always live in `*Keys.kt` |
