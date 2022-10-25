package vip.kirakira.starcitizenlite.network

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.network.search.getPlayerSearchResult
import vip.kirakira.starcitizenlite.repositories.UserRepository
import java.text.SimpleDateFormat

val DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36"
var DEFAULT_REFERER = "https://robertsspaceindustries.com/"
val RSI_COOKIE_CONSTENT = "{stamp:%27yW0Q5I4vGut12oNYLMr/N0OUTu+Q5WcW8LJgDKocZw3n9aA+4Ro4pA==%27%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cver:1%2Cutc:1647068701970%2Cregion:%27gb%27}"
var rsi_device = ""
var rsi_token = ""
var csrf_token = ""
var rsi_cookie = "CookieConsent=$RSI_COOKIE_CONSTENT"
var rsi_account_auth = ""
var rsi_ship_upgrades_context = ""


fun setRSICookie(rsiToken: String, rsiDevice: String) {
    rsi_device = rsiDevice
    rsi_token = rsiToken
    rsi_cookie = "CookieConsent=$RSI_COOKIE_CONSTENT;_rsi_device=$rsi_device;Rsi-Token=$rsi_token"
}

fun setRSIAccountAuth(token: String) {
    rsi_account_auth = token
}

fun setRSIShipUpgradesContext(context: String) {
    rsi_ship_upgrades_context = context
}

fun getShipUpgradesCookie(): String {
    return "Rsi-Ship-Upgrades-Context=$rsi_ship_upgrades_context;Rsi-Account-Auth=$rsi_account_auth; rsi_cookie"
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

fun saveUserData(uid: Int, rsi_device: String, rsi_token: String, email: String, password: String): User {
    val cookie = "CookieConsent=$RSI_COOKIE_CONSTENT;Rsi-Token=$rsi_token; _rsi_device=$rsi_device"
    val builder = Request.Builder()
    val req = builder.url("https://robertsspaceindustries.com/account/referral-program")
        .addHeader("cookie", cookie)
        .get()
        .build();
    val response = OkHttpClient().newCall(req).execute()
    val responseBody = response.body?.string()
    val doc = Jsoup.parse(responseBody)
    val csrfToken = doc.select("meta[name=csrf-token]").first().attr("content")
    if (csrfToken.isNotEmpty()) {
        csrf_token = csrfToken
    }
    val userName = doc.select(".c-account-sidebar__profile-info-displayname").text()
    val userHandle = doc.select(".c-account-sidebar__profile-info-handle").text()
    val userImage =
        doc.select(".c-account-sidebar__profile-metas-avatar").attr("style").replace("background-image:url('", "")
            .replace("');", "")
    val userCredits =
        (doc.select(".c-account-sidebar__profile-info-credits-amount--pledge").text().replace("\$", "")
            .replace(" ", "").replace("USD", "").replace(",", "").toFloat() * 100).toInt()
    val userUEC =
        doc.select(".c-account-sidebar__profile-info-credits-amount--uec").text().replace("¤", "").replace(" ", "")
            .replace("UEC", "").replace(",", "").toInt()
    val userREC =
        doc.select(".c-account-sidebar__profile-info-credits-amount--rec").text().replace("¤", "").replace(" ", "")
            .replace("REC", "").replace(",", "").toInt()
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
    val refResponseBody = refResponse.body?.string()
    val billingDoc = Jsoup.parse(refResponseBody)
    val totalSpent = (billingDoc.select(".spent-line").last().select("em").text().replace("\$", "").replace(" ", "")
        .replace("USD", "").replace(",", "").toFloat() * 100).toInt()
    val userInfo = getPlayerSearchResult(userHandle)
        ?: return User(
            uid,
            userName,
            "",
            email,
            password,
            rsi_token,
            rsi_device,
            userHandle,
            userImage,
            "",
            "",
            "",
            refCode,
            refNumber,
            userCredits,
            userUEC,
            userREC,
            0,
            totalSpent,
            isConcierge,
            isSubscribed,
            fleet,
            fleetImage,
            "",
            "",
            "",
            0,
            "",
            "",
            ""
        )
    if (userInfo.organization == null) {
        return User(
            uid,
            userName,
            "",
            email,
            password,
            rsi_token,
            rsi_device,
            userHandle,
            "https://robertsspaceindustries.com/${userInfo.image}",
            "",
            "",
            "",
            refCode,
            refNumber,
            userCredits,
            userUEC,
            userREC,
            0,
            totalSpent,
            isConcierge,
            isSubscribed,
            fleet,
            fleetImage,
            userInfo.location ?: "",
            "",
            "",
            0,
            "",
            "",
            ""
        )
    }
    return User(
        uid,
        userName,
        "",
        email,
        password,
        rsi_token,
        rsi_device,
        userHandle,
        "https://robertsspaceindustries.com/${userInfo.image}",
        "",
        "",
        "",
        refCode,
        refNumber,
        userCredits,
        userUEC,
        userREC,
        0,
        totalSpent,
        isConcierge,
        isSubscribed,
        fleet,
        "https://robertsspaceindustries.com/${userInfo.organization.logo}",
        userInfo.enlisted,
        userInfo.organization.name,
        userInfo.organization.logo,
        userInfo.organization.rank,
        userInfo.organization.rankName,
        userInfo.location ?: "",
        userInfo.fluency
    )
}