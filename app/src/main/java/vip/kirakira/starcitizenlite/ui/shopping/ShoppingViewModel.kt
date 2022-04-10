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

    var itemsAfterFilter = dataResource

    var needRefresh = MutableLiveData<Boolean>(false)

    private var _popUpItem: MutableLiveData<ShopItem> = MutableLiveData()
    val popUpItem: LiveData<ShopItem>
        get() = _popUpItem

    var catalog = itemsAfterFilter

    val filter = MutableLiveData<List<String>>().apply { value = listOf("Standalone Ship") }

    init {
        getCatalog()
        sortByName()
        sortByPriceDesc()
        itemsAfterFilter = Transformations.switchMap(filter) {
            filterBySubtitle(filter.value!!, false)
        }

    }
    private fun isNew(item: ShopItem): Boolean = System.currentTimeMillis() - item.insert_time < 1000 * 60 * 60

    fun setFilter(filter: List<String>) {
        this.filter.value = filter
    }

    fun filterBySubtitle(subtitle: List<String>, canBuy: Boolean = false): LiveData<List<ShopItem>> {
        return Transformations.map(dataResource) {
            it.filter {
                    item -> subtitle.any {value -> value.equals(item.subtitle, true)} && (!canBuy || isNew(item))
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