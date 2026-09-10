package com.doot.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doot.app.speech.DownloadState
import com.doot.app.ui.theme.*
import com.doot.app.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    onDoneClick: () -> Unit,
    viewModel: SetupViewModel = viewModel()
) {
    val languages by viewModel.languages.collectAsState()
    val hasAnyInstalled = languages.any { it.isInstalled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = "LANGUAGE SETUP",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Download offline speech models (~250MB per language). Models are required for offline voice relay.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Language List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            languages.forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .background(BgPanel)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        when (val state = lang.downloadState) {
                            is DownloadState.Downloading -> {
                                Text(state.label, color = AccentAmber, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = state.progress,
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = AccentAmber,
                                    trackColor = BgPanelAlt,
                                )
                            }
                            is DownloadState.Extracting -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⏳", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(state.label, color = AccentTeal, fontSize = 12.sp)
                                }
                            }
                            is DownloadState.Error -> {
                                Text("Failed: ${state.error}", color = AccentRed, fontSize = 12.sp)
                            }
                            else -> {
                                if (lang.isInstalled) {
                                    Text("Installed", color = AccentTeal, fontSize = 12.sp)
                                } else {
                                    Text("Not installed (~250MB)", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (lang.downloadState is DownloadState.Idle || lang.downloadState is DownloadState.Error || lang.downloadState is DownloadState.Done) {
                        if (!lang.isInstalled) {
                            Button(
                                onClick = { viewModel.downloadLanguage(lang.code) },
                                colors = ButtonDefaults.buttonColors(containerColor = BgPanelAlt, contentColor = TextPrimary),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Download")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.deleteLanguage(lang.code) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = AccentRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onDoneClick,
            enabled = hasAnyInstalled,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentTeal,
                contentColor = BgBase,
                disabledContainerColor = BgPanelAlt,
                disabledContentColor = TextDisabled
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (hasAnyInstalled) "Continue" else "Download at least one language to continue", fontWeight = FontWeight.SemiBold)
        }
    }
}
