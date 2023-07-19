package com.practice.stock_market.data.csv

import java.io.InputStream

interface CSVParser<T> {
    // use generic type..
    // depending on what we want to parse, different csv parser would be needed, as the logic will be different
    suspend fun parse(stream: InputStream): List<T>

}