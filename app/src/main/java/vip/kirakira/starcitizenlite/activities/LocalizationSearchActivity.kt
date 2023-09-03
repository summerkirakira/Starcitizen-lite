package vip.kirakira.starcitizenlite.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gyf.immersionbar.ImmersionBar
import vip.kirakira.starcitizenlite.R

class LocalizationSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localization_search)
        val immersionBar = ImmersionBar.with(this)
        immersionBar.transparentBar()
            .fullScreen(true)
            .statusBarDarkFont(true)
            .init()
    }
}