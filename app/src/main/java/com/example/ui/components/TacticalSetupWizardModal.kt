package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.system.TacticalPermissionsHelper
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Comprehensive Tactical Setup Wizard for Lyra AI
 * Covers all permissions & configurations requested:
 * Step 1: Acoustic Sensor (Microphone / RECORD_AUDIO)
 * Step 2: Display Over Other Apps (ডিসপ্লে ওভার / SYSTEM_ALERT_WINDOW)
 * Step 3: Battery Optimization Exemption (ব্যাটারি অপ্টিমাইজেশন / Always-On Background Run)
 * Step 4: Accessibility & System Assistant (এসএস বিলিটি / Accessibility & Default Assistant)
 * Step 5: Camera & Tactical Photon Emitter (ক্যামেরা ও ফ্ল্যাশলাইট / CAMERA)
 * Step 6: Neural Alerts & Auto-Duplex Protocol (নোটিফিকেশন ও কথা থামলেই অটো-মাইক)
 * Step 7: Neural Core Gemini AI API Key (এপিআই কি কনফিগারেশন)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalSetupWizardModal(
    customApiKey: String,
    autoListenOnSpeechEnd: Boolean,
    onSetCustomApiKey: (String) -> Unit,
    onSetAutoListenOnSpeechEnd: (Boolean) -> Unit,
    onTestHaptic: () -> Unit,
    onTestApiKey: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentStep by remember { mutableIntStateOf(1) }
    var apiKeyText by remember { mutableStateOf(customApiKey) }
    var isApiKeyObscured by remember { mutableStateOf(true) }

    // Live permission states
    var hasMicPermission by remember { mutableStateOf(TacticalPermissionsHelper.isMicGranted(context)) }
    var hasOverlayPermission by remember { mutableStateOf(TacticalPermissionsHelper.isOverlayGranted(context)) }
    var hasBatteryOptIgnored by remember { mutableStateOf(TacticalPermissionsHelper.isBatteryOptimizationIgnored(context)) }
    var hasCameraPermission by remember { mutableStateOf(TacticalPermissionsHelper.isCameraGranted(context)) }
    var hasNotificationPermission by remember { mutableStateOf(TacticalPermissionsHelper.isNotificationGranted(context)) }

    // Auto-refresh states when returning from system settings screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasMicPermission = TacticalPermissionsHelper.isMicGranted(context)
                hasOverlayPermission = TacticalPermissionsHelper.isOverlayGranted(context)
                hasBatteryOptIgnored = TacticalPermissionsHelper.isBatteryOptimizationIgnored(context)
                hasCameraPermission = TacticalPermissionsHelper.isCameraGranted(context)
                hasNotificationPermission = TacticalPermissionsHelper.isNotificationGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launchers for standard runtime requests
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasMicPermission = isGranted
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Breathing glow animation with Compose Animatable
    val wizardGlowAnim = remember { Animatable(0.4f) }
    LaunchedEffect(currentStep) {
        launch {
            while (isActive) {
                wizardGlowAnim.animateTo(0.85f, tween(1600, easing = FastOutSlowInEasing))
                wizardGlowAnim.animateTo(0.35f, tween(1600, easing = FastOutSlowInEasing))
            }
        }
    }

    val totalSteps = 7

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF040B18),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with title and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = wizardGlowAnim.value))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TACTICAL SYSTEM PERMISSIONS // WIZARD",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanNeon
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_wizard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "সবগুলো প্রয়োজনীয় অনুমতি ও এপিআই কি কনফিগারেশন (ধাপ $currentStep of $totalSteps)",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Track (1 to 7 Pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (stepIndex in 1..totalSteps) {
                    val isCurrent = stepIndex == currentStep
                    val isPast = stepIndex < currentStep
                    val stepColor = when {
                        isCurrent -> CyanNeon
                        isPast -> EmeraldOnline
                        else -> HudCardBorder
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isCurrent) CyanNeon.copy(alpha = 0.25f)
                                else if (isPast) EmeraldOnline.copy(alpha = 0.2f)
                                else Color(0x221E293B)
                            )
                            .border(1.dp, stepColor, RoundedCornerShape(6.dp))
                            .clickable { currentStep = stepIndex }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPast) "✓" else "$stepIndex",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) CyanBright else if (isPast) EmeraldOnline else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Step Content Frame
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "WizardStepAnimation"
            ) { step ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HudCardSurface)
                        .border(1.dp, CyanNeon.copy(alpha = wizardGlowAnim.value * 0.7f), RoundedCornerShape(16.dp))
                        .drawBehind {
                            drawHolographicCornerBrackets(
                                color = CyanNeon.copy(alpha = wizardGlowAnim.value * 0.5f),
                                bracketLength = 12.dp.toPx(),
                                inset = 3.dp.toPx()
                            )
                        }
                        .padding(18.dp)
                ) {
                    when (step) {
                        1 -> StepMicPermission(
                            hasPermission = hasMicPermission,
                            onRequestPermission = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                        )
                        2 -> StepOverlayPermission(
                            hasPermission = hasOverlayPermission,
                            onRequestPermission = { TacticalPermissionsHelper.requestOverlayPermission(context) }
                        )
                        3 -> StepBatteryOptPermission(
                            hasPermission = hasBatteryOptIgnored,
                            onRequestPermission = { TacticalPermissionsHelper.requestIgnoreBatteryOptimization(context) }
                        )
                        4 -> StepAccessibilitySettings(
                            onOpenSettings = { TacticalPermissionsHelper.openAccessibilitySettings(context) }
                        )
                        5 -> StepCameraPermission(
                            hasPermission = hasCameraPermission,
                            onRequestPermission = { cameraLauncher.launch(Manifest.permission.CAMERA) }
                        )
                        6 -> StepNotificationsAndAutoMic(
                            hasNotification = hasNotificationPermission,
                            autoListen = autoListenOnSpeechEnd,
                            onRequestNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    hasNotificationPermission = true
                                }
                            },
                            onToggleAutoListen = onSetAutoListenOnSpeechEnd,
                            onTestHaptic = onTestHaptic
                        )
                        7 -> StepApiKey(
                            apiKey = apiKeyText,
                            isObscured = isApiKeyObscured,
                            onToggleObscured = { isApiKeyObscured = !isApiKeyObscured },
                            onApiKeyChange = {
                                apiKeyText = it
                                onSetCustomApiKey(it)
                            },
                            onTestApiKey = onTestApiKey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons (Back & Next / Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanBright),
                        border = BorderStroke(1.dp, HudCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("wizard_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পূর্ববর্তী ধাপ (Back)")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < totalSteps) {
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = SpaceBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("wizard_next_button")
                    ) {
                        Text("পরবর্তী ধাপ (Next)")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onSetCustomApiKey(apiKeyText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldOnline,
                            contentColor = SpaceBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("wizard_finish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সব সেটআপ সম্পন্ন (Initialize)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ----------------- STEP 1: MICROPHONE -----------------
@Composable
private fun StepMicPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    StepContentContainer(
        icon = Icons.Default.Mic,
        stepNumber = "1/7",
        title = "Acoustic Sensor (Microphone)",
        subTitle = "মাইক্রোফোন রেকর্ডিং পারমিশন",
        description = "Lyra-কে মুখে বলে কমান্ড দেওয়ার জন্য মাইক্রোফোন পারমিশন জরুরি। কথা বলার সময় মাইক স্বয়ংক্রিয়ভাবে মিউট থাকে এবং কথা থামামাত্রই অটোমেটিকালি চালু হয়।",
        statusText = if (hasPermission) "অনুমতি দেওয়া হয়েছে (GRANTED)" else "অনুমতি প্রয়োজন (REQUIRED)",
        isGranted = hasPermission,
        buttonText = if (hasPermission) "অনুমতি সফল হয়েছে ✓" else "মাইক্রোফোন অনুমতি দিন (Grant Microphone)",
        onButtonClick = onRequestPermission
    )
}

// ----------------- STEP 2: DISPLAY OVER OTHER APPS (OVERLAY) -----------------
@Composable
private fun StepOverlayPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    StepContentContainer(
        icon = Icons.Default.Layers,
        stepNumber = "2/7",
        title = "Display Over Other Apps (ডিসপ্লে ওভার)",
        subTitle = "স্ক্রিনের উপরে ভাসমান HUD ও উইন্ডো অনুমতি",
        description = "যেকোনো অ্যাপ, গেম বা হোম স্ক্রিনের ওপর থেকে Lyra-কে দেখতে ও সরাসরি কমান্ড দিতে 'Display over other apps' অনুমতি দিন। এটি দিয়ে অন্য অ্যাপ চলাকালীনও নাইরা কথা বলতে পারে।",
        statusText = if (hasPermission) "ডিসপ্লে ওভার সক্রিয় (GRANTED)" else "ডিসপ্লে ওভার অনুমতি প্রয়োজন",
        isGranted = hasPermission,
        buttonText = if (hasPermission) "ডিসপ্লে ওভার সক্রিয় রয়েছে ✓" else "ডিসপ্লে ওভার অনুমতি দিন (Open Settings)",
        onButtonClick = onRequestPermission
    )
}

// ----------------- STEP 3: BATTERY OPTIMIZATION EXEMPTION -----------------
@Composable
private fun StepBatteryOptPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    StepContentContainer(
        icon = Icons.Default.BatteryChargingFull,
        stepNumber = "3/7",
        title = "Battery Optimization (ব্যাটারি অপ্টিমাইজেশন)",
        subTitle = "অলওয়েজ-অন ব্যাকগ্রাউন্ড রান ও সার্ভিসের সুরক্ষা",
        description = "অ্যান্ড্রয়েড সিস্টেম যাতে ব্যাকগ্রাউন্ডে Lyra-এর ভয়েস ইঞ্জিন বন্ধ বা কিল না করে, সেজন্য ব্যাটারি অপ্টিমাইজেশন নিষ্ক্রিয় (Unrestricted / Ignore Battery Optimization) করা অত্যন্ত জরুরি।",
        statusText = if (hasPermission) "অপ্টিমাইজেশন মুক্ত (UNRESTRICTED)" else "অপ্টিমাইজেশন নিষ্ক্রিয় করুন",
        isGranted = hasPermission,
        buttonText = if (hasPermission) "ব্যাটারি সুরক্ষা সক্রিয় ✓" else "ব্যাটারি অপ্টিমাইজেশন নিষ্ক্রিয় করুন (Unrestrict)",
        onButtonClick = onRequestPermission
    )
}

// ----------------- STEP 4: ACCESSIBILITY & SYSTEM ASSISTANT -----------------
@Composable
private fun StepAccessibilitySettings(
    onOpenSettings: () -> Unit
) {
    StepContentContainer(
        icon = Icons.Default.Accessibility,
        stepNumber = "4/7",
        title = "Accessibility & Assistant (এসএস বিলিটি)",
        subTitle = "অ্যাক্সেসিবিলিটি সার্ভিস ও সিস্টেম অ্যাসিস্ট্যান্ট",
        description = "সিস্টেম-লেভেল ভয়েস ইনপুট ও পাওয়ার বাটনে চেপে অ্যাসিস্ট্যান্ট ডাকতে ফোনের Accessibility Settings ও Default Assistant অ্যাপ হিসেবে Lyra-কে বেছে নেওয়া যায়।",
        statusText = "কনফিগারযোগ্য (READY)",
        isGranted = true,
        buttonText = "অ্যাক্সেসিবিলিটি সেটিংস খুলুন (Open Accessibility)",
        onButtonClick = onOpenSettings
    )
}

// ----------------- STEP 5: CAMERA & TORCH -----------------
@Composable
private fun StepCameraPermission(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    StepContentContainer(
        icon = Icons.Default.CameraAlt,
        stepNumber = "5/7",
        title = "Photon Emitter & Optics (Camera)",
        subTitle = "ক্যামেরা ও ট্যাকটিক্যাল টর্চ অনুমতি",
        description = "ডিভাইসের ফ্ল্যাশলাইট বা টর্চ সক্রিয় করতে ক্যামেরা হার্ডওয়্যার পারমিশন প্রয়োজন। 'Turn on torch' বা 'Turn off torch' মুখে বললেই এটি কাজ করে।",
        statusText = if (hasPermission) "অনুমতি দেওয়া হয়েছে (GRANTED)" else "অনুমতি প্রয়োজন (REQUIRED)",
        isGranted = hasPermission,
        buttonText = if (hasPermission) "ক্যামেরা অনুমতি নিশ্চিত ✓" else "ক্যামেরা অনুমতি দিন (Grant Camera)",
        onButtonClick = onRequestPermission
    )
}

// ----------------- STEP 6: NOTIFICATIONS & AUTO-MIC -----------------
@Composable
private fun StepNotificationsAndAutoMic(
    hasNotification: Boolean,
    autoListen: Boolean,
    onRequestNotification: () -> Unit,
    onToggleAutoListen: (Boolean) -> Unit,
    onTestHaptic: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x2200E5FF))
                        .border(1.dp, CyanBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ধাপ ৬/৭: Alerts & Auto-Duplex",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberTactical
                    )
                    Text(
                        text = "নোটিফিকেশন ও অটো-মাইক",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hasNotification) Color(0x2210B981) else Color(0x22EF4444))
                    .border(1.dp, if (hasNotification) EmeraldOnline else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (hasNotification) "✓ ALERTS OK" else "NOTIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasNotification) EmeraldOnline else Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!hasNotification) {
            Button(
                onClick = onRequestNotification,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = SpaceBlack),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("নোটিফিকেশন পারমিশন দিন (Enable Alerts)")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Auto-Listen Protocol Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33091830))
                .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "কথা থামলেই অটো মাইক অন",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Text(
                    text = "নাইরা কথা বলার সময় মাইক মিউট থাকবে, কথা শেষ হতেই স্বয়ংক্রিয় অন হবে",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = autoListen,
                onCheckedChange = onToggleAutoListen,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SpaceBlack,
                    checkedTrackColor = CyanNeon,
                    uncheckedTrackColor = Color(0x331E293B)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Haptic feedback pulse test button
        OutlinedButton(
            onClick = onTestHaptic,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanBright),
            border = BorderStroke(1.dp, HudCardBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ট্যাকটিক্যাল ভাইব্রেশন টেস্ট করুন (Test Pulse)")
        }
    }
}

