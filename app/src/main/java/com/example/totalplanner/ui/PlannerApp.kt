package com.example.totalplanner.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.totalplanner.R
import com.example.totalplanner.data.MyDate
import com.example.totalplanner.data.room.AgendaDatabase
import com.example.totalplanner.data.room.EventDAO
import com.example.totalplanner.data.room.TaskDAO
import com.example.totalplanner.ui.theme.TotalPlannerTheme
import kotlinx.coroutines.launch

enum class Routes{
    NEW_ITEM, MONTHLY, WEEKLY, SETTINGS
}
@Composable
fun PlannerBottomBar(
    weeklyAct:()->Unit,
    monthlyAct:()->Unit,
    newAct:()->Unit,
    settingsAct: ()->Unit,
    modifier: Modifier = Modifier)
{
    BottomAppBar(modifier = modifier){
        IconButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = weeklyAct
        ){
            Icon(
                painter = painterResource(R.drawable.weekly_icon),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Weekly View"
            )
        }
        IconButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = monthlyAct
        ){
            Icon(
                painter = painterResource(R.drawable.monthly_icon),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Weekly View"
            )
        }
        IconButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = settingsAct
        ){
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Weekly View"
            )
        }
        Spacer(Modifier.weight(3f))
        FloatingActionButton(
            modifier = Modifier.weight(1f),
            onClick = newAct
        ){
            Icon(
                painter = painterResource(R.drawable.plus_icon),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Weekly View"

            )
        }
    }
}

@Composable
fun PlannerApp(
    windowSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
){
    val db = AgendaDatabase.getDatabase(LocalContext.current)
    val taskDAO: TaskDAO = db.getTaskDao()
    val eventDAO: EventDAO = db.getEventDao()

    val coroutineScope = rememberCoroutineScope()

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    )
    val newItemViewModel = NewItemViewModel(
        taskDAO = taskDAO,
        eventDAO = eventDAO,
        settingsRepo = settingsViewModel.settingsRepo
    )
    val scheduleViewModel = ScheduleViewModel(
        taskDAO = taskDAO,
        eventDAO = eventDAO,
        updateTask = {
            Log.d("taskID", it.id.toString())
            coroutineScope.launch {
                newItemViewModel.initVals(
                    eventsList = eventDAO.getEventOfTask(it.id).map { event ->
                        Pair(Pair(event.startDate, event.endDate), event.id)
                    } as MutableList<Pair<Pair<MyDate, MyDate>, Int>>,
                    task = it,
                    event = null
                )
                navController.navigate(Routes.NEW_ITEM.name)
            }
        },
        updateEvent = {
            if(it.taskID == -1L){
                newItemViewModel.initVals(
                    eventsList = mutableListOf(),
                    task = null,
                    event = it
                )
                navController.navigate(Routes.NEW_ITEM.name)
            }
            else{
                coroutineScope.launch{
                    val task = taskDAO.getTaskByID(it.taskID.toInt())
                    newItemViewModel.initVals(
                        eventsList = eventDAO.getEventOfTask(task.id).map {event ->
                            Pair(Pair(event.startDate, event.endDate), event.id)
                        } as MutableList<Pair<Pair<MyDate, MyDate>, Int>>,
                        task = task,
                        event = null
                    )
                    navController.navigate(Routes.NEW_ITEM.name)
                }
            }
        },
        settingsRepo = settingsViewModel.settingsRepo
    )

    Scaffold(
        bottomBar = { PlannerBottomBar(
            weeklyAct = {
                navController.navigate(Routes.WEEKLY.name)
                scheduleViewModel.resetScreen()
            },
            monthlyAct = {
                navController.navigate(Routes.MONTHLY.name)
                scheduleViewModel.resetScreen()
            },
            settingsAct = { navController.navigate(Routes.SETTINGS.name) },
            newAct = {
                newItemViewModel.initVals(
                    eventsList = mutableListOf(),
                    event = null,
                    task = null
                )
                navController.navigate(Routes.NEW_ITEM.name)
            }
        )},
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.WEEKLY.name,
                modifier = modifier.padding(innerPadding)
            ){
                composable(route = Routes.NEW_ITEM.name) {
                    NewItemScreen(
                        windowSizeClass = windowSizeClass,
                        submitNav = {navController.navigate(Routes.WEEKLY.name)},
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        viewModel = newItemViewModel
                    )
                }
                composable(route = Routes.MONTHLY.name) {
                    MonthlyScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = scheduleViewModel
                    )
                }
                composable(route = Routes.WEEKLY.name) {
                    WeeklyScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = scheduleViewModel
                    )
                }
                composable(route = Routes.SETTINGS.name) {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = settingsViewModel
                    )
                }
            }
            BackHandler(onBack = {navController.popBackStack()})
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TotalPlannerTheme {
        Surface{
            PlannerApp(
                windowSizeClass = WindowWidthSizeClass.Compact,
                modifier = Modifier.fillMaxSize()
            )
            /*NewEventDialogue(
                updateFirstDate = {},
                updateSecondDate = {},
                dismissDialogue = {}) {
            }*/
        }
    }
}