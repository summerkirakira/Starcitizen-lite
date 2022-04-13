package vip.kirakira.starcitizenlite.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.network.rsi_cookie


class CartActivity : AppCompatActivity() {
    lateinit var loginWebView: WebView
    var recorder = PayloadRecorder()

    val RSI_URL = "https://robertsspaceindustries.com"
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        loginWebView = findViewById(R.id.webView)
        loginWebView.settings.userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:99.0) Gecko/20100101 Firefox/99.0"
        loginWebView.settings.domStorageEnabled = true
        loginWebView.settings.javaScriptCanOpenWindowsAutomatically = true

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.setAcceptThirdPartyCookies(loginWebView, true)
        rsi_cookie.split(";").forEach {
            cookieManager.setCookie(RSI_URL, it)
        }
        loginWebView.evaluateJavascript(INTERCEPT_JS, null)
        loginWebView.webViewClient = object : WebLoginActivity.LoginWebViewClient() {

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                if (url.startsWith("https://www.paypal.com/checkoutnow")){
                    val urlList = url.split("&")
                    var newUrlList = mutableListOf<String>()
                    urlList.forEach {
                        if (!it.contains("xcomponent") && !it.contains("version")){
                            newUrlList.add(it)
                        }
                    }
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    intent.data = Uri.parse(newUrlList.joinToString("&"))
                    startActivity(intent)
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(
                view: WebView,
                url: String,
                favicon: Bitmap?
            ) {
                loginWebView.evaluateJavascript(
                    INTERCEPT_JS, null
                )
            }
        }
        loginWebView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && loginWebView.canGoBack()) {
                        //表示按返回键时的操作
                        loginWebView.goBack()
                        return true
                    }
                }
                return false
            }
        })

        loginWebView.settings.javaScriptEnabled = true
//        val headers = mutableMapOf("referer" to "https://robertsspaceindustries.com", "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36")
//        Log.i("Cookie", cookieManager.getCookie(RSI_URL))
        loginWebView.addJavascriptInterface(recorder, "recorder")
        intent.getStringExtra("url")?.let {
            loginWebView.loadUrl(it)
        }
    }
}