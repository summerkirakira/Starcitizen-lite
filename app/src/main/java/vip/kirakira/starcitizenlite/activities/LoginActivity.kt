package vip.kirakira.starcitizenlite.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.gyf.immersionbar.ImmersionBar
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.toHeaderList
import vip.kirakira.starcitizenlite.MainActivity
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.createSuccessAlerter
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.*
import vip.kirakira.starcitizenlite.network.CirnoProperty.RecaptchaList
import vip.kirakira.starcitizenlite.network.shop.LoginProperty
import vip.kirakira.starcitizenlite.repositories.UserRepository
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip
import vip.kirakira.viewpagertest.network.graphql.*
import java.io.IOException
import java.util.Random

class LoginActivity : RefugeBaseActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var loginByWeb: TextView
    private lateinit var reEnterPassWordEditText: EditText
    private lateinit var referralCodeEditText: EditText
    private lateinit var handleEditText: EditText
    private lateinit var loadingLayout: ConstraintLayout
    private lateinit var loadingView: SpinKitView
    private lateinit var captchaLayout: LinearLayout
    private lateinit var captchaEditText: EditText
    private lateinit var captchaImageView: ImageView
    var isPrepared = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initStatusBar()
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        loginByWeb = findViewById(R.id.login_by_web)
        reEnterPassWordEditText = findViewById(R.id.re_enter_password_edit_text)
        referralCodeEditText = findViewById(R.id.referral_code_edit_text)
        handleEditText = findViewById(R.id.handle_edit_text)
        loadingLayout = findViewById(R.id.progress_layout)
        loadingView = findViewById(R.id.loading_view)
        captchaLayout = findViewById(R.id.captcha_layout)
        captchaEditText = findViewById(R.id.captcha_edit_text)
        captchaImageView = findViewById(R.id.captcha_image_view)

        val userRepository = UserRepository(getDatabase(application))
        getRSIToken()

        loginButton.setOnClickListener {
            if (reEnterPassWordEditText.visibility == View.VISIBLE) {
                reEnterPassWordEditText.visibility = View.GONE
                referralCodeEditText.visibility = View.GONE
                handleEditText.visibility = View.GONE
                loginByWeb.text = getString(R.string.login_by_web)
                emailEditText.hint = getString(R.string.email)
                passwordEditText.hint = getString(R.string.password)
                return@setOnClickListener
            }
            if (!isPrepared) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.please_wait_init))
                return@setOnClickListener
            }
            if (rsi_token.isEmpty()) {
                createWarningAlerter(this, getString(R.string.error), "初始化失败，官网可能在维护状态，请稍后再试")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }
            if (emailEditText.text.toString().isNotEmpty() && passwordEditText.text.toString().isNotEmpty()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                startLoading()
                lifecycleScope.launchWhenCreated {
//                    val token: ArrayList<String>
//                    try {
//                        token = CirnoApi.getReCaptchaV3(1)
//                    } catch (e: Exception) {
//                        stopLoading()
//                        createWarningAlerter(this@LoginActivity, getString(R.string.network_error), getString(R.string.token_server_error)).show()
//                        e.printStackTrace()
//                        return@launchWhenCreated
//                    }
//                    if (token.isEmpty()) {
//                        stopLoading()
//                        RefugeVip.createTokenNotSufficientWarningAlert(this@LoginActivity)
//                        return@launchWhenCreated
//                    }
//                    val loginDetail: LoginProperty
//                    val response: retrofit2.Response<LoginProperty>
                    val captcha = if (captchaEditText.text.isNotEmpty()) captchaEditText.text.toString() else null
                    val firstLoginAttempt = RSIApi.retrofitService.rsiLauncherSignIn(
                        RsiLauncherSignInBody(
                            username = email,
                            password = password,
                            remember = true,
                            captcha = captcha
                        )
                    )
                    Log.d("LoginActivity", firstLoginAttempt.toString())
                    if (firstLoginAttempt.success == 1) {
                        setRSICookie(firstLoginAttempt.data!!.session_id, rsi_device)
                        Thread {
                            val uid = Random().nextInt(1000000)
                            val newUser = saveUserData(
                                uid,
                                rsi_device,
                                rsi_token,
                                email,
                                password
                            )
    //                                            Log.d("LoginActivity", newUser.toString())
                            lifecycleScope.launchWhenCreated {
                                val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                                with(pref.edit()) {
                                    putInt(getString(R.string.primary_user_key), uid)
                                    putBoolean(getString(R.string.is_log_in), true)
                                    apply()
                                }
                                userRepository.insertUser(newUser).isSuccess.toString()
                                createSuccessAlerter(
                                    this@LoginActivity,
                                    getString(R.string.login_success),
                                    getString(R.string.jump_to_main_page)
                                ).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }.start()
                    } else {
                        rsi_token = firstLoginAttempt.data!!.session_id
                        if (firstLoginAttempt.data.device_id != null) {
                            rsi_device = firstLoginAttempt.data.device_id
                        }
                        if (firstLoginAttempt.code == "ErrCaptchaRequiredLauncher") {
                            val captchaResponse = RSIApi.retrofitService.rsiLauncherSignInCaptcha(
                                RsiLauncherRecaptchaPostbody()
                            )
                            val captchaBase64 = Base64.encodeToString(captchaResponse.bytes(), Base64.DEFAULT)
                            val captchaUrl = "data:image/png;base64,$captchaBase64"
                            captchaLayout.visibility = View.VISIBLE
                            Glide.with(this@LoginActivity).load(captchaUrl).into(captchaImageView)
                            stopLoading()
                            return@launchWhenCreated
                        } else if (firstLoginAttempt.code == "ErrMultiStepRequired") {
                            stopLoading()
                            setRSICookie(firstLoginAttempt.data!!.session_id, rsi_device)
                            val builder = QMUIDialog.EditTextDialogBuilder(this@LoginActivity)
                            builder.setTitle(getString(R.string.multi_step_required))
                                .setPlaceholder(getString(R.string.please_input_validation_code))
                                .addAction(getString(R.string.cancel)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .addAction(getString(R.string.accept)) { dialog, _ ->
                                    dialog.dismiss()
                                    lifecycleScope.launchWhenCreated {
                                        try {
                                            startLoading()
                                            val multiStepInfo = RSIApi.retrofitService.rsiLauncherSignInMultiStep(RsiLauncherSignInMultiStepBody(
                                                code = builder.editText.text.toString()
                                            ))
                                            if (multiStepInfo.success == 1) {
                                                setRSICookie(multiStepInfo.data!!.session_id, rsi_device)
                                                val uid = Random().nextInt(1000000)
                                                Thread {
                                                    val newUser = saveUserData(
                                                        uid,
                                                        rsi_device,
                                                        rsi_token,
                                                        email,
                                                        password
                                                    )
    //                                            Log.d("LoginActivity", newUser.toString())
                                                    lifecycleScope.launchWhenCreated {
                                                        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                                                        with(pref.edit()) {
                                                            putInt(getString(R.string.primary_user_key), uid)
                                                            putBoolean(getString(R.string.is_log_in), true)
                                                            apply()
                                                        }
                                                        userRepository.insertUser(newUser).isSuccess.toString()
                                                        createSuccessAlerter(
                                                            this@LoginActivity,
                                                            getString(R.string.login_success),
                                                            getString(R.string.jump_to_main_page)
                                                        ).show()
                                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                                }.start()
                                            } else {
                                                stopLoading()
                                                createWarningAlerter(
                                                    this@LoginActivity,
                                                    getString(R.string.error),
                                                    multiStepInfo.msg
                                                ).show()
                                            }

                                        } catch (e: Exception) {
                                            stopLoading()
                                            createWarningAlerter(this@LoginActivity,
                                                getString(R.string.error),
                                                getString(R.string.network_error)
                                            ).show()
                                            Log.d("LoginActivity", e.toString())
                                        }
                                    }
                                }
                                .show()
                            } else if (firstLoginAttempt.code == "ErrWrongPassword_email") {
                                stopLoading()
                                createWarningAlerter(this@LoginActivity, "登录失败", "请确认邮箱和密码是否正确哦").show()
                            } else {
                                stopLoading()
                                createWarningAlerter(this@LoginActivity, "登录失败", firstLoginAttempt.code).show()
                            }
                        }
                    }
//
            } else {
                Toast.makeText(this, getString(R.string.please_enter_email_and_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        loginByWeb.setOnClickListener {
            val intent = Intent(this, WebLoginActivity::class.java)
            startActivity(intent)
        }
        registerButton.setOnClickListener {
            if (reEnterPassWordEditText.visibility == View.GONE) {
                reEnterPassWordEditText.visibility = View.VISIBLE
                referralCodeEditText.visibility = View.VISIBLE
                handleEditText.visibility = View.VISIBLE
                loginByWeb.text = getString(R.string.register_by_web)
                emailEditText.hint = getString(R.string.input_register_email)
                passwordEditText.hint = getString(R.string.input_register_password)
                return@setOnClickListener
            }
            if (handleEditText.text.toString().isEmpty()) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.please_input_handle)).show()
            }
            if (emailEditText.text.toString().isEmpty()) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.please_enter_register_email)).show()
                return@setOnClickListener
            }
            if (passwordEditText.text.toString().isEmpty() || reEnterPassWordEditText.text.isEmpty()) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.please_enter_register_password)).show()
                return@setOnClickListener
            }
            if (passwordEditText.text.toString() != reEnterPassWordEditText.text.toString()) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.password_not_match)).show()
                return@setOnClickListener
            }
            if (!isPrepared) {
                createWarningAlerter(this, getString(R.string.error), getString(R.string.please_wait_init))
                return@setOnClickListener
            }
            if (rsi_token.isEmpty()) {
                createWarningAlerter(this, getString(R.string.error), "初始化失败，官网可能在维护状态，请稍后再试")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }
            startLoading()
            var referralCode: String = referralCodeEditText.text.toString()
            if (referralCode.isEmpty())
                referralCode = getString(R.string.default_referral_code)
            lifecycleScope.launchWhenCreated {
                val returnCode = register(activity = this@LoginActivity, email = emailEditText.text.toString(), password = passwordEditText.text.toString(), referralCode = referralCode, handle = handleEditText.text.toString())
                if (returnCode == 0) run {
                    startLoading()
//                    getRSIToken()
                    loginButton.performClick()
                    delay(6000)
                    loginButton.performClick()
                }
            }

        }
    }

    suspend fun register(activity: LoginActivity, email: String, password: String, handle: String, referralCode: String): Int {
        val tokenList: ArrayList<String>
        try {
            tokenList = CirnoApi.getReCaptchaV3(1)
        } catch (e: Exception) {
            createWarningAlerter(activity, activity.getString(R.string.error), activity.getString(R.string.token_server_error)).show()
            return 1
        }
        if (tokenList.isEmpty()) {
            RefugeVip.createTokenNotSufficientWarningAlert(activity)
            return 1
        }
        val token = tokenList[0]
        val registerDetail = RSIApi.retrofitService.signUp(RegisterBody(
            variables = RegisterBody.Variables(
                handle = handle,
                email = email,
                password = password,
                referralCode = referralCode,
                captcha = token
            ))
        )
        if (registerDetail.errors == null) {
            createSuccessAlerter(activity, activity.getString(R.string.register_success), activity.getString(R.string.jump_to_email_check)).show()
            return 0
        } else {
            if (registerDetail.errors[0].extensions.details.email != null) {
                createWarningAlerter(activity, activity.getString(R.string.email_error), registerDetail.errors[0].extensions.details.email!!).show()
                return 1
            }
            if (registerDetail.errors[0].extensions.details.handle != null) {
                createWarningAlerter(activity, activity.getString(R.string.handle_error), registerDetail.errors[0].extensions.details.handle!!).show()
                return 1
            }
            if (registerDetail.errors[0].extensions.details.password != null) {
                createWarningAlerter(activity, activity.getString(R.string.password_error), registerDetail.errors[0].extensions.details.password!!).show()
                return 1
            }
            createWarningAlerter(activity, activity.getString(R.string.error), registerDetail.errors[0].message).show()
            return 1
        }
    }

    fun startLoading() {
        loadingLayout.visibility = View.VISIBLE
        loadingView.visibility = View.VISIBLE
        setStatusBarColor(R.color.progress_background)
    }

    fun stopLoading() {
        loadingLayout.visibility = View.INVISIBLE
        loadingView.visibility = View.INVISIBLE
        setStatusBarColor(R.color.white)
    }

    private fun  initStatusBar(){
        val mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.transparentBar()
            .fullScreen(true)
            .navigationBarColor(R.color.white)
            .statusBarDarkFont(true)
            .init()
    }

    private fun setStatusBarColor(color: Int) {
        val mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.transparentBar()
            .fullScreen(true)
            .navigationBarColor(color)
            .init()
    }

    private fun getRSIToken() {
        val url = "https://robertsspaceindustries.com/pledge"
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Host", "robertsspaceindustries.com")
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.37")
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                createWarningAlerter(this@LoginActivity, getString(R.string.get_rsi_token_error), getString(R.string.network_error)).show()
            }
            override fun onResponse(call: Call, response: Response) {
                rsi_token = response.header("Set-Cookie")?.split(";")?.get(0)?.split("=")?.get(1) ?: ""
//                Log.d("rsi_token", rsi_token)
                setRSICookie(rsi_token, rsi_device)
                val csrfClient = OkHttpClient()
                val csrfRequest = okhttp3.Request.Builder()
                    .url("https://robertsspaceindustries.com")
                    .addHeader("Cookie", "CookieConsent=$RSI_COOKIE_CONSTENT; Rsi-Token=$rsi_token; _rsi_device=$rsi_device")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                    .build()
                csrfClient.newCall(csrfRequest).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        createWarningAlerter(this@LoginActivity, getString(R.string.get_csrf_token_error), getString(R.string.network_error)).show()
                    }
                    override fun onResponse(call: Call, response: Response) {
//                        Log.d("csrf", response.body!!.string())
                        csrf_token = response.body!!.string().split("csrf-token\" content=\"")?.get(1)?.split("\"")?.get(0) ?: ""
                        isPrepared = true
                    }
                })
            }

        })

    }
}