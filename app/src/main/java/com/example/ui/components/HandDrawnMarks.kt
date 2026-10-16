package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.PadBlue
import com.example.ui.theme.PassGreen

@Composable
fun HandDrawnCross(
    modifier: Modifier = Modifier.size(36.dp),
    color: Color = CheckerRed,
    animate: Boolean = true
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (animate) {
            progress.animateTo(1f, animationSpec = tween(500))
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = progress.value

        // First stroke: top-left to bottom-right with slight curve
        val strokeWidth = 3.5.dp.toPx()
        val path1 = Path().apply {
            moveTo(w * 0.18f, h * 0.20f)
            quadraticTo(w * 0.48f, h * 0.52f, w * 0.82f, h * 0.80f)
        }

        // Second stroke: top-right to bottom-left with quick counter slash
        val path2 = Path().apply {
            moveTo(w * 0.80f, h * 0.18f)
            quadraticTo(w * 0.52f, h * 0.48f, w * 0.20f, h * 0.82f)
        }

        if (p <= 0.5f) {
            val p1 = p / 0.5f
            drawLine(
                color = color,
                start = Offset(w * 0.18f, h * 0.20f),
                end = Offset(
                    w * 0.18f + (w * 0.64f) * p1,
                    h * 0.20f + (h * 0.60f) * p1
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        } else {
            drawPath(
                path = path1,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val p2 = (p - 0.5f) / 0.5f
            drawLine(
                color = color,
                start = Offset(w * 0.80f, h * 0.18f),
                end = Offset(
                    w * 0.80f - (w * 0.60f) * p2,
                    h * 0.18f + (h * 0.64f) * p2
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HandDrawnCheck(
    modifier: Modifier = Modifier.size(36.dp),
    color: Color = PassGreen,
    animate: Boolean = true
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (animate) {
            progress.animateTo(1f, animationSpec = tween(450))
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = progress.value
        val strokeWidth = 3.5.dp.toPx()

        if (p <= 0.35f) {
            val p1 = p / 0.35f
            drawLine(
                color = color,
                start = Offset(w * 0.20f, h * 0.55f),
                end = Offset(
                    w * 0.20f + (w * 0.24f) * p1,
                    h * 0.55f + (h * 0.25f) * p1
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                color = color,
                start = Offset(w * 0.20f, h * 0.55f),
                end = Offset(w * 0.44f, h * 0.80f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            val p2 = (p - 0.35f) / 0.65f
            drawLine(
                color = color,
                start = Offset(w * 0.44f, h * 0.80f),
                end = Offset(
                    w * 0.44f + (w * 0.40f) * p2,
                    h * 0.80f - (h * 0.56f) * p2
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun RuledLinesBackground(
    lineSpacing: Dp = 32.dp,
    lineColor: Color = PadBlue,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacingPx = lineSpacing.toPx()
            var y = spacingPx
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += spacingPx
            }
        }
        content()
    }
}

@Composable
fun HandDrawnWavyUnderline(
    color: Color = CheckerRed,
    animate: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (animate) {
            progress.animateTo(1f, animationSpec = tween(600))
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = progress.value
        val strokeWidth = 2.5.dp.toPx()

        val wavePath = Path()
        val waveCount = 8
        val waveWidth = w / waveCount
        val baseline = h / 2f
        val amplitude = h * 0.35f

        val currentMaxX = w * p
        wavePath.moveTo(0f, baseline)

        for (i in 0 until waveCount) {
            val startX = i * waveWidth
            val midX = startX + waveWidth / 2f
            val endX = startX + waveWidth

            if (midX <= currentMaxX) {
                val controlY1 = if (i % 2 == 0) baseline - amplitude else baseline + amplitude
                wavePath.quadraticTo(startX + waveWidth / 4f, controlY1, midX, baseline)
            } else if (startX < currentMaxX) {
                val subProgress = (currentMaxX - startX) / (waveWidth / 2f)
                val currentX = startX + (waveWidth / 2f) * subProgress
                val currentY = baseline - (if (i % 2 == 0) amplitude else -amplitude) * subProgress
                wavePath.lineTo(currentX, currentY)
                break
            }

            if (endX <= currentMaxX) {
                val controlY2 = if (i % 2 == 0) baseline + amplitude else baseline - amplitude
                wavePath.quadraticTo(midX + waveWidth / 4f, controlY2, endX, baseline)
            } else if (midX < currentMaxX) {
                val subProgress = (currentMaxX - midX) / (waveWidth / 2f)
                val currentX = midX + (waveWidth / 2f) * subProgress
                val currentY = baseline + (if (i % 2 == 0) amplitude else -amplitude) * subProgress
                wavePath.lineTo(currentX, currentY)
                break
            }
        }

        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
