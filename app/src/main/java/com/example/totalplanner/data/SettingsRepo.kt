package com.example.totalplanner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepo(
    private val dataStore: DataStore<Preferences>
){
    val americanDates: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DATE_STYLE] ?: true
    }
    val hour24: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[TIME_STYLE] ?: false
    }
    val redDeadlines: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[RED_DEADLINES] ?: true
    }

    suspend fun saveDateStyle(isAmerican: Boolean) {
        dataStore.edit {preferences ->
            preferences[DATE_STYLE] = isAmerican
        }
    }
    suspend fun saveTimeStyle(is24Hours: Boolean) {
        dataStore.edit {preferences ->
            preferences[TIME_STYLE] = is24Hours
        }
    }
    suspend fun saveRedDeadline(redDeadline: Boolean) {
        dataStore.edit {preferences ->
            preferences[RED_DEADLINES] = redDeadline
        }
    }

    private companion object {
        val DATE_STYLE = booleanPreferencesKey("date_style")
        val TIME_STYLE = booleanPreferencesKey("time_style")
        val RED_DEADLINES = booleanPreferencesKey("red_deadlines")
    }
}