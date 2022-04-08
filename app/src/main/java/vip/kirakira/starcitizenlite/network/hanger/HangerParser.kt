package vip.kirakira.starcitizenlite.network.hanger

import okhttp3.Request
import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import java.net.URL



class HangerProcess {

    data class PackageWithItem(val hangerPackages: List<HangerPackage>, val hangerItems: List<HangerItem>)

    fun parsePage(page: String): PackageWithItem {
            val doc = Jsoup.parse(page)
            val hangerPackages: MutableList<HangerPackage> = mutableListOf()
            val hangerItems: MutableList<HangerItem> = mutableListOf()
            val pledgeList = doc.select(".list-items").select(".row")
            for(pledge in pledgeList) {
                val pledgeId = pledge.select(".js-pledge-id").attr("value").toInt()
                val pledgeValue = (pledge.select(".js-pledge-value").attr("value").replace("$", "").replace(" USD", "").toFloat() * 100).toInt()
                val pledgeImage = pledge.select("div.image").attr("style").replace("background-image:url('", "").replace("');", "")
                val pledgeTitle = pledge.select(".title-col")[0].text()
                val pledgeStatus = pledge.select(".availability").text()
                val pledgeDate = pledge.select(".date-col").text().replace("Created: ", "")
                val pledgeContains = pledge.select(".items-col").text().replace("Contains:", "")
                val canGift = pledge.select(".shadow-button.js-gift").isNotEmpty()
                val canExchange = pledge.select(".shadow-button.js-reclaim").isNotEmpty()
                val canUpgrade = pledge.select(".shadow-button.js-apply-upgrade").isNotEmpty()
                val upgradeInfo = pledge.select(".js-upgrade-data").attr("value")
                val alsoContainsString = pledge.select(".title").joinToString("#") { it.text() }
                val hangerPackage = HangerPackage(pledgeId, pledgeTitle, pledgeImage, pledgeValue, pledgeStatus, canUpgrade, upgradeInfo, pledgeDate, pledgeContains, alsoContainsString, canGift, canExchange, System.currentTimeMillis())
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
            val time = pledge.select("dl").select("dd")[0].text()
            val contains = pledge.select("dl").select("dd")[2].text()
            var pledgeId = 0
            try {
                pledgeId = pledge.select(".holosmallbtn").attr("href").split("/").last().toInt()
            }
            catch (e: Exception) {
                pledgeId = pledge.select(".holosmallbtn").attr("data-pledgeid").toInt()
            }
            buybackItems.add(BuybackItem(pledgeId, title, image, time, contains, "回购此物品将会消耗一次回购机会", System.currentTimeMillis()))
        }
        return buybackItems
    }
}