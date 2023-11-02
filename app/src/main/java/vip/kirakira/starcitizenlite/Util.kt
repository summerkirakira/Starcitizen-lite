package vip.kirakira.starcitizenlite

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat.startActivity
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.tapadoo.alerter.Alert
import com.tapadoo.alerter.Alerter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.network.*
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.network.search.getPlayerSearchResult
import java.lang.reflect.Method


enum class ShopItemType(val itemName: String) {
    SHIP("Standalone Ship"),
    PAINT("Paints"),
    GEAR("Gear"),
    ADDON("Add-Ons"),
    UEC("UE Credits"),
    GIFT("Gift Cards"),
    PACKAGE("Package"),
    PACKS("Packs"),
    SHIP_UPGRADE("Upgrade"),
}

fun checkDeviceHasNavigationBar(context: Context): Boolean {
    var hasNavigationBar = false
    val rs: Resources = context.getResources()
    val id: Int = rs.getIdentifier("config_showNavigationBar", "bool", "android")
    if (id > 0) {
        hasNavigationBar = rs.getBoolean(id)
    }
    try {
        val systemPropertiesClass = Class.forName("android.os.SystemProperties")
        val m: Method = systemPropertiesClass.getMethod("get", String::class.java)
        val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
        if ("1" == navBarOverride) {
            hasNavigationBar = false
        } else if ("0" == navBarOverride) {
            hasNavigationBar = true
        }
    } catch (e: Exception) {
    }
    return hasNavigationBar
}

fun getVirtualBarHeigh(context: Context): Int {
    var vh = 0
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val dm = DisplayMetrics()
    try {
        val c = Class.forName("android.view.Display")
        val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
        method.invoke(display, dm)
        vh = dm.heightPixels - windowManager.defaultDisplay.height
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return vh
}

fun compareVersion(currentVersion: String, newVersion: String): Boolean {
    val currentVersionArray = currentVersion.split(".")
    val newVersionArray = newVersion.replace("-debug", "").split(".")
    for (i in 0 until currentVersionArray.size) {
        if (newVersionArray.size <= i) {
            return false
        }
        if (currentVersionArray[i].toInt() < newVersionArray[i].toInt()) {
            return false
        }
    }
    if(currentVersion == newVersion) {
        return false
    }
    return true
}

fun createWarningAlerter(activity: Activity, title: String, message: String, duration: Long = 5000): Alerter {
    return Alerter.create(activity)
        .setTitle(title)
        .setText(message)
        .setBackgroundColorRes(R.color.alert_dialog_background_failure)
        .setIcon(R.drawable.ic_warning)
        .setDuration(duration)
}

fun createSuccessAlerter(activity: Activity, title: String, message: String, duration: Long = 5000): Alerter {
    return Alerter.create(activity)
        .setTitle(title)
        .setText(message)
        .setBackgroundColorRes(R.color.alerter_default_success_background)
        .enableSwipeToDismiss()
        .setDuration(duration)
}

fun createMessageDialog(context: Context, title: String, message: String): QMUIDialog.MessageDialogBuilder {
    return QMUIDialog.MessageDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
}

fun getThemeName(themeName: String): String{
    return when(themeName){
        "DEEP_BLUE" -> "Theme.StarcitizenLite"
        "DEEP_RED" -> "Theme.StarcitizenLiteDeepRed"
        "BLACK" -> "Theme.StarcitizenLiteBlack"
        "LIGHT_PINK" -> "Theme.StarcitizenLiteLightPink"
        "DEEP_PURPLE" -> "Theme.StarcitizenLiteDeepPurple"
        "LIGHT_GREEN" -> "Theme.StarcitizenLiteLightGreen"
        else -> "Theme.StarcitizenLite"
    }
}

fun getShipAliasById(id: Int): ShipAlias? {
    for (ship in shipAlias) {
        if (ship.id == id) {
            return ship
        }
    }
    return null
}

fun getRSIToken(my_rsi_token: String? = null, my_rsi_device: String? = null) {
    if (my_rsi_token != null && my_rsi_device != null) {
        setRSICookie(my_rsi_token, my_rsi_device)
    } else {
        val url = "https://robertsspaceindustries.com/pledge"
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        rsi_token = response.header("Set-Cookie")?.split(";")?.get(0)?.split("=")?.get(1) ?: ""
        setRSICookie(rsi_token, rsi_device)
    }
    val csrfClient = OkHttpClient()
    val csrfRequest = okhttp3.Request.Builder()
        .url("https://robertsspaceindustries.com/pledge")
        .addHeader("Cookie", "CookieConsent=$RSI_COOKIE_CONSTENT; Rsi-Token=$rsi_token; _rsi_device=$rsi_device")
        .addHeader(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36"
        )
        .build()
    val csrfResponse = csrfClient.newCall(csrfRequest).execute()
    val str_response = csrfResponse.body!!.string()
    csrf_token = str_response.split("csrf-token\" content=\"")?.get(1)?.split("\"")?.get(0) ?: ""
    Log.d("Cirno", "CSRF Token: $csrf_token")
}