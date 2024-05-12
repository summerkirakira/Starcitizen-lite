package vip.kirakira.starcitizenlite.network.hanger

import okhttp3.Request
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.network.convertDateToLong
import java.net.URL

class HangerProcess {
    var hangarTime = 0

    data class PackageWithItem(val hangerPackages: List<HangerPackage>, val hangerItems: List<HangerItem>)

    private fun getHangarTimeId(): Int {
        if (hangarTime == 0) {
            hangarTime = 1000000
        }
        hangarTime--
        return hangarTime
    }

    fun parsePage(page: String, index: Int): PackageWithItem {
            val doc = Jsoup.parse(page)
            val hangerPackages: MutableList<HangerPackage> = mutableListOf()
            val hangerItems: MutableList<HangerItem> = mutableListOf()
            val pledgeList = doc.select(".list-items").select(".row")
            for(pledge in pledgeList) {
                val pledgeId = pledge.select(".js-pledge-id").attr("value").toInt()
                val pledgeValueString: String = pledge.select(".js-pledge-value").attr("value")
                var pledgeValue: Int = 0
                if(!pledgeValueString.contains("UEC")) {
                    pledgeValue = (pledgeValueString.replace("$", "").replace(" USD", "").replace(",", "").toFloat() * 100).toInt()
                }
//                pledgeValue = (pledge.select(".js-pledge-value").attr("value").replace("$", "").replace(" USD", "").replace(",", "").toFloat() * 100).toInt()
                val pledgeImage = pledge.select("div.image").attr("style").replace("background-image:url('", "").replace("');", "")
                var pledgeTitle = pledge.select(".js-pledge-name").attr("value")
                if (pledgeTitle.contains("nameable ship") && pledgeTitle.contains(" Contains ")) {
                    pledgeTitle = pledgeTitle.split(" Contains ")[0]
                }
                val pledgeStatus = pledge.select(".availability").text()
                val pledgeDate = convertDateToLong( pledge.select(".date-col").text().replace("Created: ", "")) - index
                val pledgeContains = pledge.select(".items-col").text().replace("Contains:", "")
                val canGift = pledge.select(".shadow-button.js-gift").isNotEmpty()
                val canExchange = pledge.select(".shadow-button.js-reclaim").isNotEmpty()
                val canUpgrade = pledge.select(".shadow-button.js-apply-upgrade").isNotEmpty()
                val upgradeInfo = pledge.select(".js-upgrade-data").attr("value")
                val alsoContainsString = pledge.select(".title").joinToString("#") { it.text() }
                val hangerPackage = HangerPackage(pledgeId, pledgeTitle, pledgeImage, pledgeValue, 0, pledgeStatus, canUpgrade, upgradeInfo, pledgeDate, pledgeContains, alsoContainsString, canGift, canExchange, System.currentTimeMillis())
                val itemList = pledge.select(".with-images").select(".item").mapIndexed { position, item ->
                    val id = "$pledgeId#$position"
                    val title = item.select(".title").text()
                    val image = item.select(".image").attr("style").replace("background-image:url('", "").replace("');", "")
                    val kind = item.select(".kind").text()
                    val subtitle = item.select(".liner").text()
                    HangerItem(id, image, pledgeId, title, kind, subtitle, System.currentTimeMillis())
                }
                hangerPackages.add(hangerPackage)
                hangerItems.addAll(itemList)
            }
            return PackageWithItem(hangerPackages, hangerItems)
        }

    fun parseBuybackItem(page: String): List<BuybackItem> {
        val doc = Jsoup.parse(page)
        val buybackItems: MutableList<BuybackItem> = mutableListOf()
        for (pledge in doc.select("article.pledge")) {
            val image = pledge.select("img").attr("src")
            val title = pledge.select(".information").select("h1").text()
            val timeString = pledge.select("dl").select("dd")[0].text()
            val time = convertDateToLong(timeString)
            val contains = pledge.select("dl").select("dd")[2].text()
            var pledgeId = 0
            var isUpgrade = false
            var fromShipId = 0
            var toShipId = 0
            var toSkuId = 0
            try {
                pledgeId = pledge.select(".holosmallbtn").attr("href").split("/").last().toInt()
            }
            catch (e: Exception) {
                pledgeId = pledge.select(".holosmallbtn").attr("data-pledgeid").toInt()
                isUpgrade = true
                fromShipId = pledge.select(".holosmallbtn").attr("data-fromshipid").toInt()
                toShipId = pledge.select(".holosmallbtn").attr("data-toshipid").toInt()
                toSkuId = pledge.select(".holosmallbtn").attr("data-toskuid").toInt()
            }
            buybackItems.add(BuybackItem(
                pledgeId,
                title,
                image,
                time,
                contains,
                "回购此物品将会消耗一次回购机会",
                System.currentTimeMillis(),
                isUpgrade,
                fromShipId,
                toShipId,
                toSkuId
            ))
        }
        return buybackItems
    }
}