package com.example.catplayground.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cat_settings")

class SettingsStore(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("app_theme")
    private val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    val reduceMotion: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REDUCE_MOTION] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = mode
        }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REDUCE_MOTION] = reduce
        }
    }
}
