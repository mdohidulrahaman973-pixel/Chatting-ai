package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageSender
import com.example.data.SipraMessage
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DialogueFeed(
    messages: List<SipraMessage>,
    onReplaySpeech: (SipraMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Automatically scroll to latest message when new one arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        // Holographic standby breathing reticle via Animatable
        val standbyPulseScale = remember { Animatable(1f) }
        val standbyGlowAlpha = remember { Animatable(0.35f) }

        LaunchedEffect(Unit) {
            launch {
                while (isActive) {
                    standbyPulseScale.animateTo(1.12f, tween(2400, easing = FastOutSlowInEasing))
                    standbyPulseScale.animateTo(0.94f, tween(2400, easing = FastOutSlowInEasing))
                }
            }
            launch {
                while (isActive) {
                    standbyGlowAlpha.animateTo(0.85f, tween(2400, easing = FastOutSlowInEasing))
                    standbyGlowAlpha.animateTo(0.25f, tween(2400, easing = FastOutSlowInEasing))
                }
            }
        }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Pulsing holographic radar node
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp * standbyPulseScale.value)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = standbyGlowAlpha.value * 0.20f))
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2200E5FF))
                            .border(1.dp, CyanNeon.copy(alpha = standbyGlowAlpha.value), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = CyanNeon.copy(alpha = standbyGlowAlpha.value),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "NEURAL LINK STANDBY",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap the Arc Reactor or microphone to issue verbal directives.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                DialogueItemCard(
                    message = msg,
                    onReplay = { onReplaySpeech(msg) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Lyra Dialogue", msg.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogueItemCard(
    message: SipraMessage,
    onReplay: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender header line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isUser) AmberTactical else CyanNeon)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isUser) "COMMANDER" else "LYRA AI",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) AmberTactical else CyanNeon
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted
            )
        }

        // Message bubble
        val bubbleShape = if (isUser) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }

        val cardBg = if (isUser) {
            Color(0xDD121F38)
        } else {
            HudCardSurface
        }

        val borderColor = if (message.isError) {
            Color(0xFFEF4444)
        } else if (isUser) {
            Color(0x33F59E0B)
        } else {
            HudCardBorder
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                .clip(bubbleShape)
                .background(cardBg)
                .border(1.dp, borderColor, bubbleShape)
                .drawBehind {
                    // Subtle holographic corner brackets on cards
                    drawHolographicCornerBrackets(
                        color = (if (isUser) AmberTactical else CyanNeon).copy(alpha = 0.35f),
                        bracketLength = 6.dp.toPx(),
                        strokeWidth = 1.dp.toPx(),
                        inset = 3.dp.toPx()
                    )
                }
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.isError) Color(0xFFFCA5A5) else TextPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom actions row for card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser && !message.isError) {
                        IconButton(
                            onClick = onReplay,
                            modifier = Modifier.size(28.dp).testTag("replay_voice_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay vocal audio",
                                tint = CyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp).testTag("copy_text_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

