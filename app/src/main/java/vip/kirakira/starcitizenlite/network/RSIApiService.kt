package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import vip.kirakira.starcitizenlite.network.hanger.BasicResponseBody
import vip.kirakira.starcitizenlite.network.hanger.CancelPledgeRequestBody
import vip.kirakira.starcitizenlite.network.hanger.GiftPledgeRequestBody
import vip.kirakira.starcitizenlite.network.hanger.ReclaimRequestBody
import vip.kirakira.starcitizenlite.network.shop.*
import vip.kirakira.viewpagertest.network.graphql.BaseGraphQLBody
import java.net.URL

//private const val BASE_URL = "http://100.70.59.3:6000"

private const val BASE_URL = "https://robertsspaceindustries.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val client: OkHttpClient = OkHttpClient
    .Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        if(request.url().toString() == "https://robertsspaceindustries.com/graphql"){
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("referer", "https://robertsspaceindustries.com/")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if(request.url().toString().startsWith("https://robertsspaceindustries.com/api/account")) {
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("referer", "https://robertsspaceindustries.com/account/pledges?page=1&pagesize=10")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        return@addInterceptor chain.proceed(request)
    }
    .build()



private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .baseUrl(BASE_URL)
    .build()

interface RSIApiService {

    @POST("graphql")
    suspend fun getCatalog(@Body body: BaseGraphQLBody): CatalogResponse

    @POST("graphql")
    suspend fun getCart(@Body body: BaseGraphQLBody): CartSummaryProperty

    @POST("graphql")
    suspend fun addToCart(@Body body: BaseGraphQLBody): AddCartItemProperty

    @POST("graphql")
    suspend fun step1Query(@Body body: BaseGraphQLBody): Step1QueryProperty

    @POST("graphql")
    suspend fun addCredit(@Body body: BaseGraphQLBody): AddCreditProperty

    @POST("graphql")
    suspend fun nextStep(@Body body: BaseGraphQLBody): NextStepProperty

    @POST("graphql")
    suspend fun clearCart(@Body body: BaseGraphQLBody): ClearCartProperty

    @POST("graphql")
    suspend fun cartValidation(@Body body: BaseGraphQLBody): CartValidationProperty

    @POST("graphql")
    suspend fun cartAddressAssign(@Body body: BaseGraphQLBody): CartAddressAssignProperty

    @POST("graphql")
    suspend fun cartAddressQuery(@Body body: BaseGraphQLBody): CartAddressProperty

    @POST("api/account/reclaimPledge")
    suspend fun reclaimPledge(@Body body: ReclaimRequestBody): BasicResponseBody

    @POST("api/account/giftPledge")
    suspend fun giftPledge(@Body body: GiftPledgeRequestBody): BasicResponseBody

    @POST("api/account/cancelGift")
    suspend fun cancelGift(@Body body: CancelPledgeRequestBody): BasicResponseBody

}


object RSIApi {
    val retrofitService : RSIApiService by lazy {
        retrofit.create(RSIApiService::class.java)
    }

    fun getHangerPage(page: Int, page_size: Int=10, headers: Map<String, String> = mapOf("cookie" to rsi_cookie)) : String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/account/pledges?page=$page&pagesize=$page_size"))
            .addHeader("cookie", headers["cookie"]!!)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    fun getBuybackPage(page: Int, page_size: Int=250, headers: Map<String, String> = mapOf("cookie" to rsi_cookie)) : String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/account/buy-back-pledges?page=$page&pagesize=$page_size"))
            .addHeader("cookie", headers["cookie"]!!)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }
}



