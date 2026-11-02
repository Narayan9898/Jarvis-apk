package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.JarvisAssistantState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisStateExecuting
import com.example.ui.theme.JarvisStateListening
import com.example.ui.theme.JarvisStateSpeaking
import com.example.ui.theme.JarvisStateThinking
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    state: JarvisAssistantState,
    audioLevel: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SlowRotation"
    )

    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FastCounterRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val coreColor = when (state) {
        JarvisAssistantState.LISTENING -> JarvisStateListening
        JarvisAssistantState.THINKING -> JarvisStateThinking
        JarvisAssistantState.SPEAKING -> JarvisStateSpeaking
        JarvisAssistantState.EXECUTING -> JarvisStateExecuting
        JarvisAssistantState.IDLE -> JarvisCyan
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.width / 2f) * 0.9f
            val audioBoost = (audioLevel * 4f).coerceIn(0f, 25f)

            // Outer glowing ambient ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = maxRadius + audioBoost
                ),
                radius = maxRadius + audioBoost,
                center = center
            )

            // Outer segmented ring with tick marks
            val outerRadius = maxRadius * 0.88f
            drawCircle(
                color = coreColor.copy(alpha = 0.4f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            val tickCount = 24
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((i * (360.0 / tickCount) + rotationSlow).toDouble())
                val innerR = outerRadius - 8.dp.toPx()
                val outerR = outerRadius + 4.dp.toPx()
                val p1 = Offset(
                    (center.x + innerR * cos(angleRad)).toFloat(),
                    (center.y + innerR * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + outerR * cos(angleRad)).toFloat(),
                    (center.y + outerR * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = coreColor.copy(alpha = if (i % 3 == 0) 0.9f else 0.4f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Middle rotating arcs (Counter-rotating)
            val midRadius = maxRadius * 0.65f
            val arcSegments = 4
            for (i in 0 until arcSegments) {
                val startAngle = (i * 90f) + rotationFast
                drawArc(
                    color = JarvisElectricBlue,
                    startAngle = startAngle,
                    sweepAngle = 45f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner energy containment circle
            val innerRadius = (maxRadius * 0.42f) * (if (state != JarvisAssistantState.IDLE) pulseScale else 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyanBright,
                        coreColor.copy(alpha = 0.8f),
                        coreColor.copy(alpha = 0.1f)
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            // Center glowing core
            val coreDotRadius = (innerRadius * 0.45f) + (audioBoost * 0.5f)
            drawCircle(
                color = Color.White,
                radius = coreDotRadius,
                center = center
            )
        }
    }
}
