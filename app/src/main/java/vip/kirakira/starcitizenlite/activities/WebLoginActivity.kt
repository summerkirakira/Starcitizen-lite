package vip.kirakira.starcitizenlite.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.MainActivity
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.DEFAULT_USER_AGENT
import vip.kirakira.starcitizenlite.network.RSI_COOKIE_CONSTENT
import vip.kirakira.starcitizenlite.network.csrf_token
import vip.kirakira.starcitizenlite.network.saveUserData
import vip.kirakira.starcitizenlite.network.search.getPlayerSearchResult
import vip.kirakira.starcitizenlite.repositories.UserRepository
import vip.kirakira.viewpagertest.network.graphql.SignInMutation
import java.io.ByteArrayInputStream

val INTERCEPT_JS = """
    function getCookie(name) {
    let cookieArray = document.cookie.split(';'); // 将 cookie 字符串分割为数组
    for(let cookie of cookieArray) {
        let [cookieName, cookieValue] = cookie.trim().split('='); // 分割每个 cookie 的名称和值
        if(cookieName === name) {
            return cookieValue; // 找到匹配的 cookie 名称，返回对应的值
        }
    }
    return ""; // 如果没有找到匹配的 cookie 名称，返回空字符串
}
const { fetch: originalFetch } = window;

window.fetch = async (...args) => {
    let [resource, config ] = args; console.log(args);
    if(resource === "/graphql") {
        data = JSON.parse(config.body);
        if (config.headers["x-csrf-token"] != undefined) {
            console.log("csdsddsdsa");
            window.recorder.saveCsrfToken(config.headers["x-csrf-token"]);
        }
        console.log(data);
        if (data.variables.email != undefined && data.variables.password != undefined) {
            window.recorder.saveEmailPassword(data.variables.email, data.variables.password);
        }
        rsi_token = getCookie("Rsi-Token");
        if (rsi_token != "") {
            window.recorder.saveRsiToken(rsi_token);
        }
        _rsi_device = getCookie("_rsi_device");
        if (_rsi_device != "") {
            window.recorder.saveDeviceId(_rsi_device);
        }
        if ("RsiAuthenticatedAccount" in config.body) {
            window.recorder.saveIsSuccess(true);
        }
    };
    const response = await originalFetch(resource, config);
    // response interceptor here
    return response;
};"""

class WebLoginActivity : RefugeBaseActivity() {
    var recorder = PayloadRecorder()

    lateinit var loginWebView: WebView
    lateinit var signInVariables: SignInMutation.SignInVariables
    lateinit var temp_rsi_token: String
    lateinit var temp_device_id: String
    var password: String? = null
    var email: String? = null

    //    var needMultiStep = MutableLiveData<Boolean>(false)
    var isLogin = MutableLiveData<User>(null)

    var isRegistered = MutableLiveData<Boolean>(false)

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    lateinit var database: ShopItemDatabase

    var isFirstReq: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)
        supportActionBar?.hide()

        isFirstReq = true

        WebView.setWebContentsDebuggingEnabled(true)

        val userRepository = UserRepository(getDatabase(application))

        loginWebView = findViewById(R.id.login_webview)
        loginWebView.evaluateJavascript(INTERCEPT_JS, null)
        database = getDatabase(application)
        isLogin.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                with(pref.edit()) {
                    putInt(getString(R.string.primary_user_key), it.id)
                    putBoolean(getString(R.string.is_log_in), true)
                    apply()
                }
                scope.launch {
                    userRepository.insertUser(it)
                }
                Alerter.create(this@WebLoginActivity)
                    .setTitle("登陆成功")
                    .setText("Login Success")
                    .setBackgroundColorRes(R.color.alerter_default_success_background)
                    .setDuration(2000)
                    .enableSwipeToDismiss()
                    .enableIconPulse(true)
                    .enableVibration(true)
                    .setOnHideListener {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("isLongin", true)
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
        })
        isRegistered.observe(this, androidx.lifecycle.Observer {
            if (it) {
                Alerter.create(this@WebLoginActivity)
                    .setTitle("注册成功")
                    .setText("点击此处以登陆账号")
                    .setBackgroundColorRes(R.color.alerter_default_success_background)
                    .setDuration(6000)
                    .enableSwipeToDismiss()
                    .enableIconPulse(true)
                    .enableVibration(true)
                    .setOnHideListener {
                        val intent = Intent(this, WebLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setOnClickListener {
                        val intent = Intent(this, WebLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
        })

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
                if (url.equals("https://robertsspaceindustries.com/graphql")) {

                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        loginWebView.addJavascriptInterface(recorder, "recorder")
        loginWebView.settings.javaScriptEnabled = true
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        loginWebView.setInitialScale(230)

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
                return WebResourceResponse(
                    response.body?.contentType().toString(),
                    "UTF-8",
                    response.body?.byteStream()
                )
            }
            return super.shouldInterceptRequest(view, request)
        }


    }

    class PayloadRecorder {
        private var csrfToken: String? = null
        private var email: String? = null
        private var password: String? = null
        private var deviceId: String? = null
        private var rsiToken: String? = null
        private var isSuccess = false

        @JavascriptInterface
        fun saveEmailPassword(email: String, password: String) {
            this.email = email
            this.password = password
        }

        @JavascriptInterface
        fun saveCsrfToken(csrfToken: String) {
            this.csrfToken = csrfToken
        }

        @JavascriptInterface
        fun saveDeviceId(deviceId: String) {
            this.deviceId = deviceId
        }

        @JavascriptInterface
        fun saveRsiToken(rsiToken: String) {
            this.rsiToken = rsiToken
        }

        @JavascriptInterface
        fun saveIsSuccess(isSuccess: Boolean) {
            this.isSuccess = isSuccess
        }
    }
}
