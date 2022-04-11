package vip.kirakira.starcitizenlite.network

import android.app.Application
import vip.kirakira.starcitizenlite.repositories.UserRepository
import java.text.SimpleDateFormat

val DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36"
var DEFAULT_REFERER = "https://robertsspaceindustries.com/"
val RSI_COOKIE_CONSTENT = "{stamp:%27yW0Q5I4vGut12oNYLMr/N0OUTu+Q5WcW8LJgDKocZw3n9aA+4Ro4pA==%27%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cver:1%2Cutc:1647068701970%2Cregion:%27gb%27}"
var rsi_cookie = RSI_COOKIE_CONSTENT
var rsi_device = ""
var rsi_token = ""


fun setRSICookie(rsiToken: String, rsiDevice: String) {
    rsi_device = rsiDevice
    rsi_token = rsiToken
    rsi_cookie = "CookieConsent=$RSI_COOKIE_CONSTENT;_rsi_device=$rsi_device;Rsi-Token=$rsi_token"
}

fun convertDateToLong(date: String): Long {
    val stringDate: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
    val parsedData = stringDate.parse(date)
    return parsedData!!.time
}

fun convertLongToDate(date: Long): String {
    val stringDate: SimpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.CHINA)
    return stringDate.format(date)
}