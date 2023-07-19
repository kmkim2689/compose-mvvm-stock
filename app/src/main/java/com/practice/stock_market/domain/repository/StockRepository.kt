package com.practice.stock_market.domain.repository

import com.practice.stock_market.domain.model.CompanyListing
import com.practice.stock_market.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    // domain layer
    // company listing
    // util Resource

    // parameters
    // 1. fetchFromRemote
    // if true : get the 'remote' data 'again' => swipe refresh layout, swipe down to refresh the list => force to get data from api again
    // if false : just get the data from cache
    // 2. query : what to search for in the list => gotta use for database query(searchCompanyListing in dao)
    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>
    // 1. as the repository is in the domain layer, the domain layer cannot access to the data layer
    // cannot access to Entity...
    // => return type is the list of CompanyListing, not CompanyListingEntity
    // 2. using Resource(util > Resource.kt) class to distinguish between success and error case...
    // => Resource<List<CompanyListing>>
    // if there was an error, Resource class that we return equals to Error() class in Resource sealed class...
    // if successful, return the success resource class... => the type of data parameter is List<CompanyListing>
    // 3. 'Cause we use local caching, the return type should be Flow
    // to emit 'multiple values' for a multiple period of time
    // Loading -> Success or Error (multiple values for a period of time)
    // Firstly, tell the viewmodel, "you should show the progress bar" => Loading
    // Then, actually load the data from database(local cache) => if retrieved, Success(data: List<CompanyListing>) should be emitted
    // At the same time, we can also request new data from the 'api'
    // we get multiple different values over a period of time...
}