package com.hisaab.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

/**
 * BrandLogoImage — loads institution/merchant logos from DuckDuckGo Favicon API.
 * Faster and more reliable than Clearbit for mobile requests.
 *
 * URL Format: https://icons.duckduckgo.com/ip3/{domain}.ico
 */

private val domainMap = mapOf(
    "HBL" to "hbl.com",
    "JAZZCASH" to "jazzcash.com.pk",
    "EASYPAISA" to "easypaisa.com.pk",
    "MEEZAN" to "meezanbank.com",
    "NETFLIX" to "netflix.com",
    "CAREEM" to "careem.com",
    "DARAZ" to "daraz.pk",
    "LESCO" to "lesco.com.pk"
)

@Composable
fun BrandLogoImage(
    institutionName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val sanitizedKey = institutionName.uppercase().trim()
    val matchedKey = domainMap.keys.firstOrNull { sanitizedKey.contains(it) }
    val domain = domainMap[matchedKey]

    if (domain != null) {
        var showFallback by androidx.compose.runtime.mutableStateOf(false)

        if (showFallback) {
            LiveFallbackCircle(institutionName, modifier, size)
        } else {
            AsyncImage(
                // DuckDuckGo's high-speed icon delivery network
                model = "https://icons.duckduckgo.com/ip3/$domain.ico",
                contentDescription = "$institutionName Logo",
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Fit,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) {
                        showFallback = true
                    }
                }
            )
        }
    } else {
        LiveFallbackCircle(institutionName, modifier, size)
    }
}

@Composable
private fun LiveFallbackCircle(name: String, modifier: Modifier, size: Dp) {
    val upper = name.uppercase()
    val backgroundColor = when {
        upper.contains("JAZZ") -> Color(0xFFFFB300)
        upper.contains("MEEZ") -> Color(0xFF1B5E20)
        upper.contains("HBL") -> Color(0xFF00796B)
        else -> Color(0xFF424242)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
