package com.doot.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doot.app.ui.theme.AccentAmber
import com.doot.app.ui.theme.BgBase
import com.doot.app.ui.theme.TextDisabled
import com.doot.app.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun SplashScreen(statusText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Wordmark
        Text(
            text = "D O O T",
            color = AccentAmber,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "voice, carried as text",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // VU Meter bar loader
        VuMeterLoader()

        Spacer(modifier = Modifier.height(24.dp))

        // Status readout
        Text(
            text = statusText.uppercase(),
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Build label
        Text(
            text = "Prototype Build",
            color = TextDisabled,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun VuMeterLoader() {
    val barCount = 7
    val inf = rememberInfiniteTransition(label = "vu")

    // Each bar oscillates with a staggered phase
    val phases = (0 until barCount).map { i ->
        val anim by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bar$i"
        )
        anim
    }

    Canvas(modifier = Modifier.width(120.dp).height(32.dp)) {
        val barWidth = size.width / (barCount * 2f)
        val gap = barWidth
        val totalWidth = barCount * barWidth + (barCount - 1) * gap
        val startX = (size.width - totalWidth) / 2f

        for (i in 0 until barCount) {
            // Stagger: offset each bar by ~80ms worth of phase
            val phase = (phases[i] + i * 0.12f) % 1f
            val wave = (sin(phase * Math.PI * 2).toFloat() + 1f) / 2f  // 0..1
            val minH = size.height * 0.15f
            val maxH = size.height * 0.95f
            val barH = minH + (maxH - minH) * wave

            val x = startX + i * (barWidth + gap)
            val y = (size.height - barH) / 2f

            drawRoundRect(
                color = AccentAmber,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
