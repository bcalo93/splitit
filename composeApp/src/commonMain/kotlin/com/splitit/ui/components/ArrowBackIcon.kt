package com.splitit.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowBackIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            lineTo(8.83f, 11f)
            lineTo(13.42f, 6.41f)
            lineTo(12f, 5f)
            lineTo(4f, 13f)
            lineTo(12f, 21f)
            lineTo(13.41f, 19.59f)
            lineTo(8.83f, 15f)
            lineTo(20f, 15f)
            close()
        }
    }.build()
}
