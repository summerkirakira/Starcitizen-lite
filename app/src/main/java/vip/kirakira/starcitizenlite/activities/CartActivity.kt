package vip.kirakira.starcitizenlite.activities

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import okhttp3.OkHttpClient
import okhttp3.Request
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.network.rsi_cookie





class CartActivity : AppCompatActivity() {
    lateinit var loginWebView: WebView
    var recorder = PayloadRecorder()
    var graphqlInterrupted = 0

    val RSI_URL = "https://robertsspaceindustries.com"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        supportActionBar?.hide()
        loginWebView = findViewById(R.id.webView)
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.setAcceptThirdPartyCookies(loginWebView, true)
        rsi_cookie.split(";").forEach {
            cookieManager.setCookie(RSI_URL, it)
        }
        loginWebView.evaluateJavascript(INTERCEPT_JS, null)
        loginWebView.webViewClient = object : WebLoginActivity.LoginWebViewClient() {

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
                if(url.contains("stripe")) {
                    val builder = Request.Builder()
                    val newRequest = builder.url(request.url.toString())
                        .header("cookie", rsi_cookie)
                        .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13E233 Safari/601.1")
                        .get()
                        .build()
                    val response = OkHttpClient().newCall(newRequest).execute()
                    val newHtml = response.body!!.string()
                    return WebResourceResponse(
                        "text/html",
                        "UTF-8",
                        newHtml.byteInputStream(Charsets.UTF_8)
                    )

                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        loginWebView.settings.javaScriptEnabled = true
        var headers = mutableMapOf<String, String>("referer" to "https://robertsspaceindustries.com/pledge", "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
        Log.i("Cookie", cookieManager.getCookie(RSI_URL))
        loginWebView.addJavascriptInterface(recorder, "recorder")
        intent.getStringExtra("url")?.let {
            loginWebView.loadUrl(it, headers)
        }
    }
}

class Client : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
        if(request.url.toString().contains("/graphql")) {
            val builder = Request.Builder()
            val newRequest = builder.url(request.url.toString()).build()
            println(request.url.toString())

        }
        return super.shouldInterceptRequest(view, request)
    }
}