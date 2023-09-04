package com.example.totalplanner.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalplanner.R
import com.example.totalplanner.data.Weekday
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.Task
import java.net.CacheRequest

@Composable
fun WeekDayLabels(modifier: Modifier = Modifier, width: Dp){
    Row(
        modifier = modifier
    ){
        Weekday.values().forEach{
            Card(
                modifier = Modifier
                    .width(width)
                    .fillMaxHeight(),
                shape = RectangleShape,
                content = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = stringResource(it.dayName)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TaskDropdown(
    modifier: Modifier = Modifier,
    task: Task,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    americanDates: Boolean,
    hour24: Boolean
){
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ){
        Text(
            modifier = Modifier.padding(4.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            text = task.name
        )
        if(task.description != "")
            Text(
                modifier = Modifier.padding(4.dp),
                fontSize = 16.sp,
                text = task.description
            )
        Text(
            modifier = Modifier.padding(4.dp),
            fontSize = 16.sp,
            text =
                stringResource(R.string.deadline) +
                ": " + task.deadline.toString(americanDates, hour24)
        )
        DropdownButtons(
            onDelete = {
                onDeleteTask(task)
                onDismissRequest()
            },
            onEdit = {
                onEditTask(task)
                onDismissRequest()
            }
        )
    }
}
@Composable
fun EventDropdown(
    modifier: Modifier = Modifier,
    event: Event,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    americanDates: Boolean,
    hour24: Boolean
){
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ){
        Text(
            modifier = Modifier.padding(4.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            text = event.name
        )
        if(event.description != "")
            Text(
                modifier = Modifier.padding(4.dp),
                fontSize = 16.sp,
                text = event.description
            )
        Text(
            modifier = Modifier.padding(4.dp),
            fontSize = 16.sp,
            text =
                event.startDate.toString(americanDates, hour24) + " - " +
                        event.endDate.toString(americanDates, hour24)
        )
        DropdownButtons(
            onDelete = {
                onDeleteEvent(event)
                onDismissRequest()
            },
            onEdit = {
                onEditEvent(event)
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun DropdownButtons(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = onDelete,
            content = {Text(stringResource(R.string.delete))},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        )
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = onEdit,
            content = {Text(stringResource(R.string.edit))}
        )
    }
}