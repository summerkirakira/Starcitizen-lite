package vip.kirakira.starcitizenlite.ui.shipUpgradeSearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.HangarLog
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradeResponse
import vip.kirakira.starcitizenlite.ui.hangarlog.TimelineAdapter

class ShipUpgradePathAdapter(private val shipUpgradePath: ShipUpgradeResponse.ShipUpgradePath): RecyclerView.Adapter<ShipUpgradePathAdapter.ShipUpgradeViewHolder> {
    private lateinit var mLayoutInflater: LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipUpgradePathAdapter.ShipUpgradeViewHolder {
        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = mLayoutInflater.inflate(R.layout.ship_upgrade_step_timeline, parent, false)
        return ShipUpgradeViewHolder(view, viewType)
    }

    override fun getItemCount(): Int {
        return shipUpgradePath.steps.size
    }

    override fun onBindViewHolder(holder: ShipUpgradeViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    inner class ShipUpgradeViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val timeline: TimelineView = itemView.findViewById(R.id.timeline)
        private val stepTitle: TextView = itemView.findViewById(R.id.text_upgrade_step_title)
        private val stepPrice: TextView = itemView.findViewById(R.id.upgrade_step_price)
        private val upgradeStepEnglishTitle: TextView = itemView.findViewById(R.id.upgrade_step_english_title)
        private val upgradeStepCloseButton: ImageView = itemView.findViewById(R.id.upgrade_step_close)
        init {
            timeline.initLine(viewType)
        }
    }
}