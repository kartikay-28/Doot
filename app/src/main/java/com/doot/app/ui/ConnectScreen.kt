package com.doot.app.ui

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doot.app.connection.ConnectionState
import com.doot.app.ui.theme.*

@Composable
fun ConnectScreen(
    connectionState: ConnectionState,
    discoveredPeers: List<android.net.wifi.p2p.WifiP2pDevice>,
    onConnectClick: () -> Unit,
    onPeerClick: (android.net.wifi.p2p.WifiP2pDevice) -> Unit,
    onManageLanguagesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top wordmark
        Text(
            text = "DOOT",
            color = AccentAmber,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Radar area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            when (connectionState) {
                is ConnectionState.Discovering, is ConnectionState.Connecting -> {
                    RadarRings()
                }
                else -> {}
            }

            // Center device icon text
            Text(
                text = "📡",
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status label
        val (statusText, statusColor) = when (connectionState) {
            is ConnectionState.Idle -> "READY TO CONNECT" to TextSecondary
            is ConnectionState.Discovering -> "SEARCHING FOR NEARBY UNIT" to TextSecondary
            is ConnectionState.Connecting -> "CONNECTING..." to AccentTeal
            is ConnectionState.Connected -> "CONNECTED TO ${connectionState.peerName.uppercase()}" to AccentTeal
            is ConnectionState.Failed -> connectionState.error.uppercase() to AccentRed
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(BgPanel)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (connectionState) {
                is ConnectionState.Failed -> {
                    Text(
                        text = "Couldn't connect. Try again.",
                        color = AccentRed,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onConnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.SemiBold)
                    }
                }
                is ConnectionState.Connected -> {
                    Text(
                        text = "● Connected",
                        color = AccentTeal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                is ConnectionState.Connecting -> {
                    Text(
                        text = "Establishing connection...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                is ConnectionState.Discovering -> {
                    if (discoveredPeers.isNotEmpty()) {
                        Text(
                            text = "Found devices. Tap to connect:",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        discoveredPeers.forEach { peer ->
                            Button(
                                onClick = { onPeerClick(peer) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentAmber,
                                    contentColor = BgBase
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(peer.deviceName, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(
                            text = "Scanning nearby devices...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onConnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentTeal,
                            contentColor = BgBase
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Help text
        if (connectionState is ConnectionState.Idle || connectionState is ConnectionState.Discovering) {
            Text(
                text = "Make sure both phones have\nWi-Fi and Location turned on",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Manage Languages Button
        Button(
            onClick = onManageLanguagesClick,
            colors = ButtonDefaults.buttonColors(containerColor = BgPanelAlt, contentColor = TextSecondary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Manage Languages", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RadarRings() {
    val inf = rememberInfiniteTransition(label = "radar")

    val ring1 by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "r1"
    )
    val ring2 by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, delayMillis = 700, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "r2"
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val maxR = size.minDimension / 2

        listOf(ring1, ring2).forEach { progress ->
            val radius = maxR * 0.3f + maxR * 0.7f * progress
            val alpha = (1f - progress) * 0.4f
            drawCircle(
                color = AccentTeal.copy(alpha = alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
