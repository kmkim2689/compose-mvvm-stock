package com.practice.stock_market.data.csv

import com.opencsv.CSVReader
import com.practice.stock_market.domain.model.CompanyListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyListingsParser @Inject constructor() : CSVParser<CompanyListing> {
    override suspend fun parse(stream: InputStream): List<CompanyListing> {
        // take the inputStream using opencv library
        // eventually parse csv into a list of CompanyListing items
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            // 첫번째 row는 drop해야함. 실 데이터가 아닌 칼럼명이기 때문
            // access single array and convert it into a single CompanyListing item
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val symbol = line.getOrNull(0)
                    val name = line.getOrNull(1)
                    val exchange = line.getOrNull(2)
                    CompanyListing(
                        name = name ?: return@mapNotNull null,
                        symbol = symbol ?: return@mapNotNull null,
                        exchange = exchange ?: return@mapNotNull null
                    )

                }
                .also {
                    // *after using CSVReader, this should be closed
                    csvReader.close()
                }
        }


    }
}