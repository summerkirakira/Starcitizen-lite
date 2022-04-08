package vip.kirakira.viewpagertest.ui.shopping

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.viewpagertest.repositories.ShopItemRepository

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    private val shopItemsRepository = ShopItemRepository(getDatabase(application))

    var isRefreshing = shopItemsRepository.isRefreshing

    private val dataResource = shopItemsRepository.allItems

    private var itemsAfterFilter = dataResource

    var needRefresh = MutableLiveData<Boolean>(false)

    private var _popUpItem: MutableLiveData<ShopItem> = MutableLiveData()
    val popUpItem: LiveData<ShopItem>
        get() = _popUpItem

    var catalog = itemsAfterFilter

    init {
        getCatalog()
        sortByPriceDesc()
        filterBySubtitle("Standalone Ship", false)
    }

    private fun isNew(item: ShopItem): Boolean = System.currentTimeMillis() - item.insert_time < 1000 * 60 * 60

    private fun filterBySubtitle(subtitle: String, canBuy: Boolean = false) {
        itemsAfterFilter = Transformations.map(dataResource) {
            it.filter {
                    item -> item.subtitle.equals(subtitle, true) && (!canBuy || isNew(item))
            }
        }
        catalog = itemsAfterFilter
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

    private fun sortByName() {
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
               shopItemsRepository.refreshItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun popUpItemDetail(item: ShopItem) {
        _popUpItem.value = item
    }
}