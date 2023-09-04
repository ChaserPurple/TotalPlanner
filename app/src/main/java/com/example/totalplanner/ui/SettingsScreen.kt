package com.example.totalplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.totalplanner.data.SettingsRepo
import com.example.totalplanner.data.room.AgendaDatabase
import com.example.totalplanner.ui.theme.TotalPlannerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
){
    val settingsState = viewModel.settingsUIState.collectAsState()

    Column(modifier = modifier.padding(16.dp)){
        SettingSwitchOption(
            modifier = Modifier.fillMaxWidth(),
            name = "Date Style",
            summary = "Format the dates such that the month is displayed first",
            checked = settingsState.value.americanDates,
            onCheckedChange = {viewModel.saveDateStyle(it)}
        )
        SettingSwitchOption(
            modifier = Modifier.fillMaxWidth(),
            name = "Use 24 Hours",
            summary = "Use the 24 hour system when displaying the time of day",
            checked = settingsState.value.hour24,
            onCheckedChange = {viewModel.saveTimeStyle(it)}
        )
        SettingSwitchOption(
            modifier = Modifier.fillMaxWidth(),
            name = "Red Deadlines",
            summary = "Show deadlines in the planner as red instead of their chosen color",
            checked = settingsState.value.redDeadline,
            onCheckedChange = {viewModel.saveRedDeadline(it)}
        )
    }
}

@Composable
fun SettingSwitchOption(
    modifier: Modifier = Modifier,
    name: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column{
            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                text = name
            )
            if(summary != null)
                Text(
                    fontSize = 12.sp,
                    text = summary
                )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview(){
    TotalPlannerTheme {
        Surface{
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel(
                    factory = SettingsViewModel.Factory
                )
            )
        }
    }
}