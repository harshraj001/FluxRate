package com.oss.fluxrate.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphismBorder(color: Color = SurfaceBorder) = composed {
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
