package vip.kirakira.viewpagertest.ui.shopping

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.ShoppingFragmentBinding

class ShoppingFragment : Fragment() {

    companion object {
        fun newInstance() = ShoppingFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: ShoppingViewModel by activityViewModels()
        val binding: ShoppingFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.shopping_fragment, container, false)
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

        binding.lifecycleOwner = this

        return binding.root
    }

}