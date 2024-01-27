package vip.kirakira.starcitizenlite.network

import android.util.Log
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.kirakira.starcitizenlite.database.GameTranslation
import vip.kirakira.starcitizenlite.database.ShipDetail
import vip.kirakira.starcitizenlite.network.CirnoProperty.*
import vip.kirakira.starcitizenlite.uuid
import java.util.Locale

private const val BASE_URL = "https://biaoju.site:6188/"
//private const val BASE_URL = "http://192.168.173.154:6088/"

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
    }
    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(cirnoClient)
    .build()



interface CirnoApiService {
    @GET("announcement/latest")
    suspend fun getAnnouncement(): Announcement?

    @POST("version")
    suspend fun getVersion(@Body refugeInfo: RefugeInfo): Version

    @POST("client/info")
    suspend fun updateClientInfo(@Body clientInfo: ClientInfo)

    @GET("startup")
    suspend fun getStartup(): StarUp

    @GET("v2/reCaptchaV3")
    suspend fun getReCaptchaV3(@Query("limit") limit: Int): RecaptchaList

    @GET("translation/version")
    suspend fun getTranslationVersion(): TranslationVersionProperty

    @GET("translation/all")
    suspend fun getAllTranslation(): List<TranslationProperty>

    @POST("translation/add")
    suspend fun addNotTranslation(@Body translations: List<AddNotTranslationBody>): AddTranslationResult

    @POST("translation/ship_alias")
    suspend fun addShipAlias(@Body shipAlias: AddShipAliasBody): AddTranslationResult

    @POST("upgrade/add")
    suspend fun addUpgradeRecord(@Body upgrade: AddUpgradeRecord): AddTranslationResult

    @GET("promotion/all")
    suspend fun getAllPromotion(): List<PromotionCode>

    @POST("ship/upgrade/path")
    suspend fun getUpgradePath(@Body upgradePath: ShipUpgradePathPostBody): ShipUpgradeResponse


    @GET("app/update")
    suspend fun getAppUpdate(): AppUpdateInfo
}


object CirnoApi {
    val retrofitService: CirnoApiService by lazy {
        retrofit.create(CirnoApiService::class.java)
    }

    fun getShipDetail(url: String): List<ShipDetail> {
        val request = Request.Builder()
            .url(url)
            .addHeader("cirno-token", uuid)
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val shipDetails = Gson().fromJson(json, Array<ShipDetail>::class.java)
        return shipDetails.toList()
    }

    fun getGameTranslation(url: String): List<GameTranslation> {
        val request = Request.Builder()
            .url(url)
            .addHeader("cirno-token", uuid)
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val gameTranslations = Gson().fromJson(json, Array<GameTranslation>::class.java)
        return gameTranslations.toList()
    }

    fun getShipAliasFromUrl(url: String): List<ShipAlias> {
        val request = Request.Builder()
            .url(url)
            .addHeader("cirno-token", uuid)
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val shipAlias = Gson().fromJson(json, Array<ShipAlias>::class.java)
        return shipAlias.toList()
    }

    fun getSubscribeUrl(): String {
        return BASE_URL + "subscribe"
    }

    suspend fun getReCaptchaV3(num: Int): ArrayList<String> {
        val reCaptchaList = ArrayList<String>()
        try {
            val token = RecaptchaV3.getRecaptchaToken()
            for (i in 0 until num) {
                reCaptchaList.add(token)
            }
        } catch (e: Exception) {
            val result = retrofitService.getReCaptchaV3(num)
            if (result.error != null) {
                return reCaptchaList
            }
            for (i in 0 until num) {
                reCaptchaList.add(result.captcha_list[i].token)
            }
        }
        return reCaptchaList
    }

    suspend fun getRecaptchaOldV3(num: Int): ArrayList<String> {
        val reCaptchaList = ArrayList<String>()
        val result = retrofitService.getReCaptchaV3(num)
        if (result.error != null) {
            Log.d("CirnoApi", "getRecaptchaOldV3: ${result.error}")
        }
        for (i in 0 until num) {
            reCaptchaList.add(result.captcha_list[i].token)
        }
        return reCaptchaList
    }

}