package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.data.model.JarvisAssistantState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisElectricBlue
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    state: JarvisAssistantState,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPhase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 36
        val barSpacing = width / barCount

        val isDynamic = state == JarvisAssistantState.LISTENING || state == JarvisAssistantState.SPEAKING
        val baseAmplitude = if (isDynamic) (audioLevel * 4f).coerceIn(4f, height * 0.45f) else 3f

        for (i in 0 until barCount) {
            val x = i * barSpacing + (barSpacing / 2f)
            val waveOffset = sin((i * 0.35f) + phase)
            val barHeight = if (isDynamic) {
                (baseAmplitude * (0.4f + 0.6f * (waveOffset + 1f) / 2f)).coerceAtLeast(3f)
            } else {
                3f + (sin((i * 0.2f) + phase) * 2f).coerceAtLeast(0f)
            }

            val topY = centerY - barHeight
            val bottomY = centerY + barHeight

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(JarvisCyanBright, JarvisCyan, JarvisElectricBlue),
                    startY = topY,
                    endY = bottomY
                ),
                start = Offset(x, topY),
                end = Offset(x, bottomY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
