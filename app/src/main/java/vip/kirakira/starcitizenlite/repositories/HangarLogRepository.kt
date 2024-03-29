package vip.kirakira.starcitizenlite.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.database.HangarLog
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.translation.Translation

class HangarLogRepository(private val application: Application) {
    private val database = getDatabase(application)
    val allItems: LiveData<List<HangarLog>> = database.hangarLogDao.getAll()
    var isRefreshing = MutableLiveData<Boolean>(false)
    val preference = application.getSharedPreferences(application.getString(R.string.preference_file_key), 0)

    suspend fun refreshItems() {
        isRefreshing.postValue(true)
        val crawledPage = preference.getInt(application.getString(R.string.crawled_page_key), 0)
        withContext(Dispatchers.IO) {
            try {
                val pageCount = RSIApi.getPledgeLogCount()
                var page = pageCount - crawledPage + 1
                if (page < 0)
                    page = 127
                while (true) {
                    val data = RSIApi.getPledgeLog(page)
                    for(item in data) {
                        item.chineseName = Translation(database).getTranslation(item.name)
                        if (item.reason != null) {
                            item.order = Translation(database).getTranslation(item.reason!!)
                        }
                    }
                    val firstItem = data.firstOrNull()
                    if (firstItem != null) {
                        val oldIdList = firstItem.id.split("#")
                        val oldId = oldIdList[0] + "#" + oldIdList[1] + "#" + oldIdList[2]
                        if (database.hangarLogDao.getById(oldId) != null) {
                            database.hangarLogDao.deleteAll()
                            page = pageCount + 1
                        }
                    }
                    database.hangarLogDao.insertAll(data)

                    page--
                    Log.d("HangarLogRepository", "page: $page")
                    preference.edit().putInt(application.getString(R.string.crawled_page_key), pageCount - page).apply()
                    if (page == 0) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
            isRefreshing.postValue(false)
        }
    }
}