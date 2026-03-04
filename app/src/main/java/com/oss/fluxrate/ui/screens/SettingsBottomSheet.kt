package com.oss.fluxrate.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oss.fluxrate.ui.theme.SpaceGrotesk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentTheme: ThemePreference,
    currentMetalUnit: MetalWeightUnit,
    currentNumberFormat: NumberFormat,
    onThemeSelected: (ThemePreference) -> Unit,
    onMetalUnitSelected: (MetalWeightUnit) -> Unit,
    onNumberFormatSelected: (NumberFormat) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = config.screenWidthDp
    val screenHeightDp = config.screenHeightDp

    // Scale sizes based on screen width
    val hPadding = (screenWidthDp * 0.06f).dp  // 6% of screen width
    val sectionGap = if (isLandscape) (screenHeightDp * 0.03f).dp else 20.dp
    val chipGap = (screenWidthDp * 0.015f).dp.coerceIn(4.dp, 10.dp)
    val chipPadV = if (isLandscape) 6.dp else 10.dp
    val chipPadH = (screenWidthDp * 0.015f).dp.coerceIn(4.dp, 12.dp)
    val chipFontSize = if (screenWidthDp < 360) 10.sp else 12.sp
    val titleFontSize = if (screenWidthDp < 360) 14.sp else 18.sp
    val labelFontSize = if (screenWidthDp < 360) 9.sp else 11.sp
    val bottomPad = if (isLandscape) 16.dp else 28.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.surfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = hPadding)
                .padding(bottom = bottomPad),
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            // Title
            Text(
                text = "SETTINGS",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            // ── Theme ──
            SettingsSection(title = "THEME", labelSize = labelFontSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(chipGap)
                ) {
                    ThemePreference.entries.forEach { pref ->
                        val icon = when (pref) {
                            ThemePreference.SYSTEM -> "⚙"
                            ThemePreference.LIGHT -> "☼"
                            ThemePreference.DARK -> "☾"
                        }
                        SettingsChip(
                            label = "$icon ${pref.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            isSelected = pref == currentTheme,
                            onClick = { onThemeSelected(pref) },
                            modifier = Modifier.weight(1f),
                            verticalPadding = chipPadV,
                            horizontalPadding = chipPadH,
                            fontSize = chipFontSize
                        )
                    }
                }
            }

            // ── Metal Weight Unit ──
            SettingsSection(title = "METAL WEIGHT UNIT", labelSize = labelFontSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(chipGap)
                ) {
                    MetalWeightUnit.entries.forEach { unit ->
                        SettingsChip(
                            label = unit.label,
                            isSelected = unit == currentMetalUnit,
                            onClick = { onMetalUnitSelected(unit) },
                            modifier = Modifier.weight(1f),
                            verticalPadding = chipPadV,
                            horizontalPadding = chipPadH,
                            fontSize = chipFontSize
                        )
                    }
                }
            }

            // ── Number Format ──
            SettingsSection(title = "NUMBER FORMAT", labelSize = labelFontSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(chipGap)
                ) {
                    NumberFormat.entries.forEach { fmt ->
                        val example = when (fmt) {
                            NumberFormat.INTERNATIONAL -> "1,234,567"
                            NumberFormat.INDIAN -> "12,34,567"
                        }
                        SettingsChip(
                            label = "${fmt.label}\n$example",
                            isSelected = fmt == currentNumberFormat,
                            onClick = { onNumberFormatSelected(fmt) },
                            modifier = Modifier.weight(1f),
                            verticalPadding = chipPadV,
                            horizontalPadding = chipPadH,
                            fontSize = chipFontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    labelSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = labelSize,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun SettingsChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    verticalPadding: androidx.compose.ui.unit.Dp = 10.dp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 8.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = SpaceGrotesk,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSize,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
