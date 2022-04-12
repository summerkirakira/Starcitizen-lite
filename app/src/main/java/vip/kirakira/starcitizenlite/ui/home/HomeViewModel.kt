package vip.kirakira.starcitizenlite.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.BuyBackItemRepository
import vip.kirakira.starcitizenlite.repositories.HangerItemRepository
import vip.kirakira.starcitizenlite.repositories.UserRepository

class HomeViewModel(application: Application): AndroidViewModel(application) {
    private val hangerItemRepository = HangerItemRepository(getDatabase(application))

    private val buybackItemRepository = BuyBackItemRepository(getDatabase(application))

    private val userRepository = UserRepository(getDatabase(application))

    val preferences = application.getSharedPreferences(application.getString(R.string.preference_file_key), 0)

    private val primaryUserId = preferences.getInt(application.getString(R.string.primary_user_key), 0)

    val isBuybackRefreshing = buybackItemRepository.isRefreshing

    val buybackItems = buybackItemRepository.allItems

    var isRefreshing = hangerItemRepository.isRefreshing

    val hangerItems = hangerItemRepository.allPackagesAndItems

    var currentUser: LiveData<User> = userRepository.getUserById(primaryUserId)

    var currentMode = MutableLiveData<Mode>(Mode.HANGER)

    var refreshBuybackError = MutableLiveData<Boolean>(false)

    var refreshHangerError = MutableLiveData<Boolean>(false)

    enum class Mode {
        BUYBACK,
        HANGER
    }

    init {
        refreshBuybackItems()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            println("refreshing")
            try {
                hangerItemRepository.refreshItems()
            } catch (e: Exception) {
                e.printStackTrace()
                refreshHangerError.value = true
            }

            if(currentUser.value != null) {
                val newUser = currentUser.value
                newUser?.hanger_value = hangerItemRepository.getTotalValue()
                if (newUser != null) {
                    userRepository.insertUser(newUser)
                }
            }
        }
    }

    fun refreshBuybackItems() {
        viewModelScope.launch {
            try {
                buybackItemRepository.refreshItems()
            } catch (e: Exception) {
                e.printStackTrace()
                refreshBuybackError.value = true
            }

        }
    }

}