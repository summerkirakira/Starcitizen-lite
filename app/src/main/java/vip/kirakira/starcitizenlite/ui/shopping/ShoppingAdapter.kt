package vip.kirakira.viewpagertest.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.databinding.ShoppingCatalogListItemBinding

class ShoppingAdapter(private val onClickListener: OnClickListener) : ListAdapter<ShopItem, ShoppingAdapter.ViewHolder>(DiffCallback) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.clickListener(item)
        }
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ShoppingCatalogListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(shopItem: ShopItem) {
            binding.property = shopItem
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ShoppingCatalogListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<ShopItem>() {
        override fun areItemsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean {
            return oldItem.id == newItem.id
        }

    }

    class OnClickListener(val clickListener: (shopItem: ShopItem) -> Unit) {
        fun onClick(shopItem: ShopItem) = clickListener(shopItem)
    }

}