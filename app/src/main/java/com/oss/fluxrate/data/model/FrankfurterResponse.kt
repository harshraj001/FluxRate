package com.oss.fluxrate.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FrankfurterResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
