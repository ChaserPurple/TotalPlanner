package com.example.totalplanner.ui

import android.content.res.Resources
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.totalplanner.R
import com.example.totalplanner.data.Month
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.Weekday
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.Task
import kotlinx.coroutines.launch

@Composable
fun MonthlyScreen (
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel
){
    val tasksUI = viewModel.tasksUIState.collectAsState()
    val eventsUI = viewModel.eventsUIState.collectAsState()
    val settingsUIState = viewModel.settingsUIState.collectAsState()
    val screenUI = viewModel.scheduleUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val topBarHeight = 40.dp
    val labelHeight = 32.dp
    val columnWidth = maxOf((Resources.getSystem()
        .displayMetrics
        .run{widthPixels / density}.dp / 7), 160.dp)
    val dayHeight = maxOf((Resources.getSystem()
        .displayMetrics
        .run{heightPixels / density}.dp -
            topBarHeight - labelHeight), 88.dp)

    val horizontalScrollState = rememberScrollState()
    Column(modifier = modifier) {
        ScheduleTopBar(
            modifier = Modifier
                .height(topBarHeight)
                .fillMaxWidth(),
            text = stringResource(screenUI.value.monthDate.month.monthName) +
                    " " + screenUI.value.monthDate.year,
            prevAction = {viewModel.updateMonthDate(
                screenUI.value.monthDate.copy(
                    month =
                        Month.values()[
                            //add 11 instead of subtracting 1 so it's never < 0
                            (screenUI.value.monthDate.month.ordinal + 11) % 12
                        ],
                )
            )},
            nextAction = {viewModel.updateMonthDate(
                screenUI.value.monthDate.copy(
                    month =
                        Month.values()[
                            (screenUI.value.monthDate.month.ordinal + 1) % 12
                        ],
                )
            )}
        )
        WeekDayLabels(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .height(labelHeight)
                .horizontalScroll(horizontalScrollState),
            width = columnWidth
        )
        MonthlyGrid(
            modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(horizontalScrollState),
            date = screenUI.value.monthDate.lastSunday(),
            month = screenUI.value.monthDate.month,
            height = dayHeight,
            width = columnWidth,
            tasks = tasksUI.value.tasks,
            events = eventsUI.value.events,
            onDeleteTask = { coroutineScope.launch{ viewModel.deleteTask(it) } },
            onEditTask = { coroutineScope.launch{ viewModel.editTask(it) } },
            onDeleteEvent = { coroutineScope.launch{ viewModel.deleteEvent(it) } },
            onEditEvent = { coroutineScope.launch{ viewModel.editEvent(it) } },
            americanDates = settingsUIState.value.americanDates,
            hour24 = settingsUIState.value.hour24,
            redDeadlines = settingsUIState.value.redDeadline
        )
    }
}

/*
 * The meat of the calendar, lays out each day and lists each day's events
 */
@Composable
fun MonthlyGrid(
    modifier: Modifier = Modifier,
    date: MyDate,
    month: Month,
    height: Dp,
    width: Dp,
    tasks: List<Task>,
    events: List<Event>,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    americanDates: Boolean,
    hour24: Boolean,
    redDeadlines: Boolean
){
    val numRows =
        if(date.addDays(35).month == month) 6
        else 5
    var d = date.copy()
    val corrMonthCols =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    val wrongMonthCols = CardDefaults.cardColors()
    val sameDayCols =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    Column(modifier = modifier){
        for(i in 1..numRows){
            Row{
                Weekday.values().forEach{
                    Card(
                        modifier = Modifier
                            .width(width)
                            .height(height / numRows)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            ),
                        shape = RectangleShape,
                        colors = if (d.sameDay(MyDate())) sameDayCols
                                 else if(d.month == month) corrMonthCols
                                 else wrongMonthCols
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            text = d.day,
                            fontWeight = FontWeight.Bold
                        )
                        MonthDay(
                            modifier = Modifier
                                .width(width)
                                .padding(4.dp),
                            date = d,
                            tasks = tasks,
                            events = events,
                            onDeleteTask = onDeleteTask,
                            onEditTask = onEditTask,
                            onDeleteEvent = onDeleteEvent,
                            onEditEvent = onEditEvent,
                            americanDates = americanDates,
                            hour24 = hour24,
                            redDeadlines = redDeadlines
                        )
                    }
                    d = d.addDays(1)
                }
            }
        }
    }
}
@Composable
fun MonthDay(
    modifier: Modifier = Modifier,
    date: MyDate,
    tasks: List<Task>,
    events: List<Event>,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    americanDates: Boolean,
    hour24: Boolean,
    redDeadlines: Boolean
){
    LazyColumn (modifier = modifier){
        items(tasks) { task ->
            if (date.sameDay(task.deadline)) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        modifier = Modifier
                            .clickable(
                                onClick = { expanded = true }
                            )
                            .padding(1.dp),
                        text = task.name + " " +
                                stringResource(R.string.deadline),
                        color = if(redDeadlines) Color.Red else task.color.toColor()
                    )
                    TaskDropdown(
                        task = task,
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onDeleteTask = { onDeleteTask(task) },
                        onEditTask = { onEditTask(task) },
                        americanDates = americanDates,
                        hour24 = hour24
                    )
                }
            }
        }
        items(events) { event ->
            if (event.startDate.sameDay(date)) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    Text(
                        modifier = Modifier
                            .clickable(
                                onClick = { expanded = true }
                            )
                            .padding(1.dp),
                        text = event.name,
                        color = event.color.toColor()
                    )
                    EventDropdown(
                        event = event,
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onDeleteEvent = { onDeleteEvent(event) },
                        onEditEvent = { onEditEvent(event) },
                        americanDates = americanDates,
                        hour24 = hour24
                    )
                }
            }
        }
    }
}
/*
@Preview(showBackground = true)
@Composable
fun PreviewMonthly(){
    MonthlyScreen(
        viewModel = ScheduleViewModel(
            AgendaDatabase.getDatabase(LocalContext.current).getTaskDao(),
            AgendaDatabase.getDatabase(LocalContext.current).getEventDao(),
            updateTask = {},
            updateEvent = {}
        )
    )
}*/