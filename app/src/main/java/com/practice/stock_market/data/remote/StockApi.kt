package com.practice.stock_market.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    // get list of stocks
    // download csv file by using response body
    @GET("query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apiKey: String = API_KEY
    ): ResponseBody

    // get response body
    // with retrofit, can use a response body to simply 'download' or get access to a 'file stream'



    companion object {
        const val API_KEY = "2BRNMLJE6UOKJULP"
        const val BASE_URL = "https://alphavantage.co/"
    }
}