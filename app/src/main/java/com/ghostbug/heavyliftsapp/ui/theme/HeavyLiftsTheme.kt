package com.ghostbug.heavyliftsapp.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.ghostbug.heavyLifts.R

object HeavyLiftsColors {
    val Olive900 = Color(0xFF2F2E25)
//    val Olive800 = Color(0xFF3B3A30)
    val Olive800 = Color(0xFF32312D)
//    val Olive700 = Color(0xFF4D4B3D)
    val Olive700 = Color(0xFF2B2A27)
//    val Olive600 = Color(0xFF5C5949)
    val Olive600 = Color(0xFF3A3934)
    val Olive500 = Color(0xFF6F6C5D)
    val Olive400 = Color(0xFF8A8676)
    val Olive300 = Color(0xFFA8A494)

    val Cream50  = Color(0xFFFDFAF2)
    val Cream100 = Color(0xFFF4EFE2)
    val Cream200 = Color(0xFFE5DFCF)
    val Cream300 = Color(0xFFCFC9B7)
    val Cream400 = Color(0xFFA8A494)

//    val Orange500 = Color(0xFFF38C39)
    val Orange500 = Color(0xFFE07A5F)
    val Orange600 = Color(0xFFE07720)
    val Orange700 = Color(0xFFB85C12)
    val OrangeSoft = Color(0x29F38C39)

    val Blue500  = Color(0xFF659DD0)
    val Green500 = Color(0xFF91B566)
    val Red500   = Color(0xFFE05B4A)

    val Bg            = Olive700
    val BgElevated    = Olive600
    val BgOverlay     = Olive800
    val BgChip        = Olive500
    val Fg1           = Cream50
    val Fg2           = Cream100
    val Fg3           = Cream300
    val Fg4           = Cream400
    val Accent        = Orange500
    val AccentSoft    = OrangeSoft
    val Success       = Green500
    val Info          = Blue500
    val Danger        = Red500
    val BorderSubtle  = Color(0x14FDFAF2)
    val BorderDefault = Color(0x24FDFAF2)
    val BorderStrong  = Color(0x3DFDFAF2)
    val Scrim         = Color(0x8C161510)
}

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val fredoka = GoogleFont("Fredoka")

private val nunito  = GoogleFont("Nunito")

object HeavyLiftsType {
    val Display = FontFamily(
        Font(googleFont = fredoka, fontProvider = fontProvider, weight = FontWeight.Normal),
        Font(googleFont = fredoka, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = fredoka, fontProvider = fontProvider, weight = FontWeight.Bold),
    )

    val Body = FontFamily(
        Font(googleFont = nunito, fontProvider = fontProvider, weight = FontWeight.Normal),
        Font(googleFont = nunito, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = nunito, fontProvider = fontProvider, weight = FontWeight.Bold),
    )
}