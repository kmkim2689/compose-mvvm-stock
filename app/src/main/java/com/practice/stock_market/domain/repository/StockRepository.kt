package com.practice.stock_market.domain.repository

import com.practice.stock_market.domain.model.CompanyListing
import com.practice.stock_market.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    // domain layer

    suspend fun getCompanyListings(

    ): Flow<Resource<List<CompanyListing>>>
}