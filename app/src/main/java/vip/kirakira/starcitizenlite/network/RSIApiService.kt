package vip.kirakira.starcitizenlite.network
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import vip.kirakira.starcitizenlite.database.HangarLog
import vip.kirakira.starcitizenlite.network.account.PtuAccountBody
import vip.kirakira.starcitizenlite.network.account.ResetCharacterBody
import vip.kirakira.starcitizenlite.network.hanger.*
import vip.kirakira.starcitizenlite.network.shop.*
import vip.kirakira.starcitizenlite.network.upgrades.InitUpgradeProperty
import vip.kirakira.viewpagertest.network.graphql.*
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
        if (request.url.toString() == "https://robertsspaceindustries.com/") {
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("referer", "https://robertsspaceindustries.com/")
                .addHeader("origin", "https://robertsspaceindustries.com")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        if(request.url.toString() == "https://robertsspaceindustries.com/graphql"){
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("x-csrf-token", csrf_token)
                .addHeader("referer", "https://robertsspaceindustries.com/")
                .addHeader("origin", "https://robertsspaceindustries.com")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if(request.url.toString().startsWith("https://robertsspaceindustries.com/api/account") ||
                request.url.toString() == "https://robertsspaceindustries.com/api/promo/redeemPromoCode"
                ) {
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("referer", "https://robertsspaceindustries.com/account/pledges?page=1&pagesize=10")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if (request.url.toString().startsWith("https://robertsspaceindustries.com/api/ship-upgrades/setContextToken")) {
            val newRequest = request.newBuilder()
                .addHeader("cookie", getRSIAccountAuthCookie())
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("referer", "https://robertsspaceindustries.com/account/pledges")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if (request.url.toString().startsWith("https://robertsspaceindustries.com/pledge-store/api/upgrade/graphql")) {
            val newRequest = request.newBuilder()
                .addHeader("cookie", getShipUpgradesCookie())
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("referer", "https://robertsspaceindustries.com/account/pledges")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if (
            request.url.toString() == "https://robertsspaceindustries.com/api/store/v2/cart/token" ||
            request.url.toString() == "https://robertsspaceindustries.com/api/store/buyBackPledge" ||
            request.url.toString() == "https://robertsspaceindustries.com/api/account/pledgeLog"
        ) {
            val newRequest = request.newBuilder()
                .addHeader("cookie", rsi_cookie)
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("referer", "https://robertsspaceindustries.com/pledge")
                .addHeader("origin", "https://robertsspaceindustries.com")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if (request.url.toString() == "https://robertsspaceindustries.com/api/launcher/v3/account/check") {
            val newRequest = request.newBuilder()
                .addHeader("x-rsi-token", rsi_token)
                .addHeader("x-rsi-device", rsi_device)
                .addHeader("user-agent", "RSI Launcher/1.6.8")
                .build()
            return@addInterceptor chain.proceed(newRequest)
        } else if (request.url.toString() == "https://robertsspaceindustries.com/api/launcher/v3/signin") {
            val newRequest = request.newBuilder()
                .addHeader("x-rsi-device", rsi_device)
                .addHeader("user-agent", "RSI Launcher/1.6.8")
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

    @POST("graphql")
    suspend fun promotionCodeQuery(@Body body: BaseGraphQLBody): PromotionCodeQueryResponseProperty

    @POST("api/account/reclaimPledge")
    suspend fun reclaimPledge(@Body body: ReclaimRequestBody): BasicResponseBody

    @POST("api/account/giftPledge")
    suspend fun giftPledge(@Body body: GiftPledgeRequestBody): BasicResponseBody

    @POST("api/account/cancelGift")
    suspend fun cancelGift(@Body body: CancelPledgeRequestBody): BasicResponseBody

    @POST("api/account/v2/resetCharacter")
    suspend fun resetCharacter(@Body body: ResetCharacterBody): BasicResponseBody

    @POST("api/account/copyAccount")
    suspend fun copyAccount(@Body body: PtuAccountBody): BasicResponseBody

    @POST("api/account/eraseCopyAccount")
    suspend fun eraseCopyAccount(@Body body: PtuAccountBody): BasicResponseBody

    @POST("pledge-store/api/upgrade/graphql")
    suspend fun initUpgrade(@Body body: BaseGraphQLBody): InitUpgradeProperty

    @POST("pledge-store/api/upgrade/graphql")
    suspend fun pledgeAddToCart(@Body body: BaseGraphQLBody): AddCartItemProperty

    @POST("api/account/chooseUpgradeTarget")
    suspend fun chooseUpgradeTarget(@Body body: ChooseUpgradeTargetBody): ChooseUpgradeTargetProperty

    @POST("api/account/applyUpgrade")
    suspend fun applyUpgrade(@Body body: ApplyUpgradeBody): ApplyUpgradeProperty

    @POST("api/account/v2/setAuthToken")
    suspend fun  setAuthToken(@Body body: SetAuthTokenBody): Response<ResponseBody>

    @POST("api/ship-upgrades/setContextToken")
    suspend fun  setContextToken(@Body body: SetContextTokenBody): Response<ResponseBody>

    @POST("pledge-store/api/upgrade/graphql")
    suspend fun  initShipUpgrade(@Body body: BaseGraphQLBody): InitShipUpgradeProperty

    @POST("pledge-store/api/upgrade/graphql")
    suspend fun  filterShips(@Body body: BaseGraphQLBody): FilterShipsProperty

    @POST("pledge-store/api/upgrade/graphql")
    suspend fun addUpgradeToCart(@Body body: BaseGraphQLBody): AddUpgradeToCartProperty

    @POST("api/store/v2/cart/token")
    suspend fun applyToken(@Body body: ApplyTokenBody): ApplyTokenProperty

    @POST("api/store/buyBackPledge")
    suspend fun buyBackPledge(@Body body: BuyBackPledgeBody): BuyBackPledgeProperty

    @POST("graphql")
    suspend fun login(@Body body: LoginBody): Response<LoginProperty>

    @POST("graphql")
    suspend fun loginAgain(@Body body: BaseGraphQLBody): LoginAgainProperty

    @POST("graphql")
    suspend fun multiStepLogin(@Body body: BaseGraphQLBody): MultiStepLoginProperty

    @POST("graphql")
    suspend fun signUp(@Body body: RegisterBody): SignUpProperty

    @POST("api/promo/redeemPromoCode")
    suspend fun redeemPromoCode(@Body body: ApplyPromoBody): ApplyPromoProperty

    @POST("api/account/pledgeLog")
    suspend fun getPledgeLog(@Body body: GetPledgeBody): HangarLogProperty

    @POST("api/launcher/v3/account/check")
    suspend fun checkLauncherAccount(): RsiLauncherSignInCheckResponse

    @POST("api/launcher/v3/signin")
    suspend fun rsiLauncherSignIn(@Body body: RsiLauncherSignInBody): RsiLauncherSignInResponse

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
        return response.body!!.string()
    }

    fun getBuybackPage(page: Int, page_size: Int=250, headers: Map<String, String> = mapOf("cookie" to rsi_cookie)) : String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/account/buy-back-pledges?page=$page&pagesize=$page_size"))
            .addHeader("cookie", headers["cookie"]!!)
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    fun getFleetYardsShipsPage(): String {
        val request = Request.Builder().url(URL("http://biaoju.site:6088/banner"))
            .get()
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    fun getPlayerInfoPage(name: String): String {
        val request = Request.Builder().url(URL("https://robertsspaceindustries.com/citizens/$name"))
            .get()
            .build()
        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    suspend fun resetCharacter(password: String, reason: String="Encounter some problems", issue_council: String): BasicResponseBody {
        return retrofitService.resetCharacter(ResetCharacterBody(password, reason, issue_council))
    }

    suspend fun copyAccount(): BasicResponseBody {
        return retrofitService.copyAccount(PtuAccountBody("ptu"))
    }

    suspend fun eraseCopyAccount(): BasicResponseBody {
        return retrofitService.eraseCopyAccount(PtuAccountBody("ptu"))
    }

    suspend fun setAuthToken(): String {
        val response = retrofitService.setAuthToken(SetAuthTokenBody())
        val setCookie = response.headers()["set-cookie"]!!
        val authToken = setCookie.substringAfter("Rsi-Account-Auth=").substringBefore(";")
        setRSIAccountAuth(authToken)
        return authToken
    }

    suspend fun setBuybackAuthToken(): String {
        val response = retrofitService.setAuthToken(SetAuthTokenBody())
        val setCookie = response.headers()["set-cookie"]!!
        val authToken = setCookie.substringAfter("Rsi-Account-Auth=").substringBefore(";")
        val buybackAuthTokenResponse = Gson().fromJson(response.body()!!.string(), BuybackAuthTokenResponse::class.java)
        setRSIAccountAuth(authToken)
        return buybackAuthTokenResponse.data
    }

    suspend fun setUpgradeToken(): String {
        val response = retrofitService.setContextToken(SetContextTokenBody())
        val setCookie = response.headers()["set-cookie"]!!
        val authToken = setCookie.substringAfter("Rsi-ShipUpgrades-Context=").substringBefore(";")
        setRSIShipUpgradesContext(authToken)
        return authToken
    }

    suspend fun getBuybackContextToken(
        fromShipId: Int? = null,
        pledgeId: Int? = null,
        toShipId: Int? = null,
        toSkuId: Int? = null
    ): String {
        val setContextTokenBody = SetContextTokenBody(fromShipId, pledgeId, toShipId, toSkuId)
        val response = retrofitService.setContextToken(
            setContextTokenBody
        )
        val setCookie = response.headers()["set-cookie"]!!
        val authToken = setCookie.substringAfter("Rsi-ShipUpgrades-Context=").substringBefore(";")
        setRSIShipUpgradesContext(authToken)
        return authToken
    }

    suspend fun chooseUpgradeTarget(upgrade_id: String): List<PledgeUpgradeParser.ChooseUpgradeTargetItem>? {
        val response = retrofitService.chooseUpgradeTarget(ChooseUpgradeTargetBody(upgrade_id))
        return if (response.data.rendered != null) {
            PledgeUpgradeParser().parse(response.data.rendered)
        } else {
            null
        }
    }

    suspend fun applyUpgrade(upgrade_id: String, target_id: String, password: String): ApplyUpgradeProperty {
        return retrofitService.applyUpgrade(ApplyUpgradeBody(password, target_id, upgrade_id))
    }

    suspend fun redeemPromoCode(promo: String, code: String, currency: String): PromotionCodeQueryResponseProperty {
        return retrofitService.promotionCodeQuery(PromotionCodeQuery().getRequestBody(code = code, id = promo))
    }

    suspend fun redeemPromoCodeNew(promo: String, code: String, currency: String): ApplyPromoProperty {
        return retrofitService.redeemPromoCode(ApplyPromoBody(promo, currency, code))
    }

    suspend fun getPledgeLog(page: Int): List<HangarLog> {
        return HangarLogParser().parseLog(retrofitService.getPledgeLog(GetPledgeBody(page)).data!!.rendered)
    }

    suspend fun getPledgeLogCount(): Int {
        return retrofitService.getPledgeLog(GetPledgeBody(1)).data!!.pagecount
    }

}



