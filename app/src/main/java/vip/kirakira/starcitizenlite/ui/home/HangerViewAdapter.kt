package vip.kirakira.starcitizenlite.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.HangerListItemBinding

class HangerViewAdapter(private val onClickListener: HangerViewAdapter.OnClickListener, private val onTagClickListener: OnTagClickListener): ListAdapter<HangerItemProperty, HangerViewAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HangerViewAdapter.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HangerViewAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)
        }
        holder.itemView.findViewById<QMUIRoundButton>(R.id.can_gift).setOnClickListener {
            onTagClickListener.onClick("can_gift", item)
        }
        holder.itemView.findViewById<QMUIRoundButton>(R.id.can_reclaim).setOnClickListener {
            onTagClickListener.onClick("can_reclaim", item)
        }
        holder.itemView.findViewById<QMUIRoundButton>(R.id.hanger_item_status).setOnClickListener {
            onTagClickListener.onClick("status", item)
        }
        holder.itemView.findViewById<QMUIRoundButton>(R.id.can_upgrade).setOnClickListener {
            onTagClickListener.onClick("can_upgrade", item)
        }
        holder.bind(item)
    }


    class ViewHolder private constructor(val binding: HangerListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HangerItemProperty) {
            if(item.price == -1){
                binding.dollarIcon.visibility = View.GONE
                binding.priceText.visibility = View.GONE
            } else {
                binding.dollarIcon.visibility = View.VISIBLE
                binding.priceText.visibility = View.VISIBLE
            }
            binding.hangerItemProperty = item
            binding.canUpgrade.visibility = View.GONE
            binding.canReclaim.visibility = View.GONE
            binding.canGift.visibility = View.GONE

            for(tag in item.tags) {
                if(tag.name == "可赠送"){
                    binding.canGift.visibility = View.VISIBLE
                } else if(tag.name == "可融"){
                    binding.canReclaim.visibility = View.VISIBLE
                } else if(tag.name == "可升级"){
                    binding.canUpgrade.visibility = View.VISIBLE
                }
            }
            if(item.status == "已礼物"){
                binding.hangerItemStatus.setStrokeData(4, ColorStateList.valueOf(Color.parseColor("#F89BAC")))
                binding.hangerItemStatus.setTextColor(Color.parseColor("#F89BAC"))
            }
            if (item.insurance.isNotEmpty()){
                binding.insuranceItemButton.visibility = View.VISIBLE
            } else {
                binding.insuranceItemButton.visibility = View.GONE
            }
            if (item.name.contains("Warbond") || item.name.contains("战争债券")){
                binding.isWarbond.visibility = View.VISIBLE
            } else {
                binding.isWarbond.visibility = View.GONE
            }

            if (item.savingString != null && item.savingString != "-0%") {
                binding.savingButton.visibility = View.VISIBLE
                binding.priceText2.visibility = View.VISIBLE
                binding.dollarIcon2.visibility = View.VISIBLE
            } else {
                binding.savingButton.visibility = View.GONE
                binding.priceText2.visibility = View.GONE
                binding.dollarIcon2.visibility = View.GONE
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HangerListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }

            fun getTag(text: String, color: Int, binding: HangerListItemBinding): QMUIRoundButton {
                val tag = QMUIRoundButton(binding.root.context)
                tag.text = text
                tag.textSize = 10f
                tag.setTextColor(Color.BLACK)
//                var roundButtonDrawable = tag.background
//                roundButtonDrawable.setIs
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    tag.layoutParams = this
                }
                return tag
            }
        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<HangerItemProperty>() {
        override fun areItemsTheSame(oldItem: HangerItemProperty, newItem: HangerItemProperty): Boolean {

            return "${oldItem.id}#${oldItem.idList}#${oldItem.alsoContains}#${oldItem.status}#${oldItem.image}#${oldItem.price}" == "${newItem.id}#${newItem.idList}#${newItem.alsoContains}#${newItem.status}#${newItem.image}#${newItem.price}"
        }

        override fun areContentsTheSame(oldItem: HangerItemProperty, newItem: HangerItemProperty): Boolean {
            return oldItem.id == newItem.id
        }

    }

    class OnClickListener(val clickListener: (hangerListProperty: HangerItemProperty) -> Unit) {
        fun onClick(hangerListProperty: HangerItemProperty) = clickListener(hangerListProperty)
    }

    class OnTagClickListener(val clickListener: (tag: String, hangerListProperty: HangerItemProperty) -> Unit) {
        fun onClick(tag: String, hangerListProperty: HangerItemProperty) = clickListener(tag, hangerListProperty)
    }



}