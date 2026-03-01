package com.oss.fluxrate.data.network

import retrofit2.http.GET

/**
 * fawazahmed0/exchange-api
 * Base URL: https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/
 */
interface ExchangeApi {
    /** Returns {"date":"...","usd":{"eur":0.84,"inr":90.8,...}} */
    @GET("currencies/usd.json")
    suspend fun getRates(): ExchangeRatesResponse

    /** Returns {"usd":"US Dollar","eur":"Euro",...} */
    @GET("currencies.json")
    suspend fun getCurrencyNames(): Map<String, String>
}

data class ExchangeRatesResponse(
    val date: String,
    val usd: Map<String, Double>
)
