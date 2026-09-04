package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LobbyScreen
import com.example.ui.VoiceRoomScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PartyDarkBg
import com.example.viewmodel.VoiceRoomViewModel

enum class AppScreen {
    ROOM,
    LOBBY
}

class MainActivity : ComponentActivity() {
    private val viewModel: VoiceRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PartyDarkBg
                ) {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: VoiceRoomViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AppScreen.ROOM) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted && uiState.mySeatIndex >= 0 && uiState.isMicMuted) {
            viewModel.toggleMic(hasPermission = true) {}
        }
    }

    BackHandler(enabled = currentScreen == AppScreen.ROOM) {
        currentScreen = AppScreen.LOBBY
    }

    when (currentScreen) {
        AppScreen.ROOM -> {
            VoiceRoomScreen(
                viewModel = viewModel,
                uiState = uiState,
                hasAudioPermission = hasAudioPermission,
                onRequestAudioPermission = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onLeaveRoom = {
                    currentScreen = AppScreen.LOBBY
                }
            )
        }
        AppScreen.LOBBY -> {
            LobbyScreen(
                viewModel = viewModel,
                uiState = uiState,
                onEnterRoom = {
                    currentScreen = AppScreen.ROOM
                }
            )
        }
    }
}

