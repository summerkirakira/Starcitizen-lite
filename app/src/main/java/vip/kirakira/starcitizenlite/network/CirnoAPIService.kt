package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.kirakira.starcitizenlite.network.CirnoProperty.*
import vip.kirakira.starcitizenlite.uuid

private const val BASE_URL = "http://biaoju.site:6088/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val cirnoClient: OkHttpClient = OkHttpClient
    .Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader("User-Agent", "StarCitizenLite")
            .addHeader("cirno-token", uuid)
            .build()
        chain.proceed(newRequest)
    }.build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(cirnoClient)
    .build()



interface CirnoApiService {
    @GET("announcement/latest")
    suspend fun getAnnouncement(): Announcement?

    @GET("version/latest")
    suspend fun getVersion(): Version

    @GET("startup")
    suspend fun getStartup(): StarUp

    @GET("reCaptchaV3")
    suspend fun getReCaptchaV3(@Query("limit") limit: Int): RecaptchaList

    @GET("translation/version")
    suspend fun getTranslationVersion(): TranslationVersionProperty

    @GET("translation/all")
    suspend fun getAllTranslation(): List<TranslationProperty>

    @POST("translation/add")
    suspend fun addNotTranslation(@Body translations: List<AddNotTranslationBody>): AddTranslationResult
}

object CirnoApi {
    val retrofitService: CirnoApiService by lazy {
        retrofit.create(CirnoApiService::class.java)
    }

}