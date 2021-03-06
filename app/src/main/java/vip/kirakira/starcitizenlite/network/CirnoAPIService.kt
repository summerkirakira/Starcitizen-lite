package vip.kirakira.starcitizenlite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import vip.kirakira.starcitizenlite.network.CirnoProperty.Announcement
import vip.kirakira.starcitizenlite.network.CirnoProperty.StarUp
import vip.kirakira.starcitizenlite.network.CirnoProperty.Version

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
}

object CirnoApi {
    val retrofitService: CirnoApiService by lazy {
        retrofit.create(CirnoApiService::class.java)
    }

}