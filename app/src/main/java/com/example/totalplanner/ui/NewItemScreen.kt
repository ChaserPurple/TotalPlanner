package com.example.totalplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import com.example.totalplanner.R
import com.example.totalplanner.data.Month
import com.example.totalplanner.ui.theme.TotalPlannerTheme
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.room.AgendaDatabase
import com.example.totalplanner.data.room.Event
import com.example.totalplanner.data.room.Task
import com.example.totalplanner.data.toMyColor
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.launch

@Composable
fun NewItemScreen(
    windowSizeClass: WindowWidthSizeClass,
    submitNav: () -> Unit,
    viewModel: NewItemViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsUIState = viewModel.settingsUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val basePadding = 8.dp
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(basePadding * 2)
                .height(IntrinsicSize.Min)
        ) {
            //Top tabs to create either task or event
            if(uiState.updateID == -1)
                Row(modifier = Modifier.height(50.dp)) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RectangleShape,
                    onClick = { viewModel.updateMode(false) },
                    content = { Text(stringResource(R.string.event)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                        if (uiState.newTask)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        contentColor =
                        if (uiState.newTask)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onBackground
                    )
                )
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RectangleShape,
                    onClick = { viewModel.updateMode(true) },
                    content = { Text(stringResource(R.string.task)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                        if (!uiState.newTask)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        contentColor =
                        if (!uiState.newTask)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            Color.DarkGray
                    )
                )
            }
            when (windowSizeClass) {
                //SMALL SCREENS
                WindowWidthSizeClass.Compact -> {
                    if (uiState.newTask) {
                        Column(
                            modifier = Modifier.verticalScroll(
                                rememberScrollState()
                            )
                        ) {
                            NameAndDescription(
                                Modifier.padding(basePadding), 3,
                                uiState.name, { viewModel.updateName(it) },
                                uiState.desc, { viewModel.updateDesc(it) }
                            )
                            Text(
                                modifier = Modifier.padding(
                                    top = basePadding,
                                    start = basePadding,
                                    end = basePadding
                                ),
                                text = stringResource(R.string.deadline)
                            )
                            MyDatePicker(
                                modifier = Modifier.padding(
                                    top = basePadding / 2,
                                    start = basePadding,
                                    end = basePadding
                                ),
                                date = uiState.deadlineDate,
                                updateDate = {coroutineScope.launch{
                                    viewModel.updateDeadline(it)
                                }}
                            )
                            ColorDisplay(
                                modifier = Modifier.padding(basePadding),
                                color = uiState.color,
                                openDialogue = { viewModel.updateColorDialogue(true) }
                            )
                            EventList(
                                modifier = Modifier
                                    .padding(basePadding)
                                    .height(200.dp)
                                    .height(80.dp),
                                eventsList = uiState.taskEvents,
                                deadline = uiState.deadlineDate,
                                showDialogue = { first: MyDate, second: MyDate, ndx: Int ->
                                    viewModel.showDateDialogue(first, second, ndx)
                                },
                                popFromList = {e: Pair<Pair<MyDate, MyDate>,Int>, ndx: Int ->
                                    viewModel.confirmDeletion(e, ndx)
                                },
                                showAlert = { viewModel.updateInputAlert(true) },
                                periodCardHeight = 60.dp,
                                americanDates = settingsUIState.value.americanDates,
                                hour24 = settingsUIState.value.hour24
                            )
                        }
                    }
                    else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            NameAndDescription(
                                Modifier.padding(basePadding), 3,
                                uiState.name, { viewModel.updateName(it) },
                                uiState.desc, { viewModel.updateDesc(it) }
                            )
                            ColorDisplay(
                                modifier = Modifier.padding(basePadding),
                                color = uiState.color,
                                openDialogue = { viewModel.updateColorDialogue(true) }
                            )
                            EventDatePickers(
                                modifier = Modifier.padding(8.dp),
                                firstDate = uiState.startDate,
                                updateFirstDate = { viewModel.updateStart(it) },
                                secondDate = uiState.endDate,
                                updateSecondDate = { viewModel.updateEnd(it) }
                            )
                        }
                    }
                }
                //MEDIUM SCREENS
                WindowWidthSizeClass.Medium -> {
                    if (uiState.newTask) {
                        Row {
                            Column(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(IntrinsicSize.Min)
                            ) {
                                NameAndDescription(
                                    Modifier.padding(basePadding), 3,
                                    uiState.name, { viewModel.updateName(it) },
                                    uiState.desc, { viewModel.updateDesc(it) }
                                )
                                Text(
                                    modifier = Modifier.padding(
                                        top = basePadding,
                                        start = basePadding,
                                        end = basePadding
                                    ),
                                    text = stringResource(R.string.deadline)
                                )
                                MyDatePicker(
                                    modifier = Modifier.padding(
                                        top = basePadding / 2,
                                        start = basePadding,
                                        end = basePadding
                                    ),
                                    date = uiState.deadlineDate,
                                    updateDate = { date: MyDate ->
                                        coroutineScope.launch {
                                            viewModel.updateDeadline(d = date)
                                        }
                                    }
                                )
                                ColorDisplay(
                                    modifier = Modifier.padding(basePadding),
                                    color = uiState.color,
                                    openDialogue = {
                                        viewModel.updateColorDialogue(true)
                                    }
                                )
                            }
                            EventList(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(400.dp)
                                    .padding(basePadding),
                                eventsList = uiState.taskEvents,
                                deadline = uiState.deadlineDate,
                                showDialogue = { first: MyDate, second: MyDate, ndx: Int ->
                                    viewModel.showDateDialogue(
                                        first, second, ndx
                                    )
                                },
                                popFromList = {e: Pair<Pair<MyDate, MyDate>,Int>, ndx: Int ->
                                    viewModel.confirmDeletion(e, ndx)
                                },
                                showAlert = {
                                    viewModel.updateInputAlert(true)
                                },
                                periodCardHeight = 60.dp,
                                americanDates = settingsUIState.value.americanDates,
                                hour24 = settingsUIState.value.hour24
                            )
                        }
                    }
                    else {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                NameAndDescription(
                                    Modifier.padding(basePadding), 5,
                                    uiState.name, { viewModel.updateName(it) },
                                    uiState.desc, { viewModel.updateDesc(it) }
                                )
                                ColorDisplay(
                                    modifier = Modifier.padding(basePadding),
                                    color = uiState.color,
                                    openDialogue = { viewModel.updateColorDialogue(true) }
                                )
                            }
                            EventDatePickers(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .padding(8.dp),
                                firstDate = uiState.startDate,
                                updateFirstDate = { viewModel.updateStart(it) },
                                secondDate = uiState.endDate,
                                updateSecondDate = { viewModel.updateEnd(it) }
                            )
                        }
                    }
                }
                //LARGE SCREENS
                WindowWidthSizeClass.Expanded -> {
                    if (uiState.newTask) {
                        Row {
                            Column(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(IntrinsicSize.Min)
                            ) {
                                NameAndDescription(
                                    Modifier.padding(basePadding * 4), 3,
                                    uiState.name, { viewModel.updateName(it) },
                                    uiState.desc, { viewModel.updateDesc(it) }
                                )
                                Text(
                                    modifier = Modifier.padding(
                                        top = basePadding * 4,
                                        start = basePadding * 4,
                                        end = basePadding * 4
                                    ),
                                    text = stringResource(R.string.deadline)
                                )
                                MyDatePicker(
                                    modifier = Modifier.padding(
                                        top = basePadding / 2,
                                        start = basePadding,
                                        end = basePadding
                                    ),
                                    date = uiState.deadlineDate,
                                    updateDate = { date: MyDate ->
                                        coroutineScope.launch {
                                            viewModel.updateDeadline(d = date)
                                        }
                                    }
                                )
                                ColorDisplay(
                                    modifier = Modifier.padding(basePadding * 4),
                                    color = uiState.color,
                                    openDialogue = {
                                        viewModel.updateColorDialogue(true)
                                    }
                                )
                            }
                            EventList(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(400.dp)
                                    .padding(basePadding * 4),
                                eventsList = uiState.taskEvents,
                                deadline = uiState.deadlineDate,
                                showDialogue = { first: MyDate, second: MyDate, ndx: Int ->
                                    viewModel.showDateDialogue(
                                        first, second, ndx
                                    )
                                },
                                popFromList = {e: Pair<Pair<MyDate, MyDate>,Int>, ndx: Int ->
                                    viewModel.confirmDeletion(e, ndx)
                                },
                                showAlert = {
                                    viewModel.updateInputAlert(true)
                                },
                                periodCardHeight = 60.dp,
                                americanDates = settingsUIState.value.americanDates,
                                hour24 = settingsUIState.value.hour24
                            )
                        }
                    }
                    else {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                NameAndDescription(
                                    Modifier.padding(basePadding * 4), 5,
                                    uiState.name, { viewModel.updateName(it) },
                                    uiState.desc, { viewModel.updateDesc(it) }
                                )
                                ColorDisplay(
                                    modifier = Modifier.padding(basePadding * 4),
                                    color = uiState.color,
                                    openDialogue = { viewModel.updateColorDialogue(true) }
                                )
                            }
                            EventDatePickers(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .padding(8.dp),
                                firstDate = uiState.startDate,
                                updateFirstDate = { viewModel.updateStart(it) },
                                secondDate = uiState.endDate,
                                updateSecondDate = { viewModel.updateEnd(it) }
                            )
                        }
                    }
                }
            }
            //Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        coroutineScope.launch{
                            viewModel.restoreDeletions()
                            viewModel.reset(submitNav)
                        }
                    },
                    content = { Text(stringResource(R.string.cancel)) }
                )
                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        if (viewModel.noErrors()) {
                            if(uiState.updateID == -1){
                                if (uiState.newTask)
                                    coroutineScope.launch {
                                        viewModel.saveTask(
                                            task = Task(
                                                name = uiState.name,
                                                description = uiState.desc,
                                                deadline = uiState.deadlineDate,
                                                color = uiState.color.toMyColor()
                                            ),
                                            events = uiState.taskEvents
                                        )
                                    }
                                else
                                    coroutineScope.launch {
                                        viewModel.saveEvent(
                                            Event(
                                                name = uiState.name,
                                                description = uiState.desc,
                                                startDate = uiState.startDate,
                                                endDate = uiState.endDate,
                                                color = uiState.color.toMyColor(),
                                                taskID = -1
                                            )
                                        )
                                    }
                            }
                            else{
                                if (uiState.newTask)
                                    coroutineScope.launch {
                                        viewModel.updateTask(
                                            task = Task(
                                                id = uiState.updateID,
                                                name = uiState.name,
                                                description = uiState.desc,
                                                deadline = uiState.deadlineDate,
                                                color = uiState.color.toMyColor()
                                            ),
                                            eventsList = uiState.taskEvents
                                        )
                                    }
                                else
                                    coroutineScope.launch {
                                        viewModel.updateEvent(
                                            event = Event(
                                                id = uiState.updateID,
                                                name = uiState.name,
                                                description = uiState.desc,
                                                startDate = uiState.startDate,
                                                endDate = uiState.endDate,
                                                color = uiState.color.toMyColor(),
                                                taskID = -1
                                            )
                                        )
                                    }
                            }
                            viewModel.reset(submitNav)
                        } else
                            viewModel.updateInputAlert(true)
                    },
                    content = { Text(stringResource(R.string.confirm)) }
                )
            }
        }
        if (uiState.showInput) {
            IncorrectInputAlert(
                modifier = Modifier.padding(20.dp),
                dismissDialogue = { viewModel.updateInputAlert(false) }
            )
        }
        else if (uiState.showDatesDialogue) {
            NewEventDialogue(
                modifier = Modifier.padding(20.dp),
                deadline = uiState.deadlineDate,
                firstDate = uiState.startDate,
                updateFirstDate = { viewModel.updateStart(it) },
                secondDate = uiState.endDate,
                updateSecondDate = { viewModel.updateEnd(it) },
                dismissDialogue = { viewModel.dismissDatesDialogue() },
                confirmChoice = { viewModel.confirmDatesChoice() }
            )
        }
        //NOTE: Anything used to display a color picker was created by skydoves
        else if (uiState.showColorDialogue) {
            ColorPickerDialogue(
                modifier = Modifier.padding(20.dp),
                updateColor = { viewModel.updateColor(it) },
                dismissDialogue = { viewModel.updateColorDialogue(false) }
            )
        }
        else if (uiState.showConfirmationDialogue) {
            ConfirmDeleteDialogue(
                modifier = Modifier.padding(20.dp),
                pair = uiState.taskEvents[uiState.periodNdx],
                performDeletion = { coroutineScope.launch{
                    viewModel.removePeriod(it)
                } },
                dismissDialogue = { viewModel.dismissConfirmDialogue() },
                americanDates = settingsUIState.value.americanDates,
                hour24 = settingsUIState.value.hour24
            )
        }
    }
}

