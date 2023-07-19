package com.practice.stock_market.data.respository

import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.practice.stock_market.data.local.StockDatabase
import com.practice.stock_market.data.mapper.toCompanyListing
import com.practice.stock_market.data.mapper.toCompanyListingEntity
import com.practice.stock_market.data.remote.StockApi
import com.practice.stock_market.domain.model.CompanyListing
import com.practice.stock_market.domain.repository.StockRepository
import com.practice.stock_market.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    // needed : api, database, csv processing
    val api: StockApi,
    val db: StockDatabase,
    val companyListingsParser: com.practice.stock_market.data.csv.CSVParser<CompanyListing>
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            // 발행 순서 : 로딩 화면 -> 실제 데이터
            // emit() : flow throws out the value we passed to emit function
            // 처음에는 로딩을 해야 하므로 isLoading에 true를 넘겨줌.
            emit(Resource.Loading(true))
            // 로딩 동안에, 데이터베이스에 있는 모든 리스트들을 가져오고자 함.
            val localListings = dao.searchCompanyListing(query)
            // 성공적으로 가져오면, Success 클래스를 발행
            // 그러나 localListings의 타입과 Success가 가지는 타입은 다름. 데이터베이스 개체와 실제 보여줄 화면에 대한 클래스의 차이...
            // the main role of repository!!! -> map data layer object to the domain layer object!!! => to use easily the data in viewmodel!!!
            emit(Resource.Success(data = localListings.map { it.toCompanyListing() }))

            // check if api should be called
            // why? => to prevent redundant api calls
            // 데이터베이스가 비어있고 + 검색어가 비어있을 때에만 api로부터 '모든' 데이터를 가져와야 할 것
            // 만약 검색어가 xyz(xyz라는 기업은 없음 - 빈 데이터베이스)인데 api 콜을 할 수 있도록 하면, 아무런 결과도 안나오는데 쓸데없는 api 호출을 하게 되는 셈...
            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                // 캐시로부터 가져오기만 하면 된다면 flow 발행 종료...
                return@flow
            }

            // remote로부터 불러와야 하는 경우
            val remoteListings = try {
                val response = api.getListings()
                // csv 파일 처리...
                // bytestream - used to read the csv file
                // below code is not recommended as it violates SRP
                // SRP : every single class should only have a single responsibility
                // this class should have the only responsibility for 'caching data', not parsing csv files...
                // val csvReader = CSVReader(InputStreamReader(response.byteStream()))
                // put it into separate class...

                companyListingsParser.parse(response.byteStream())

            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("couldn't load data - io exception"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("couldn't load data - http exception"))
                null
            }

            // put it on local cache.. update database
            remoteListings?.let { companyListings ->
                // 초기화하기 위해 데이터베이스 비우기
                dao.clearCompanyListings()
                // 다시 삽입하기
                dao.insertCompanyListings(
                    companyListings.map { it.toCompanyListingEntity() }
                )
                // 성공 시 '데이터베이스로부터' 가져오기...
                // companyListing(네트워크로 얻은 결과)를 직접 발행하지 않음
                // Single Source of truth 달성을 위함...
                emit(Resource.Success(data = dao
                    .searchCompanyListing("")
                    .map { it.toCompanyListing() }))
                // 성공 시 로딩 중지
                emit(Resource.Loading(false))


            }

        }
    }
}