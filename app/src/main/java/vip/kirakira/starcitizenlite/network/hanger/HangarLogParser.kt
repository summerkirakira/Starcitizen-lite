package vip.kirakira.starcitizenlite.network.hanger

import org.jsoup.Jsoup
import vip.kirakira.starcitizenlite.database.HangarLog
import java.text.SimpleDateFormat
import java.util.*

class HangarLogParser {
    fun parseLog(log: String): String {
        var doc = log
        doc = doc.replace("\"", "")
            .replace("\\", "")
            .replace("\n", "")
        val page = Jsoup.parse(doc)
        val logList = page.select(".pledge-log-entry")
        for(logEntry in logList) {
            val logText = logEntry.select("p").text()
            val inputFormat = SimpleDateFormat("MMM dd yyyy, h:mm a", Locale.ENGLISH)
            val timeText = logText.split("-")[0].trim()
            val time = inputFormat.parse(timeText)
            var title = logText.split("#")[0].trim().replace("$timeText - ", "")
            val targetId = logText.split("#").last().split("-")[0].trim().toInt()
            val type = logText.split("-")[2].trim()
            val a = 1
        }
        return ""
    }
}