package com.oss.fluxrate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oss.fluxrate.data.repository.RateItem
import com.oss.fluxrate.data.repository.RateType
import com.oss.fluxrate.ui.theme.*
import com.oss.fluxrate.ui.util.FlagMapper
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionBottomSheet(
    rates: List<RateItem>,
    metalWeightUnit: MetalWeightUnit,
    onDismiss: () -> Unit,
    onSelect: (RateItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredRates = remember(searchQuery, rates) {
        if (searchQuery.isBlank()) rates
        else rates.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val hPadding = (screenWidthDp * 0.06f).dp.coerceIn(16.dp, 32.dp)
    val titleSize = if (screenWidthDp < 360) 18.sp else 22.sp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.surfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPadding)
        ) {
            Text(
                text = "SELECT CURRENCY",
                style = Typography.titleLarge,
                fontSize = titleSize,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRates, key = { it.code }) { rate ->
                    CurrencyListItem(
                        rateItem = rate,
                        metalWeightUnit = metalWeightUnit,
                        onClick = {
                            onSelect(rate)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyListItem(rateItem: RateItem, metalWeightUnit: MetalWeightUnit, onClick: () -> Unit) {
    val iconColor = when (rateItem.type) {
        RateType.CRYPTO -> MaterialTheme.colorScheme.secondary
        RateType.METAL -> Color(0xFFD4A017)
        else -> MaterialTheme.colorScheme.primary
    }
    val flagResId = FlagMapper.getFlagResId(rateItem.code)

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isSmallScreen = screenWidthDp < 360
    val iconSize = (screenWidthDp * 0.1f).dp.coerceIn(32.dp, 48.dp)
    val hPad = (screenWidthDp * 0.04f).dp.coerceIn(12.dp, 24.dp)
    val vPad = (screenWidthDp * 0.04f).dp.coerceIn(12.dp, 20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = hPad, vertical = vPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (flagResId != 0) {
            Image(
                painter = painterResource(id = flagResId),
                contentDescription = "${rateItem.code} flag",
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rateItem.code.take(1),
                    color = iconColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rateItem.code,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = if (isSmallScreen) 14.sp else 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = rateItem.name,
                style = Typography.labelSmall,
                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = when (rateItem.type) {
                RateType.CRYPTO -> "Crypto"
                RateType.METAL -> "Metal · ${metalWeightUnit.label.lowercase()}"
                else -> "Fiat"
            },
            style = Typography.labelSmall,
            color = iconColor
        )
    }
}
