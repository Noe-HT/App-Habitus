package com.app.habitus.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// MARCA — Dirección B (expresiva/cálida)
//
// El verde deja de ser "uno más entre los colores de hábito" y pasa a
// ser el color de identidad de la propia app: aparece en el botón
// principal, en la barra de navegación seleccionada y en el anillo de
// progreso. "Spark" es el acento cálido reservado para todo lo que
// tenga que ver con la racha y la celebración — es el color que
// "premia" al usuario, deliberadamente distinto del verde de marca
// para que el momento de racha se sienta especial, no rutinario.
// ============================================================

val BrandPrimary = Color(0xFF0F9D6A)       // verde más rico y profundo que el mint anterior
val BrandPrimaryDark = Color(0xFF34D399)   // se aclara en modo oscuro (más luminoso sobre fondo oscuro)
val BrandPrimarySoft = Color(0xFFDCF5EA)   // fondo suave para chips/estados sobre superficie clara
val BrandPrimarySoftDark = Color(0xFF163429)

val Spark = Color(0xFFFF8A3D)              // acento cálido: racha, celebración, "🔥"
val SparkSoft = Color(0xFFFFE9D9)
val SparkSoftDark = Color(0xFF3D2716)

// Texto/iconos sobre el verde de marca en modo oscuro: como BrandPrimaryDark
// es un verde claro y brillante (para destacar sobre fondo oscuro), el
// contenido que va encima necesita ser oscuro para tener contraste real.
val OnBrandPrimaryDark = Color(0xFF0B2118)

// Fondo general — blanco roto cálido en vez de blanco puro, más carácter
val AppBackground = Color(0xFFFFFBF5)

val CardBackground = Color(0xFFF7F1E8)
val CardBackgroundSoft = Color(0xFFFDF9F2)
val BorderColor = Color(0xFFE3D9C8)

// Texto
val TextPrimary = Color(0xFF241F16)
val TextSecondary = Color(0xFF7A705E)
val TextMuted = Color(0xFFA79C87)

// --- Modo oscuro: contrapartida de los tokens neutros de arriba ---
val AppBackgroundDark = Color(0xFF141210)
val CardBackgroundDark = Color(0xFF201C17)
val CardBackgroundSoftDark = Color(0xFF262119)
val BorderColorDark = Color(0xFF3A342A)
val TextPrimaryDark = Color(0xFFF5F0E6)
val TextSecondaryDark = Color(0xFFB8AD98)

// Colores principales (heredan del verde de marca)
val PrimaryGreen = BrandPrimary
val PrimaryGreenDark = BrandPrimaryDark

val PrimaryBlue = Color(0xFF6B7FE8)
val PrimaryBlueDark = Color(0xFF8B9CFF)

val PrimaryPurple = Color(0xFFA06CE8)

// Estados
val ErrorSoft = Color(0xFFE5584A)
val White = Color(0xFFFFFFFF)

// Colores de hábito — más saturados/vivos que la paleta pastel anterior,
// coherentes con una dirección expresiva
val GreenHabit = Color(0xFF1FAE72)
val BlueHabit = Color(0xFF5D6EF0)
val PurpleHabit = Color(0xFF9C5FF0)
val AmberHabit = Color(0xFFF0A020)
val RoseHabit = Color(0xFFF0609C)
val TealHabit = Color(0xFF149E96)
val CoralHabit = Color(0xFFF2604A)
val SkyHabit = Color(0xFF2E9BD6)