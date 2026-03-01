package com.oss.fluxrate.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BinanceTickerResponse(
    val symbol: String,
    val price: String
)
