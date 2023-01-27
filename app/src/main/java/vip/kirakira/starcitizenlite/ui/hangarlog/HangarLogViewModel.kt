package vip.kirakira.starcitizenlite.ui.hangarlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.repositories.HangarLogRepository

class HangarLogViewModel(application: Application): AndroidViewModel(application) {
    private val hangarLogRepository = HangarLogRepository(application)

    val hangarLogs = hangarLogRepository.allItems

    var isRefreshing = hangarLogRepository.isRefreshing

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                hangarLogRepository.refreshItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}