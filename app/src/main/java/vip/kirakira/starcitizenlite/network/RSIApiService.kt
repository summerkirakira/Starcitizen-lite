package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import vip.kirakira.starcitizenlite.network.shop.CatalogResponse
import vip.kirakira.viewpagertest.network.graphql.BaseGraphQLBody
import java.net.URL

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

val client = OkHttpClient.Builder().build()

object RSIApi {
    val retrofitService : RSIApiService by lazy {
        retrofit.create(RSIApiService::class.java) }

    fun getHangerPage(page: Int, page_size: Int=10, headers: Map<String, String>) : String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/account/pledges?page=$page&pagesize=$page_size"))
            .addHeader("cookie", headers["cookie"]!!)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    fun getBuybackPage(page: Int, page_size: Int=250, headers: Map<String, String>) : String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/account/buy-back-pledges?page=$page&pagesize=$page_size"))
            .addHeader("cookie", headers["cookie"]!!)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }
}



