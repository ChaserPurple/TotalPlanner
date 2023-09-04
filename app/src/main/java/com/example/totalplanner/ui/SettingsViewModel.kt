package com.example.totalplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.totalplanner.SettingsApplication
import com.example.totalplanner.data.SettingsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class SettingsViewModel(
    val settingsRepo: SettingsRepo
) : ViewModel() {
    val settingsUIState: StateFlow<SettingsUIState> =
        settingsRepo.americanDates
            .zip(settingsRepo.hour24){ d, t ->
                Pair(d, t)
            }.zip(settingsRepo.redDeadlines){dt, r ->
                Triple(dt.first, dt.second, r)
            }.map {
                SettingsUIState(
                    americanDates = it.first,
                    hour24 = it.second,
                    redDeadline = it.third
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUIState()
            )

    fun saveDateStyle(dateStyle: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveDateStyle(dateStyle)
        }
    }
    fun saveTimeStyle(timeStyle: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveTimeStyle(timeStyle)
        }
    }
    fun saveRedDeadline(redDead: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveRedDeadline(redDead)
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SettingsApplication)
                SettingsViewModel(application.settingsRepo)
            }
        }
    }
}