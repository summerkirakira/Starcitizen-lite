package vip.kirakira.starcitizenlite.ui.home

import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.databinding.HomeFragmentBinding
import vip.kirakira.starcitizenlite.ui.loadImage


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding: HomeFragmentBinding

    private lateinit var viewModel: HomeViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: HomeViewModel by activityViewModels()
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this

        binding.contentContainer.setOnClickListener(View.OnClickListener {
            binding.popupLayout.visibility = View.GONE
        })

        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.swipeRefreshLayout.visibility = View.GONE
                binding.errorBox.setTitleText(getString(R.string.not_login_error_box_title))
                binding.errorBox.setDetailText(getString(R.string.not_login_error_box_detail))
                binding.errorBox.setButton(
                    getString(R.string.click_to_login)
                ) {
                    val intent = Intent(context, WebLoginActivity::class.java)
                    startActivity(intent)
                }
                binding.errorBox.show()
            } else {
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.errorBox.hide()
            }
        }

        val adapter = HangerViewAdapter(
            HangerViewAdapter.OnClickListener {
                if(binding.popupLayout.visibility == View.VISIBLE) {
                    binding.popupLayout.visibility = View.GONE
                    return@OnClickListener
                }
                binding.itemsLinearLayout.removeAllViews()
                if(it.items.isNotEmpty()){
                    binding.itemsLinearLayout.visibility = View.VISIBLE
                    binding.itemsTitle.visibility = View.VISIBLE
                } else{
                    binding.itemsLinearLayout.visibility = View.GONE
                    binding.itemsTitle.visibility = View.GONE
                }
                for(item in it.items) {
                    val deatail_item = inflater.inflate(R.layout.hanger_detail_item, binding.itemsLinearLayout, false)
                    val itemImage = deatail_item.findViewById<ImageView>(R.id.detail_imageview)
                    val itemName = deatail_item.findViewById<TextView>(R.id.detail_title)
                    val itemKind = deatail_item.findViewById<TextView>(R.id.detail_kind)
                    val itemSubtitle = deatail_item.findViewById<TextView>(R.id.detail_subtitle)
                    loadImage(itemImage, item.image)
                    itemName.text = item.title
                    itemKind.text = item.kind
                    itemSubtitle.text = item.subtitle
                    binding.itemsLinearLayout.addView(deatail_item)
                }
                binding.alsoContainsLinearLayout.removeAllViews()
                if(it.alsoContains.isNotEmpty()){
                    binding.alsoContainsLinearLayout.visibility = View.VISIBLE
                    binding.alsoContainsTitle.visibility = View.VISIBLE
                } else{
                    binding.alsoContainsLinearLayout.visibility = View.GONE
                    binding.alsoContainsTitle.visibility = View.GONE
                }
                for(alsoContains in it.alsoContains.split("#")) {
                    val alsoContainsView = TextView(context)
                    alsoContainsView.text = alsoContains
                    alsoContainsView.textSize = 16f
                    alsoContainsView.setPadding(15, 4, 0, 4)
                    binding.alsoContainsLinearLayout.addView(alsoContainsView)
                }
                binding.popupLayout.visibility = View.VISIBLE
            }
        )

        binding.hangerRecyclerView.adapter = adapter
        binding.hangerRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        viewModel.hangerItems.observe(viewLifecycleOwner) {
            if (viewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                adapter.submitList(it.toItemPropertyList())
            } else if (viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                adapter.submitList(viewModel.buybackItems.value?.toItemProperty())
            }
        }

        viewModel.buybackItems.observe(viewLifecycleOwner) {
            if (viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                adapter.submitList(it.toItemProperty())

            } else if (viewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                adapter.submitList(viewModel.hangerItems.value?.toItemPropertyList())
            }
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it || viewModel.isBuybackRefreshing.value!!
        }

        viewModel.isBuybackRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it || viewModel.isRefreshing.value!!
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if(viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                viewModel.refreshBuybackItems()
            } else {
                viewModel.refresh()
            }
        }


        return binding.root
    }

}