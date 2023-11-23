package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradePathPostBody
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradeResponse
import vip.kirakira.starcitizenlite.repositories.RepoUtil
import vip.kirakira.starcitizenlite.ui.home.Parser
import vip.kirakira.starcitizenlite.R

class ShipUpgradeCartViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ShipUpgradeCartViewModel"
    val shipUpgradePathList: MutableLiveData<List<UpgradeItemProperty>> = MutableLiveData()
    val warningMessage: MutableLiveData<WarningMessage> = MutableLiveData()
    private val translationDao = getDatabase(application).translationDao
    private val allHangarPackageWithItems = getDatabase(application).hangerItemDao.getAll()
    private val allBuyBacks = getDatabase(application).buybackItemDao.getAll()
    private val ownedUpgradeList = mutableListOf<OwnedUpgrade>()
    private val ownedBuybackUpgradeList = mutableListOf<OwnedUpgrade>()
    private val ownedHangarShip = mutableListOf<ShipAlias>()
    private val preferences = application.getSharedPreferences(
        application.getString(R.string.preference_file_key),
        android.content.Context.MODE_PRIVATE
    )
    lateinit var fromShipAlias: ShipAlias
    lateinit var toShipAlias: ShipAlias
    var isFromShipInHangar = false

    private val bannedUpgradeList = mutableListOf<BannedUpgrade>()
    var originalUpgradePrice = 0

    init {
//        fetchShipUpgradePath()
        allHangarPackageWithItems.observeForever {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    updateOwnedHangarUpgradeList()
                }
            }
        }
        allBuyBacks.observeForever {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    updateOwnedBuybackUpgradeList()
                }
            }
        }
    }

    fun fetchShipUpgradePath() {
        viewModelScope.launch {
            try {
                val fromShipId = preferences.getInt("upgrade_search_from_ship_id", 1)
                val toShipId = preferences.getInt("upgrade_search_to_ship_id", 98)

                bannedUpgradeList.clear()
                val bannedUpgradeString = preferences.getString("upgrade_search_banned_list", "")
                if (bannedUpgradeString != "") {
                    bannedUpgradeString!!.split(",").map {
                        bannedUpgradeList.add(convertStringToBannedUpgrade(it))
                    }
                }

                val useHistoryCcu = preferences.getBoolean("upgrade_search_use_history_ccu", true)
                val onlyCanBuyShips = preferences.getBoolean("upgrade_search_only_can_buy_ships", false)
                val upgradeMultiplier = preferences.getFloat("upgrade_search_upgrade_multiplier", 1.5f)
                val useBuyback = preferences.getBoolean("upgrade_search_use_buyback", true)
                val useHangar = preferences.getBoolean("upgrade_search_use_hangar", true)
                if (getHangarShipById(fromShipId) != null) {
                    isFromShipInHangar = true
                } else {
                    isFromShipInHangar = false
                }
                fromShipAlias = getShipAliasById(fromShipId)!!
                toShipAlias = getShipAliasById(toShipId)!!

                val result = CirnoApi.retrofitService.getUpgradePath(
                    ShipUpgradePathPostBody(
                        from_ship_id = fromShipId,
                        to_ship_id = toShipId,
                        banned_list = bannedUpgradeList.toIdList(),
                        hangar_upgrade_list = ownedUpgradeList.toHangarUpgrade(),
                        buyback_upgrade_list = ownedBuybackUpgradeList.toHangarUpgrade(),
                        use_history_ccu = useHistoryCcu,
                        use_buyback_ccu = useBuyback,
                        use_hangar_ccu = useHangar,
                        only_can_buy_ships = onlyCanBuyShips,
                        upgrade_multiplier = upgradeMultiplier)
                )
                if (result.code == 0) {
                    withContext(Dispatchers.IO) {
                        val upgradePathList = mutableListOf<UpgradeItemProperty>()
                        for (step in result.path.steps) {
                            convertPathResponseToUpgradeItem(step)?.let {
                                upgradePathList.add(it)
                            }
                        }
                        shipUpgradePathList.postValue(upgradePathList)
                    }
                } else {
                    warningMessage.postValue(WarningMessage(result.message, result.code))
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    private fun convertPathResponseToUpgradeItem(step: ShipUpgradeResponse.ShipUpgradePath.Step): UpgradeItemProperty? {
        when(step.type) {
            1 -> {
                return convertNormalStepToUpgradeItem(step)
            }
            2 -> {
                return convertHistoryStepToUpgradeItem(step)
            }
            3 -> {
                return getHangarUpgradeItem(step.id)
            }
            4 -> {
                return getBuybackUpgradeItem(step.id)
            }
        }
        return null
    }

    private fun convertNormalStepToUpgradeItem(step: ShipUpgradeResponse.ShipUpgradePath.Step): UpgradeItemProperty? {
        val fromShip = getShipAliasById(step.from_ship)
        val toShip = getShipAliasById(step.to_ship)
        if (fromShip == null || toShip == null) {
            return null
        }
        return UpgradeItemProperty(
            id = step.id,
            name = "${fromShip.name} to ${toShip.name} Upgrade",
            fromShipName = translateShipName(fromShip.name),
            toShipName = translateShipName(toShip.name),
            image = "",
            isAvailable = step.available,
            isWarbond = false,
            origin = UpgradeItemProperty.OriginType.NORMAL,
            price = step.price,
            originalPrice = toShip.getHighestSku() - fromShip.getHighestSku(),
            saving = toShip.getHighestSku() - fromShip.getHighestSku() - step.price,
            isLimitedTime = step.available,
            type = "",
            currentPrice = toShip.getHighestSku() - fromShip.getHighestSku(),
            shipPrice = toShip.getHighestSku()
        )
    }

    private fun convertHistoryStepToUpgradeItem(step: ShipUpgradeResponse.ShipUpgradePath.Step): UpgradeItemProperty? {
        val fromShip = getShipAliasById(step.from_ship)
        val toShip = getShipAliasById(step.to_ship)
        if (fromShip == null || toShip == null) {
            return null
        }
        return UpgradeItemProperty(
            id = step.id,
            name = step.name,
            fromShipName = translateShipName(fromShip.name),
            toShipName = translateShipName(toShip.name),
            image = "",
            isAvailable = false,
            isWarbond = step.name.contains("Warbond"),
            origin = UpgradeItemProperty.OriginType.HISTORY,
            price = step.price,
            originalPrice = step.price,
            saving = toShip.getHighestSku() - fromShip.getHighestSku() - step.price,
            isLimitedTime = step.available,
            type = "",
            currentPrice = toShip.getHighestSku() - fromShip.getHighestSku(),
            shipPrice = toShip.getHighestSku()
        )
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

    private fun getShipAliasById(id: Int): ShipAlias? {
        return RepoUtil.getShipAlias(id)
    }

    private fun getFullUpgradeName(upgradeTitle: String): List<String> {
        val nameList = mutableListOf<String>()
        for(shipName in upgradeTitle.replace("Upgrade - ", "").replace(" Upgrade", "").split(" to ")){
            nameList.add(Parser.getFormattedShipName(shipName))
        }
        return nameList
    }

    private fun getOwnedUpgradeByHangarPackage(hangarPackage: HangerPackage): OwnedUpgrade? {
        val shipNameList = getFullUpgradeName(hangarPackage.title)
        if (shipNameList.isEmpty()) {
            return null
        }
        if (shipNameList.size < 2) {
            return null
        }
        val fromShip = getShipAliasByName(shipNameList[0])
        val toShip = getShipAliasByName(shipNameList[1])
        if (fromShip == null || toShip == null) {
            return null
        }
        val fromShipName = translateShipName(fromShip.name)
        val toShipName = translateShipName(toShip.name)
        return OwnedUpgrade(
            id = hangarPackage.id,
            fromShip = fromShip,
            toShip = toShip,
            type = UpgradeItemProperty.OriginType.HANGAR,
            name = hangarPackage.title,
            fromShipName = fromShipName,
            toShipName = toShipName,
            price = hangarPackage.value
        )
    }

    private fun getOwnedUpgradeByBuyBackItem(buybackItem: BuybackItem): OwnedUpgrade? {
        val shipNameList = getFullUpgradeName(buybackItem.title)
        if (shipNameList.isEmpty()) {
            return null
        }
        val fromShip = getShipAliasByName(shipNameList[0])
        val toShip = getShipAliasByName(shipNameList[1])
        if (fromShip == null || toShip == null) {
            return null
        }
        if (fromShip.getHighestSku() >= toShip.getHighestSku()) {
            return null
        }
        val fromShipName = translateShipName(fromShip.name)
        val toShipName = translateShipName(toShip.name)
        return OwnedUpgrade(
            id = buybackItem.id,
            fromShip = fromShip,
            toShip = toShip,
            type = UpgradeItemProperty.OriginType.BUYBACK,
            name = buybackItem.title,
            fromShipName = fromShipName,
            toShipName = toShipName,
            price = toShip.getHighestSku() - fromShip.getHighestSku()
        )
    }

    private fun updateOwnedHangarUpgradeList() {
        ownedUpgradeList.clear()
        ownedHangarShip.clear()
        val hangarUpgradeNameList = mutableListOf<String>()
        if (allHangarPackageWithItems.value == null) return
        for (hangerPackageWithItem in allHangarPackageWithItems.value!!) {
            if (hangarUpgradeNameList.contains(hangerPackageWithItem.hangerPackage.title)) continue
            hangarUpgradeNameList.add(hangerPackageWithItem.hangerPackage.title)
            if (hangerPackageWithItem.hangerPackage.is_upgrade) {
                getOwnedUpgradeByHangarPackage(hangerPackageWithItem.hangerPackage)?.let {
                    ownedUpgradeList.add(it)
                }
            } else {
                hangerPackageWithItem.hangerItems.forEach {
                    if (it.kind == "Ship") {
                        getShipAliasByName(it.title)?.let { it1 -> ownedHangarShip.add(it1) }
                    }
                }
            }
        }
//        Log.d(TAG, ownedUpgradeList.toString())
    }

    private fun updateOwnedBuybackUpgradeList() {
        ownedBuybackUpgradeList.clear()
        val buybackUpgradeNameList = mutableListOf<String>()
        if (allBuyBacks.value == null) return
        for (buybackItem in allBuyBacks.value!!) {
            if (buybackUpgradeNameList.contains(buybackItem.title)) continue
            buybackUpgradeNameList.add(buybackItem.title)
            if (buybackItem.title.contains("Upgrade")) {
                getOwnedUpgradeByBuyBackItem(buybackItem)?.let {
                    ownedBuybackUpgradeList.add(it)
                }
            }
        }
    }

    private fun getOwnedHangarUpgradeById(id: Int): OwnedUpgrade? {
        for (ownedUpgrade in ownedUpgradeList) {
            if (ownedUpgrade.id == id) {
                return ownedUpgrade
            }
        }
        return null
    }

    private fun getOwnedBuybackUpgradeById(id: Int): OwnedUpgrade? {
        for (ownedUpgrade in ownedBuybackUpgradeList) {
            if (ownedUpgrade.id == id) {
                return ownedUpgrade
            }
        }
        return null
    }

    private fun getHangarUpgradeItem(id: Int): UpgradeItemProperty {
        val upgradeItem = getOwnedHangarUpgradeById(id)
        return UpgradeItemProperty(
            id = upgradeItem!!.id,
            name = upgradeItem.name,
            fromShipName = upgradeItem.fromShipName,
            toShipName = upgradeItem.toShipName,
            image = "",
            isAvailable = true,
            isWarbond = upgradeItem.name.contains("Warbond"),
            origin = UpgradeItemProperty.OriginType.HANGAR,
            price = upgradeItem.price,
            originalPrice = upgradeItem.price,
            saving = upgradeItem.toShip.getHighestSku() - upgradeItem.fromShip.getHighestSku() - upgradeItem.price,
            isLimitedTime = false,
            type = "",
            currentPrice = upgradeItem.toShip.getHighestSku() - upgradeItem.fromShip.getHighestSku(),
            shipPrice = upgradeItem.toShip.getHighestSku()
        )
    }

    private fun getHangarShipById(id: Int): ShipAlias? {
        for (ship in ownedHangarShip) {
            if (ship.id == id) {
                return ship
            }
        }
        return null
    }

    private fun getBuybackUpgradeItem(id: Int): UpgradeItemProperty {
        val upgradeItem = getOwnedBuybackUpgradeById(id)
        return UpgradeItemProperty(
            id = upgradeItem!!.id,
            name = upgradeItem.name,
            fromShipName = upgradeItem.fromShipName,
            toShipName = upgradeItem.toShipName,
            image = "",
            isAvailable = false,
            isWarbond = upgradeItem.name.contains("Warbond"),
            origin = UpgradeItemProperty.OriginType.BUYBACK,
            price = upgradeItem.price,
            originalPrice = upgradeItem.price,
            saving = upgradeItem.toShip.getHighestSku() - upgradeItem.fromShip.getHighestSku() - upgradeItem.price,
            isLimitedTime = false,
            type = "",
            currentPrice = upgradeItem.toShip.getHighestSku() - upgradeItem.fromShip.getHighestSku(),
            shipPrice = upgradeItem.toShip.getHighestSku()
        )
    }

    fun List<OwnedUpgrade>.toHangarUpgrade(): List<ShipUpgradePathPostBody.HangarUpgrade> {
        val bannedIdList = bannedUpgradeList.toIdList()
        return this
            .filter {
                it.id !in bannedIdList
            }
            .map {
                ShipUpgradePathPostBody.HangarUpgrade(
                    id = it.id,
                    from_ship = it.fromShip.id,
                    to_ship = it.toShip.id,
                    price = it.price
                )
        }
    }

    companion object {
        fun convertStringToBannedUpgrade(string: String): BannedUpgrade {
            val type = when(string.split("#")[1]) {
                "1" -> UpgradeItemProperty.OriginType.NORMAL
                "2" -> UpgradeItemProperty.OriginType.HISTORY
                "3" -> UpgradeItemProperty.OriginType.HANGAR
                "4" -> UpgradeItemProperty.OriginType.BUYBACK
                else -> UpgradeItemProperty.OriginType.NOT_AVAILABLE
            }
            return BannedUpgrade(
                id = string.split("#")[0].toInt(),
                type = type,
                name = string.split("#")[2]
            )
//            return BannedUpgrade(
//                id = 1,
//                type = UpgradeItemProperty.OriginType.NOT_AVAILABLE,
//                name =  ""
//            )
        }
    }


    fun List<BannedUpgrade>.toIdList(): List<Int> {
        val idList = mutableListOf<Int>()
        for (bannedUpgrade in this) {
            idList.add(bannedUpgrade.id)
        }
        return idList
    }

}