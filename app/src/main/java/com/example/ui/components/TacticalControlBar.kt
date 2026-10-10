package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldCore
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SipraStatus
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun TacticalControlBar(
    status: SipraStatus,
    liveSpeechText: String,
    onMicClick: () -> Unit,
    onSubmitText: (String) -> Unit,
    onClearFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isKeyboardOpen by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }

    // Animatable 1: Center voice orb breathing scale
    val micBreathingScale = remember { Animatable(1f) }

    // Animatable 2: Outer aura halo glow alpha
    val micHaloAlpha = remember { Animatable(0.25f) }

    // Animatable 3: Control bar top border luminescence
    val barBorderGlow = remember { Animatable(0.35f) }

    LaunchedEffect(status) {
        val (cycleDuration, minScale, maxScale, minHalo, maxHalo) = when (status) {
            SipraStatus.IDLE -> HexBarCadence(2800, 0.98f, 1.05f, 0.15f, 0.40f)
            SipraStatus.LISTENING -> HexBarCadence(900, 0.95f, 1.25f, 0.35f, 0.85f)
            SipraStatus.PROCESSING -> HexBarCadence(750, 0.96f, 1.15f, 0.30f, 0.70f)
            SipraStatus.SPEAKING -> HexBarCadence(1200, 0.96f, 1.10f, 0.25f, 0.60f)
            SipraStatus.ERROR -> HexBarCadence(550, 0.95f, 1.12f, 0.20f, 0.65f)
        }
        val half = cycleDuration / 2

        launch {
            while (isActive) {
                micBreathingScale.animateTo(
                    targetValue = maxScale,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                micBreathingScale.animateTo(
                    targetValue = minScale,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                micHaloAlpha.animateTo(
                    targetValue = maxHalo,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                micHaloAlpha.animateTo(
                    targetValue = minHalo,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                barBorderGlow.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                barBorderGlow.animateTo(
                    targetValue = 0.30f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(HudCardSurface)
            .border(
                width = 1.dp,
                color = CyanNeon.copy(alpha = barBorderGlow.value.coerceIn(0.2f, 0.85f)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Live partial speech preview box
        AnimatedVisibility(
            visible = liveSpeechText.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3300E5FF))
                    .border(1.dp, CyanBright, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VOICE INPUT: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberTactical
                    )
                    Text(
                        text = liveSpeechText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Manual Keyboard Text Input field (toggled via keyboard icon)
        AnimatedVisibility(
            visible = isKeyboardOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Type directive for Lyra...",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("directive_text_input"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                onSubmitText(textInput)
                                textInput = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = HudCardBorder,
                        focusedContainerColor = Color(0x33030712),
                        unfocusedContainerColor = Color(0x22030712),
                        cursorColor = CyanNeon
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSubmitText(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CyanNeon)
                        .testTag("send_text_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send directive",
                        tint = SpaceBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Main Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action: Keyboard toggle
            IconButton(
                onClick = { isKeyboardOpen = !isKeyboardOpen },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x2200E5FF))
                    .border(1.dp, HudCardBorder, CircleShape)
                    .testTag("toggle_keyboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Toggle Manual Input",
                    tint = if (isKeyboardOpen) CyanNeon else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Primary Voice Orb Button
            val isListening = status == SipraStatus.LISTENING
            val isBusy = status == SipraStatus.SPEAKING || status == SipraStatus.PROCESSING

            val orbGradient = when {
                isListening -> Brush.radialGradient(listOf(GoldCore, AmberTactical))
                isBusy -> Brush.radialGradient(listOf(CyanBright, CyanDark))
                else -> Brush.radialGradient(listOf(CyanNeon, CyanDark))
            }

            val auraColor = if (isListening) AmberTactical else CyanNeon

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Outer subtle breathing halo ring via Animatable
                Box(
                    modifier = Modifier
                        .size(74.dp * micBreathingScale.value)
                        .clip(CircleShape)
                        .background(auraColor.copy(alpha = micHaloAlpha.value * 0.35f))
                )

                // Secondary soft breathing ring
                Box(
                    modifier = Modifier
                        .size(66.dp * (0.95f + (micBreathingScale.value - 1f) * 0.5f))
                        .clip(CircleShape)
                        .background(auraColor.copy(alpha = (micHaloAlpha.value * 0.20f)))
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(orbGradient)
                        .clickable(onClick = onMicClick)
                        .testTag("voice_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBusy) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isBusy) "Stop" else "Speak to Lyra",
                        tint = SpaceBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Right Action: Clear Dialogue Feed
            IconButton(
                onClick = onClearFeed,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x2200E5FF))
                    .border(1.dp, HudCardBorder, CircleShape)
                    .testTag("clear_feed_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear telemetry log",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class HexBarCadence(
    val cycleMs: Int,
    val minScale: Float,
    val maxScale: Float,
    val minHalo: Float,
    val maxHalo: Float
)

