package vip.kirakira.starcitizenlite.ui.shipupgrade

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.FragmentShipUpgradeCartBinding

class ShipUpgradeCart : Fragment() {

    companion object {
        fun newInstance() = ShipUpgradeCart()
    }

    private lateinit var viewModel: ShipUpgradeCartViewModel
    private lateinit var binding: FragmentShipUpgradeCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ship_upgrade_cart, container, false)
        val shipUpgradePathAdapter = ShipUpgradePathAdapter()
        binding.shipUpgradePathRecyclerview.adapter = shipUpgradePathAdapter
        binding.shipUpgradePathRecyclerview.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        shipUpgradePathAdapter.submitList(shipUpgradePathAdapter.testUpgradeList)
        shipUpgradePathAdapter.notifyDataSetChanged()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShipUpgradeCartViewModel::class.java)
        // TODO: Use the ViewModel
    }

}