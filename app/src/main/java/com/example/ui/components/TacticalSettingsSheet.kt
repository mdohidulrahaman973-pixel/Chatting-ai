package com.example.ui.components

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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalContext
import com.example.system.TacticalPermissionsHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalSettingsSheet(
    userTitle: String,
    isContinuousMode: Boolean,
    autoListenOnSpeechEnd: Boolean,
    currentPitch: Float,
    currentSpeed: Float,
    availableVoices: List<String>,
    selectedVoice: String,
    customApiKey: String,
    onSetUserTitle: (String) -> Unit,
    onSetContinuousMode: (Boolean) -> Unit,
    onSetAutoListenOnSpeechEnd: (Boolean) -> Unit,
    onSetPitch: (Float) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSelectVoice: (String) -> Unit,
    onSetApiKey: (String) -> Unit,
    onTestVoice: () -> Unit,
    onOpenWizard: () -> Unit,
    onTestApiKey: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pitchVal by remember { mutableFloatStateOf(currentPitch) }
    var speedVal by remember { mutableFloatStateOf(currentSpeed) }
    var keyInput by remember { mutableStateOf(customApiKey) }
    var customTitleInput by remember { mutableStateOf(userTitle) }
    var isTestingSettingsKey by remember { mutableStateOf(false) }
    var testSettingsKeyResult by remember { mutableStateOf<String?>(null) }
    var testSettingsKeySuccess by remember { mutableStateOf(false) }

    val presetTitles = listOf("Sir", "Captain", "Commander", "Boss", "Doctor")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF060E1E),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LYRA CORE PROTOCOLS",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanNeon
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Launch Card for 6-Step Setup Wizard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2200E5FF))
                    .border(1.dp, CyanNeon, RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onOpenWizard()
                    }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanNeon),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SpaceBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "6-Step Setup Wizard (পারমিশন ও API)",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "মাইক, ক্যামেরা, নোটিফিকেশন অনুমতি ও এপিআই কি ধাপ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = CyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. User Call-sign / Title
            Text(
                text = "CALL-SIGN / DESIGNATION",
                style = MaterialTheme.typography.labelSmall,
                color = AmberTactical
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetTitles.forEach { title ->
                    val isSelected = userTitle == title
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanNeon else HudCardSurface)
                            .border(1.dp, if (isSelected) CyanNeon else HudCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onSetUserTitle(title)
                                customTitleInput = title
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) SpaceBlack else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Female Voice Synthesis Calibration
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = CyanBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FEMALE VOCAL SYNTHESIS (LYRA)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberTactical
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pitch slider
            Text(
                text = "Vocal Pitch (${String.format("%.2f", pitchVal)}x)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Slider(
                value = pitchVal,
                onValueChange = {
                    pitchVal = it
                    onSetPitch(it)
                },
                valueRange = 0.8f..1.4f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanBright,
                    inactiveTrackColor = Color(0x3300E5FF)
                )
            )

            // Speed slider
            Text(
                text = "Speech Cadence (${String.format("%.2f", speedVal)}x)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Slider(
                value = speedVal,
                onValueChange = {
                    speedVal = it
                    onSetSpeed(it)
                },
                valueRange = 0.8f..1.4f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanBright,
                    inactiveTrackColor = Color(0x3300E5FF)
                )
            )

            // Test Voice button
            Button(
                onClick = onTestVoice,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TEST FEMALE VOCAL CADENCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanNeon
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Hands-free Continuous Conversation Loop
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudCardSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Continuous Dialogue Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = TextPrimary
                    )
                    Text(
                        text = "Auto-listen after Lyra finishes speaking for conversational flow",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextMuted
                    )
                }
                Switch(
                    checked = isContinuousMode,
                    onCheckedChange = onSetContinuousMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SpaceBlack,
                        checkedTrackColor = CyanNeon,
                        uncheckedTrackColor = Color(0x3300E5FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Listen on Speech End (কথা থামলেই অটো অন)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudCardSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Listen on Speech End",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = TextPrimary
                    )
                    Text(
                        text = "কথা শেষ হওয়ামাত্রই স্বয়ংক্রিয়ভাবে মাইক অন হবে এবং কথা বলার সময় মাইক মিউট থাকবে",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextMuted
                    )
                }
                Switch(
                    checked = autoListenOnSpeechEnd,
                    onCheckedChange = onSetAutoListenOnSpeechEnd,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SpaceBlack,
                        checkedTrackColor = CyanNeon,
                        uncheckedTrackColor = Color(0x3300E5FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. System Permissions & Background Resilience Quick Actions
            Text(
                text = "SYSTEM PERMISSIONS & BACKGROUND WHITING",
                style = MaterialTheme.typography.labelSmall,
                color = AmberTactical
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display Over Other Apps button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HudCardSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                    .clickable { TacticalPermissionsHelper.requestOverlayPermission(context) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Display Over Other Apps (ডিসপ্লে ওভার)", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = TextPrimary)
                        Text("যেকোনো স্ক্রিনে ভাসমান HUD ও উইন্ডো চালু", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Text(
                    text = if (TacticalPermissionsHelper.isOverlayGranted(context)) "✓ ACTIVE" else "CONFIG ↗",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (TacticalPermissionsHelper.isOverlayGranted(context)) EmeraldOnline else CyanBright
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Battery Optimization Whitelist button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HudCardSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                    .clickable { TacticalPermissionsHelper.requestIgnoreBatteryOptimization(context) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Battery Optimization (ব্যাটারি অপ্টিমাইজেশন)", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = TextPrimary)
                        Text("ব্যাকগ্রাউন্ডে ভয়েস ও সেবা চালু রাখার নিশ্চয়তা", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Text(
                    text = if (TacticalPermissionsHelper.isBatteryOptimizationIgnored(context)) "✓ ACTIVE" else "CONFIG ↗",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (TacticalPermissionsHelper.isBatteryOptimizationIgnored(context)) EmeraldOnline else AmberTactical
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Accessibility Settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HudCardSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                    .clickable { TacticalPermissionsHelper.openAccessibilitySettings(context) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.Accessibility, contentDescription = null, tint = CyanBright, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Accessibility & Assistant (অ্যাক্সেসিবিলিটি)", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = TextPrimary)
                        Text("সিস্টেম অ্যাসিস্ট্যান্ট ও অ্যাক্সেসিবিলিটি সেটিংস", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Text("OPEN ↗", style = MaterialTheme.typography.labelSmall, color = CyanBright)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. API Key Security & Connectivity
            Text(
                text = "NEURAL CORE API CONNECTION",
                style = MaterialTheme.typography.labelSmall,
                color = AmberTactical
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldOnline)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Model: Gemini 3.5 Flash (Direct REST API & AI Studio Secrets)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = keyInput,
                onValueChange = {
                    keyInput = it
                    onSetApiKey(it)
                },
                placeholder = {
                    Text(
                        "Custom GEMINI_API_KEY override (Optional)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextMuted
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = HudCardBorder,
                    focusedContainerColor = Color(0x33030712),
                    unfocusedContainerColor = Color(0x22030712)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            if (onTestApiKey != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isTestingSettingsKey = true
                        testSettingsKeyResult = null
                        onTestApiKey(keyInput) { success, msg ->
                            isTestingSettingsKey = false
                            testSettingsKeySuccess = success
                            testSettingsKeyResult = msg
                        }
                    },
                    enabled = !isTestingSettingsKey,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (testSettingsKeySuccess) Color(0x3310B981) else Color(0x2200E5FF),
                        contentColor = if (testSettingsKeySuccess) EmeraldOnline else CyanNeon
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (isTestingSettingsKey) "TESTING CONNECTION..."
                        else "TEST GEMINI API CONNECTION"
                    )
                }

                if (testSettingsKeyResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (testSettingsKeySuccess) "✓ $testSettingsKeyResult" else "✗ $testSettingsKeyResult",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (testSettingsKeySuccess) EmeraldOnline else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
