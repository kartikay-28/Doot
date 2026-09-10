package com.doot.app.ui

import android.view.MotionEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doot.app.AppState
import com.doot.app.ui.theme.*
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TalkScreen(
    appState: AppState,
    myTranscript: String,
    receivedTranscript: String,
    selectedLang: String,
    peerStatus: String,
    onLangSelected: (String) -> Unit,
    onTalkPressed: () -> Unit,
    onTalkReleased: () -> Unit,
    onAlertClicked: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgPanel)
                .border(width = 1.dp, color = BorderHairline, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = AccentTeal)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONNECTED",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.2f), contentColor = AccentRed),
                modifier = Modifier.height(32.dp).padding(end = 12.dp)
            ) {
                Text("DISCONNECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            // Signal bars
            SignalBars()
        }

        // ── Language selector ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgPanel)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("en" to "EN", "hi" to "HI", "pa" to "PA").forEach { (code, label) ->
                val isActive = selectedLang == code
                Button(
                    onClick = { onLangSelected(code) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) AccentAmber else Color.Transparent,
                        contentColor = if (isActive) BgBase else TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .then(
                            if (!isActive) Modifier.border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                            else Modifier
                        )
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        // ── Hairline divider ──
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderHairline)
        )

        // ── Message log ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (receivedTranscript.isNotBlank()) {
                LogEntry(
                    text = receivedTranscript,
                    langTag = "IN",
                    sender = "Peer",
                    isReceived = true
                )
            }
            if (myTranscript.isNotBlank()) {
                LogEntry(
                    text = myTranscript,
                    langTag = selectedLang.uppercase(),
                    sender = "You",
                    isReceived = false
                )
            }
            if (myTranscript.isBlank() && receivedTranscript.isBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Channel open.", color = TextSecondary, fontSize = 16.sp)
                    Text("Hold to talk.", color = TextSecondary, fontSize = 16.sp)
                }
            }
        }

        // ── Peer recording indicator ──
        if (peerStatus == "RECORDING") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentTeal.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = AccentTeal)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Peer is recording a message...",
                    color = AccentTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Hairline divider ──
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderHairline)
        )

        // ── Waveform (when recording) ──
        if (appState == AppState.Recording) {
            WaveformVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        // ── Bottom controls ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgPanel)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Alert button
            Button(
                onClick = onAlertClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = AccentRed
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .border(1.dp, AccentRed, RoundedCornerShape(8.dp))
            ) {
                Text("⚠ Alert", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(24.dp))

            // PTT Button
            val isPressed = remember { mutableStateOf(false) }
            val pttColor = if (isPressed.value) AccentRed else AccentAmber
            val glowColor = if (isPressed.value) AccentRed.copy(alpha = 0.3f) else Color.Transparent

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .drawBehind {
                        if (isPressed.value) {
                            drawCircle(
                                color = glowColor,
                                radius = size.minDimension / 2 + 12.dp.toPx()
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(pttColor)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isPressed.value = true
                                onTalkPressed()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isPressed.value = false
                                onTalkReleased()
                            }
                        }
                        true
                    }
            ) {
                Text(
                    text = "🎙",
                    fontSize = 28.sp
                )
            }
        }

        // Status bar
        if (appState != AppState.Connected && appState != AppState.Recording) {
            val stateLabel = when (appState) {
                AppState.Transcribing -> "TRANSCRIBING..."
                AppState.Sending -> "SENDING..."
                AppState.Receiving -> "RECEIVING..."
                AppState.Speaking -> "PLAYING BACK..."
                else -> ""
            }
            if (stateLabel.isNotEmpty()) {
                Text(
                    text = stateLabel,
                    color = AccentAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgPanelAlt)
                        .padding(vertical = 6.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

// ── Log Entry ──
@Composable
private fun LogEntry(text: String, langTag: String, sender: String, isReceived: Boolean) {
    val accentColor = if (isReceived) AccentTeal else AccentAmber
    val bgColor = if (isReceived) AccentTealDim else AccentAmberDim
    val alignment = if (isReceived) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(4.dp))
                .background(bgColor)
                .then(
                    if (isReceived)
                        Modifier.drawBehind {
                            drawRect(
                                color = accentColor,
                                topLeft = Offset(0f, 0f),
                                size = Size(2.dp.toPx(), size.height)
                            )
                        }
                    else
                        Modifier.drawBehind {
                            drawRect(
                                color = accentColor,
                                topLeft = Offset(size.width - 2.dp.toPx(), 0f),
                                size = Size(2.dp.toPx(), size.height)
                            )
                        }
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "$langTag · $sender",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ── Signal Bars ──
@Composable
private fun SignalBars() {
    Canvas(modifier = Modifier.width(24.dp).height(14.dp)) {
        val barCount = 4
        val barWidth = size.width / (barCount * 2f)
        val gap = barWidth
        for (i in 0 until barCount) {
            val h = size.height * (0.3f + 0.7f * (i.toFloat() / (barCount - 1)))
            val x = i * (barWidth + gap)
            val y = size.height - h
            drawRoundRect(
                color = AccentTeal,
                topLeft = Offset(x, y),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}

// ── Waveform Visualizer ──
@Composable
private fun WaveformVisualizer(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "wave")
    val phase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase"
    )

    Canvas(modifier = modifier.height(28.dp)) {
        val barCount = 15
        val barWidth = size.width / (barCount * 1.8f)
        val gap = (size.width - barCount * barWidth) / (barCount - 1).coerceAtLeast(1)
        for (i in 0 until barCount) {
            val p = (phase + i * 0.08f) % 1f
            val wave = (sin(p * Math.PI * 2).toFloat() + 1f) / 2f
            val h = size.height * (0.2f + 0.8f * wave)
            val x = i * (barWidth + gap)
            val y = (size.height - h) / 2f
            drawRoundRect(
                color = AccentRed,
                topLeft = Offset(x, y),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}
