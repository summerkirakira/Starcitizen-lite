package vip.kirakira.starcitizenlite.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.gyf.immersionbar.ImmersionBar
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.getThemeName
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip

open class RefugeBaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val sharedPreferences = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE)
        var theme = sharedPreferences.getString(getString(R.string.theme_color_key), "DEEP_BLUE")
        if(!RefugeVip.isVip()) {
            theme = "DEEP_BLUE"
        }
        val themeId = resources.getIdentifier(getThemeName(theme!!), "style", packageName)
        setTheme(themeId)
        super.onCreate(savedInstanceState)
        initStatusBar(theme)
    }


    private fun initStatusBar(theme: String = "DEEP_BLUE") {
        val navigationBarColor = when(theme) {
            "DEEP_BLUE" -> R.color.white
            "BLACK" -> R.color.color_night_secondary
            else -> R.color.white
        }
        val mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.transparentBar()
            .fullScreen(false)
            .navigationBarColor(navigationBarColor)
            .init()
    }
}