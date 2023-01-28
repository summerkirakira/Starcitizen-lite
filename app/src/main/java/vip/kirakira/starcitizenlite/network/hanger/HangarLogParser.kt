package vip.kirakira.starcitizenlite.network.hanger

import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.HangarLog
import java.text.SimpleDateFormat
import java.util.*

class HangarLogParser {
    enum class HangarLogRegex(val regex: Regex) {
        CREATED(Regex("^#(\\d+?) - Created by ([\\w\\d-]+?) - order #([A-Z0-9]+?), value: \\\$([0-9.]+?) USD\$")),
        RECLAIMED(Regex("^#(\\d+?) - Reclaimed by ([\\w\\d-]+?) for \\\$([0-9.]+?) USD\$")),
        CONSUMED(Regex("^#(\\d+?) - Consumed by ([\\w\\d-]+?) on pledge #(\\d+?), value: \\\$([0-9.]+?) USD\$")),
        APPLIED_UPGRADE(Regex("^#(\\d+?) - Upgrade applied: #(\\d+?) ([^,]+?), new value: \\\$([0-9.]+?) USD\$")),
        BUYBACK(Regex("^#(\\d+?) - Buy-back by ([\\w\\d-]+?) - order #([\\w\\d]+?)\$")),
        GIFT(Regex("^#(\\d+?) - Gifted to ([^,]+?), value: \\\$([0-9.]+?) USD\$")),
        GIFT_CLAIMED(Regex("^#(\\d+) - Claimed as a gift by ([\\w\\d-]+?), value: \\\$([0-9.]+?) USD\$")),
        GIFT_CANCELLED(Regex("^#(\\d+?) - Gift cancelled by ([\\d\\w-]+?), value: \\\$([0-9.]+?) USD\$")),
        NAME_CHANGE(Regex("^#(\\d+) - Name Reservation: \\((.+)\\) on item (.+)\$")),
        NAME_CHANGE_RECLAIMED(Regex("^#(\\d+) - Name Release: \\(([^\\)]+)\\) on item (\\S+) Reclaimed\$")),
        GIVEAWAY(Regex("^#(\\d+?) - (.*?)\$"))
    }
    fun parseLog(log: String): List<HangarLog> {
        val page = Jsoup.parse(log)
        val logList = page.select(".pledge-log-entry")
        val logListResult = mutableListOf<HangarLog>()
        for(logEntry in logList) {
            val logText = logEntry.select("p").text()
            val name = logEntry.select("span").text()
            val inputFormat = SimpleDateFormat("MMM dd yyyy, h:mm a", Locale.ENGLISH)
            val messagePart = logText.split("-", limit = 2)
            val timeText = messagePart[0].trim()
            val time = inputFormat.parse(timeText)
            val content = messagePart[1].trim().substring(name.length).trim()
            var type = "UNKNOWN"
            var price: Int? = null
            var order: String? = null
            var source: String? = null
            var target: String? = null
            var operator: String? = null
            var reason: String? = null

            if(content.matches(HangarLogRegex.CREATED.regex)) {
                val resultGroup = HangarLogRegex.CREATED.regex.find(content)!!.groupValues
                type = "CREATED"
                target = resultGroup[1]
                operator = resultGroup[2]
                order = resultGroup[3]
                price = (resultGroup[4].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.RECLAIMED.regex)) {
                val resultGroup = HangarLogRegex.RECLAIMED.regex.find(content)!!.groupValues
                type = "RECLAIMED"
                target = resultGroup[1]
                operator = resultGroup[2]
                price = (resultGroup[3].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.CONSUMED.regex)) {
                val resultGroup = HangarLogRegex.CONSUMED.regex.find(content)!!.groupValues
                type = "CONSUMED"
                target = resultGroup[1]
                operator = resultGroup[2]
                source = resultGroup[3]
                price = (resultGroup[4].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.APPLIED_UPGRADE.regex)) {
                val resultGroup = HangarLogRegex.APPLIED_UPGRADE.regex.find(content)!!.groupValues
                type = "APPLIED_UPGRADE"
                target = resultGroup[1]
                source = resultGroup[2]
                reason = resultGroup[3]
                price = (resultGroup[4].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.BUYBACK.regex)) {
                val resultGroup = HangarLogRegex.BUYBACK.regex.find(content)!!.groupValues
                type = "BUYBACK"
                target = resultGroup[1]
                operator = resultGroup[2]
                order = resultGroup[3]
            } else if(content.matches(HangarLogRegex.GIFT.regex)) {
                val resultGroup = HangarLogRegex.GIFT.regex.find(content)!!.groupValues
                type = "GIFT"
                target = resultGroup[1]
                operator = resultGroup[2]
                price = (resultGroup[3].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.GIFT_CLAIMED.regex)) {
                val resultGroup = HangarLogRegex.GIFT_CLAIMED.regex.find(content)!!.groupValues
                type = "GIFT_CLAIMED"
                target = resultGroup[1]
                operator = resultGroup[2]
                price = (resultGroup[3].toDouble() * 100).toInt()
            } else if(content.matches(HangarLogRegex.GIFT_CANCELLED.regex)) {
                val resultGroup = HangarLogRegex.GIFT_CANCELLED.regex.find(content)!!.groupValues
                type = "GIFT_CANCELLED"
                target = resultGroup[1]
                operator = resultGroup[2]
                price = (resultGroup[3].toDouble() * 100).toInt()
            } else if (content.matches(HangarLogRegex.NAME_CHANGE.regex)) {
                val resultGroup = HangarLogRegex.NAME_CHANGE.regex.find(content)!!.groupValues
                type = "NAME_CHANGE"
                target = resultGroup[1]
                source = resultGroup[2]
                reason = resultGroup[3]
            } else if (content.matches(HangarLogRegex.NAME_CHANGE_RECLAIMED.regex)) {
                val resultGroup = HangarLogRegex.NAME_CHANGE_RECLAIMED.regex.find(content)!!.groupValues
                type = "NAME_CHANGE_RECLAIMED"
                target = resultGroup[1]
                source = resultGroup[2]
                reason = resultGroup[3]
            } else if (content.matches(HangarLogRegex.GIVEAWAY.regex)) {
                val resultGroup = HangarLogRegex.GIVEAWAY.regex.find(content)!!.groupValues
                type = "GIVEAWAY"
                target = resultGroup[1]
                reason = resultGroup[2]
            }


            logListResult.add(
                HangarLog(
                    id = "${type}#${target}#${time!!.time}",
                    time = time!!.time,
                    operator = operator,
                    type = type,
                    name = name,
                    insert_time = Date().time,
                    price = price,
                    order = order,
                    source = source,
                    reason = reason,
                    target = target
                )
            )
        }
        return logListResult
    }
}