package vip.kirakira.viewpagertest.ui.shopping

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.ShoppingFragmentBinding

class ShoppingFragment : Fragment() {

    companion object {
        fun newInstance() = ShoppingFragment()
    }

    private lateinit var viewModel: ShoppingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: ShoppingFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.shopping_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ShoppingViewModel::class.java)
        val adapter = ShoppingAdapter(
            ShoppingAdapter.OnClickListener {
                    item -> Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
            }
        )
        binding.catalogRecyclerView.adapter = adapter
        binding.catalogRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        viewModel.catalog.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }



}