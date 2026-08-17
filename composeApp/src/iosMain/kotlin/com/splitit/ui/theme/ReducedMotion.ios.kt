package com.splitit.ui.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun isReduceMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
