package vip.kirakira.starcitizenlite.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.MainActivity
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.DEFAULT_USER_AGENT
import vip.kirakira.starcitizenlite.network.RSI_COOKIE_CONSTENT
import vip.kirakira.viewpagertest.network.graphql.SignInMutation

class WebLoginActivity : AppCompatActivity() {
    var recorder = PayloadRecorder()
    val INTERCEPT_JS = """XMLHttpRequest.prototype.origOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
    // these will be the key to retrieve the payload
    this.recordedMethod = method;
    this.recordedUrl = url;
    this.origOpen(method, url, async, user, password);
};
XMLHttpRequest.prototype.origSend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function(body) {
    // interceptor is a Kotlin interface added in WebView
    if(body) recorder.recordPayload(this.recordedMethod, this.recordedUrl, body);
    this.origSend(body);
};"""
    lateinit var loginWebView: WebView
    lateinit var signInVariables: SignInMutation.SignInVariables
    lateinit var temp_rsi_token: String
    lateinit var temp_device_id: String
    var password: String? = null
    var email: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)
        supportActionBar?.hide()
        loginWebView = findViewById(R.id.login_webview)
        loginWebView.evaluateJavascript(INTERCEPT_JS, null)
        loginWebView.webViewClient = object : LoginWebViewClient() {

            override fun onPageStarted(
                view: WebView,
                url: String,
                favicon: Bitmap?
            ) {
                loginWebView.evaluateJavascript(
                    INTERCEPT_JS, null
                )
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                if (url.equals("https://robertsspaceindustries.com/graphql")){
                    val payload = recorder.getPayload(request.method, "/graphql")

//                    Log.i("WebLoginActivity", "request: $request payload: $payload")
                    // handle the request with the given payload and return the response
                    if (payload?.contains("signin") == true){
                        signInVariables = SignInMutation().parseRequest(payload)
                        email = signInVariables.variables.email
                        password = signInVariables.variables.password
                        if(signInVariables.variables.captcha != null) {
                            val builder = Request.Builder()
                            val req = builder.url("https://robertsspaceindustries.com/graphql")
                                .addHeader("user-agent", DEFAULT_USER_AGENT)
                                .addHeader("content-type", "application/json")
                                .post(RequestBody.create(MediaType.parse("application/json"), payload))
                                .build();
                            val response = OkHttpClient().newCall(req).execute()
                            val contentType = response.header("content-type")
                            val responseBody = response.body()?.string()
                            if("errors" in responseBody!!){
                                val error = SignInMutation().parseFailure(responseBody).errors[0]
                                if(error.message == "MultiStepRequired"){
//                                    Log.w("WebLoginActivity", "-----------MultiStepRequired-----------")
                                    temp_device_id = error.extensions.details.device_id
                                    temp_rsi_token = error.extensions.details.session_id
                                }
                            }
                            return WebResourceResponse(contentType, "UTF-8", responseBody.byteInputStream())
                        }
                    } else if(payload?.contains("multistep") == true) {
                        val builder = Request.Builder()
                        Log.i("WebLoginActivity", "CookieConsent=$RSI_COOKIE_CONSTENT;Rsi-Token=$temp_rsi_token; _device_id=$temp_device_id")
                        val req = builder.url("https://robertsspaceindustries.com/graphql")
                            .addHeader("user-agent", DEFAULT_USER_AGENT)
                            .addHeader("content-type", "application/json")
                            .addHeader("cookie", "CookieConsent=$RSI_COOKIE_CONSTENT;Rsi-Token=$temp_rsi_token; _rsi_device=$temp_device_id")
                            .post(RequestBody.create(MediaType.parse("application/json"), payload))
                            .build();
                        val response = OkHttpClient().newCall(req).execute()
                        val responseBody = response.body()?.string()
                        Log.i("WebLoginActivity", responseBody!!)
                        if("RsiAuthenticatedAccount" in responseBody!!){
                            val successLoginInfo = SignInMutation().parseLoginSuccess(responseBody)
                            saveUserData(successLoginInfo.data.account_multistep.id, temp_device_id, temp_rsi_token, email!!, password!!)
//                            Log.i("WebLoginActivity", "-----------Success-----------")
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        loginWebView.addJavascriptInterface(recorder, "recorder")
        loginWebView.settings.javaScriptEnabled = true
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)

        loginWebView.loadUrl("https://robertsspaceindustries.com/connect")

    }

    open class LoginWebViewClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            var url = request.url.toString()
            if (url.startsWith("https://www.google.com/recaptcha")) {
                url = url.replace("https://www.google.com/recaptcha", "https://recaptcha.net/recaptcha")
                val builder = Request.Builder();
                val req = builder.url(url).get().build();
                val response = OkHttpClient().newCall(req).execute()
                return WebResourceResponse(response.body()?.contentType().toString(), "UTF-8", response.body()?.byteStream())
            }
            return super.shouldInterceptRequest(view, request)
        }


    }

    fun saveUserData(uid: Int, rsi_device: String, rsi_token: String, email: String, password: String): User {
        val cookie = "CookieConsent=$RSI_COOKIE_CONSTENT;Rsi-Token=$rsi_token; _rsi_device=$rsi_device"
        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val builder = Request.Builder()
        val req = builder.url("https://robertsspaceindustries.com/account/referral-program")
            .addHeader("cookie", cookie)
            .get()
            .build();
        val response = OkHttpClient().newCall(req).execute()
        val responseBody = response.body()?.string()
        val doc = Jsoup.parse(responseBody)
        val userName = doc.select(".c-account-sidebar__profile-info-displayname").text()
        val userHandle = doc.select(".c-account-sidebar__profile-info-handle").text()
        val userImage = doc.select(".c-account-sidebar__profile-metas-avatar").attr("style").replace("background-image:url('", "").replace("');", "")
        val userCredits = (doc.select(".c-account-sidebar__profile-info-credits-amount--pledge").text().replace("\$","").replace(" ", "").replace("USD", "").replace(",", "").toFloat()*100).toInt()
        val userUEC = doc.select(".c-account-sidebar__profile-info-credits-amount--uec").text().replace("¤", "").replace(" ", "").replace("UEC", "").replace(",", "").toInt()
        val userREC = doc.select(".c-account-sidebar__profile-info-credits-amount--rec").text().replace("¤", "").replace(" ", "").replace("REC", "").replace(",", "").toInt()
        val isConcierge = doc.select(".c-account-sidebar__links-link--concierge").isNotEmpty()
        val isSubscribed = doc.select(".c-account-sidebar__links-link--subscribe").isNotEmpty()
        val fleet = doc.select(".c-account-sidebar__profile-metas-badge--org").attr("href").split("/").last()
        val fleetImage = doc.select(".c-account-sidebar__profile-metas-badge--org").select("img").attr("src")
        val refNumber = doc.select("div.progress").select(".label").text().replace("Total recruits: ", "").toInt()
        val refCode = doc.select("#share-referral-form").select("input").attr("value")

        val newRquest = Request.Builder()
            .url("https://robertsspaceindustries.com/account/billing")
            .addHeader("cookie", cookie)
            .get()
            .build()
        val refResponse = OkHttpClient().newCall(newRquest).execute()
        val refResponseBody = refResponse.body()?.string()
        val billingDoc = Jsoup.parse(refResponseBody)
        val totalSpent = (billingDoc.select(".spent-line").last().select("em").text().replace("\$","").replace(" ", "").replace("USD", "").replace(",", "").toFloat()*100).toInt()
        val newUser = User(uid, userName, "", email, password, rsi_token, rsi_device, userHandle, userImage, "", "", "", refCode, refNumber, userCredits, userUEC, userREC, 0, totalSpent, isConcierge, isSubscribed, fleet, fleetImage)
        val database = getDatabase(application)
        println(newUser)
        database.userDao.insert(newUser)
        val intent = Intent(this, MainActivity::class.java)
        return newUser
    }

}

class PayloadRecorder {
    private val payloadMap: MutableMap<String, String> =
        mutableMapOf()
    @JavascriptInterface
    fun recordPayload(
        method: String,
        url: String,
        payload: String
    ) {
        payloadMap["$method-$url"] = payload
    }
    fun getPayload(
        method: String,
        url: String
    ): String? =
        payloadMap["$method-$url"]
}