// ----------------- STEP 7: GEMINI API KEY -----------------
@Composable
private fun StepApiKey(
    apiKey: String,
    isObscured: Boolean,
    onToggleObscured: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onTestApiKey: ((String, (Boolean, String) -> Unit) -> Unit)? = null
) {
    var isTestingKey by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testIsSuccess by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x2200E5FF))
                    .border(1.dp, CyanBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ধাপ ৭/৭: Neural Core Intelligence",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberTactical
                )
                Text(
                    text = "Gemini AI API Key (এপিআই কি)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "আপনার ব্যক্তিগত Gemini API Key এখানে যোগ করতে পারেন। খালি রাখলে AI Studio-র ডিফল্ট ইন্টিগ্রেটেড কি ব্যবহৃত হবে।",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = {
                onApiKeyChange(it)
                testResultText = null
            },
            placeholder = { Text("AIzaSy...", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("wizard_api_key_input"),
            visualTransformation = if (isObscured) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = onToggleObscured) {
                    Icon(
                        imageVector = if (isObscured) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility",
                        tint = CyanBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanNeon,
                unfocusedBorderColor = HudCardBorder,
                focusedContainerColor = Color(0x33030712),
                unfocusedContainerColor = Color(0x22030712),
                cursorColor = CyanNeon
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // API Test Connection Button
        if (onTestApiKey != null) {
            OutlinedButton(
                onClick = {
                    isTestingKey = true
                    testResultText = null
                    onTestApiKey(apiKey) { success, msg ->
                        isTestingKey = false
                        testIsSuccess = success
                        testResultText = msg
                    }
                },
                enabled = !isTestingKey,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanBright),
                border = BorderStroke(1.dp, if (testIsSuccess) EmeraldOnline else CyanBright),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("test_api_key_button")
            ) {
                Text(
                    if (isTestingKey) "যাচাই করা হচ্ছে (Testing Connection...)"
                    else "API Key সংযোগ পরীক্ষা করুন (Test Connection)"
                )
            }

            if (testResultText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (testIsSuccess) Color(0x2210B981) else Color(0x22EF4444))
                        .border(1.dp, if (testIsSuccess) EmeraldOnline else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (testIsSuccess) "✓ সফল: $testResultText" else "✗ ব্যর্থ: $testResultText",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (testIsSuccess) EmeraldOnline else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = if (apiKey.isNotBlank())
                "✓ কাস্টম API কি সংরক্ষিত রয়েছে।"
            else
                "ℹ️ খালি রাখলে বিল্ট-ইন সার্ভার-সাইড Gemini Core কাজ করবে।",
            style = MaterialTheme.typography.labelSmall,
            color = if (apiKey.isNotBlank()) EmeraldOnline else CyanBright
        )
    }
}

// Reusable Step Container
@Composable
private fun StepContentContainer(
    icon: ImageVector,
    stepNumber: String,
    title: String,
    subTitle: String,
    description: String,
    statusText: String,
    isGranted: Boolean,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x2200E5FF))
                        .border(1.dp, CyanBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ধাপ $stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberTactical
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGranted) Color(0x2210B981) else Color(0x22EF4444))
                    .border(1.dp, if (isGranted) EmeraldOnline else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGranted) "✓ GRANTED" else "PENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) EmeraldOnline else Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = subTitle,
            style = MaterialTheme.typography.labelMedium,
            color = CyanBright
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0x3310B981) else CyanNeon,
                contentColor = if (isGranted) EmeraldOnline else SpaceBlack
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("step_action_button")
        ) {
            Text(buttonText)
        }
    }
}
