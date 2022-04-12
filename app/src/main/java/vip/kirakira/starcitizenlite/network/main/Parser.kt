package vip.kirakira.starcitizenlite.network.main

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.BannerImage
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.ui.main.Banner

fun parseImageURL(page: String): List<String> {
    val listMyData = Types.newParameterizedType(MutableList::class.java, Ship::class.java)
    val jsonAdapter: JsonAdapter<List<Ship>> = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listMyData)
    val imgs = jsonAdapter.fromJson(page)!!.map { it.storeImageLarge }
    return imgs.shuffled().take(6)
}

fun List<String>.toBannerImage(): List<BannerImage> {
    return this.map { BannerImage(it) }
}

fun getRandomBannerURL(): List<BannerImage> {
    val page = RSIApi.getFleetYardsShipsPage()
    return parseImageURL(page).toBannerImage()
}

data class Ship(val slug: String, val storeImageLarge: String)