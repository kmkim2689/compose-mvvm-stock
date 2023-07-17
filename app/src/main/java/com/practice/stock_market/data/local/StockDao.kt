package com.practice.stock_market.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockDao {

    // 1. insert company listings
    // to insert csv file data into company listings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyListings(
        companyListingEntity: List<CompanyListingEntity>
    )

    // 2. clear company listings
    @Query("DELETE FROM companylistingentity")
    suspend fun clearCompanyListings()

    // 3. searching database
    // in sqlite, '||' means to concatenate strings... just like '+' in kotlin
    // LOWER() : to lower case, UPPER() : to upper case
    // 결국 검색어를 소문자로 변환한 것이 주식 이름에 포함되는 것 또는 검색어를 대문자로 변환한 것이 심볼 이름에 포함되는 결과물 검색
    @Query(
        """
        SELECT * 
        FROM CompanyListingEntity
        WHERE LOWER(name) LIKE "%" || LOWER(:query) || '%' OR UPPER(:query) == symbol
    """
    )
    suspend fun searchCompanyListing(query: String): List<CompanyListingEntity>
}