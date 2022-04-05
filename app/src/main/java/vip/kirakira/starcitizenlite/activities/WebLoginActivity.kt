package vip.kirakira.starcitizenlite.activities

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import okhttp3.OkHttpClient
import okhttp3.Request
import vip.kirakira.starcitizenlite.R

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)
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
                    Log.i("WebLoginActivity", "request: $request payload: $payload")
                    // handle the request with the given payload and return the response

                    return super.shouldInterceptRequest(view, request)
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        loginWebView.addJavascriptInterface(recorder, "recorder")
        loginWebView.settings.javaScriptEnabled = true
        loginWebView.loadUrl("https://robertsspaceindustries.com/connect")

    }

    open class LoginWebViewClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            var url = request.url.toString()
            if (url.startsWith("https://www.google.com/recaptcha")) {
                Log.w("WebLoginActivity", "onLoadResource: $url")
                url = url.replace("https://www.google.com/recaptcha", "https://recaptcha.net/recaptcha")
                val builder = Request.Builder();
                val req = builder.url(url).get().build();
                val response = OkHttpClient().newCall(req).execute()
                return WebResourceResponse(response.body()?.contentType().toString(), "UTF-8", response.body()?.byteStream())
            }
            return super.shouldInterceptRequest(view, request)
        }


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