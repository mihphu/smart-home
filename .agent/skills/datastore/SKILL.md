---
name: datastore
description: Use when creating or modifying anything in the DataStore layer — preference keys, typed accessor classes (prefs/), proto serializers, or DI wiring (Koin/Hilt) for DataStore. MUST be read before adding any new stored preference or replacing SharedPreferences with DataStore. Do NOT use SharedPreferences for new code.
---

# DataStore (Preferences & Proto)

Patterns and copy-ready templates for the DataStore persistence layer. All code MUST follow the layer rules in `docs/ARCHITECTURE.md`: DataStore definitions live under `data/local/datastore/`, typed accessors live under `data/local/prefs/`, and values are exposed as `Flow<T>` to the repository — never directly to a ViewModel or UI.

## 1. When to use this skill

- Adding a new stored user preference (boolean, string, int, long, float, enum)
- Replacing an existing `SharedPreferences` call with DataStore
- **Changing the type of an existing preference key** → rename the key constant to avoid stale data
- Setting up DataStore for the first time in a module (Preferences or Proto)
- Wiring DataStore classes into DI (Koin or Hilt)
- Writing a repository that reads/writes DataStore values

**Not for:** structured relational data (use Room), network response caching, file storage.

## 2. Constraints (hard rules)

1. **Placement:** preference keys → `data/local/datastore/`, typed accessor interfaces + implementations → `data/local/prefs/`, DI modules → `di/DataStoreModule.kt`.
2. **DataStore is never injected into a ViewModel.** Only the repository interface (from `domain/repository/`) is injected into ViewModels.
3. **Catch `IOException` on every `DataStore.data` flow** and `emit(emptyPreferences())` — never let a read crash propagate. All other exceptions are rethrown.
4. **Apply `distinctUntilChanged()`** on every `Flow` exposed from a prefs accessor to prevent redundant downstream emissions.
5. **All write operations use `withContext(Dispatchers.IO)`** inside `context.dataStore.edit { }` — never block the main thread.
6. **Keys are `object` constants** in a `*Keys.kt` file (e.g. `AppConfigKeys`). Never define preference keys inline inside an accessor class.
7. **Each logical group of preferences shares exactly one named DataStore** (e.g. `"app_config"`). Do not create multiple DataStore instances for a single preference group.
8. **DataStore delegate (`preferencesDataStore` / `dataStoreFile`) is declared as a top-level extension on `Context`** — declared once per file, never inside a class.
9. **Typed accessor interface** (e.g. `AppConfig`) is defined in the same package (`data/local/prefs/`) and exposes only `Flow<T>` getters and `suspend` setters.
10. **The DataStore instance is provided by Koin** (preferred) or Hilt — never instantiated manually inside an accessor or repository.
11. **Kotlin style:** functions that return a value use block bodies (`{ return ... }`), never expression bodies (`= ...`).

## 3. Key-type rules

| Preference type | Key factory | Example |
|---|---|---|
| `Boolean` | `booleanPreferencesKey("name")` | `KEY_IS_DARK_MODE_ON` |
| `String` | `stringPreferencesKey("name")` | `KEY_USER_TOKEN` |
| `Int` | `intPreferencesKey("name")` | `KEY_SELECTED_THEME` |
| `Long` | `longPreferencesKey("name")` | `KEY_LAST_SYNC_TIME` |
| `Float` | `floatPreferencesKey("name")` | `KEY_FONT_SCALE` |
| `String` (for enum) | `stringPreferencesKey("name")` | `KEY_SORT_ORDER` — store `enum.name`, read back with `enumValueOf<T>()` |

> **Changing an existing key type?** Create a new key constant with a new name (e.g. `KEY_IS_DARK_MODE_ON_V2`). The old key will be ignored naturally. Never reuse the old name with a different type — type mismatches are silent errors at runtime.

## 4. Quick reference

| Task | Read |
|---|---|
| Preference keys file template | [reference/keys.md](reference/keys.md) |
| Typed accessor interface + implementation | [reference/prefs.md](reference/prefs.md) |
| Repository interface + implementation using prefs | [reference/repository.md](reference/repository.md) |
| DI with Koin (DataStore, prefs, repository) | [reference/di-koin.md](reference/di-koin.md) |
| DI with Hilt (DataStore, prefs, repository) | [reference/di-hilt.md](reference/di-hilt.md) |

## 5. Example — adding `AppConfig` (dark mode) end-to-end

The same `AppConfig` example runs through every reference file. Order of work:

1. **Keys** — `data/local/datastore/AppConfigKeys.kt` (key constants) → [reference/keys.md](reference/keys.md).
2. **Accessor interface** — `data/local/prefs/AppConfig.kt` (`Flow` + suspend setters) → [reference/prefs.md](reference/prefs.md).
3. **Accessor implementation** — `data/local/prefs/AppConfigImpl.kt` → [reference/prefs.md](reference/prefs.md).
4. **Repository** (if the ViewModel needs this data) — interface `domain/repository/SettingsRepository.kt`, impl `data/repository/SettingsRepositoryImpl.kt` → [reference/repository.md](reference/repository.md).
5. **DI** — provide DataStore instance + `AppConfig` binding + repository binding → [reference/di-koin.md](reference/di-koin.md) or [reference/di-hilt.md](reference/di-hilt.md).

## 6. Checklist

Run through this before declaring DataStore work done:

- [ ] Keys in `data/local/datastore/*Keys.kt` as an `object` — named with `KEY_` prefix, typed with the correct `*PreferencesKey` factory
- [ ] `preferencesDataStore` delegate declared as a **top-level** extension on `Context` in the `*Impl.kt` file — NOT inside the class
- [ ] Every `DataStore.data` flow has `.catch { if (it is IOException) emit(emptyPreferences()) else throw it }`
- [ ] Every exposed `Flow` ends with `.distinctUntilChanged()`
- [ ] All write operations wrapped in `withContext(Dispatchers.IO) { context.dataStore.edit { } }`
- [ ] Accessor interface defines only `Flow<T>` vals and `suspend` funs — no Android/DataStore imports in the interface file
- [ ] DataStore instance provided by DI (Koin `single` or Hilt `@Provides @Singleton`); `AppConfigImpl` injected via its interface
- [ ] Repository interface in `domain/repository/`, implementation in `data/repository/` — ViewModel never imports from `data/`
- [ ] Repository registered in `RepositoryModule.kt`; DataStore wiring in `DataStoreModule.kt`
- [ ] Functions returning a value use block bodies `{ return ... }`, not `= ...`
- [ ] No `DataStore`/`Preferences` import in `ui/` or `domain/`
