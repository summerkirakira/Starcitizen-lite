package vip.kirakira.starcitizenlite.repositories

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.MainActivity
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.compareVersion
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.database.Translation
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.TranslationProperty
import java.time.LocalTime
import java.util.Date

class TranslationRepository(private val database: ShopItemDatabase) {
    val allTranslation: LiveData<List<Translation>>
        get() = database.translationDao.getAll()

    suspend fun refreshTranslation(activity: Activity){
        val pref = activity.getSharedPreferences(
            activity.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE)
        val currentVersion = pref.getString("TranslationVersion", "0.0")?:"0.0"
        withContext(Dispatchers.IO) {
            try {
                val translationVersion = CirnoApi.retrofitService.getTranslationVersion().version
                if (compareVersion(currentVersion, translationVersion)) {
                    val translations = CirnoApi.retrofitService.getAllTranslation()
                    database.translationDao.insertAll(translations.toTranslation())
                    pref.edit().putString("TranslationVersion", translationVersion).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun List<TranslationProperty>.toTranslation(): List<Translation> {
        return map {
            Translation(
                id = it.id,
                product_id = it.product_id,
                type = it.type,
                english_title = it.english_title,
                content = it.content,
                contains = it.contains,
                excerpt = it.excerpt,
                from_ship = it.from_ship,
                to_ship = it.to_ship,
                insert_time = Date().time,
            )
        }
    }
}