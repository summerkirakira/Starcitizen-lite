package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.ShipDetail
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradePathPostBody
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradeResponse
import vip.kirakira.starcitizenlite.repositories.HangerItemRepository
import vip.kirakira.starcitizenlite.repositories.RepoUtil
import vip.kirakira.starcitizenlite.repositories.TranslationRepository
import vip.kirakira.starcitizenlite.ui.home.Parser

class ShipUpgradeCartViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ShipUpgradeCartViewModel"
    private val shipUpgradePath: MutableLiveData<List<UpgradeItemProperty>> = MutableLiveData()
    private val translationDao = getDatabase(application).translationDao
    private val shipDetailDao = getDatabase(application).shipDetailDao
    private val allHangarItems = getDatabase(application).hangerItemDao.getAll()
    private val allBuyBacks = getDatabase(application).buybackItemDao.getAll()

    init {
        fetchShipUpgradePath()
    }

    fun fetchShipUpgradePath() {
        viewModelScope.launch {
            try {
                val a = CirnoApi.retrofitService.getUpgradePath(
                    ShipUpgradePathPostBody(1, 37, listOf(1, 2, 3), listOf(
                        ShipUpgradePathPostBody.HangarUpgrade(10001,1, 2, 100)), listOf(), true, true, 1.5f)
                )
                Log.d(TAG, a.toString())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    private fun convertPathResponseToUpgradeItem(step: ShipUpgradeResponse.ShipUpgradePath.Step): UpgradeItemProperty {

    }

    private fun translateShipName(name: String): String {
        translationDao.getByEnglishTitle(name)?.let {
            return it.title
        }
        return name
    }

    private fun getShipAliasByName(name: String): ShipAlias? {
        return RepoUtil.getShipAlias(name)
    }

    private fun getCurrentPrice(shipAlias: ShipAlias): Int {
        return RepoUtil.getHighestAliasPrice(shipAlias)
    }

    fun getFullUpgradeName(upgradeTitle: String): List<String> {
        val nameList = mutableListOf<String>()
        for(shipName in upgradeTitle.replace("Upgrade - ", "").replace(" Upgrade", "").split(" to ")){
            nameList.add(Parser.getFormattedShipName(shipName))
        }
        return nameList
    }

}