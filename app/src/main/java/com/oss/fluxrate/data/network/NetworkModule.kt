package com.oss.fluxrate.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Primary: jsDelivr CDN
    private val primaryRetrofit = Retrofit.Builder()
        .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Fallback: Cloudflare Pages
    private val fallbackRetrofit = Retrofit.Builder()
        .baseUrl("https://latest.currency-api.pages.dev/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val exchangeApi: ExchangeApi = primaryRetrofit.create(ExchangeApi::class.java)
    val fallbackExchangeApi: ExchangeApi = fallbackRetrofit.create(ExchangeApi::class.java)
}
