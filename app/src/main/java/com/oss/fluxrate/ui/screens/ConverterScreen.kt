package com.oss.fluxrate.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import com.oss.fluxrate.data.repository.RateItem
import com.oss.fluxrate.data.repository.RateType
import com.oss.fluxrate.ui.theme.*
import com.oss.fluxrate.ui.util.FlagMapper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    uiState: ConverterUiState,
    onInputChanged: (String) -> Unit,
    onSwap: () -> Unit,
    onOpenCurrencySelector: (isFrom: Boolean) -> Unit,
    onRefresh: () -> Unit,
    onThemeSelected: (ThemePreference) -> Unit,
    onMetalUnitSelected: (MetalWeightUnit) -> Unit,
    onNumberFormatSelected: (NumberFormat) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    
    val hPadding = (screenWidthDp * 0.06f).dp.coerceIn(16.dp, 32.dp)
    val vPadding = if (isLandscape) (screenHeightDp * 0.02f).dp else (screenHeightDp * 0.03f).dp
    val titleSize = if (screenWidthDp < 360) 18.sp else 22.sp

    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .blur(if (showSettings) 16.dp else 0.dp)
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(top = vPadding, bottom = vPadding, start = hPadding, end = hPadding)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FluxRate",
                fontFamily = AfterRegular,
                fontSize = titleSize,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Settings Button
                IconButton(onClick = { showSettings = true }) {
                    Text(
                        text = "⚙",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                }
                if (showSettings) {
                    SettingsBottomSheet(
                        currentTheme = uiState.themePreference,
                        currentMetalUnit = uiState.metalWeightUnit,
                        currentNumberFormat = uiState.numberFormat,
                        onThemeSelected = onThemeSelected,
                        onMetalUnitSelected = onMetalUnitSelected,
                        onNumberFormatSelected = onNumberFormatSelected,
                        onDismiss = { showSettings = false }
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Sync Button with rotation animation
                SyncButton(
                    isSyncing = uiState.isLoading,
                    error = uiState.error,
                    onClick = onRefresh
                )
            }
        }

        val spacerHeight = if (isLandscape) (screenHeightDp * 0.03f).dp else (screenHeightDp * 0.04f).dp
        Spacer(modifier = Modifier.height(spacerHeight))

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Conversion Area
                Box(modifier = Modifier.weight(1f)) {
                    ConversionArea(uiState, isLandscape, onOpenCurrencySelector, onSwap)
                }
                
                // Right Side: Numpad
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Numpad(
                        isLandscape = isLandscape,
                        modifier = Modifier.fillMaxSize(),
                        onKeyPress = { key ->
                            handleNumpadPress(key, uiState.inputAmount, onInputChanged)
                        }
                    )
                }
            }
        } else {
            // Portrait Layout
            // Conversion Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                ConversionArea(uiState, isLandscape, onOpenCurrencySelector, onSwap)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Numpad
            Numpad(
                modifier = Modifier.weight(1.3f).fillMaxWidth(),
                onKeyPress = { key ->
                    handleNumpadPress(key, uiState.inputAmount, onInputChanged)
                }
            )
        }
    }
}

private fun handleNumpadPress(key: String, currentAmount: String, onInputChanged: (String) -> Unit) {
    if (key == "DEL") {
        if (currentAmount.isNotEmpty()) {
            val newStr = currentAmount.dropLast(1)
            onInputChanged(newStr.ifEmpty { "0" })
        }
    } else {
        val current = if (currentAmount == "0" && key != ".") "" else currentAmount
        onInputChanged(current + key)
    }
}