@Composable
fun EarlyDateWarning() {
    Text(
        text = stringResource(R.string.earlyDateWarning),
        color = Color.Red
    )
}
@Composable
fun InvalidDateWarning() {
    Text(
        text = stringResource(R.string.badDateWarning),
        color = Color.Red
    )
}
@Composable
fun DateOrderWarning() {
    Text(
        text = stringResource(R.string.dateOrderWarning),
        color = Color.Red
    )
}
@Composable
fun NameWarning() {
    Text(
        text = stringResource(R.string.noNameWarning),
        color = Color.Red
    )
}
@Composable
fun DeadlineWarning() {
    Text(
        text = stringResource(R.string.afterDeadWarning),
        color = Color.Red
    )
}
@Composable
fun DiffDayWarning() {
    Text(
        text = stringResource(R.string.diffDayWarning),
        color = Color.Red
    )
}

@Composable
fun IncorrectInputAlert(
    modifier:Modifier = Modifier,
    dismissDialogue: () -> Unit
){
    Dialog(onDismissRequest = dismissDialogue) {
        Card(modifier = modifier) {
            Text(
                modifier = Modifier.padding(4.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = stringResource(R.string.error)
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = stringResource(R.string.inputValidAnswers)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = dismissDialogue
                ){
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Composable
fun NameAndDescription(
    modifier: Modifier = Modifier, minLines: Int,
    name: String, nameChange: (String) -> Unit,
    desc: String, descChange: (String) -> Unit
){
    Column (modifier = modifier) {
        TextField(
            value = name,
            onValueChange = {
                nameChange(it)
            },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        if(name == "")
            NameWarning()
        TextField(
            value = desc,
            onValueChange = descChange,
            label = {Text(stringResource(R.string.desc) + stringResource(R.string.optional))},
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            minLines = minLines
        )
    }
}

@Composable
fun ConfirmDeleteDialogue(
    modifier: Modifier = Modifier,
    pair: Pair<Pair<MyDate, MyDate>,Int>,
    performDeletion: (Pair<Pair<MyDate,MyDate>, Int>) -> Unit,
    dismissDialogue: () -> Unit,
    americanDates: Boolean,
    hour24: Boolean
){
    Dialog(
        onDismissRequest = dismissDialogue
    ){
        Card(modifier = modifier){
            Text(
                modifier = Modifier.padding(4.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = stringResource(R.string.wait)
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = stringResource(R.string.removePeriodRequest)
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = pair.first.first.toString(americanDates, hour24)
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = pair.first.second.toString(americanDates, hour24)
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ){
                OutlinedButton(
                    modifier = Modifier.padding(4.dp),
                    content = { Text(text = stringResource(R.string.cancel)) },
                    onClick = dismissDialogue
                )
                Button(
                    modifier = Modifier.padding(4.dp),
                    content = { Text(text = stringResource(R.string.confirm)) },
                    onClick = {
                        performDeletion(pair)
                        dismissDialogue()
                    }
                )
            }
        }
    }
}
@Composable
fun NewEventDialogue(
    modifier:Modifier = Modifier,
    firstDate:MyDate = MyDate(),
    updateFirstDate: (MyDate) -> Unit,
    secondDate: MyDate = MyDate(),
    updateSecondDate: (MyDate) -> Unit,
    deadline: MyDate,
    dismissDialogue: () -> Unit,
    confirmChoice: () -> Unit
){
    Dialog(
        onDismissRequest = dismissDialogue
    ) {
        Card(
            modifier = modifier,
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ){
                EventDatePickers(
                    modifier = Modifier.padding(8.dp),
                    deadline = deadline,
                    firstDate = firstDate,
                    updateFirstDate = updateFirstDate,
                    secondDate = secondDate,
                    updateSecondDate = updateSecondDate,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        modifier = Modifier.padding(4.dp),
                        content = { Text(text = stringResource(R.string.cancel)) },
                        onClick = dismissDialogue
                    )
                    Button(
                        modifier = Modifier.padding(4.dp),
                        content = { Text(text = stringResource(R.string.confirm)) },
                        onClick = {
                            if (firstDate.isFuture() && secondDate.isFuture() &&
                                firstDate.isValid() && secondDate.isValid() &&
                                !secondDate.after(deadline) &&
                                firstDate.before(secondDate) &&
                                firstDate.sameDay(secondDate)
                            ) {
                                confirmChoice()
                                dismissDialogue()
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun EventDatePickers(
    modifier: Modifier = Modifier,
    deadline: MyDate? = null,
    firstDate:MyDate,
    updateFirstDate: (MyDate) -> Unit,
    secondDate: MyDate,
    updateSecondDate: (MyDate) -> Unit
){
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = stringResource(R.string.whenEventStart)
        )
        MyDatePicker(
            modifier = Modifier.padding(4.dp),
            date = firstDate,
            updateDate = updateFirstDate
        )
        Text(
            modifier = Modifier.padding(4.dp),
            text = stringResource(R.string.whenEventEnd)
        )
        MyDatePicker(
            modifier = Modifier.padding(4.dp),
            date = secondDate,
            updateDate = updateSecondDate
        )
        if(firstDate.isValid() && secondDate.isValid() ){
            if(!secondDate.after(firstDate))
                DateOrderWarning()
            else if(!firstDate.sameDay(secondDate))
                DiffDayWarning()
            else if (deadline != null && secondDate.after(deadline))
                DeadlineWarning()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePicker(
    modifier: Modifier = Modifier,
    date: MyDate,
    updateDate: (MyDate) -> Unit
){
    Column (modifier = modifier) {
        //DATE
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(stringResource(R.string.date) + ":")
            //DAY
            TextField(
                modifier = Modifier.weight(1f),
                label = {Text(stringResource(R.string.day))},
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                value = date.day,
                onValueChange = {
                    if(it == "" || it.isDigitsOnly())
                        updateDate(date.copy(day = it))
                },
            )
            //MONTH
            var monthExpanded by remember{ mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
                modifier = Modifier.weight(2f)
            ){
                TextField(
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = stringResource(date.month.monthName),
                    onValueChange = {},
                    label = {Text(stringResource(R.string.month))}
                )
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }
                ) {
                    Month.values().forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.monthName)) },
                            onClick = {
                                updateDate(
                                    date.copy(month = it)
                                )
                                monthExpanded = false
                            }
                        )
                    }
                }
            }
            //YEAR
            TextField(
                modifier = Modifier.weight(1.25f),
                label = {Text(stringResource(R.string.year))},
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                value = date.year,
                onValueChange = {
                    if(it == "" || it.isDigitsOnly())
                        updateDate(date.copy(year = it))
                },
            )
        }
        //TIME
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(stringResource(R.string.time) + ":")
            //HOUR
            TextField(
                modifier = Modifier.weight(1f),
                label = {Text(stringResource(R.string.hour))},
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                value = date.hour,
                onValueChange = {
                    if(it == "" || it.isDigitsOnly())
                        updateDate(date.copy(hour = it))
                },
            )
            Text(":")
            //MINUTE
            TextField(
                modifier = Modifier.weight(1.25f),
                label = {Text(stringResource(R.string.minute))},
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                value = date.minute,
                onValueChange = {
                    if(it == "" || it.isDigitsOnly())
                        updateDate(date.copy(minute = it))
                },
            )
            //NOON
            var timeExpanded by remember{mutableStateOf(false)}
            ExposedDropdownMenuBox(
                expanded = timeExpanded,
                onExpandedChange = { timeExpanded = !timeExpanded },
                modifier = Modifier.weight(1f)
            ){
                TextField(
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = if(date.isAfternoon) "PM" else "AM",
                    onValueChange = {},
                )
                ExposedDropdownMenu(
                    expanded = timeExpanded,
                    onDismissRequest = { timeExpanded = false }
                ) {
                    //Leave the raw string since it shouldn't be translated
                    DropdownMenuItem(
                        text = { Text("AM") },
                        onClick = {
                            updateDate(date.copy(isAfternoon = false))
                            timeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("PM") },
                        onClick = {
                            updateDate(date.copy(isAfternoon = true))
                            timeExpanded = false
                        }
                    )
                }
            }
        }
        if(!date.isValid())
            InvalidDateWarning()
        else if(!date.isFuture())
            EarlyDateWarning()
    }
}
@Composable
fun EventList(
    modifier: Modifier = Modifier,
    deadline: MyDate,
    eventsList: List<Pair<Pair<MyDate, MyDate>, Int>>,
    showDialogue: (MyDate, MyDate, Int) -> Unit,
    popFromList: (e: Pair<Pair<MyDate, MyDate>, Int>, ndx: Int) -> Unit,
    showAlert: () -> Unit,
    periodCardHeight: Dp,
    americanDates: Boolean,
    hour24: Boolean
){
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ){
        //New Event Button
        OutlinedButton(
            modifier= Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            content = {
                Text(stringResource(R.string.addWorkTime))
            },
            onClick = {
                if(deadline.isValid() && deadline.isFuture())
                    showDialogue(MyDate(), MyDate(), -1)
                else
                    showAlert()
            }
        )
        //List of Events Already Made
        LazyColumn {
            items(eventsList) { event ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(periodCardHeight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        //Info Display
                        Column (modifier = Modifier
                            .weight(4.5f)
                            .padding(4.dp)){
                            Text(
                                fontSize = 12.sp,
                                text = event.first.first.toString(americanDates, hour24)
                            )
                            Text(
                                fontSize = 12.sp,
                                text = event.first.second.toString(americanDates, hour24)
                            )
                        }
                        //Edit Button
                        Card(
                            modifier = Modifier
                                .weight(1f, true)
                                .fillMaxHeight()
                                .clickable {
                                    if (deadline.isValid()) {
                                        showDialogue(
                                            event.first.first, event.first.second,
                                            eventsList.indexOf(event)
                                        )
                                    } else
                                        showAlert()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RectangleShape
                        ){
                            Box (
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_edit),
                                    contentDescription = stringResource(R.string.edit),
                                    modifier = Modifier.size(periodCardHeight * 2 / 3),
                                    tint = Color.White
                                )
                            }
                        }
                        //Delete Button
                        Card(
                            modifier = Modifier
                                .weight(1f, true)
                                .fillMaxHeight()
                                .clickable {
                                    popFromList(event, eventsList.indexOf(event))
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.Red),
                            shape = RectangleShape
                        ){
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_delete),
                                    contentDescription = stringResource(R.string.delete),
                                    modifier = Modifier.size(periodCardHeight * 2 / 3),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialogue(
    modifier: Modifier = Modifier,
    updateColor: (Color) -> Unit,
    dismissDialogue : () -> Unit
){
    Dialog(
        onDismissRequest = dismissDialogue
    ){
        Card (modifier = modifier){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ColorPicker(
                    modifier = Modifier.padding(4.dp),
                    updateColor = updateColor
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    modifier = Modifier.padding(4.dp),
                    content = { Text(text = stringResource(R.string.confirm)) },
                    onClick = dismissDialogue
                )
            }
        }
    }
}
@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    updateColor: (Color) -> Unit
){
    Column(
        modifier = modifier
    ) {
        val controller = rememberColorPickerController()
        HsvColorPicker(
            modifier = Modifier
                .aspectRatio(.85f)
                .padding(8.dp),
            controller = controller,
            onColorChanged = {updateColor(it.color)}
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(5.25f)
            .padding(8.dp)){
            AlphaTile(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(4.dp),
                controller = controller
            )
            BrightnessSlider(
                modifier = Modifier
                    .weight(6f)
                    .aspectRatio(5.25f)
                    .padding(4.dp),
                controller = controller,
            )
        }
    }
}
@Composable
fun ColorDisplay(
    modifier: Modifier = Modifier,
    color: Color,
    openDialogue: () -> Unit
){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = "Color:"
        )
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    RoundedCornerShape(4.dp)
                )
                .height(32.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clickable { openDialogue() },
                shape = RectangleShape,
                colors = CardDefaults.cardColors(containerColor = color),
                content = {}
            )
        }
    }
}

/*@Preview(showBackground = true, widthDp = 600)
@Composable
fun SurveyPreview() {
    TotalPlannerTheme {
        Surface{
            NewItemScreen(
                windowSizeClass = WindowWidthSizeClass.Medium,
                {},
                modifier = Modifier.padding(24.dp),
                viewModel = NewItemViewModel(
                    AgendaDatabase.getDatabase(LocalContext.current).getTaskDao(),
                    AgendaDatabase.getDatabase(LocalContext.current).getEventDao(),
                )
            )
            /*NewEventDialogue(
                updateFirstDate = {},
                updateSecondDate = {},
                dismissDialogue = {}) {
            }*/
            //ColorPickerDialogue(updateColor = {}) {}
        }
    }
}*/