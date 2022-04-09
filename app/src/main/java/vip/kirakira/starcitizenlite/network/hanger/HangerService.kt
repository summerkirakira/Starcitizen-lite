package vip.kirakira.starcitizenlite.network.hanger

import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.network.RSIApi

class HangerService {
    suspend fun getHangerInfo(page: Int): HangerProcess.PackageWithItem {
        val hangerInfo = RSIApi.getHangerPage(page=page)
        return HangerProcess().parsePage(hangerInfo)
    }

    suspend fun getBuybackInfo(page: Int): List<BuybackItem> {
        val buybackInfo = RSIApi.getBuybackPage(page=page)
        return HangerProcess().parseBuybackItem(buybackInfo)
    }
}