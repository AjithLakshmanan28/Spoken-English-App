package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PronunciationDrillRepository
import com.example.data.model.TongueTwisterRepository
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SpokenEnglishViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PronunciationScreen(
    viewModel: SpokenEnglishViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categoryIndex by viewModel.drillCategoryIndex.collectAsState()
    val currentDrillIdx by viewModel.currentDrillIndex.collectAsState()
    val currentTwisterIdx by viewModel.currentTwisterIndex.collectAsState()
    val isListening by viewModel.isDrillListening.collectAsState()
    val drillTranscript by viewModel.drillTranscript.collectAsState()
    val drillScore by viewModel.drillScore.collectAsState()
    val playingMessageId by viewModel.playingMessageId.collectAsState()

    val currentDrill = PronunciationDrillRepository.drills[currentDrillIdx]
    val currentTwister = TongueTwisterRepository.twisters[currentTwisterIdx]

    val activeTargetPhrase = if (categoryIndex == 0) currentDrill.targetPhrase else currentTwister.text

    // Mic permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningForDrill(activeTargetPhrase)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Pronunciation Clinic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SCENARIOS) },
                    modifier = Modifier.testTag("pronunciation_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Segmented Tab Row
        SecondaryTabRow(
            selectedTabIndex = categoryIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = categoryIndex == 0,
                onClick = { viewModel.setDrillCategory(0) },
                text = { Text("Phoneme Drills (${PronunciationDrillRepository.drills.size})") },
                modifier = Modifier.testTag("tab_phoneme_drills")
            )
            Tab(
                selected = categoryIndex == 1,
                onClick = { viewModel.setDrillCategory(1) },
                text = { Text("Tongue Twisters (${TongueTwisterRepository.twisters.size})") },
                modifier = Modifier.testTag("tab_tongue_twisters")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (categoryIndex == 0) {
                // --- PHONEME DRILLS CONTENT ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CoralSecondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentDrill.targetSound,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${currentDrillIdx + 1} of ${PronunciationDrillRepository.drills.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = currentDrill.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Target Phrase
                            Text(
                                text = "“${currentDrill.targetPhrase}”",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                lineHeight = 30.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phonetic IPA
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = currentDrill.phoneticIpa,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Syllable breakdown chips
                            Text(
                                text = "Syllable Rhythm Breakdown:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                currentDrill.syllables.forEach { syllable ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = syllable,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Listen to Native Model Audio Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.playTts("drill_${currentDrill.id}", currentDrill.targetPhrase)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("listen_drill_tts_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IndigoPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (playingMessageId == "drill_${currentDrill.id}") Icons.Default.Stop else Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Listen Native Model")
                                }
                            }
                        }
                    }
                }

                // Coach Phonetic Tips
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TealTertiary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealTertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TealTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Articulatory Position Tip",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TealTertiary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentDrill.tips,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // --- TONGUE TWISTERS CONTENT ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Twister: ${currentTwister.difficulty}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${currentTwisterIdx + 1} of ${TongueTwisterRepository.twisters.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = currentTwister.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "“${currentTwister.text}”",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = CoralSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = currentTwister.focusSound,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.playTts("twister_${currentTwister.id}", currentTwister.text)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("listen_twister_tts_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IndigoPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (playingMessageId == "twister_${currentTwister.id}") Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Listen at Normal Speed")
                            }
                        }
                    }
                }
            }

            // --- PRACTICE & EVALUATION SECTION ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Test Your Pronunciation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the mic and read the phrase aloud clearly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Mic Action Button
                        Button(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopListeningForDrill()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startListeningForDrill(activeTargetPhrase)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) CoralSecondary else IndigoPrimary
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("drill_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop" else "Speak",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        if (isListening) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AudioWaveformVisualizer(
                                isSpeaking = true,
                                barColor = CoralSecondary,
                                barCount = 5
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (drillTranscript.isNotBlank()) drillTranscript else "Listening to your voice...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = CoralSecondary
                            )
                        }

                        // Pronunciation Accuracy Results
                        if (drillScore != null) {
                            val score = drillScore!!
                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pronunciation Accuracy",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "${score.accuracyPercentage}%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                score.accuracyPercentage >= 80 -> SuccessGreen
                                                score.accuracyPercentage >= 60 -> WarningAmber
                                                else -> CoralSecondary
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LinearProgressIndicator(
                                        progress = { score.accuracyPercentage / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = when {
                                            score.accuracyPercentage >= 80 -> SuccessGreen
                                            score.accuracyPercentage >= 60 -> WarningAmber
                                            else -> CoralSecondary
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Word-by-word match
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        score.wordResults.forEach { wordRes ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (wordRes.isMatched) {
                                                    SuccessGreen.copy(alpha = 0.15f)
                                                } else {
                                                    CoralSecondary.copy(alpha = 0.15f)
                                                }
                                            ) {
                                                Text(
                                                    text = wordRes.targetWord,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (wordRes.isMatched) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (wordRes.isMatched) {
                                                        Color(0xFF047857)
                                                    } else {
                                                        CoralSecondary
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = score.feedbackMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Prev / Next Navigation Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (categoryIndex == 0) viewModel.prevDrill() else viewModel.prevTwister()
                        },
                        modifier = Modifier.testTag("drill_prev_btn")
                    ) {
                        Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (categoryIndex == 0) viewModel.nextDrill() else viewModel.nextTwister()
                        },
                        modifier = Modifier.testTag("drill_next_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndigoPrimary
                        )
                    ) {
                        Text("Next Drill")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    }
}
