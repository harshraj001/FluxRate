package com.oss.fluxrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.oss.fluxrate.data.network.ExchangeApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class RateItem(
    val code: String,
    val name: String,
    val type: RateType,
    val rateToUsd: Double
)

enum class RateType { FIAT, CRYPTO, METAL }

class RateRepository(
    private val primaryApi: ExchangeApi,
    private val fallbackApi: ExchangeApi,
    context: Context
) {
    val prefs: SharedPreferences = context.getSharedPreferences("fluxrate_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, RateItem::class.java)
    private val adapter = moshi.adapter<List<RateItem>>(listType)
    private val mapAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )

    private val _rates = MutableStateFlow<List<RateItem>>(emptyList())
    val rates: StateFlow<List<RateItem>> = _rates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    companion object {
        // Known crypto currency codes (to differentiate from fiat in UI)
        private val CRYPTO_CODES = setOf(
            "btc", "eth", "bnb", "sol", "xrp", "doge", "ada", "avax", "dot",
            "link", "matic", "shib", "ltc", "uni", "atom", "xlm", "near",
            "apt", "fil", "arb", "op", "sui", "trx", "ton", "pepe",
            "aave", "algo", "ape", "ar", "axs", "cake", "comp", "crv",
            "ens", "etc", "ftm", "gala", "grt", "hbar", "icp", "imx",
            "inj", "jasmy", "kcs", "ldo", "lrc", "mana", "mkr", "neo",
            "qnt", "ren", "rndr", "rose", "rune", "sand", "snx", "stx",
            "theta", "vet", "xtz", "zec", "zil", "1inch", "agix", "akt",
            "amp", "bch", "bsv", "celo", "cfx", "chz", "dash", "dcr",
            "egld", "enj", "eos", "fxs", "gmx", "gt", "hot", "ht",
            "icx", "iotx", "kava", "kda", "ksm", "mina", "nexo", "okb",
            "one", "ont", "osmo", "paxg", "pendle", "qtum", "rvn", "sxp",
            "tfuel", "waves", "woo", "xdc", "xem", "xmr", "yfi", "zrx",
            "xbt", "xch", "xaut", "xcg", "xec"
        )

        // Precious metal codes (verified from API)
        private val METAL_CODES = setOf(
            "xau", // Gold
            "xag", // Silver
            "xpt", // Platinum
            "xpd"  // Palladium
        )

        // Hardcoded fallback so app is NEVER empty
        private val FALLBACK_RATES = listOf(
            RateItem("USD", "US Dollar", RateType.FIAT, 1.0),
            RateItem("EUR", "Euro", RateType.FIAT, 1.0 / 0.85),
            RateItem("GBP", "British Pound", RateType.FIAT, 1.0 / 0.74),
            RateItem("JPY", "Japanese Yen", RateType.FIAT, 1.0 / 156.0),
            RateItem("INR", "Indian Rupee", RateType.FIAT, 1.0 / 91.0),
            RateItem("AUD", "Australian Dollar", RateType.FIAT, 1.0 / 1.40),
            RateItem("CAD", "Canadian Dollar", RateType.FIAT, 1.0 / 1.37),
            RateItem("CHF", "Swiss Franc", RateType.FIAT, 1.0 / 0.77),
            RateItem("CNY", "Chinese Yuan", RateType.FIAT, 1.0 / 6.84),
            RateItem("KRW", "South Korean Won", RateType.FIAT, 1.0 / 1350.0),
            RateItem("BTC", "Bitcoin", RateType.CRYPTO, 88000.0),
            RateItem("ETH", "Ethereum", RateType.CRYPTO, 2400.0),
            RateItem("BNB", "Binance Coin", RateType.CRYPTO, 620.0),
            RateItem("SOL", "Solana", RateType.CRYPTO, 140.0),
            RateItem("XRP", "Ripple", RateType.CRYPTO, 2.3),
            RateItem("DOGE", "Dogecoin", RateType.CRYPTO, 0.25),
        ).sortedBy { it.code }
    }

    init {
        val cachedJson = prefs.getString("cached_rates", null)
        if (cachedJson != null) {
            try {
                val cachedList = adapter.fromJson(cachedJson)
                if (!cachedList.isNullOrEmpty()) {
                    _rates.value = cachedList
                } else {
                    _rates.value = FALLBACK_RATES
                }
            } catch (e: Exception) {
                Log.e("RateRepository", "Failed to parse cached rates", e)
                _rates.value = FALLBACK_RATES
            }
        } else {
            _rates.value = FALLBACK_RATES
        }
    }

    suspend fun fetchAllRates() {
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            try {
                // Try primary API first, fallback on failure
                val (ratesMap, namesMap) = try {
                    val rates = primaryApi.getRates()
                    val names = primaryApi.getCurrencyNames()
                    Pair(rates.usd, names)
                } catch (e: Exception) {
                    Log.w("RateRepository", "Primary API failed, trying fallback", e)
                    val rates = fallbackApi.getRates()
                    val names = fallbackApi.getCurrencyNames()
                    Pair(rates.usd, names)
                }

                // Also cache the names for offline use
                try {
                    prefs.edit().putString("cached_names", mapAdapter.toJson(namesMap)).apply()
                } catch (_: Exception) {}

                val items = mutableListOf<RateItem>()
                // Add USD as base (rate = 1.0)
                items.add(RateItem("USD", namesMap["usd"] ?: "US Dollar", RateType.FIAT, 1.0))

                ratesMap.forEach { (code, rate) ->
                    if (rate <= 0) return@forEach
                    if (code == "usd") return@forEach // skip duplicate base
                    val name = namesMap[code] ?: code.uppercase()
                    val type = if (code in CRYPTO_CODES) RateType.CRYPTO 
                               else if (code in METAL_CODES) RateType.METAL 
                               else RateType.FIAT
                    items.add(
                        RateItem(
                            code = code.uppercase(),
                            name = name,
                            type = type,
                            rateToUsd = 1.0 / rate // rate = units per 1 USD, so rateToUsd = 1/rate
                        )
                    )
                }

                val sorted = items.sortedBy { it.code }
                _rates.value = sorted
                prefs.edit().putString("cached_rates", adapter.toJson(sorted)).apply()

            } catch (e: Exception) {
                Log.e("RateRepository", "All APIs failed", e)
                _error.value = "Failed to fetch rates: ${e.message}"
            }

            _isLoading.value = false
        }
    }
}
