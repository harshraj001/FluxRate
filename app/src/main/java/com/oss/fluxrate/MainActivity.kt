package com.oss.fluxrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oss.fluxrate.data.network.NetworkModule
import com.oss.fluxrate.data.repository.RateRepository
import com.oss.fluxrate.ui.screens.ConverterScreen
import com.oss.fluxrate.ui.screens.CurrencySelectionBottomSheet
import com.oss.fluxrate.ui.screens.MainViewModel
import com.oss.fluxrate.ui.screens.SplashScreen
import com.oss.fluxrate.ui.theme.FluxRateTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = RateRepository(
                    primaryApi = NetworkModule.exchangeApi,
                     fallbackApi = NetworkModule.fallbackExchangeApi,
                    context = this@MainActivity.applicationContext
                )
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            
            val darkTheme = when (uiState.themePreference) {
                com.oss.fluxrate.ui.screens.ThemePreference.LIGHT -> false
                com.oss.fluxrate.ui.screens.ThemePreference.DARK -> true
                com.oss.fluxrate.ui.screens.ThemePreference.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            FluxRateTheme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }
                val uiState by viewModel.uiState.collectAsState()
                var isBottomSheetOpen by remember { mutableStateOf(false) }
                var isSelectingFromCurrency by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith
                                fadeOut(animationSpec = tween(400))
                    },
                    label = "SplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ConverterScreen(
                                uiState = uiState,
                                onInputChanged = viewModel::onInputAmountChanged,
                                onSwap = viewModel::onSwapCurrencies,
                                onOpenCurrencySelector = { isFrom ->
                                    isSelectingFromCurrency = isFrom
                                    isBottomSheetOpen = true
                                },
                                onRefresh = viewModel::onRefreshRates,
                                onThemeSelected = viewModel::setThemePreference,
                                onMetalUnitSelected = viewModel::setMetalWeightUnit,
                                onNumberFormatSelected = viewModel::setNumberFormat
                            )
                        }

                        if (isBottomSheetOpen) {
                            CurrencySelectionBottomSheet(
                                rates = uiState.rates,
                                metalWeightUnit = uiState.metalWeightUnit,
                                onDismiss = { isBottomSheetOpen = false },
                                onSelect = { rate ->
                                    viewModel.onSelectCurrency(rate, isSelectingFromCurrency)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}