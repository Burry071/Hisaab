package com.hisaab.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.data.agent.HisaabAgentResponse
import com.hisaab.presentation.ui.theme.HisaabTheme
import kotlinx.coroutines.launch

@Composable
fun BudgetSimulatorCard(
    onSimulate: suspend (String) -> Unit,
    modifier: Modifier = Modifier,
    result: HisaabAgentResponse? = null
) {
    var query by remember { mutableStateOf("") }
    var isSimulating by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusLg))
            .background(HisaabTheme.Surface)
            .border(
                width = 1.dp,
                color = HisaabTheme.Purple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(HisaabTheme.RadiusLg)
            )
            .drawBehind {
                val w = size.width
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(HisaabTheme.Purple.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w / 2f, 0f),
                        radius = w * 0.8f
                    ),
                    radius = w * 0.8f,
                    center = Offset(w / 2f, 0f)
                )
            }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = HisaabTheme.Purple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "What-If Simulator",
                    style = HisaabTheme.TypographyTitle.copy(
                        color = HisaabTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }

            Text(
                text = "Ask Gemini to simulate financial scenarios. e.g., 'What if I buy a car for 20 Lakhs?'",
                style = HisaabTheme.TypographyBody.copy(
                    color = HisaabTheme.TextSecondary,
                    fontSize = 13.sp
                )
            )

            // Input Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Simulate a scenario...", color = HisaabTheme.TextMuted, fontSize = 14.sp)
                },
                trailingIcon = {
                    if (isSimulating) {
                        CircularProgressIndicator(
                            color = HisaabTheme.Purple,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (query.isNotBlank()) {
                                    focusManager.clearFocus()
                                    isSimulating = true
                                    scope.launch {
                                        onSimulate(query)
                                        isSimulating = false
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Simulate",
                                tint = HisaabTheme.Purple
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HisaabTheme.Purple,
                    unfocusedBorderColor = HisaabTheme.BorderSubtle,
                    focusedTextColor = HisaabTheme.TextPrimary,
                    unfocusedTextColor = HisaabTheme.TextPrimary,
                    cursorColor = HisaabTheme.Purple
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (query.isNotBlank()) {
                        focusManager.clearFocus()
                        isSimulating = true
                        scope.launch {
                            onSimulate(query)
                            isSimulating = false
                        }
                    }
                }),
                shape = RoundedCornerShape(12.dp)
            )

            // Structured Result
            AnimatedVisibility(
                visible = result != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (result != null) {
                    SimulatorResultCard(result)
                }
            }
        }
    }
}

@Composable
fun SimulatorResultCard(result: HisaabAgentResponse) {
    val feasColor = when (result.feasibility) {
        "YES" -> HisaabTheme.Teal
        "PARTIAL" -> HisaabTheme.Amber
        else -> HisaabTheme.Red
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HisaabTheme.SurfaceElev)
            .border(1.dp, HisaabTheme.Purple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = feasColor.copy(0.15f)
            ) {
                Text(
                    text = result.feasibility ?: "PARTIAL",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = HisaabTheme.TypographyTrace.copy(
                        color = feasColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
            
            Text(
                text = "${result.confidenceScore}% confidence",
                style = HisaabTheme.TypographyTrace.copy(
                    color = HisaabTheme.TextMuted,
                    fontSize = 11.sp
                )
            )
        }

        Text(
            text = result.title,
            style = HisaabTheme.TypographyBody.copy(
                color = HisaabTheme.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        )

        Text(
            text = result.simulatorBreakdown ?: result.analysis,
            style = HisaabTheme.TypographyTrace.copy(
                color = HisaabTheme.TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        )

        HorizontalDivider(color = HisaabTheme.BorderSubtle, thickness = 0.5.dp)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Recommendation",
                style = HisaabTheme.TypographyTrace.copy(
                    color = HisaabTheme.Purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
            Text(
                text = result.actionableRecommendation,
                style = HisaabTheme.TypographyBody.copy(
                    color = HisaabTheme.TextPrimary,
                    fontSize = 14.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HisaabTheme.Purple.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            Text(
                text = "Impact: ${result.projectedImpact}",
                style = HisaabTheme.TypographyBody.copy(
                    color = HisaabTheme.Purple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
        }
    }
}
