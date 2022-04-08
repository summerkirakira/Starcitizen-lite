package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.hanger.HangerService

class BuyBackItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<BuybackItem>> = database.buybackItemDao.getAll()
    var isRefreshing = MutableLiveData<Boolean>(false)
    private val headers = mapOf<String, String>("cookie" to "CookieConsent={stamp:%27yW0Q5I4vGut12oNYLMr/N0OUTu+Q5WcW8LJgDKocZw3n9aA+4Ro4pA==%27%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cver:1%2Cutc:1647068701970%2Cregion:%27gb%27}; _gcl_au=1.1.193600841.1647068702; _ga=GA1.2.1021954892.1647068702; _rsi_device=lrya90x95d53j94m721xvm8k8p; __stripe_mid=3c5840ef-b484-4276-a889-652f21c1358ce969f7; wsc_view_count=2; wsc_hide=true; _gid=GA1.2.455835242.1649000218; Rsi-Token=635c2ab43f37cd7f72f49b6f60ac874b; Rsi-XSRF=H3ROYg%3AA2t6WtVhKcapJZbowjODlg%3AeoxALTZbX2vxmIuJ5FniXA%3A1649310504103")
    suspend fun refreshItems() {

        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            while (true) {
                val data = HangerService().getBuybackInfo(page, headers)
                if (data.isEmpty()) {
                    break
                }
                database.buybackItemDao.insertAll(data)
                page++
            }
        }
        isRefreshing.postValue(false)
    }

}