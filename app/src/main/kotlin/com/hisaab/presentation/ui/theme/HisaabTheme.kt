package com.hisaab.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Hisaab Design Tokens — single source of truth for the entire UI.
 *
 * RULES (from DESIGN.md — non-negotiable):
 *   1. Purple = only action color, once per screen
 *   2. Teal = income/success only. Amber = warning only. Red = danger only.
 *   3. NO shadows. Elevation via surface lightness only.
 *   4. Agent trace: JetBrains Mono, tree prefix format.
 *   5. Balance display: Clash Display only. One per screen.
 */
object HisaabTheme {

    // ── Colors ────────────────────────────────────────────────────────────────

    // Backgrounds — tonal elevation ladder
    val BgBase      = Color(0xFF0B0D11)   // Absolute matte black/charcoal
    val BgSecondary = Color(0xFF0B0D11)   // Same as base for minimalist look
    val Surface     = Color(0xFF161920)   // Card/surface background
    val SurfaceElev = Color(0xFF1C1F26)   // Slightly lighter elevation
    val SurfaceOver = Color(0xFF222733)   // Overlays

    // Accents — refactored to new design spec
    val Purple      = Color(0xFFD6C5F0)   // Soothing pastel violet (Lavender)
    val PurpleDim   = Color(0xFFB4A5D0)   // Darker lavender
    val Teal        = Color(0xFF81C784)   // Tactical terminal green (Income)
    val TealDim     = Color(0xFF66BB6A)   // Green variant
    val Amber       = Color(0xFFFFD54F)   // Soft amber
    val AmberDim    = Color(0xFFFBC02D)   // Darker amber
    val Red         = Color(0xFFE57373)   // Desaturated coral red (Expense)

    // Text
    val TextPrimary   = Color(0xFFE2E2E9)   // Crisp off-white
    val TextSecondary = Color(0xFF8E9AA8)   // Tactical slate gray
    val TextMuted     = Color(0xFF5A6370)   // Muted gray
    val TextDisabled  = Color(0xFF353B45)

    // Border
    val BorderSubtle  = Color(0xFF222733)   // Subtle stroke

    // Institution brand colors — NEVER approximate
    val HBL       = Color(0xFF006B3C)
    val JazzCash  = Color(0xFFD4002A)
    val Easypaisa = Color(0xFF43B02A)
    val NayaPay   = Color(0xFF5B2D8E)
    val SadaPay   = Color(0xFF0066FF)
    val Meezan    = Color(0xFF006747)
    val Alfalah   = Color(0xFFE4002B)
    val MCB       = Color(0xFF009B77)
    val UPaisa    = Color(0xFFFF6B00)
    val Zindigi   = Color(0xFF6C2BD9)
    val UBL       = Color(0xFF1E3A5F)

    fun institutionColor(name: String): Color = when {
        name.contains("HBL", ignoreCase = true)       -> HBL
        name.contains("JazzCash", ignoreCase = true)  -> JazzCash
        name.contains("Easypaisa", ignoreCase = true) -> Easypaisa
        name.contains("NayaPay", ignoreCase = true)   -> NayaPay
        name.contains("SadaPay", ignoreCase = true)   -> SadaPay
        name.contains("Meezan", ignoreCase = true)    -> Meezan
        name.contains("Alfalah", ignoreCase = true)   -> Alfalah
        name.contains("MCB", ignoreCase = true)       -> MCB
        name.contains("UBL", ignoreCase = true)       -> UBL
        name.contains("UPaisa", ignoreCase = true)    -> UPaisa
        name.contains("Zindigi", ignoreCase = true)   -> Zindigi
        else                                          -> TextSecondary
    }

    // ── Typography ────────────────────────────────────────────────────────────

    // Font families (loaded via res/font/ or downloadable fonts)
    val ClashDisplay  = FontFamily.Default   // Replace with actual Clash Display font
    val DmSans        = FontFamily.Default   // Replace with actual DM Sans font
    val JetBrainsMono = FontFamily.Monospace // Replace with actual JetBrains Mono font

    // Text styles
    val TypographyDisplay = TextStyle(
        fontFamily  = FontFamily.SansSerif,
        fontWeight  = FontWeight.Light,
        fontSize    = 36.sp,
        lineHeight  = 44.sp,
        letterSpacing = (-0.5).sp,
    )
    val TypographyHeadline = TextStyle(
        fontFamily  = ClashDisplay,
        fontWeight  = FontWeight.Bold,
        fontSize    = 24.sp,
        lineHeight  = 32.sp,
        letterSpacing = (-1).sp,
    )
    val TypographyTitle = TextStyle(
        fontFamily  = DmSans,
        fontWeight  = FontWeight.Medium,
        fontSize    = 17.sp,
        lineHeight  = 22.sp,
    )
    val TypographyBody = TextStyle(
        fontFamily  = DmSans,
        fontWeight  = FontWeight.Normal,
        fontSize    = 15.sp,
        lineHeight  = 22.5.sp,
    )
    val TypographyCaption = TextStyle(
        fontFamily  = DmSans,
        fontWeight  = FontWeight.Normal,
        fontSize    = 13.sp,
        lineHeight  = 18.sp,
        color       = TextSecondary,
    )
    val TypographyLabelMicro = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
    /** Agent trace output — JetBrains Mono. MANDATORY per DESIGN.md. */
    val TypographyTrace = TextStyle(
        fontFamily  = JetBrainsMono,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 16.8.sp,
        letterSpacing = 0.24.sp,
    )

    // ── Dimensions ────────────────────────────────────────────────────────────
    val RadiusSm   = 12.dp
    val RadiusMd   = 18.dp
    val RadiusLg   = 24.dp
    val RadiusXl   = 28.dp
    val RadiusPill = 28.dp

    val SpaceXs  = 4.dp
    val SpaceSm  = 8.dp
    val SpaceMd  = 16.dp
    val SpaceLg  = 24.dp
    val SpaceXl  = 32.dp
    val SpaceXxl = 48.dp
}

/** MaterialTheme wrapper — use in MainActivity setContent {} */
@Composable
fun HisaabTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary      = HisaabTheme.Purple,
        secondary    = HisaabTheme.Teal,
        tertiary     = HisaabTheme.Amber,
        background   = HisaabTheme.BgBase,
        surface      = HisaabTheme.Surface,
        onPrimary    = Color.White,
        onBackground = HisaabTheme.TextPrimary,
        onSurface    = HisaabTheme.TextPrimary,
        error        = HisaabTheme.Red,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
