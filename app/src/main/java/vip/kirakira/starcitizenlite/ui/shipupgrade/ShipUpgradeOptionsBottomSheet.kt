package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.UpgradeBottomSheetOptionsBinding
import java.util.prefs.Preferences

class ShipUpgradeOptionsBottomSheet: RoundedCornerBottomSheet() {

    private lateinit var binding: UpgradeBottomSheetOptionsBinding
    private lateinit var preferences: SharedPreferences
    private var mCallbacks: Callbacks? = null
    companion object {

        fun showDialog(fragmentManager: FragmentManager, callbacks: Callbacks) {
            val dialog = ShipUpgradeOptionsBottomSheet()
            dialog.setCallback(callbacks)
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }
    }

    interface Callbacks {
        fun onAttributesChanged(attributes: UpgradeOptions)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UpgradeBottomSheetOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        setOptions()
    }

    private fun setOptions() {
        val fromShipId = preferences.getInt("upgrade_search_from_ship_id", 1)
        val toShipId = preferences.getInt("upgrade_search_to_ship_id", 37)
        val bannedList: List<Int> = preferences.getString("upgrade_search_banned_list", "")!!.split(",").mapNotNull {
            if (it == "") {
                null
            } else {
                it.toInt()
            }
        }
        val useHistoryCcu = preferences.getBoolean("upgrade_search_use_history_ccu", false)
        val onlyCanBuyShips = preferences.getBoolean("upgrade_search_only_can_buy_ships", true)
        val upgradeMultiplier = preferences.getFloat("upgrade_search_upgrade_multiplier", 1.5f) // A float value ranging form 1 to 20
        val useBuyback = preferences.getBoolean("upgrade_search_use_buyback", false)

        binding.upgradeOption = UpgradeOptions(
            fromShipId=fromShipId,
            toShipId=toShipId,
            useHistoryCcu=useHistoryCcu,
            onlyCanBuyShips=onlyCanBuyShips,
            upgradeMultiplier=upgradeMultiplier,
            useBuyBack=useBuyback,
            bannedList=bannedList
        )

        binding.imageCloseBtn.setOnClickListener {
            dismiss()
        }

    }

    private fun setCallback(callbacks: Callbacks) {
        mCallbacks = callbacks
    }


}