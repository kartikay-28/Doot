package com.doot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.doot.app.connection.ConnectionState
import com.doot.app.ui.ConnectScreen
import com.doot.app.ui.SplashScreen
import com.doot.app.ui.TalkScreen
import com.doot.app.ui.theme.DootTheme
import com.doot.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DootTheme {
                var splashStatus by remember { mutableStateOf("CHECKING PERMISSIONS...") }
                var showSplash by remember { mutableStateOf(true) }

                var permissionsGranted by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    permissionsGranted = true
                }

                // Request permissions on first launch
                LaunchedEffect(Unit) {
                    val perms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.NEARBY_WIFI_DEVICES,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } else {
                        arrayOf(
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    permissionLauncher.launch(perms)
                }

                var showSetup by remember { mutableStateOf(false) }

                // After permissions, show splash for a few seconds to simulate model loading
                LaunchedEffect(permissionsGranted) {
                    if (permissionsGranted) {
                        splashStatus = "LOADING SPEECH ENGINE..."
                        delay(1200)
                        splashStatus = "LOADING VOICE ENGINE..."
                        delay(1000)
                        splashStatus = "READY"
                        delay(500)
                        showSplash = false
                        if (!com.doot.app.speech.ModelAssets.isAnyLanguageInstalled(this@MainActivity)) {
                            showSetup = true
                        }
                    }
                }

                Crossfade(targetState = showSplash, animationSpec = tween(300), label = "splash") { isSplash ->
                    if (isSplash) {
                        SplashScreen(statusText = splashStatus)
                    } else if (showSetup) {
                        com.doot.app.ui.SetupScreen(
                            onDoneClick = { showSetup = false }
                        )
                    } else {
                        val appState by viewModel.appState.collectAsState()
                        val myTranscript by viewModel.myTranscript.collectAsState()
                        val receivedTranscript by viewModel.receivedTranscript.collectAsState()
                        val selectedLang by viewModel.selectedLang.collectAsState()
                        val connectionState by viewModel.connectionState.collectAsState()
                        val discoveredPeers by viewModel.discoveredPeers.collectAsState()
                        val peerStatus by viewModel.peerStatus.collectAsState()

                        if (appState == AppState.Disconnected) {
                            ConnectScreen(
                                connectionState = connectionState,
                                discoveredPeers = discoveredPeers,
                                onConnectClick = { viewModel.onConnectClicked() },
                                onPeerClick = { peer -> viewModel.onPeerClicked(peer) },
                                onManageLanguagesClick = { showSetup = true }
                            )
                        } else {
                            TalkScreen(
                                appState = appState,
                                myTranscript = myTranscript,
                                receivedTranscript = receivedTranscript,
                                selectedLang = selectedLang,
                                peerStatus = peerStatus,
                                onLangSelected = { viewModel.onLangToggle(it) },
                                onTalkPressed = { viewModel.onTalkPressed() },
                                onTalkReleased = { viewModel.onTalkReleased() },
                                onAlertClicked = { viewModel.onAlertClicked() },
                                onDisconnectClick = { viewModel.onDisconnectClicked() }
                            )
                        }
                    }
                }
            }
        }
    }
}
