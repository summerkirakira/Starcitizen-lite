package vip.kirakira.starcitizenlite.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.BannerRepository
import vip.kirakira.viewpagertest.repositories.ShopItemRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bannerRepository = BannerRepository(getDatabase(application))

    val bannerItems = bannerRepository.allItems

    val takeBanners = Transformations.map(bannerItems) {
        it.shuffled().take(6)
    }


    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            bannerRepository.refresh()
        }
    }
}