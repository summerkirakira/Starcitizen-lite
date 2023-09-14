package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.databinding.FragmentShipUpgradeCartBinding
import vip.kirakira.starcitizenlite.ui.home.Parser

class ShipUpgradeCart : Fragment() {

    companion object {
        fun newInstance() = ShipUpgradeCart()
    }

    private lateinit var viewModel: ShipUpgradeCartViewModel
    private lateinit var binding: FragmentShipUpgradeCartBinding
    private lateinit var shipUpgradePathAdapter: ShipUpgradePathAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ship_upgrade_cart, container, false)
        shipUpgradePathAdapter = ShipUpgradePathAdapter()
        binding.shipUpgradePathRecyclerview.adapter = shipUpgradePathAdapter
        binding.shipUpgradePathRecyclerview.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
//        shipUpgradePathAdapter.submitList(shipUpgradePathAdapter.testUpgradeList)
//        shipUpgradePathAdapter.notifyDataSetChanged()
        binding.upgradeSettingsBtn.setOnClickListener {
            viewModel.fetchShipUpgradePath()
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShipUpgradeCartViewModel::class.java)
        viewModel.shipUpgradePathList.observe(viewLifecycleOwner) {
            shipUpgradePathAdapter.submitList(it)
            shipUpgradePathAdapter.notifyDataSetChanged()
            var creditPrice = 0
            var cashPrice = 0
            var totalPrice = 0
            for (item in it) {
                when (item.origin) {
                    UpgradeItemProperty.OriginType.NORMAL -> {
                        creditPrice += item.price
                        totalPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.HANGAR -> {
                        totalPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.HISTORY -> {
                        cashPrice += item.price
                        totalPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.BUYBACK -> {
                        cashPrice += item.price
                        totalPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.NOT_AVAILABLE -> {
                        TODO()
                    }
                }
            }
            binding.totalCreditCostTitle.text = "${"$"}${Parser.priceFormatter(creditPrice)}"
            binding.totalCashCost.text = "${"$"}${Parser.priceFormatter(cashPrice)}"
            binding.totalCost.text = "${"$"}${Parser.priceFormatter(totalPrice)}"
        }
    }

}