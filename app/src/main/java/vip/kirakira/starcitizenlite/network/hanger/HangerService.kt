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

    suspend fun reclaimPledge(itemId: String, password: String): BasicResponseBody {
        return RSIApi.retrofitService.reclaimPledge(ReclaimRequestBody(itemId, password))
    }

    suspend fun giftPledge(itemId: String, password: String, receiver: String="Powered by Refuge", email: String): BasicResponseBody {
//        println(GiftPledgeRequestBody(itemId, password, email, receiver))
        return RSIApi.retrofitService.giftPledge(GiftPledgeRequestBody(itemId, password, email, receiver))
    }

    suspend fun cancelPledge(itemId: String): BasicResponseBody {
        return RSIApi.retrofitService.cancelGift(CancelPledgeRequestBody(itemId))
    }
}