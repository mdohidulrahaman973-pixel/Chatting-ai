package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.ArcReactorHologram
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.DialogueFeed
import com.example.ui.components.HudTelemetryHeader
import com.example.ui.components.TacticalControlBar
import com.example.ui.components.TacticalDirectiveChips
import com.example.ui.components.TacticalSettingsSheet
import com.example.ui.components.TacticalSetupWizardModal
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GridOverlay
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.LyraTheme
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.SipraStatus
import com.example.viewmodel.SipraViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SipraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LyraTheme {
                LyraHudScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LyraHudScreen(viewModel: SipraViewModel) {
    val context = LocalContext.current

    val status by viewModel.status.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val liveSpeechText by viewModel.liveSpeechText.collectAsState()
    val userTitle by viewModel.userTitle.collectAsState()
    val isContinuousMode by viewModel.isContinuousMode.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val systemStats by viewModel.systemStats.collectAsState()
    val statusNotice by viewModel.statusNotice.collectAsState()
    val combinedAmplitude by viewModel.combinedAmplitude.collectAsState()
    val availableVoices by viewModel.availableVoices.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val autoListenOnSpeechEnd by viewModel.autoListenOnSpeechEnd.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSetupWizard by remember { mutableStateOf(false) }

    // Check if acoustic permission is granted on startup; if not, open 6-step setup wizard
    var hasCheckedInitialPermissions by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasCheckedInitialPermissions) {
            hasCheckedInitialPermissions = true
            val micGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (!micGranted) {
                showSetupWizard = true
            }
        }
    }

    // Animatable 1: Ambient atmospheric backdrop respiration scale & glow
    val ambientAtmosphereScale = remember { Animatable(1f) }
    val ambientAtmosphereGlow = remember { Animatable(0.14f) }

    // Animatable 2: Status pill subtle breathing scale & border glow
    val statusPillScale = remember { Animatable(1f) }
    val statusPillBorderAlpha = remember { Animatable(0.35f) }

    LaunchedEffect(status) {
        val (cycleDuration, minScale, maxScale, minGlow, maxGlow) = when (status) {
            SipraStatus.IDLE -> HexMainCadence(3200, 0.96f, 1.05f, 0.10f, 0.22f)
            SipraStatus.LISTENING -> HexMainCadence(1200, 0.94f, 1.08f, 0.18f, 0.35f)
            SipraStatus.PROCESSING -> HexMainCadence(800, 0.92f, 1.10f, 0.20f, 0.38f)
            SipraStatus.SPEAKING -> HexMainCadence(1400, 0.95f, 1.07f, 0.15f, 0.30f)
            SipraStatus.ERROR -> HexMainCadence(600, 0.93f, 1.06f, 0.12f, 0.30f)
        }
        val half = cycleDuration / 2

        // Ambient atmosphere breathing
        launch {
            while (isActive) {
                ambientAtmosphereScale.animateTo(
                    targetValue = maxScale,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                ambientAtmosphereScale.animateTo(
                    targetValue = minScale,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                ambientAtmosphereGlow.animateTo(
                    targetValue = maxGlow,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                ambientAtmosphereGlow.animateTo(
                    targetValue = minGlow,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        // Status pill breathing
        launch {
            while (isActive) {
                statusPillScale.animateTo(
                    targetValue = 1.03f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                statusPillScale.animateTo(
                    targetValue = 0.97f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                statusPillBorderAlpha.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                statusPillBorderAlpha.animateTo(
                    targetValue = 0.30f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Audio recording permission launcher
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(
                context,
                "Microphone permission required for vocal directives.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val onMicAction: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleVoiceInput()
        } else {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lyra_hud_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            TacticalControlBar(
                status = status,
                liveSpeechText = liveSpeechText,
                onMicClick = onMicAction,
                onSubmitText = { text -> viewModel.processUserSpeech(text) },
                onClearFeed = { viewModel.clearLog() }
            )
        }
    ) { innerPadding ->
        val topInsets = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBlack)
                .drawBehind {
                    // Draw subtle tactical HUD grid background lines
                    val step = 48.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = GridOverlay,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = GridOverlay,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }

                    // Ambient cyan glow centered behind Arc Reactor pulsing via Animatable
                    val currentAtmosphereRadius = size.width * 0.55f * ambientAtmosphereScale.value
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanGlow.copy(alpha = ambientAtmosphereGlow.value),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height * 0.28f),
                            radius = currentAtmosphereRadius
                        ),
                        radius = currentAtmosphereRadius,
                        center = Offset(size.width / 2f, size.height * 0.28f)
                    )
                }
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInsets),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top HUD Telemetry Bar
                HudTelemetryHeader(
                    stats = systemStats,
                    status = status,
                    isMuted = isMuted,
                    isTorchOn = isTorchOn,
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleTorch = { viewModel.toggleTorch() },
                    onOpenSettings = { showSettingsSheet = true },
                    onOpenWizard = { showSetupWizard = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Centerpiece: Holographic Arc Reactor
                ArcReactorHologram(
                    status = status,
                    amplitude = combinedAmplitude,
                    onCoreClick = onMicAction,
                    modifier = Modifier.size(190.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Status Notice Hologram Pill with Animatable breathing
                val pillDotColor = when (status) {
                    SipraStatus.LISTENING -> AmberTactical
                    SipraStatus.SPEAKING -> CyanNeon
                    SipraStatus.PROCESSING -> CyanBright
                    SipraStatus.ERROR -> Color(0xFFEF4444)
                    SipraStatus.IDLE -> CyanBright
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(HudCardSurface)
                        .border(
                            width = 1.dp,
                            color = pillDotColor.copy(alpha = statusPillBorderAlpha.value),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp * statusPillScale.value)
                                .clip(CircleShape)
                                .background(pillDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status == SipraStatus.LISTENING) AmberTactical else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Audio Waveform Visualizer
                AudioWaveformVisualizer(
                    status = status,
                    amplitude = combinedAmplitude,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // 5. Quick Directive Chips
                TacticalDirectiveChips(
                    onSelectDirective = { cmd -> viewModel.processUserSpeech(cmd) },
                    onOpenWizard = { showSetupWizard = true }
                )

                // 6. Dialogue Feed (User & SIPRA logs)
                DialogueFeed(
                    messages = messages,
                    onReplaySpeech = { msg -> viewModel.repeatMessage(msg) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Settings Modal Sheet
        if (showSettingsSheet) {
            TacticalSettingsSheet(
                userTitle = userTitle,
                isContinuousMode = isContinuousMode,
                autoListenOnSpeechEnd = autoListenOnSpeechEnd,
                currentPitch = viewModel.speechPitch,
                currentSpeed = viewModel.speechSpeed,
                availableVoices = availableVoices,
                selectedVoice = selectedVoiceName,
                customApiKey = customApiKey,
                onSetUserTitle = { viewModel.setUserTitle(it) },
                onSetContinuousMode = { viewModel.setContinuousMode(it) },
                onSetAutoListenOnSpeechEnd = { viewModel.setAutoListenOnSpeechEnd(it) },
                onSetPitch = { viewModel.setVoicePitch(it) },
                onSetSpeed = { viewModel.setVoiceSpeed(it) },
                onSelectVoice = { viewModel.setVoiceByName(it) },
                onSetApiKey = { viewModel.setCustomApiKey(it) },
                onTestVoice = { viewModel.testVocalEngine() },
                onOpenWizard = { showSetupWizard = true },
                onTestApiKey = { key, callback -> viewModel.testApiKey(key, callback) },
                onDismiss = { showSettingsSheet = false }
            )
        }

        // 6-Step Setup Wizard Modal (৫-৬ টি ধাপের পারমিশন ও এপিআই উইজার্ড)
        if (showSetupWizard) {
            TacticalSetupWizardModal(
                customApiKey = customApiKey,
                autoListenOnSpeechEnd = autoListenOnSpeechEnd,
                onSetCustomApiKey = { viewModel.setCustomApiKey(it) },
                onSetAutoListenOnSpeechEnd = { viewModel.setAutoListenOnSpeechEnd(it) },
                onTestHaptic = { viewModel.vibrateTactile(80) },
                onTestApiKey = { key, callback -> viewModel.testApiKey(key, callback) },
                onDismiss = { showSetupWizard = false }
            )
        }
    }
}

private data class HexMainCadence(
    val cycleMs: Int,
    val minScale: Float,
    val maxScale: Float,
    val minGlow: Float,
    val maxGlow: Float
)

