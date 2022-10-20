package vip.kirakira.starcitizenlite.network.hanger

import org.jsoup.Jsoup

class PledgeUpgradeParser {
    data class ChooseUpgradeTargetItem(val pledge_id: String, val name: String)
    fun parse(page: String): List<ChooseUpgradeTargetItem> {
        val doc = Jsoup.parse(page)
        val chooseUpgradeTargetItems = mutableListOf<ChooseUpgradeTargetItem>()
        val targetItems = doc.select("div[class=row]")
        for (item in targetItems) {
            val pledgeId = item.select("input").attr("value")
            val name = item.select("span").text()
            chooseUpgradeTargetItems.add(ChooseUpgradeTargetItem(pledgeId, name))
        }
        return chooseUpgradeTargetItems
    }
}