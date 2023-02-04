package vip.kirakira.starcitizenlite.activities

import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.gyf.immersionbar.ImmersionBar
import vip.kirakira.starcitizenlite.*
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.repositories.TranslationRepository
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip
import vip.kirakira.viewpagertest.repositories.ShopItemRepository

class SettingsActivity : RefugeBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        initStatusBar()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(application))
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        with(window) {
            exitTransition = Explode()
            enterTransition = Explode()
            enterTransition.duration =500
        }
    }

    class SettingsFragment(application: Application) : PreferenceFragmentCompat() {
        private val translationRepository = TranslationRepository(getDatabase(application))
        private val shopItemRepository = ShopItemRepository(getDatabase(application))
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)
            val application = requireActivity().application

            val pref = application.getSharedPreferences(
                application.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE)

            val currentVersion: Preference? = findPreference("current_version")
            if(currentVersion != null){
                currentVersion.summary = BuildConfig.VERSION_NAME
            }
            val localUUID: Preference? = findPreference("my_uuid")
            if(localUUID != null){
                localUUID.summary = uuid
                localUUID.setOnPreferenceClickListener {
                   //copy to clipboard
                    val clip: ClipData = ClipData.newPlainText("uuid", uuid)
                    val cm = application.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cm.setPrimaryClip(clip)
                    Toast.makeText(application, getString(R.string.uuid_is_copied), Toast.LENGTH_SHORT).show()
                    true
                }
            }
            val localization: Preference? = findPreference("enable_localization")
            localization?.setOnPreferenceChangeListener { _, newValue ->
                val enableLocalization = newValue as Boolean
                if (enableLocalization) {
                    lifecycleScope.launchWhenCreated {
                        pref.edit().apply {
                            putBoolean(getString(R.string.enable_localization_key), true)
                            putString(getString(R.string.translation_version_key), "0.0")
                            apply()
                        }
                        translationRepository.refreshTranslation(requireActivity().application)
                        shopItemRepository.refreshItems(requireActivity().application)
                    }
                } else {
                    lifecycleScope.launchWhenCreated {
                        translationRepository.deleteAll()
                        pref.edit().apply {
                            putBoolean(getString(R.string.enable_localization_key), false)
                            putString(getString(R.string.translation_version_key), "0.0")
                            apply()
                        }
                    }
                }
                true
            }

            val themeSelector: ListPreference? = findPreference("theme_color")
            themeSelector?.setOnPreferenceChangeListener { _, newValue ->
                val theme = newValue as String
                if(!RefugeVip.isVip()) {
                    RefugeVip.createWarningAlert(requireActivity())
                    return@setOnPreferenceChangeListener false
                }
                pref.edit().apply {
                    putString(getString(R.string.theme_color_key), theme)
                    apply()
                }
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                true
            }

            val bannedReclaimSwitch: SwitchPreferenceCompat? = findPreference("banned_reclaim")
            bannedReclaimSwitch?.setOnPreferenceChangeListener { preference, newValue ->
                pref.edit().apply {
                    putBoolean(getString(R.string.banned_reclaim_key), newValue as Boolean)
                    apply()
                }
                true
            }

        }
    }

    fun initStatusBar(){
        val mImmersionBar = ImmersionBar.with(this)
        mImmersionBar.transparentBar()
            .fullScreen(true)
            .navigationBarColor(R.color.white)
            .statusBarDarkFont(true)
            .init()
    }
}