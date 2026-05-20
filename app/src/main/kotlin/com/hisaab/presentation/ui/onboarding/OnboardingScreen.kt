package com.hisaab.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.presentation.ui.components.HisaabBrandLogo
import com.hisaab.presentation.ui.theme.HisaabTheme

@Composable
fun HisaabOnboardingScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HisaabTheme.BgBase)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. High-fidelity central vector asset mark
        HisaabBrandLogo(size = 96.dp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 2. Main Title - Lightweight, highly tracked display typography
        Text(
            text = "HISAAB",
            color = HisaabTheme.TextPrimary,
            style = HisaabTheme.TypographyDisplay.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = 8.sp,
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "AUTONOMOUS SYSTEM ENGINE",
            color = HisaabTheme.TextSecondary,
            style = HisaabTheme.TypographyLabelMicro.copy(
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 3. Action Pill Button
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(HisaabTheme.RadiusPill)
        ) {
            Text(
                text = "INITIALIZE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
