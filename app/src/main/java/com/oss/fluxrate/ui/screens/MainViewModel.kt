package com.oss.fluxrate.ui.screens

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oss.fluxrate.data.repository.RateItem
import com.oss.fluxrate.data.repository.RateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ThemePreference { SYSTEM, LIGHT, DARK }
enum class MetalWeightUnit(val label: String, val conversionFromOz: Double) {
    OUNCE("Troy oz", 1.0),
    GRAM("Gram", 31.1035),
    KILOGRAM("Kilogram", 0.0311035)
}
enum class NumberFormat(val label: String) {
    INTERNATIONAL("International"),
    INDIAN("Indian")
}

data class ConverterUiState(
    val rates: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val fromCurrency: RateItem? = null,
    val toCurrency: RateItem? = null,
    val inputAmount: String = "1",
    val selectedInputCurrencyId: String? = null,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val metalWeightUnit: MetalWeightUnit = MetalWeightUnit.OUNCE,
    val numberFormat: NumberFormat = NumberFormat.INTERNATIONAL
)

class MainViewModel(
    private val repository: RateRepository
) : ViewModel() {

    private val prefs: SharedPreferences = repository.prefs

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    // Load saved currency codes (default: USD → INR)
    private val savedFromCode: String get() = prefs.getString("selected_from", "USD") ?: "USD"
    private val savedToCode: String get() = prefs.getString("selected_to", "INR") ?: "INR"
    
    // Load saved preferences
    private val savedThemePref: ThemePreference get() {
        val name = prefs.getString("theme_pref", ThemePreference.SYSTEM.name) ?: ThemePreference.SYSTEM.name
        return try { ThemePreference.valueOf(name) } catch (e: Exception) { ThemePreference.SYSTEM }
    }
    private val savedMetalUnit: MetalWeightUnit get() {
        val name = prefs.getString("metal_unit", MetalWeightUnit.OUNCE.name) ?: MetalWeightUnit.OUNCE.name
        return try { MetalWeightUnit.valueOf(name) } catch (e: Exception) { MetalWeightUnit.OUNCE }
    }
    private val savedNumberFormat: NumberFormat get() {
        val name = prefs.getString("number_format", NumberFormat.INTERNATIONAL.name) ?: NumberFormat.INTERNATIONAL.name
        return try { NumberFormat.valueOf(name) } catch (e: Exception) { NumberFormat.INTERNATIONAL }
    }

    init {
        _uiState.update { it.copy(
            themePreference = savedThemePref,
            metalWeightUnit = savedMetalUnit,
            numberFormat = savedNumberFormat
        ) }
        viewModelScope.launch {
            launch {
                repository.rates.collect { list ->
                    _uiState.update { state ->
                        val fromCode = state.fromCurrency?.code ?: savedFromCode
                        val toCode = state.toCurrency?.code ?: savedToCode

                        val from = if (list.isNotEmpty()) {
                            list.find { it.code == fromCode }
                                ?: list.find { it.code == "USD" }
                                ?: list.first()
                        } else state.fromCurrency

                        val to = if (list.isNotEmpty()) {
                            list.find { it.code == toCode }
                                ?: list.find { it.code == "INR" }
                                ?: list.last()
                        } else state.toCurrency
                        
                        state.copy(rates = list, fromCurrency = from, toCurrency = to)
                    }
                }
            }
            launch {
                repository.isLoading.collect { loading ->
                    _uiState.update { it.copy(isLoading = loading) }
                }
            }
            launch {
                repository.error.collect { err ->
                    _uiState.update { it.copy(error = err) }
                }
            }
        }

        viewModelScope.launch {
            repository.fetchAllRates()
        }
    }

    fun onInputAmountChanged(amount: String) {
        var cleaned = amount
        if (cleaned.count { it == '.' } > 1) return
        _uiState.update { it.copy(inputAmount = cleaned) }
    }

    fun onSwapCurrencies() {
        _uiState.update {
            it.copy(
                fromCurrency = it.toCurrency,
                toCurrency = it.fromCurrency
            )
        }
        saveCurrencySelection()
    }

    fun onSelectCurrency(rateItem: RateItem, isFrom: Boolean) {
        _uiState.update {
            if (isFrom) {
                if (it.toCurrency?.code == rateItem.code) {
                    it.copy(fromCurrency = rateItem, toCurrency = it.fromCurrency)
                } else {
                    it.copy(fromCurrency = rateItem)
                }
            } else {
                if (it.fromCurrency?.code == rateItem.code) {
                    it.copy(toCurrency = rateItem, fromCurrency = it.toCurrency)
                } else {
                    it.copy(toCurrency = rateItem)
                }
            }
        }
        saveCurrencySelection()
    }

    fun onRefreshRates() {
        viewModelScope.launch {
            repository.fetchAllRates()
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        _uiState.update { it.copy(themePreference = pref) }
        prefs.edit().putString("theme_pref", pref.name).apply()
    }

    fun setMetalWeightUnit(unit: MetalWeightUnit) {
        _uiState.update { it.copy(metalWeightUnit = unit) }
        prefs.edit().putString("metal_unit", unit.name).apply()
    }

    fun setNumberFormat(format: NumberFormat) {
        _uiState.update { it.copy(numberFormat = format) }
        prefs.edit().putString("number_format", format.name).apply()
    }

    private fun saveCurrencySelection() {
        val state = _uiState.value
        prefs.edit()
            .putString("selected_from", state.fromCurrency?.code)
            .putString("selected_to", state.toCurrency?.code)
            .apply()
    }
}

