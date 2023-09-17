package com.example.totalplanner.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.SettingsRepo
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.EventDAO
import com.example.totalplanner.data.room.Task
import com.example.totalplanner.data.room.TaskDAO
import com.example.totalplanner.data.toMyColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip

//Because no application data is requested when adding a new item to the agenda,
//viewModel only has functions, and no variables outside of uiState
class NewItemViewModel(
    private val taskDAO: TaskDAO,
    private val eventDAO: EventDAO,
    settingsRepo: SettingsRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewItemUIState())
    val uiState: StateFlow<NewItemUIState> = _uiState
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

    /*
     * updates whether the user is entering a task
     */
    fun updateMode(t: Boolean){
        _uiState.update {
            it.copy(
                newTask = t
            )
        }
    }
    /*
     * updates the name of the new item held in state
     */
    fun updateName(n: String){
        _uiState.update {
            it.copy(
                name = n
            )
        }
    }
    /*
     * updates the description of the new item held in state
     */
    fun updateDesc(d: String){
        _uiState.update {
            it.copy(
                desc = d
            )
        }
    }
    /*
     * updates the color of the new item held in state
     */
    fun updateColor(c: Color){
        _uiState.update {
            it.copy(
                color = c
            )
        }
    }
    /*
     * Determines whether to show color dialogue
     */
    fun updateColorDialogue(c: Boolean){
        _uiState.update {
            it.copy(
                showColorDialogue = c
            )
        }
    }
    /*
     * updates the deadline of the new task held in state
     */
    suspend fun updateDeadline(d: MyDate){
        _uiState.update {
            it.copy(
                deadlineDate = d
            )
        }
        _uiState.value.taskEvents.forEach{eventPair ->
            if(eventPair.second != -1)
                eventDAO.deleteEvent(Event(
                    id = eventPair.second,
                    name = _uiState.value.name,
                    description = _uiState.value.desc,
                    startDate = eventPair.first.first,
                    endDate = eventPair.first.second,
                    taskID = _uiState.value.updateID.toLong(),
                    color = _uiState.value.color.toMyColor()
                ))
        }
        _uiState.value.taskEvents.clear()
    }
    /*
     * updates the startDate of the new event held in state
     */
    fun updateStart(s: MyDate){
        _uiState.update {
            it.copy(
                startDate = s
            )
        }
    }
    /*
     * updates the endDate of the new event held in state
     */
    fun updateEnd(e: MyDate){
        _uiState.update {
            it.copy(
                endDate = e
            )
        }
    }
    /*
     * removes the work period at the given index
     */
    suspend fun removePeriod(e: Pair<Pair<MyDate, MyDate>, Int>){
        if(e.second != -1){
            _uiState.value.restoreEvents.add(eventDAO.getEventByID(e.second))
            eventDAO.deleteEvent(eventDAO.getEventByID(e.second))
        }
        _uiState.value.taskEvents.remove(e)
        _uiState.update {
            it.copy(
                periodNdx = -1
            )
        }
    }
    /*
     * prepares values to open the dates dialogue
     */
    fun showDateDialogue(first:MyDate = MyDate(), second:MyDate = MyDate(), ndx:Int){
        _uiState.update {
            it.copy(
                startDate = first,
                endDate = second,
                periodNdx = ndx,
                showDatesDialogue = true
            )
        }
    }
    /*
     * confirms the new work period added in the date dialogue
     */
    fun confirmDatesChoice(){
        if(_uiState.value.periodNdx == -1)
            _uiState.value.taskEvents.add(Pair(Pair(_uiState.value.startDate,_uiState.value.endDate), -1))
        else{
            _uiState.value.taskEvents[_uiState.value.periodNdx] =
                Pair(
                    Pair(_uiState.value.startDate,_uiState.value.endDate),
                    _uiState.value.taskEvents[_uiState.value.periodNdx].second
                )
            _uiState.update {
                it.copy(
                    periodNdx = -1
                )
            }
        }
    }
    /*
     * Closes the date dialogue
     */
    fun dismissDatesDialogue(){
        _uiState.update {
            it.copy(
                showDatesDialogue = false
            )
        }
    }
    /*
     * shows a confirmation dialogue with the dates of the period to be removed
     */
    fun confirmDeletion(e: Pair<Pair<MyDate,MyDate>, Int>, ndx: Int){
        _uiState.update {
            it.copy(
                startDate = e.first.first,
                endDate = e.first.second,
                showConfirmationDialogue = true,
                periodNdx = ndx
            )
        }
    }
    /*
     * Closes the confirmation dialogue
     */
    fun dismissConfirmDialogue(){
        _uiState.update {
            it.copy(
                showConfirmationDialogue = false,
                startDate = MyDate(),
                endDate = MyDate()
            )
        }
    }
    /*
     * Updates whether invalidInputDialogue shows
     */
    fun updateInputAlert(i: Boolean){
        _uiState.update {
            it.copy(
                showInput = i
            )
        }
    }

    /*
     * Returns true if all input is valid, else false
     */
    fun noErrors(): Boolean{
        return if (_uiState.value.name != "") {
            if (_uiState.value.newTask &&
                _uiState.value.deadlineDate.isValid() &&
                _uiState.value.deadlineDate.isFuture()
            ) true
            else !_uiState.value.newTask &&
                    _uiState.value.startDate.isValid() &&
                    _uiState.value.endDate.isValid() &&
                    _uiState.value.startDate.isFuture() &&
                    _uiState.value.endDate.isFuture() &&
                    _uiState.value.startDate.before(_uiState.value.endDate) &&
                    _uiState.value.startDate.sameDay(_uiState.value.endDate)
        } else false
    }

    /*
     * saves an event to the event database
     */
    suspend fun saveEvent(event: Event){
        eventDAO.insertEvent(event)
    }
    /*
     * saves a task to the task database and converts any date pairs in the
     * list to an even and saves those to the event database
     */
    suspend fun saveTask(task: Task, events: List<Pair<Pair<MyDate,MyDate>,Int>>){
        val id = taskDAO.insertTask(task)
        events.forEach{
            saveEvent(Event(
                name = task.name,
                description = task.description,
                color = task.color,
                startDate = it.first.first,
                endDate = it.first.second,
                taskID = id
            ))
        }
    }

    /*
     * updates an event in the event database
     * precondition: update id != -1
     */
    suspend fun updateEvent(event: Event){
        eventDAO.updateEvent(event)
    }
    /*
     * saves a task to the task database and converts any date pairs in the
     * list to an even and saves those to the event database
     */
    suspend fun updateTask(
        task: Task,
        eventsList: MutableList<Pair<Pair<MyDate, MyDate>, Int>>
    ){
        taskDAO.updateTask(task)
        eventsList.forEach{
            if(it.second != -1){
                updateEvent(
                    Event(
                        id = it.second,
                        name = task.name,
                        description = task.description,
                        startDate = it.first.first,
                        endDate = it.first.second,
                        taskID = task.id.toLong(),
                        color = task.color
                    )
                )
            }
            else{
                saveEvent(
                    Event(
                        id = it.second,
                        name = task.name,
                        description = task.description,
                        startDate = it.first.first,
                        endDate = it.first.second,
                        taskID = task.id.toLong(),
                        color = task.color
                    )
                )
            }
        }
    }

    suspend fun restoreDeletions(){
        _uiState.value.restoreEvents.forEach{
            saveEvent(it)
        }
        _uiState.value.restoreEvents.clear()
    }

    /*
     * clears the state data so that when the screen is navigated back to after
     * submitting the form, the form is cleared
     */
    fun reset(leaveScreen: () -> Unit){
        _uiState.value = NewItemUIState()
        leaveScreen()
    }
    /*
     * clears the state data so that when the screen is navigated back to after
     * submitting the form, the form is cleared
     */
    fun initVals(
        eventsList: MutableList<Pair<Pair<MyDate,MyDate>, Int>>,
        task: Task? = null,
        event: Event? = null
    ){
        if(task == null && event == null)
            _uiState.value = NewItemUIState()
        else
            _uiState.update{
                if(task!= null)
                    it.copy(
                        newTask = true,
                        updateID = task.id,
                        name = task.name,
                        desc = task.description,
                        deadlineDate = task.deadline,
                        color = task.color.toColor(),
                        taskEvents = eventsList
                    )
                else
                    it.copy(
                        newTask = false,
                        updateID = event!!.id,
                        name = event.name,
                        desc = event.description,
                        startDate = event.startDate,
                        endDate = event.endDate,
                        color = event.color.toColor()
                    )
            }
    }
}

data class NewItemUIState(
    //Both
    val updateID: Int = -1,
    val eventsTaskID: Int = -1,
    val newTask: Boolean = false,
    val name: String = "",
    val desc: String = "",
    val color: Color = Color.White,
    val showColorDialogue: Boolean = false,
    val startDate: MyDate = MyDate(),
    val endDate: MyDate = MyDate(),
    val deadlineDate: MyDate = MyDate(),
    val taskEvents: MutableList<Pair<Pair<MyDate, MyDate>, Int>> = mutableListOf(),
    val restoreEvents: MutableList<Event> = mutableListOf(),
    val showDatesDialogue: Boolean = false,
    val showConfirmationDialogue: Boolean = false,
    val periodNdx: Int = -1,
    val showInput: Boolean = false
)