@Composable
private fun ConversionArea(
    uiState: ConverterUiState,
    isLandscape: Boolean,
    onOpenCurrencySelector: (isFrom: Boolean) -> Unit,
    onSwap: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val spacing = if (isLandscape) (screenHeightDp * 0.02f).dp else (screenHeightDp * 0.02f).dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // FROM Block
            CurrencyBlock(
                rateItem = uiState.fromCurrency,
                amount = formatWithCommas(uiState.inputAmount, uiState.numberFormat),
                isOutput = false,
                isLandscape = isLandscape,
                metalWeightUnit = uiState.metalWeightUnit,
                modifier = Modifier.weight(1f),
                onClick = { onOpenCurrencySelector(true) }
            )

            // TO Block
            val calculatedOutput = calculateConversion(
                amount = uiState.inputAmount,
                from = uiState.fromCurrency,
                to = uiState.toCurrency,
                metalWeightUnit = uiState.metalWeightUnit,
                numberFormat = uiState.numberFormat
            )

            CurrencyBlock(
                rateItem = uiState.toCurrency,
                amount = calculatedOutput,
                isOutput = true,
                isLandscape = isLandscape,
                metalWeightUnit = uiState.metalWeightUnit,
                modifier = Modifier.weight(1f),
                onClick = { onOpenCurrencySelector(false) }
            )
        }

        // Swap Button exactly between them
        var swapRotation by remember { mutableFloatStateOf(0f) }
        val animatedSwapRotation by androidx.compose.animation.core.animateFloatAsState(
            targetValue = swapRotation,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            label = "swap"
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    swapRotation += 180f
                    onSwap()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↕", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 24.sp,
                modifier = Modifier.rotate(animatedSwapRotation)
            )
        }
    }
}

@Composable
fun SyncButton(isSyncing: Boolean, error: String?, onClick: () -> Unit) {
    // Track if we've completed at least one sync
    var hasSynced by remember { mutableStateOf(false) }
    
    LaunchedEffect(isSyncing) {
        if (!isSyncing && hasSynced) {
            // Sync just finished
        }
        if (isSyncing) {
            hasSynced = true
        }
    }

    val showSynced = !isSyncing && hasSynced && error == null
    val showFailed = !isSyncing && hasSynced && error != null

    val labelText = when {
        isSyncing -> "SYNCING"
        showFailed -> "FAILED"
        showSynced -> "SYNCED"
        else -> "SYNC"
    }
    val labelColor = when {
        showFailed -> MaterialTheme.colorScheme.error
        showSynced -> MaterialTheme.colorScheme.primary
        isSyncing -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !isSyncing, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .width(IntrinsicSize.Min) // or just fixed values
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync",
                    tint = labelColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = labelText,
            style = Typography.labelSmall,
            color = labelColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(54.dp)
        )
    }
}

fun formatWithCommas(raw: String, numberFormat: NumberFormat = NumberFormat.INTERNATIONAL): String {
    if (raw.isEmpty()) return "0"
    val parts = raw.split(".")
    val intPart = parts[0]
    val formatted = when (numberFormat) {
        NumberFormat.INDIAN -> formatIndian(intPart)
        NumberFormat.INTERNATIONAL -> intPart.reversed().chunked(3).joinToString(",").reversed()
    }
    return if (parts.size > 1) "$formatted.${parts[1]}" else formatted
}

