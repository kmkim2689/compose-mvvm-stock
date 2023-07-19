package com.practice.stock_market.presentation.company_listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.stock_market.domain.repository.StockRepository
import com.practice.stock_market.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
): ViewModel() {

    // the state for ui is needed...
    // implement this with data class(CompanyListingsState)
    // define different events by sealed class(CompanyListingsEvent)
    var state by mutableStateOf(CompanyListingsState())

    // when the search query changes,
    // on every text change the result should be changed by query text
    // by coroutine Job
    private var searchJob: Job? = null

    // 이벤트에 따른 분기처리
    fun onEvent(event: CompanyListingsEvent) {
        when (event) {
            is CompanyListingsEvent.Refresh -> {
                // swipe to refresh
                // refetch data from api
                getCompanyListings(fetchFromRemote = true)
            }
            is CompanyListingsEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                // cancel the current job(searching for the previous text)
                // 마지막으로 텍스트를 입력하고 0.5초 이내에 다시 텍스트 입력 시, 최근에 입력한 결과만 나오도록 해야 사용자 경험이 좋아짐
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }
            }
        }
    }

    // recall api
    // the same parameter with getCompanyListings func. of StockRepositoryImpl.kt
    fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            // retrieve entries from cache
            repository.getCompanyListings(fetchFromRemote, query)
                .collect { result ->
                    // result : Resource<List<CompanyListings>>
                    // Resource의 유형에 따른 분기처리 필요... 로딩/성공/에러
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                state = state.copy(
                                    companies = listings
                                )
                            }
                        }
                        is Resource.Error -> Unit
                        is Resource.Loading -> {
                            // 로딩 이벤트인 경우
                            state = state.copy(isLoading = result.isLoading)
                        }
                    }

                }
        }
    }
}