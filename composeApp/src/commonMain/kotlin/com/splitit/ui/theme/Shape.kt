package com.splitit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Radios de esquina (§5.1)
object SplitItRadius {
    val extraSmall: Dp = 8.dp
    val small: Dp = 12.dp
    val medium: Dp = 20.dp
    val large: Dp = 28.dp
}

val SplitItShapes = Shapes(
    extraSmall = RoundedCornerShape(SplitItRadius.extraSmall),
    small = RoundedCornerShape(SplitItRadius.small),
    medium = RoundedCornerShape(SplitItRadius.medium),
    large = RoundedCornerShape(SplitItRadius.large),
)
