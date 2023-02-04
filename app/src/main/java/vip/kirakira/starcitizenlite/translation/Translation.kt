package vip.kirakira.starcitizenlite.translation

import android.app.Application
import vip.kirakira.starcitizenlite.database.ShopItemDatabase

class Translation(private val shopItemDatabase: ShopItemDatabase) {
    private val translationDao = shopItemDatabase.translationDao

    fun getTranslation(key: String): String? {
        var translation = translationDao.getByEnglishTitle(key)
        if(translation != null) {
            return translation.title
        }
        var message = ""
        if(key.contains("Upgrade - ")) {
            message = "升级包 - "
            val shipNames = key.replace("Upgrade - ", "").split(" to ")
            val fromShip = shipNames[0].trim()
            val toShip = shipNames[1].trim()
            var fromShipTrans = ""
            if(fromShip.contains("Standard Edition")) {
                translation = translationDao.getByEnglishTitle(fromShip.replace("Standard Edition", "").trim())
                if(translation != null) {
                    fromShipTrans = translation.title + "标准版"
                } else {
                    fromShipTrans = fromShip
                }
            } else if(fromShip.contains("BIS Warbond Edition")) {
                translation = translationDao.getByEnglishTitle(fromShip.replace("BIS Warbond Edition", "").trim())
                if(translation != null) {
                    fromShipTrans = translation.title + "最佳展示版"
                } else {
                    fromShipTrans = fromShip
                }
            } else if(fromShip.contains("Warbond Edition")) {
                translation = translationDao.getByEnglishTitle(fromShip.replace("Warbond Edition", "").trim())
                if(translation != null) {
                    fromShipTrans = translation.title + "战争债券版"
                } else {
                    fromShipTrans = fromShip
                }
            } else {
                translation = translationDao.getByEnglishTitle(fromShip)
                if(translation != null) {
                    fromShipTrans = translation.title
                } else {
                    fromShipTrans = fromShip
                }
            }
            var toShipTrans = ""
            if(toShip.contains("Standard Edition")) {
                translation = translationDao.getByEnglishTitle(toShip.replace("Standard Edition", "").trim())
                if(translation != null) {
                    toShipTrans = translation.title + "标准版"
                } else {
                    toShipTrans = toShip
                }
            } else if(toShip.contains("Warbond Edition")) {
                translation = translationDao.getByEnglishTitle(toShip.replace("Warbond Edition", "").trim())
                if(translation != null) {
                    toShipTrans = translation.title + "战争债券版"
                } else {
                    toShipTrans = toShip
                }
            } else {
                translation = translationDao.getByEnglishTitle(toShip)
                if(translation != null) {
                    toShipTrans = translation.title
                } else {
                    toShipTrans = toShip
                }
            }
            message += "$fromShipTrans 到 $toShipTrans"
        }
        if(key.contains("Standalone Ship - ")) {
            translation = translationDao.getByEnglishTitle(key.replace("Standalone Ship - ", "").trim())
            if(translation != null) {
                message = "单船 - " + translation.title
            } else {
                message = key
            }
        }
        return if(message == "") {
            null
        } else {
            message
        }
    }
}