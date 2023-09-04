package com.example.totalplanner.ui

import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.SettingsRepo
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.EventDAO
import com.example.totalplanner.data.room.Task
import com.example.totalplanner.data.room.TaskDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip

class ScheduleViewModel(
    private val taskDAO: TaskDAO,
    private val eventDAO: EventDAO,
    private val updateTask: (Task) -> Unit,
    private val updateEvent: (Event) -> Unit,
    settingsRepo: SettingsRepo
): ViewModel(){
    val tasksUIState: StateFlow<TasksUIState> =
        taskDAO.getAllTasks().map { TasksUIState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = TasksUIState()
            )
    val eventsUIState: StateFlow<EventsUiState> =
        eventDAO.getAllEvents().map { EventsUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = EventsUiState()
            )
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
    private val _scheduleUIState = MutableStateFlow(ScheduleUIState())
    val scheduleUIState: StateFlow<ScheduleUIState> = _scheduleUIState

    suspend fun deleteTask(task: Task){
        var taskEvents = eventDAO.getEventOfTask(task.id)
        taskEvents.forEach{
            eventDAO.deleteEvent(it)
        }
        taskDAO.deleteTask(task)
    }
    suspend fun deleteEvent(event: Event){
        eventDAO.deleteEvent(event)
    }

    fun editTask(task: Task){
        updateTask(task)
    }
    fun editEvent(event: Event){
        updateEvent(event)
    }

    fun updateWeekDate(date: MyDate){
        _scheduleUIState.update {
            it.copy(
                weekDate = date
            )
        }
    }
    fun updateMonthDate(date: MyDate){
        _scheduleUIState.update {
            it.copy(
                monthDate = date
            )
        }
    }

    fun resetScreen(){
        _scheduleUIState.update {
            it.copy(
                weekDate = MyDate(hour = "12", minute = "00", isAfternoon = false).lastSunday(),
                monthDate = MyDate(hour = "12", minute = "00", isAfternoon = false, day = "1")
            )
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

data class TasksUIState(val tasks: List<Task> = listOf())
data class EventsUiState(val events: List<Event> = listOf())
data class ScheduleUIState(
    val weekDate: MyDate = MyDate(hour = "12", minute = "00", isAfternoon = false).lastSunday(),
    val monthDate: MyDate = MyDate(hour = "12", minute = "00", isAfternoon = false, day = "1")
)