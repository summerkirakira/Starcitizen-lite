package vip.kirakira.starcitizenlite.ui.shipUpgradeSearch

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.ui.hangarlog.HangarLogBottomSheet
import vip.kirakira.starcitizenlite.ui.hangarlog.HangarLogViewModel

class ShipUpgradeSearchBottomSheet: RoundedCornerBottomSheet() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var shipUpgradeSearchViewModel: ShipUpgradeSearchViewModel
    private lateinit var closeButton: AppCompatImageView

    companion object {

        fun showDialog(fragmentManager: FragmentManager) {
            val dialog = ShipUpgradeSearchBottomSheet()
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        shipUpgradeSearchViewModel = ViewModelProvider(this).get(ShipUpgradeSearchViewModel::class.java)
        val contextThemeWrapper = ContextThemeWrapper(activity, theme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.bottom_sheet_ship_upgrade_search, container, false)
    }
}