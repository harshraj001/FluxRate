package com.oss.fluxrate.data.network

import com.oss.fluxrate.data.model.FrankfurterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("latest")
    suspend fun getLatestRates(@Query("base") base: String = "USD"): FrankfurterResponse
}
