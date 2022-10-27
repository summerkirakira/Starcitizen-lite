package vip.kirakira.starcitizenlite.repositories

import android.app.Activity
import android.app.Application
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

    suspend fun refreshTranslation(application: Application){
        val pref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE)
        val currentVersion = pref.getString(application.getString(R.string.translation_version_key), "0.0")?:"0.0"
        withContext(Dispatchers.IO) {
            try {
                val enabledTranslation = pref.getBoolean(application.getString(R.string.enable_localization_key), false)
                if (!enabledTranslation)
                    return@withContext
                val translationVersion = CirnoApi.retrofitService.getTranslationVersion().version
                if (compareVersion(translationVersion, currentVersion)) {
                    val translations = CirnoApi.retrofitService.getAllTranslation()
                    database.translationDao.insertAll(translations.toTranslation())
                    pref.edit().putString(application.getString(R.string.translation_version_key), translationVersion).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            database.translationDao.deleteAll()
        }
    }

    private fun List<TranslationProperty>.toTranslation(): List<Translation> {
        return map {
            Translation(
                id = it.id,
                product_id = it.product_id,
                type = it.type,
                english_title = it.english_title,
                excerpt = it.excerpt,
                from_ship = it.from_ship,
                to_ship = it.to_ship,
                insert_time = Date().time,
                title = it.title,
            )
        }
    }
}