package com.oss.fluxrate.ui.util

import android.content.Context

/**
 * Maps currency codes to their icon drawable resource IDs.
 * Fiat currencies -> country flags (flag_XX.png)
 * Crypto currencies -> coin icons (crypto_XX.png)
 *
 * Uses context.resources.getIdentifier() instead of R class reflection
 * to remain compatible with R8 obfuscation in release builds.
 */
object FlagMapper {
    // Currency code -> Country code (lowercase, matching flag_XX.png filenames)
    private val currencyToCountry = mapOf(
        "AED" to "ae", "AFN" to "af", "ALL" to "al", "AMD" to "am", "ANG" to "an",
        "AOA" to "ao", "ARS" to "ar", "AUD" to "au", "AWG" to "aw", "AZN" to "az",
        "BAM" to "ba", "BBD" to "bb", "BDT" to "bd", "BGN" to "bg", "BHD" to "bh",
        "BIF" to "bi", "BMD" to "bm", "BND" to "bn", "BOB" to "bo", "BRL" to "br",
        "BSD" to "bs", "BTN" to "bt", "BWP" to "bw", "BYN" to "by", "BZD" to "bz",
        "CAD" to "ca", "CDF" to "cd", "CHF" to "ch", "CLP" to "cl", "CNY" to "cn",
        "COP" to "co", "CRC" to "cr", "CUP" to "cu", "CVE" to "cv", "CZK" to "cz",
        "DJF" to "dj", "DKK" to "dk", "DOP" to "do", "DZD" to "dz",
        "EGP" to "eg", "ERN" to "er", "ETB" to "et", "EUR" to "eu",
        "FJD" to "fj", "FKP" to "fk",
        "GBP" to "gb", "GEL" to "ge", "GGP" to "gg", "GHS" to "gh", "GIP" to "gi",
        "GMD" to "gm", "GNF" to "gn", "GTQ" to "gt", "GYD" to "gy",
        "HKD" to "hk", "HNL" to "hn", "HRK" to "hr", "HTG" to "ht", "HUF" to "hu",
        "IDR" to "id", "ILS" to "il", "IMP" to "im", "INR" to "in", "IQD" to "iq",
        "IRR" to "ir", "ISK" to "is",
        "JEP" to "je", "JMD" to "jm", "JOD" to "jo", "JPY" to "jp",
        "KES" to "ke", "KGS" to "kg", "KHR" to "kh", "KMF" to "km", "KPW" to "kp",
        "KRW" to "kr", "KWD" to "kw", "KYD" to "ky", "KZT" to "kz",
        "LAK" to "la", "LBP" to "lb", "LKR" to "lk", "LRD" to "lr", "LSL" to "ls",
        "LYD" to "ly",
        "MAD" to "ma", "MDL" to "md", "MGA" to "mg", "MKD" to "mk", "MMK" to "mm",
        "MNT" to "mn", "MOP" to "mo", "MRU" to "mr", "MUR" to "mu", "MVR" to "mv",
        "MWK" to "mw", "MXN" to "mx", "MYR" to "my", "MZN" to "mz",
        "NAD" to "na", "NGN" to "ng", "NIO" to "ni", "NOK" to "no", "NPR" to "np",
        "NZD" to "nz",
        "OMR" to "om",
        "PAB" to "pa", "PEN" to "pe", "PGK" to "pg", "PHP" to "ph", "PKR" to "pk",
        "PLN" to "pl", "PYG" to "py",
        "QAR" to "qa",
        "RON" to "ro", "RSD" to "rs", "RUB" to "ru", "RWF" to "rw",
        "SAR" to "sa", "SBD" to "sb", "SCR" to "sc", "SDG" to "sd", "SEK" to "se",
        "SGD" to "sg", "SHP" to "sh", "SLL" to "sl", "SOS" to "so", "SRD" to "sr",
        "SSP" to "ss", "STN" to "st", "SVC" to "sv", "SYP" to "sy", "SZL" to "sz",
        "THB" to "th", "TJS" to "tj", "TMT" to "tm", "TND" to "tn", "TOP" to "to",
        "TRY" to "tr", "TTD" to "tt", "TWD" to "tw", "TZS" to "tz",
        "UAH" to "ua", "UGX" to "ug", "USD" to "us", "UYU" to "uy", "UZS" to "uz",
        "VES" to "ve", "VND" to "vn", "VUV" to "vu",
        "WST" to "ws",
        "XAF" to "cm", "XCD" to "ag", "XOF" to "sn", "XPF" to "pf",
        "YER" to "ye",
        "ZAR" to "za", "ZMW" to "zm", "ZWL" to "zw"
    )

    private var appContext: Context? = null
    private var packageName: String = ""

    fun init(context: Context) {
        appContext = context.applicationContext
        packageName = context.packageName
    }

    /**
     * Get the icon drawable resource ID for a currency code.
     * Checks for crypto icon first, then country flag.
     * Returns 0 if nothing found.
     */
    fun getFlagResId(currencyCode: String): Int {
        val ctx = appContext ?: return 0
        val code = currencyCode.lowercase()

        // Handle codes that start with digits (invalid Android resource names)
        val safeName = when (code) {
            "1inch" -> "oneinch"
            else -> code
        }

        // Try crypto icon first
        val cryptoResId = ctx.resources.getIdentifier("crypto_$safeName", "drawable", packageName)
        if (cryptoResId != 0) return cryptoResId

        // Try country flag
        val countryCode = currencyToCountry[currencyCode.uppercase()]
        if (countryCode != null) {
            val flagResId = ctx.resources.getIdentifier("flag_$countryCode", "drawable", packageName)
            if (flagResId != 0) return flagResId
        }

        return 0
    }
}
