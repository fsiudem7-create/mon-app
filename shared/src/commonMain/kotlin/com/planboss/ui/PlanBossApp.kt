package com.planboss.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.planboss.domain.TaskStatus

enum class AppTab { TODAY, TASKS, CALENDAR, FOCUS }

@Composable
fun PlanBossApp(viewModel: PlanBossViewModel = PlanBossViewModel()) {
    var tab by remember { mutableStateOf(AppTab.TODAY) }

    LaunchedEffect(Unit) { viewModel.seedDemoData() }

    MaterialTheme(colorScheme = planBossDarkScheme()) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF121417)) {
                    AppTab.entries.forEach {
                        NavigationBarItem(selected = it == tab, onClick = { tab = it }, label = { Text(it.name) }, icon = { Text("•") })
                    }
                }
            },
            containerColor = Color(0xFF0B0D10)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B0D10))
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("PlanBoss", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFE8EAED))
                Text(viewModel.disciplineMessage(), color = Color(0xFFA7F3D0), modifier = Modifier.padding(vertical = 8.dp))
                when (tab) {
                    AppTab.TODAY -> TodayScreen(viewModel)
                    AppTab.TASKS -> TasksScreen(viewModel)
                    AppTab.CALENDAR -> CalendarScreen(viewModel)
                    AppTab.FOCUS -> FocusScreen()
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(viewModel: PlanBossViewModel) {
    val tasks = viewModel.tasks.value
    val events = viewModel.events.value
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Prochain événement")
            Text(events.firstOrNull()?.title ?: "Journée claire. Planifie 1 action.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Snooze 10 min") })
                AssistChip(onClick = {}, label = { Text("Démarrer focus 25 min") })
            }
        }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tasks) { task ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text(task.title); Text(task.status.name) }
                    Button(onClick = {}) { Text("Done") }
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(viewModel: PlanBossViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(viewModel.tasks.value) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(it.title)
                    Text("Statut: ${it.status}")
                    Text("Source: ${it.source}")
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(viewModel: PlanBossViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(viewModel.events.value) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(it.title)
                    Text("${it.startDateTime} → ${it.endDateTime}")
                    Text(if (it.isCompleted) "Complété" else "À faire")
                }
            }
        }
    }
}

@Composable
private fun FocusScreen() {
    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mode Stopwatch")
            Text("Mode Pomodoro 25/5")
            Button(onClick = {}) { Text("Démarrer") }
        }
    }
}

@Composable
private fun planBossDarkScheme() = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF56D4FF),
    background = Color(0xFF0B0D10),
    surface = Color(0xFF121417),
    onPrimary = Color.Black,
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
)
