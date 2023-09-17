package com.example.totalplanner.ui

import android.content.res.Resources
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.Weekday
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.Task
import kotlinx.coroutines.launch

@Composable
fun WeeklyScreen (
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel
){
    val tasksUI = viewModel.tasksUIState.collectAsState()
    val eventsUI = viewModel.eventsUIState.collectAsState()
    val settingsUIState = viewModel.settingsUIState.collectAsState()
    val screenUI = viewModel.scheduleUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val leftColWidth = 72.dp
    val topBarHeight = 40.dp
    val labelHeight = 32.dp
    val scheduleHeight = maxOf((Resources.getSystem()
        .displayMetrics
        .run{heightPixels / density}.dp -
            topBarHeight - labelHeight) / 24, 52.dp)
    val columnWidth = maxOf((Resources.getSystem()
        .displayMetrics
        .run{widthPixels / density}.dp -
            leftColWidth) / 7, 160.dp)
    
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    Column (modifier = modifier){
        ScheduleTopBar(
            modifier =Modifier
                .height(topBarHeight)
                .fillMaxWidth(),
            text = if(settingsUIState.value.americanDates)
                       "${screenUI.value.weekDate.toAmericanString()} - " +
                       screenUI.value.weekDate.addDays(6).toAmericanString()
                   else
                       "${screenUI.value.weekDate.toEuropeanString()} - " +
                       screenUI.value.weekDate.addDays(6).toEuropeanString(),
            prevAction = {
                viewModel.updateWeekDate(screenUI.value.weekDate.addDays(-7))
                         },
            nextAction = {
                viewModel.updateWeekDate(screenUI.value.weekDate.addDays(7))
            }
        )
        Row {
            HourMarkers(
                Modifier
                    .width(leftColWidth)
                    .verticalScroll(verticalScroll),
                labelHeight,
                scheduleHeight,
                settingsUIState.value.hour24
            )
            Column (
                modifier = Modifier.horizontalScroll(horizontalScroll)
            ){
                WeekDayLabels(
                    Modifier
                        .height(labelHeight)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    columnWidth
                )
                Box(
                    modifier = Modifier.verticalScroll(verticalScroll)
                ) {
                    WeeklyBackground(width = columnWidth, height = scheduleHeight)
                    WeeklyForeground(
                        width = columnWidth,
                        minuteHeight = scheduleHeight / 60,
                        date = screenUI.value.weekDate,
                        tasks = tasksUI.value.tasks,
                        events = eventsUI.value.events,
                        onDeleteTask = { coroutineScope.launch{ viewModel.deleteTask(it) } },
                        onEditTask = { viewModel.editTask(it) },
                        onDeleteEvent = { coroutineScope.launch{ viewModel.deleteEvent(it) } },
                        onEditEvent = { viewModel.editEvent(it) },
                        americanDates = settingsUIState.value.americanDates,
                        hour24 = settingsUIState.value.hour24,
                        redDeadlines = settingsUIState.value.redDeadline
                    )
                }
            }
        }
    }
}