private fun formatIndian(intPart: String): String {
    if (intPart.length <= 3) return intPart
    val last3 = intPart.takeLast(3)
    val rest = intPart.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

fun calculateConversion(
    amount: String,
    from: RateItem?,
    to: RateItem?,
    metalWeightUnit: MetalWeightUnit = MetalWeightUnit.OUNCE,
    numberFormat: NumberFormat = NumberFormat.INTERNATIONAL
): String {
    if (amount.isBlank() || from == null || to == null) return "0.00"
    val valDouble = amount.toDoubleOrNull() ?: 0.0
    var valInUsd = valDouble * from.rateToUsd
    var valInTarget = valInUsd / to.rateToUsd

    // Apply metal weight conversion if either currency is a metal
    if (from.type == RateType.METAL && metalWeightUnit != MetalWeightUnit.OUNCE) {
        // User is converting FROM metal: input is in user's chosen unit
        // API rate is per troy oz, so convert input to troy oz first
        valInUsd = (valDouble / metalWeightUnit.conversionFromOz) * from.rateToUsd
        valInTarget = valInUsd / to.rateToUsd
    } else if (to.type == RateType.METAL && metalWeightUnit != MetalWeightUnit.OUNCE) {
        // User is converting TO metal: output should be in user's chosen unit
        valInTarget = valInTarget * metalWeightUnit.conversionFromOz
    }

    // Format with number format preference
    val symbols = DecimalFormatSymbols(java.util.Locale.US)
    val pattern = when (numberFormat) {
        NumberFormat.INDIAN -> "##,##,##0.####"
        NumberFormat.INTERNATIONAL -> "#,##0.####"
    }
    val df = DecimalFormat(pattern, symbols)
    return df.format(valInTarget)
}

@Composable
fun CurrencyBlock(
    rateItem: RateItem?,
    amount: String,
    isOutput: Boolean,
    isLandscape: Boolean = false,
    metalWeightUnit: MetalWeightUnit = MetalWeightUnit.OUNCE,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (isOutput) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val hPad = (screenWidthDp * 0.04f).dp.coerceIn(12.dp, 24.dp)
    val vPad = if (isLandscape) 12.dp else 16.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isLandscape) 16.dp else 24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = hPad, vertical = vPad)
    ) {
        val availableHeight = this.maxHeight.value
        val iconSizeVal = (availableHeight * 0.35f).coerceIn(24f, 48f)
        val iconSize = iconSizeVal.dp
        val titleSize = (iconSizeVal * 0.5f).coerceIn(16f, 24f).sp
        val subtitleSize = (iconSizeVal * 0.35f).coerceIn(10f, 14f).sp
        val dropIconSize = (iconSizeVal * 0.5f).coerceIn(14f, 24f).sp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().weight(0.4f)
            ) {
                // Flag or Letter Icon
                val flagResId = rateItem?.let { FlagMapper.getFlagResId(it.code) } ?: 0
                if (flagResId != 0) {
                    Image(
                        painter = painterResource(id = flagResId),
                        contentDescription = "${rateItem?.code} flag",
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isOutput) MaterialTheme.colorScheme.secondary.copy(alpha=0.3f) else MaterialTheme.colorScheme.primary.copy(alpha=0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rateItem?.code?.take(1) ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = titleSize
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rateItem?.code ?: "---",
                        style = Typography.titleLarge,
                        fontSize = titleSize,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rateItem?.name ?: "Select currency",
                            style = Typography.labelSmall,
                            fontSize = subtitleSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (rateItem != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            val typeColor = when (rateItem.type) {
                                RateType.CRYPTO -> MaterialTheme.colorScheme.secondary
                                RateType.METAL -> Color(0xFFD4A017)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Text(
                                text = when (rateItem.type) {
                                    RateType.CRYPTO -> "CRYPTO"
                                    RateType.METAL -> "METAL · ${metalWeightUnit.label.lowercase()}"
                                    else -> "FIAT"
                                },
                                style = Typography.labelSmall,
                                fontSize = (subtitleSize.value * 0.8f).coerceIn(8f, 12f).sp,
                                color = typeColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(typeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text("▼", fontSize = dropIconSize, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Animated Ticker for Amount — horizontally scrollable
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(0.6f).padding(top = 4.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                val numberMaxHeight = this.maxHeight.value
                val targetFontSize = (numberMaxHeight * 0.85f).coerceIn(28f, 90f).sp

                val scrollState = rememberScrollState()
                LaunchedEffect(amount) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                AnimatedContent(
                    targetState = amount,
                    transitionSpec = {
                        slideInVertically(animationSpec = tween(150)) { height -> height } togetherWith
                                slideOutVertically(animationSpec = tween(150)) { height -> -height }
                    }, label = "AmountAnim"
                ) { targetAmount ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = targetAmount,
                            style = Typography.displayLarge,
                            fontSize = targetFontSize,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Numpad(isLandscape: Boolean = false, modifier: Modifier = Modifier, onKeyPress: (String) -> Unit) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        ".", "0", "DEL"
    )

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val spacing = if (isLandscape) 8.dp else (screenHeightDp * 0.015f).dp.coerceIn(8.dp, 16.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (row in keys.chunked(3)) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (key in row) {
                    NumpadKey(
                        key = key,
                        isLandscape = isLandscape,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onKeyPress(key) }
                    )
                }
            }
        }
    }
}

@Composable
fun NumpadKey(key: String, isLandscape: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isAction = key == "DEL"
    val textColor = if (isAction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(if (isLandscape) 16.dp else 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val keyHeight = this.maxHeight.value
        val fontSizeDynamic = (keyHeight * 0.35f).coerceIn(16f, 48f)
        Text(
            text = if (isAction) "⌫" else key,
            fontSize = if (isAction) (fontSizeDynamic * 0.8f).sp else fontSizeDynamic.sp,
            color = textColor,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Normal
        )
    }
}
