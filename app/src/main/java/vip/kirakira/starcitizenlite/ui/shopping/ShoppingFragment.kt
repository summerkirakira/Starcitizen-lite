package vip.kirakira.viewpagertest.ui.shopping

import android.animation.ObjectAnimator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.ShoppingFragmentBinding
import java.util.*
import kotlin.concurrent.schedule

class ShoppingFragment : Fragment() {

    companion object {
        fun newInstance() = ShoppingFragment()
    }

    lateinit var binding: ShoppingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: ShoppingViewModel by activityViewModels()
        binding = DataBindingUtil.inflate(inflater, R.layout.shopping_fragment, container, false)
        val adapter = ShoppingAdapter(
            ShoppingAdapter.OnClickListener {
                    item -> viewModel.popUpItemDetail(item)
            }
        )
        binding.catalogRecyclerView.adapter = adapter
        binding.catalogRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        viewModel.catalog.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            if(!it) {
                binding.catalogRecyclerView.scrollToPosition(0)
            }
        }

        binding.lifecycleOwner = this

        viewModel.popUpItem.observe(this, androidx.lifecycle.Observer {
            binding.textViewShopItemDetailTitle.text = it.name
            binding.textViewShopItemDetailSubtitle.text = it.subtitle
            binding.textViewShopItemDetailDescription.text = it.excerpt
            if(binding.popupLayout.visibility == View.INVISIBLE) {
                binding.popupLayout.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(binding.fab, "rotation", 0f, 360f).apply {
                    duration = 600
                    start()
                }
            } else {
                binding.popupLayout.visibility = View.INVISIBLE
                binding.fab.visibility = View.INVISIBLE
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCatalog()
        }

        return binding.root
    }

}