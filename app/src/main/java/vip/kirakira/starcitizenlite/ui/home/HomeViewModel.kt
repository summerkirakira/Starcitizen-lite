package vip.kirakira.starcitizenlite.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.BuybackItem
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

    private val originBuyBackItems = buybackItemRepository.allItems

    private val originHangerItems = hangerItemRepository.allPackagesAndItems

    var buybackItems = originBuyBackItems

    var isRefreshing = hangerItemRepository.isRefreshing

    var hangerItems = originHangerItems

    var currentUser: LiveData<User> = userRepository.getUserById(primaryUserId)

    var currentMode = MutableLiveData<Mode>(Mode.HANGER)

    var refreshBuybackError = MutableLiveData<Boolean>(false)

    var refreshHangerError = MutableLiveData<Boolean>(false)

    var hangerItemFilter = MutableLiveData<String>("")

    var buybackItemFilter = MutableLiveData<String>("")

    var isDetailShowing = MutableLiveData<Boolean>(false)

    enum class Mode {
        BUYBACK,
        HANGER
    }

    init {
//        refreshBuybackItems()
        refresh()

        hangerItems = Transformations.switchMap(hangerItemFilter){
            filterHangerByTitle(it)
        }

        buybackItems = Transformations.switchMap(buybackItemFilter){
            filterBuybackByTitle(it)
        }

    }

    fun refresh() {
        viewModelScope.launch {
            hangerItemFilter.value = ""
            try {
                hangerItemRepository.refreshItems(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
                refreshHangerError.value = true
            }

            if(currentUser.value != null) {
                val newUser = currentUser.value
                newUser?.hanger_value = hangerItemRepository.getTotalValue()
                preferences.edit().putInt(getApplication<Application>().getString(R.string.current_hanger_value_key), hangerItemRepository.getCurrentValue()).apply()
                if (newUser != null) {
                    userRepository.insertUser(newUser)
                }
            }
        }
    }

    fun refreshBuybackItems() {
        viewModelScope.launch {
            buybackItemFilter.value = ""
            try {
                buybackItemRepository.refreshItems(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
                refreshBuybackError.value = true
            }

        }
    }

    private fun filterHangerByTitle(filter: String):  LiveData<List<HangerPackageWithItems>> {
        return when (filter) {
            "Subscribe" -> {
                Transformations.map(originHangerItems) {
                    it.filter { item -> item.hangerPackage.can_gift && item.hangerPackage.currentPrice == 0 }
                }
            }
            "Ship" -> {
                Transformations.map(originHangerItems) {
                    it.filter { item -> !item.hangerPackage.is_upgrade && item.hangerPackage.currentPrice >= 15 }
                }
            }
            "Trash" -> {
                Transformations.map(originHangerItems) {
                    it.filter { item -> !item.hangerPackage.is_upgrade &&
                            item.hangerPackage.currentPrice == 0 &&
                            !item.hangerPackage.can_gift
                    }
                }
            }
            else -> {
                Transformations.map(originHangerItems) {
                    it.filter { item -> item.hangerPackage.title.contains(filter, true) }
                }
            }
        }

    }

    private fun filterBuybackByTitle(filter: String):  LiveData<List<BuybackItem>> {
        return Transformations.map(originBuyBackItems) {
            it.filter {
                item -> item.title.contains(filter, true)
            }
        }
    }

    fun setFilter(filter: String) {
        when(currentMode.value) {
            Mode.HANGER -> {
                hangerItemFilter.value = filter
            }
            Mode.BUYBACK -> {
                buybackItemFilter.value = filter
            }
            else -> {}
        }
    }

}