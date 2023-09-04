package com.example.totalplanner.ui

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.totalplanner.R
import com.example.totalplanner.data.Month
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.Weekday
import com.example.totalplanner.data.room.AgendaDatabase
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
        MonthlyTopBar(
            modifier = Modifier
                .height(topBarHeight)
                .fillMaxWidth(),
            month = screenUI.value.monthDate.month,
            year = screenUI.value.monthDate.year,
            height = topBarHeight,
            changeMonth = {viewModel.updateMonthDate(
                if(screenUI.value.monthDate.month == Month.JANUARY && it == -1)
                    screenUI.value.monthDate.copy(
                        month = Month.DECEMBER,
                    )
                else if(screenUI.value.monthDate.month == Month.DECEMBER && it == 1)
                    screenUI.value.monthDate.copy(
                        month = Month.JANUARY,
                    )
                else
                    screenUI.value.monthDate.copy(
                        month =
                            Month.values()[
                                screenUI.value.monthDate.month.ordinal + it
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
 * My own top app bar, because Material Design is a tyrant and I want
 * the title in between 2 buttons, and why is a TOP BAR experimental?
 * I'm beginning to think Composable wasn't very well thought out...
 */
@Composable
fun MonthlyTopBar(
    modifier: Modifier = Modifier,
    month: Month,
    year: String,
    height: Dp,
    changeMonth:(Int) -> Unit
){
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {changeMonth(-1)}) {
                Icon(
                    painter = painterResource(android.R.drawable.arrow_up_float),
                    contentDescription = stringResource(R.string.last_week)
                )
            }
            Text(stringResource(month.monthName) + " $year")
            IconButton(onClick = {changeMonth(1)}) {
                Icon(
                    painter = painterResource(android.R.drawable.arrow_down_float),
                    contentDescription = stringResource(R.string.next_week)
                )
            }
        }
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