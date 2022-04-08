package vip.kirakira.starcitizenlite.network.hanger

import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.network.RSIApi

class HangerService {
    suspend fun getHangerInfo(page: Int, headerMap: Map<String, String>): HangerProcess.PackageWithItem {
        val hangerInfo = RSIApi.getHangerPage(page=page, headers=headerMap)
        return HangerProcess().parsePage(hangerInfo)
    }

    suspend fun getBuybackInfo(page: Int, headerMap: Map<String, String>): List<BuybackItem> {
        val buybackInfo = RSIApi.getBuybackPage(page=page, headers=headerMap)
        return HangerProcess().parseBuybackItem(buybackInfo)
    }
}