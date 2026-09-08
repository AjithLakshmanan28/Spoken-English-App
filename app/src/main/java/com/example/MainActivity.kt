package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DailyPhrasesScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.PronunciationScreen
import com.example.ui.screens.RoleplayScreen
import com.example.ui.screens.SavedPhrasesScreen
import com.example.ui.screens.ScenariosListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SpokenEnglishViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SpokenEnglishApp()
            }
        }
    }
}

@Composable
fun SpokenEnglishApp(
    viewModel: SpokenEnglishViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val showBottomNav = currentScreen != AppScreen.ROLEPLAY

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    modifier = Modifier.testTag("main_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SCENARIOS,
                        onClick = { viewModel.navigateTo(AppScreen.SCENARIOS) },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Scenarios") },
                        label = { Text("Practice") },
                        modifier = Modifier.testTag("nav_item_scenarios")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PRONUNCIATION,
                        onClick = { viewModel.navigateTo(AppScreen.PRONUNCIATION) },
                        icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Pronunciation") },
                        label = { Text("Pronounce") },
                        modifier = Modifier.testTag("nav_item_pronunciation")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DAILY_PHRASES || currentScreen == AppScreen.SAVED_PHRASES,
                        onClick = { viewModel.navigateTo(AppScreen.DAILY_PHRASES) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Daily Idioms") },
                        label = { Text("Idioms") },
                        modifier = Modifier.testTag("nav_item_daily_idioms")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROGRESS,
                        onClick = { viewModel.navigateTo(AppScreen.PROGRESS) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Progress") },
                        label = { Text("Progress") },
                        modifier = Modifier.testTag("nav_item_progress")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.SCENARIOS -> ScenariosListScreen(viewModel = viewModel)
                    AppScreen.ROLEPLAY -> RoleplayScreen(viewModel = viewModel)
                    AppScreen.PRONUNCIATION -> PronunciationScreen(viewModel = viewModel)
                    AppScreen.DAILY_PHRASES -> DailyPhrasesScreen(viewModel = viewModel)
                    AppScreen.SAVED_PHRASES -> SavedPhrasesScreen(viewModel = viewModel)
                    AppScreen.PROGRESS -> ProgressScreen(viewModel = viewModel)
                }
            }
        }
    }
}
