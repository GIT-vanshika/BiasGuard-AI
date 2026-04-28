package com.example.solutionchallenge.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Professional Elegant Palette
val PrimaryLight = Color(0xFF1A237E) // Deep Indigo
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE8EAF6)
val OnPrimaryContainerLight = Color(0xFF000051)

val SecondaryLight = Color(0xFF00695C) // Soft Teal
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE0F2F1)
val OnSecondaryContainerLight = Color(0xFF003D33)

val BackgroundLight = Color(0xFFF8F9FA)
val OnBackgroundLight = Color(0xFF202124)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF202124)

val ErrorLight = Color(0xFFB00020)
val OnErrorLight = Color(0xFFFFFFFF)

// Dark Mode Palette
val PrimaryDark = Color(0xFFC5CAE9)
val OnPrimaryDark = Color(0xFF000051)
val PrimaryContainerDark = Color(0xFF1A237E)
val OnPrimaryContainerDark = Color(0xFFE8EAF6)

val SecondaryDark = Color(0xFFB2DFDB)
val OnSecondaryDark = Color(0xFF003D33)
val SecondaryContainerDark = Color(0xFF004D40)
val OnSecondaryContainerDark = Color(0xFFE0F2F1)

val BackgroundDark = Color(0xFF121212)
val OnBackgroundDark = Color(0xFFE8EAED)
val SurfaceDark = Color(0xFF1E1E1E)
val OnSurfaceDark = Color(0xFFE8EAED)

val ErrorDark = Color(0xFFCF6679)
val OnErrorDark = Color(0xFF000000)

// Gradients
val PrimaryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB))
)

val SecondaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00695C), Color(0xFF4DB6AC))
)

val GlassyGradient = Brush.verticalGradient(
    colors = listOf(Color(0x22FFFFFF), Color(0x11FFFFFF))
)

val FairColor = Color(0xFF43A047)
val UnfairColor = Color(0xFFE53935)
val WarningColor = Color(0xFFFB8C00)