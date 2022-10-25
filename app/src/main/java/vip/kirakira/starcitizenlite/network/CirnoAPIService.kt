package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.kirakira.starcitizenlite.network.CirnoProperty.*

private const val BASE_URL = "http://biaoju.site:6088/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
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