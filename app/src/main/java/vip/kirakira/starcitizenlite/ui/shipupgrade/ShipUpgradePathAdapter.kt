package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.UpgradeSearchListItemBinding

class ShipUpgradePathAdapter(private val onDeleteBtnClickListener: OnClickListener): ListAdapter<UpgradeItemProperty, ShipUpgradePathAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder private constructor(val binding: UpgradeSearchListItemBinding, viewType: Int) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UpgradeItemProperty) {
            binding.itemProperty = item
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup, viewType: Int): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = UpgradeSearchListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, viewType = viewType)
            }
        }
        init {
            binding.timeline.initLine(viewType)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewType = viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item.isAvailable && item.origin != UpgradeItemProperty.OriginType.HANGAR) {
            holder.binding.isAvailableTag.text = "可购买"
            holder.binding.isAvailableTag.visibility = View.VISIBLE
        } else {
            holder.binding.isAvailableTag.visibility = View.GONE
        }
        if (item.isWarbond) {
            holder.binding.isWarbondTag.text = "战争债券"
            holder.binding.isWarbondTag.visibility = View.VISIBLE
        } else {
            holder.binding.isWarbondTag.visibility = View.GONE
        }
        when (item.origin) {
            UpgradeItemProperty.OriginType.HANGAR -> {
                holder.binding.originFromTag.text = "机库中"
                holder.binding.originFromTag.setColor(Color.parseColor("#4CAF50"))
            }
            UpgradeItemProperty.OriginType.NORMAL -> {
                holder.binding.originFromTag.text = "基础升级"
                holder.binding.originFromTag.setColor(Color.parseColor("#673AB7"))
            }
            UpgradeItemProperty.OriginType.HISTORY -> {
                holder.binding.originFromTag.text = "历史升级"
                holder.binding.originFromTag.setColor(Color.parseColor("#F44336"))
            }
            UpgradeItemProperty.OriginType.BUYBACK -> {
                holder.binding.originFromTag.text = "回购中"
                holder.binding.originFromTag.setColor(Color.parseColor("#2196F3"))
            }
            UpgradeItemProperty.OriginType.NOT_AVAILABLE -> {

            }
        }
        if (item.isAvailable) {
            holder.binding.timeline.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.baseline_radio_button_checked_24)
        } else {
            holder.binding.timeline.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.baseline_highlight_off_24)
        }
        holder.binding.deleteUpgradeBtn.setOnClickListener {
            onDeleteBtnClickListener.onClick(item)
        }
        return holder.bind(item = item)
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    class OnClickListener(val clickListener: (hangerListProperty: UpgradeItemProperty) -> Unit) {
        fun onClick(upgradeItemProperty: UpgradeItemProperty) = clickListener(upgradeItemProperty)
    }


    companion object DiffCallback : DiffUtil.ItemCallback<UpgradeItemProperty>() {
        override fun areItemsTheSame(oldItem: UpgradeItemProperty, newItem: UpgradeItemProperty): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: UpgradeItemProperty, newItem: UpgradeItemProperty): Boolean {
            return oldItem == newItem
        }

    }
}