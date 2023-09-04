package com.example.totalplanner

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.totalplanner.data.SettingsRepo

private const val SETTINGS_NAME = "settings_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_NAME
)

/*
 * Custom app entry point for manual dependency injection
 */
class SettingsApplication: Application() {
    lateinit var settingsRepo: SettingsRepo

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepo(dataStore)
    }
}