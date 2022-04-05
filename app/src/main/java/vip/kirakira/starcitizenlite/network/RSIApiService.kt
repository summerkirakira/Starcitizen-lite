package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import vip.kirakira.starcitizenlite.network.shop.CatalogResponse
import vip.kirakira.viewpagertest.network.graphql.BaseGraphQLBody

//private const val BASE_URL = "http://100.70.59.3:6000"

private const val BASE_URL = "https://robertsspaceindustries.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface RSIApiService {

    @Headers("Accept: */*" )
    @POST("graphql")
    suspend fun getCatalog(@Body body: BaseGraphQLBody): CatalogResponse
}

object RSIApi {
    val retrofitService : RSIApiService by lazy {
        retrofit.create(RSIApiService::class.java) }
}