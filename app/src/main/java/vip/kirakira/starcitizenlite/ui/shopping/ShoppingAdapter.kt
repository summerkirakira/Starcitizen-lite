package vip.kirakira.viewpagertest.ui.shopping

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.databinding.ShoppingCatalogListItemBinding
import vip.kirakira.viewpagertest.repositories.WARBOND_SHIP_IDS
import kotlin.coroutines.coroutineContext

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
            if (shopItem.id - 100000 in WARBOND_SHIP_IDS) {
                binding.isShopItemWarbond.visibility = android.view.View.VISIBLE
            } else {
                binding.isShopItemWarbond.visibility = android.view.View.GONE
            }
            if (System.currentTimeMillis() - shopItem.insert_time > 60 * 60 * 1000 * 24) {
                binding.canBuy.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        binding.root.context.resources,
                        R.drawable.ic_shop_no_data,
                        null
                    )
                )
                binding.canBuy.setColorFilter(binding.root.context.resources.getColor(R.color.shop_item_no_data))
            } else {
                binding.canBuy.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        binding.root.context.resources,
                        R.drawable.ic_double_check,
                        null
                    )
                )
                binding.canBuy.setColorFilter(binding.root.context.resources.getColor(R.color.shop_item_can_buy))
            }
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