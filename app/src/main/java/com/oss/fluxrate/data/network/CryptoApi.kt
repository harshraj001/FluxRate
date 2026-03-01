package com.oss.fluxrate.data.network

import com.oss.fluxrate.data.model.BinanceTickerResponse
import retrofit2.http.GET

interface CryptoApi {
    @GET("api/v3/ticker/price")
    suspend fun getAllPrices(): List<BinanceTickerResponse>
}
