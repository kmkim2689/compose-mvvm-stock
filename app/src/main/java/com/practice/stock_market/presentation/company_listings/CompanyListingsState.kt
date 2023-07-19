package com.practice.stock_market.presentation.company_listings

import com.practice.stock_market.domain.model.CompanyListing

// state class for ui...
data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    // if swipe refresh layout is currently refreshing
    val isRefreshing: Boolean = false,
    // the text currently entered on search bar
    val searchQuery: String = ""
)