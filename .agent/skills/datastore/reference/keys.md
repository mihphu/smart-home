# Preference Keys

Keys are the only DataStore-specific classes that live in `data/local/datastore/`. They are grouped by concern into `object` files. The accessor classes in `data/local/prefs/` import from here — nowhere else defines keys.

## Rules

- One `object` per logical DataStore group (one `*Keys.kt` per `*Impl.kt`).
- Key names use `SCREAMING_SNAKE_CASE` with a `KEY_` prefix.
- The string argument to each factory function must be **stable across app versions** — changing it silently loses stored data for users.
- Enum preferences are stored as `String` (the enum's `.name`).

## Template

```kotlin
// data/local/datastore/AppConfigKeys.kt
package <root>.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AppConfigKeys {
    // Boolean
    val KEY_IS_DARK_MODE_ON = booleanPreferencesKey("is_dark_mode_on")

    // String
    val KEY_USER_TOKEN     = stringPreferencesKey("user_token")

    // Int — e.g. selected theme index
    val KEY_SELECTED_THEME = intPreferencesKey("selected_theme")

    // Long — e.g. timestamp
    val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

    // Float — e.g. font scale
    val KEY_FONT_SCALE     = floatPreferencesKey("font_scale")

    // Enum stored as String — read back with enumValueOf<SortOrder>(value)
    val KEY_SORT_ORDER     = stringPreferencesKey("sort_order")
}
```

## Adding a new key — checklist

1. Choose the correct factory from the table in `SKILL.md §3`.
2. Add the constant to the matching `*Keys.kt` object.
3. The string argument must be **lowercase_snake_case** and must never change once released.
4. If you need to change the type of an existing key, add a **new** constant with a new name (suffix `_v2`, etc.) — never reuse the old name.
