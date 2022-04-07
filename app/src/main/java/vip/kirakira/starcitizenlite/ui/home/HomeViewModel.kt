package vip.kirakira.starcitizenlite.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.HangerItemRepository

class HomeViewModel(application: Application): AndroidViewModel(application) {
    private val hangerItemRepository = HangerItemRepository(getDatabase(application))

    val hangerItems = hangerItemRepository.allPackagesAndItems

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            hangerItemRepository.refreshItems()
        }
    }


}