package vip.kirakira.starcitizenlite.ui.home

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
//        viewModel.hangerItems.observe(viewLifecycleOwner) {
//            adapter.submitList(it.toItemPropertyList())
//        }
        viewModel.buybackItems.observe(viewLifecycleOwner) {
            adapter.submitList(it.toItemProperty())
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        return binding.root
    }

}