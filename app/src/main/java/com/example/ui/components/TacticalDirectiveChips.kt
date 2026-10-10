package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DirectiveItem(
    val label: String,
    val commandText: String,
    val icon: ImageVector
)

@Composable
fun TacticalDirectiveChips(
    onSelectDirective: (String) -> Unit,
    onOpenWizard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val chipBorderAlpha = remember { Animatable(0.30f) }

    LaunchedEffect(Unit) {
        launch {
            while (isActive) {
                chipBorderAlpha.animateTo(0.65f, tween(2600, easing = FastOutSlowInEasing))
                chipBorderAlpha.animateTo(0.25f, tween(2600, easing = FastOutSlowInEasing))
            }
        }
    }

    val directives = listOf(
        DirectiveItem("পারমিশন ও API সেটআপ (Wizard)", "__OPEN_WIZARD__", Icons.Default.Security),
        DirectiveItem("Status Report", "Run diagnostics and report system status", Icons.Default.Bolt),
        DirectiveItem("Tactical Briefing", "Give me a tactical briefing for today", Icons.Default.Info),
        DirectiveItem("Quantum Analysis", "Analyze the principles of quantum computing in three sentences", Icons.Default.Psychology),
        DirectiveItem("Weather Protocol", "Provide a simulated atmospheric and weather telemetry report", Icons.Default.WbSunny)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        directives.forEach { item ->
            DirectiveChip(
                item = item,
                borderAlpha = chipBorderAlpha.value,
                onClick = {
                    if (item.commandText == "__OPEN_WIZARD__" && onOpenWizard != null) {
                        onOpenWizard()
                    } else {
                        onSelectDirective(item.commandText)
                    }
                }
            )
        }
    }
}

@Composable
private fun DirectiveChip(
    item: DirectiveItem,
    borderAlpha: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HudCardSurface)
            .border(
                width = 1.dp,
                color = CyanNeon.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("directive_chip_${item.label.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = CyanNeon.copy(alpha = (borderAlpha + 0.35f).coerceIn(0.6f, 1f)),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
    }
}

