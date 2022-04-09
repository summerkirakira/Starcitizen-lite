package vip.kirakira.starcitizenlite.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.BuyBackItemRepository
import vip.kirakira.starcitizenlite.repositories.HangerItemRepository

class HomeViewModel(application: Application): AndroidViewModel(application) {
    private val hangerItemRepository = HangerItemRepository(getDatabase(application))

    private val buybackItemRepository = BuyBackItemRepository(getDatabase(application))

    val isBuybackRefreshing = buybackItemRepository.isRefreshing

    val buybackItems = buybackItemRepository.allItems

    var isRefreshing = hangerItemRepository.isRefreshing

    val hangerItems = hangerItemRepository.allPackagesAndItems

    init {
//        refreshBuybackItems()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            hangerItemRepository.refreshItems()
        }
    }

    fun refreshBuybackItems() {
        viewModelScope.launch {
            buybackItemRepository.refreshItems()
        }
    }


}