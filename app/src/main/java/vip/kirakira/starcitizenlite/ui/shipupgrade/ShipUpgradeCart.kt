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
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip

class  ShipUpgradeCart : Fragment() {

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
//            viewModel.fetchShipUpgradePath()
            showSettingsBottomSheet()
        }
        showSettingsBottomSheet()
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
            var hangarPrice = 0
            var otherPrice = 0
            for (item in it) {
                when (item.origin) {
                    UpgradeItemProperty.OriginType.NORMAL -> {
                        creditPrice += item.price
                        totalPrice += item.price
                        otherPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.HANGAR -> {
                        totalPrice += item.price
                        hangarPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.HISTORY -> {
                        cashPrice += item.price
                        totalPrice += item.price
                        otherPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.BUYBACK -> {
                        cashPrice += item.price
                        totalPrice += item.price
                        otherPrice += item.price
                    }
                    UpgradeItemProperty.OriginType.NOT_AVAILABLE -> {
                        TODO()
                    }
                }
            }
            if (viewModel.isFromShipInHangar) {
                hangarPrice += viewModel.fromShipAlias.getHighestSku()
                binding.textviewIsContainFromShip.text = "机库中存在起始舰船"
            } else {
                otherPrice += viewModel.fromShipAlias.getHighestSku()
                creditPrice += viewModel.fromShipAlias.getHighestSku()
                totalPrice += viewModel.fromShipAlias.getHighestSku()
                binding.textviewIsContainFromShip.text = "机库中不存在起始舰船"
            }

            binding.totalCreditCostTitle.text = "${"$"}${Parser.priceFormatter(creditPrice)}"
            binding.totalCashCost.text = "${"$"}${Parser.priceFormatter(cashPrice)}"
            binding.totalCost.text = "${"$"}${Parser.priceFormatter(totalPrice)}"
            binding.textviewHangarUpgradeCost.text = "${"$"}${Parser.priceFormatter(hangarPrice)}"
            binding.textviewOtherUpgradeCost.text = "${"$"}${Parser.priceFormatter(otherPrice)}"
            binding.summaryLayout.visibility = View.VISIBLE
        }
        viewModel.warningMessage.observe(viewLifecycleOwner) {
            showWarningLog(it)
        }
    }
    private fun showSettingsBottomSheet() {
        ShipUpgradeOptionsBottomSheet.showDialog(parentFragmentManager, object: ShipUpgradeOptionsBottomSheet.Callbacks {
            override fun onApplyButtonClicked() {
                viewModel.fetchShipUpgradePath()
            }
        })
    }

    private fun showWarningLog(message: WarningMessage) {
        if (message.type == 2) {
            RefugeVip.createWarningAlert(requireActivity(), title = message.message)
        } else if (message.type == 1) {
            RefugeVip.createWarningAlert(requireActivity(), title = message.message)
        }
    }

}