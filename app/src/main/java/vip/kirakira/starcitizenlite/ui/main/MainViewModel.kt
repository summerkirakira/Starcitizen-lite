package vip.kirakira.starcitizenlite.ui.main

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.BannerRepository
import vip.kirakira.starcitizenlite.repositories.UserRepository
import java.security.AccessController.getContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bannerRepository = BannerRepository(getDatabase(application))

    val bannerItems = bannerRepository.allItems

    val takeBanners = bannerItems.map {
        it.shuffled().take(6)
    }

    val preferences = application.getSharedPreferences(application.getString(R.string.preference_file_key), 0)

    private val primaryUserId = preferences.getInt(application.getString(R.string.primary_user_key), 0)

    private val userRepository = UserRepository(getDatabase(application))

    var currentUser: LiveData<User> = userRepository.getUserById(primaryUserId)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            bannerRepository.refresh()
        }
    }
}