package vip.kirakira.starcitizenlite.network.search

import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.network.RSIApi

data class PlayerSearchResult(
    val name: String,
    val image: String,
    val handle: String,
    val enlisted: String,
    val location: String?,
    val fluency: String,
    val organization: Organization?,
    val isHidden: Boolean
) {
    data class Organization(
        val name: String,
        val id: String,
        val rank: Int,
        val rankName: String,
        val logo: String
    )
}

fun playerParser(page: String): PlayerSearchResult? {
    if (page.contains("You are currently venturing unknown space")) {
        return null
    }
    val doc = Jsoup.parse(page)
    val image = doc.select(".left-col.profile").select(".thumb").select("img").attr("src")
    val name = doc.select(".left-col.profile").select(".entry")[0].select("strong").text()
    val handle = doc.select(".left-col.profile").select(".entry")[1].select("strong").text()
    val enlisted = doc.select(".left-col")[1].select(".entry")[0].select("strong").text()
    var location: String? = null
    var fluency: String = ""
    if(doc.select(".left-col")[1].select(".entry")[1].select(".label").text() == "Fluency") {
        location = null
        fluency = doc.select(".left-col")[1].select(".entry")[1].select("strong").text()
    } else {
        location = doc.select(".left-col")[1].select(".entry")[1].select("strong").text()
        fluency = doc.select(".left-col")[1].select(".entry")[2].select("strong").text()
    }
    if(doc.select(".member-visibility-restriction").isNotEmpty()){
        return PlayerSearchResult(
            name = name,
            image = image,
            handle = handle,
            enlisted = enlisted,
            location = location,
            fluency = fluency,
            organization = null,
            isHidden = true
        )
    }

    if ("NO MAIN ORG FOUND IN PUBLIC RECORDS" in page) {
        return PlayerSearchResult(
            name = name,
            image = image,
            handle = handle,
            enlisted = enlisted,
            location = location,
            fluency = fluency,
            organization = null,
            isHidden = false
        )
    }
    val logo = doc.select(".right-col").select(".thumb").select("img").attr("src")
    val orgId = doc.select(".right-col").select(".thumb").select("a").attr("href").split("/")[2]
    val orgName = doc.select(".right-col").select(".entry")[0].select("a").text()
    val orgRankList = doc.select(".ranking").select("span")
    var orgRank = 0
    for (item in orgRankList){
        if(item.attr("class") == "active") {
            orgRank ++
        }
    }
    val orgRankName = doc.select(".right-col").select(".entry")[2].select("strong").text()
    return PlayerSearchResult(
        name = name,
        image = image,
        handle = handle,
        enlisted = enlisted,
        location = location,
        fluency = fluency,
        organization = PlayerSearchResult.Organization(
            name = orgName,
            id = orgId,
            rank = orgRank,
            rankName = orgRankName,
            logo = logo,
        ),
        isHidden = false
    )
}

fun getPlayerSearchResult(handle: String): PlayerSearchResult? {
    val page = RSIApi.getPlayerInfoPage(handle)
    return playerParser(page)
}