@Composable
fun HourMarkers(
    modifier: Modifier = Modifier,
    firstHeight: Dp,
    height: Dp,
    hour24: Boolean
) {
    Column(modifier = modifier){
        Card(
            modifier = Modifier
                .height(firstHeight)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
            shape = RectangleShape,
            content = {}
        )
        for(i in 1..2){
            for(j in 1..12){
                Card(
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        ),
                    shape = RectangleShape
                ){
                    Box(
                        contentAlignment = Alignment.TopEnd
                    ){
                        var text: String
                        if(hour24){
                            text = "${j+12*(i-1)-1}:00"
                        }
                        else{
                            text = if (j == 1) "12:00 "
                            else "${j - 1}:00 "
                            text += if (i == 1) "a.m."
                            else "p.m."
                        }
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = text
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun WeeklyBackground(modifier: Modifier = Modifier, width: Dp, height: Dp){
    Row(modifier = modifier){
        for(j in 1..7){
            Column {
                for (i in 1..24) {
                    Box(
                        modifier = Modifier
                            .height(height)
                            .width(width)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            ),
                        content = {}
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyForeground(
    modifier: Modifier = Modifier,
    width: Dp, minuteHeight: Dp,
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
    Row (modifier = modifier){
        Weekday.values().forEach { weekday ->
            val d = date.addDays(weekday.ordinal)
            ForegroundColumn(
                modifier = Modifier.width(width),
                width = width,
                minuteHeight = minuteHeight,
                date = d,
                tasks = tasks.filter{it.deadline.sameDay(d)}.sortedBy {it.deadline},
                events = events.filter{it.startDate.sameDay(d)}.sortedBy {it.startDate},
                onDeleteTask = onDeleteTask,
                onEditTask = onEditTask,
                onDeleteEvent = onDeleteEvent,
                onEditEvent = onEditEvent,
                americanDates = americanDates,
                hour24 = hour24,
                redDeadlines = redDeadlines
            )
        }
    }
}
//Precondition: events and tasks are sorted
@Composable
fun ForegroundColumn(
    modifier: Modifier = Modifier,
    width: Dp,
    minuteHeight: Dp,
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
    Box(modifier = modifier){
        //Iterate through events
        var listSize = events.size
        var ndx = 0
        while(ndx  < listSize){
            //Put all events that overlap each other in one list
            val overlappingEvents = mutableListOf(events[ndx])
            while(ndx < listSize - 1 && events[ndx+1].startDate.before(events[ndx].endDate)){
                overlappingEvents.add(events[ndx+1])
                ndx++
            }
            for((count, e) in overlappingEvents.withIndex()) {
                val h = minuteHeight * e.startDate.minuteDifference(e.endDate)
                val w = if(overlappingEvents.size == 1) width - 2.dp
                        else (width - 2.dp) / 2
                val yOffset = minuteHeight * date.minuteDifference(e.startDate)
                val xOffset = if(count % 2 == 0) 1.dp else w
                EventBox(
                    modifier = Modifier
                        .height(h)
                        .width(w)
                        .offset(y = yOffset, x = xOffset),
                    event = e,
                    onDeleteEvent = onDeleteEvent,
                    onEditEvent = onEditEvent,
                    americanDates = americanDates,
                    hour24 = hour24
                )
            }
            ndx++
        }
        //Iterate through tasks
        listSize = tasks.size
        ndx = 0
        while(ndx < listSize){
            val t = tasks[ndx]
            val overlappingTasks = mutableListOf(t)
            //Add each task with the same deadline to a list together
            while(ndx < listSize - 1 && tasks[ndx+1].deadline == t.deadline){
                overlappingTasks.add(tasks[ndx+1])
                ndx++
            }
            //Display overlapping tasks
            TaskLine(
                modifier = Modifier
                    .fillMaxWidth(),
                offset = minuteHeight * date.minuteDifference(t.deadline),
                tasks = overlappingTasks,
                onDeleteTask = onDeleteTask,
                onEditTask = onEditTask,
                americanDates = americanDates,
                hour24 = hour24,
                redDeadlines = redDeadlines
            )
            ndx++
        }
    }
    val curr = MyDate()
    if(date.sameDay(curr)) {
        Card(
            modifier = Modifier
                .offset(y = minuteHeight * date.minuteDifference(curr))
                .height(15.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RectangleShape,
            content = {}
        )
    }
}
@Composable
fun TaskLine(
    modifier: Modifier = Modifier,
    offset: Dp,
    tasks: List<Task>,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    americanDates: Boolean,
    hour24: Boolean,
    redDeadlines:Boolean
){
    val isLate = tasks[0].deadline.hour.toInt() > 10 && tasks[0].deadline.isAfternoon
    var height by remember {mutableStateOf(0.dp)}
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .onGloballyPositioned {
                height = with(density) {
                    it.size.height.toDp()
                }
            }
            .offset(
                y = if (!isLate) offset else offset - height
            )
    ){
        Column{
            if(!isLate){
                Divider(
                    color = if(redDeadlines || tasks.size > 1) Color.Red
                            else tasks[0].color.toColor(),
                    thickness = 1.dp
                )
            }
            LazyColumn (
                modifier = Modifier
                    .heightIn(max = 40.dp)
            ){
                items(tasks){t ->
                    Box {
                        var expanded by remember{mutableStateOf(false)}
                        Text(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth()
                                .clickable {
                                    expanded = true
                                },
                            color = if (redDeadlines) Color.Red else t.color.toColor(),
                            text = t.name
                        )
                        TaskDropdown(
                            task = t,
                            expanded = expanded,
                            onDismissRequest = { expanded= false },
                            onDeleteTask = { onDeleteTask(it) },
                            onEditTask = { onEditTask(it) },
                            americanDates = americanDates,
                            hour24 = hour24
                        )
                    }
                }
            }
            if(isLate){
                Divider(
                    color = if(redDeadlines || tasks.size > 1) Color.Red
                            else tasks[0].color.toColor(),
                    thickness = 1.dp
                )
            }
        }
    }
}
@Composable
fun EventBox(
    modifier: Modifier = Modifier,
    event: Event,
    onDeleteEvent: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    americanDates: Boolean,
    hour24: Boolean
){
    Box(modifier = modifier) {
        var expanded by remember{mutableStateOf(false)}
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { expanded = true }
                ),
            colors = CardDefaults.cardColors(
                containerColor = event.color.toColor().copy(alpha = .8f),
                contentColor = if (event.color.isLight()) Color.Black
                else Color.White
            ),
            shape = RectangleShape,
            content = {
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = event.name
                )
            }
        )
        EventDropdown(
            event = event,
            expanded = expanded,
            onDismissRequest = { expanded = false },
            onDeleteEvent = { onDeleteEvent(it) },
            onEditEvent = { onEditEvent(it) },
            americanDates = americanDates,
            hour24 = hour24
        )
    }
}

/*
@Preview(showBackground = true)
@Composable
fun previewWeekly(){
    WeeklyScreen(
        viewModel = ScheduleViewModel(
            AgendaDatabase.getDatabase(LocalContext.current).getTaskDao(),
            AgendaDatabase.getDatabase(LocalContext.current).getEventDao(),
            updateTask = {},
            updateEvent = {},
            SettingsRepo(LocalContext.current.dataStore)
        )
    )
}*/