package vip.kirakira.starcitizenlite.network.hanger

import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.HangarLog
import java.text.SimpleDateFormat
import java.util.*

class HangarLogParser {
    fun parseLog(log: String): List<HangarLog> {
        var doc = log
//        doc = doc.replace("\"", "")
//            .replace("\\", "")
//            .replace("\n", "")
        val page = Jsoup.parse(doc)
        val logList = page.select(".pledge-log-entry")
        val logListResult = mutableListOf<HangarLog>()
        for(logEntry in logList) {
            val logText = logEntry.select("p").text()
            val inputFormat = SimpleDateFormat("MMM dd yyyy, h:mm a", Locale.ENGLISH)
            val timeText = logText.split("-")[0].trim()
            val time = inputFormat.parse(timeText)
            val title = logText.split("#")[0].trim().replace("$timeText - ", "")
            val targetId = logText.split("#")[1].split("-")[0].trim().toInt()
            var type = "UNKNOWN"
            if(logText.contains("Reclaimed by")) {
                type = "RECLAIMED"
            }
            if(logText.contains("Created by")) {
                type = "CREATED"
            }
            var order: String? = null
            if (logText.contains("order")) {
                order = logText.split("order")[1].trim().split(",")[0].trim().replace("#", "")
            }
            var price: Int? = null
            if (logText.contains("value: ")) {
                price = if (logText.contains("newValue: "))
                    (logText.split("newValue: ")[1].replace("$", "").replace("USD", "").trim().toFloat() * 100).toInt()
                else
                    (logText.split("value: ")[1].replace("$", "").replace("USD", "").trim().toFloat() * 100).toInt()
            }
            logListResult.add(
                HangarLog(
                    id = "${type}#${targetId}#${time!!.time}",
                    time = time!!.time,
                    type = type,
                    name = title,
                    insert_time = Date().time,
                    price = price,
                    order = order
                )
            )
        }
        return logListResult
    }
}