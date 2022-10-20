package vip.kirakira.starcitizenlite.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import vip.kirakira.starcitizenlite.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

    }
}