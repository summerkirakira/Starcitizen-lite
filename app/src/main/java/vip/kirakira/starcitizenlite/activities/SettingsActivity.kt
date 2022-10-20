package vip.kirakira.starcitizenlite.activities

import android.os.Bundle
import android.transition.Explode
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.gyf.immersionbar.ImmersionBar
import vip.kirakira.starcitizenlite.BuildConfig
import vip.kirakira.starcitizenlite.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        initStatusBar()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        with(window) {
            exitTransition = Explode()
            enterTransition = Explode()
            enterTransition.duration =500
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)
            val currentVersion: Preference? = findPreference("current_version")
            if(currentVersion != null){
                currentVersion.summary = BuildConfig.VERSION_NAME
            }
        }
    }

    fun  initStatusBar(){
        val mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.transparentBar()
            .fullScreen(true)
            .navigationBarColor(R.color.white)
            .statusBarDarkFont(true)
            .init()

    }
}