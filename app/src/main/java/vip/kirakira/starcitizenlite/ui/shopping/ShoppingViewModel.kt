package vip.kirakira.viewpagertest.ui.shopping

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.network.RSIApiService
import vip.kirakira.starcitizenlite.repositories.TranslationRepository
import vip.kirakira.starcitizenlite.ui.shopping.ShopItemFilter
import vip.kirakira.viewpagertest.network.graphql.FilterShipsQuery
import vip.kirakira.viewpagertest.repositories.ShopItemRepository

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    private val shopItemsRepository = ShopItemRepository(getDatabase(application))

    private val translationRepository = TranslationRepository(getDatabase(application))

    var isRefreshing = shopItemsRepository.isRefreshing

    private val dataResource = shopItemsRepository.allItems

    var itemsAfterFilter = dataResource

    var needRefresh = MutableLiveData<Boolean>(false)

    var isUpgradeMode = MutableLiveData<Boolean>(false)

    private var _popUpItem: MutableLiveData<ShopItem> = MutableLiveData()
    val popUpItem: LiveData<ShopItem>
        get() = _popUpItem

    var catalog = itemsAfterFilter

    val filter = MutableLiveData<ShopItemFilter>().apply { value = ShopItemFilter("", listOf("Standalone Ship")) }

    var isDetailShowing = MutableLiveData<Boolean>(false)

    var currentUpgradeStage = MutableLiveData<UpgradeStage>(UpgradeStage.UNDEFINED)

    init {
        getCatalog()
        sortByName()
        sortByPriceDesc()
        itemsAfterFilter = Transformations.switchMap(filter) {
            filterBySubtitle(filter.value!!, false)
        }

    }
    private fun isNew(item: ShopItem): Boolean = System.currentTimeMillis() - item.insert_time < 1000 * 60 * 60

    fun setFilter(filter: ShopItemFilter) {
        this.filter.value = filter
    }

    fun filterBySubtitle(filter: ShopItemFilter, canBuy: Boolean = false): LiveData<List<ShopItem>> {
        return Transformations.map(dataResource) {
            it.filter {
                    item -> ((
                        filter.name.isEmpty() &&
                                filter.type.any {value -> value.equals(item.subtitle, true)}
                    ) ||
                    (filter.name.isNotEmpty() && item.name.contains(filter.name, true)) && (
                        !canBuy || isNew(item)
                    )) && (
                        filter.ids == null || filter.ids.contains(item.id)
                    ) && (
                        !filter.onlyCanUpgradeTo || item.isUpgradeAvailable
                    )
            }
        }
    }

    private fun filterBySearch(search: String, canBuy: Boolean = false) {
        itemsAfterFilter = Transformations.map(dataResource) {
            it.filter {
                    item -> item.name.contains(search, true) ||
                    item.subtitle.contains(search, true) &&
                    (!canBuy || isNew(item))
            }

        }
        catalog = itemsAfterFilter
    }

    fun sortByName() {
        catalog = Transformations.map(itemsAfterFilter) {
            it.sortedBy { it.name }
        }
    }

    private  fun sortByPriceAsc() {
        catalog = Transformations.map(itemsAfterFilter) {
            it.sortedBy { it.price }
        }
    }

    private fun sortByPriceDesc() {
        catalog = Transformations.map(itemsAfterFilter) {
            it.sortedByDescending { it.price }
        }
    }

    fun getCatalog() {
        viewModelScope.launch {
            try{
                if (filter.value != null && filter.value!!.name.isEmpty()) {
                    setFilter(filter.value!!)
                } else {
                    setFilter(ShopItemFilter("", listOf("Standalone Ship")))
                }
                shopItemsRepository.refreshItems(getApplication())
                shopItemsRepository.initShipUpgrades(getApplication())
                translationRepository.refreshTranslation(getApplication())

//                val data = RSIApi.retrofitService.filterShips(FilterShipsQuery().getRequestBody())
//                val selectedShips: MutableList<Int> = mutableListOf()
//                data.data.to.ships.forEach {
//                    selectedShips.add(it.id + 100000)
//                }
//                setFilter(ShopItemFilter("", listOf("Upgrade"), onlyCanUpgradeTo = true))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun popUpItemDetail(item: ShopItem) {
        _popUpItem.value = item
    }

    enum class UpgradeStage {
        CHOOSE_TO_SHIP,
        CHOOSE_FROM_SHIP,
        UNDEFINED
    }



}