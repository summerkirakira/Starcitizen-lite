package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.createWarningAlerter
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
    private lateinit var bannedUpgradeList: MutableList<BannedUpgrade>
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ship_upgrade_cart, container, false)
        shipUpgradePathAdapter = ShipUpgradePathAdapter(ShipUpgradePathAdapter.OnClickListener {
            if (it.id == 0) {
                createWarningAlerter(requireActivity(), "无法删除基础CCU哦", "删除后会导致CCU链断裂").show()
                return@OnClickListener
            }
            val builder = QMUIDialog.MessageDialogBuilder(requireContext())
            builder.setTitle("屏蔽升级包")
                .setMessage("确定要将升级: ${it.fromShipName} -> ${it.toShipName} (${"$"}${it.originalPrice}) 加入黑名单吗？之后可以在规划器菜单中手动移出黑名单哦")
                .addAction("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .addAction("确定") { dialog, _ ->
                    addBannedUpgrade(BannedUpgrade(it.id, it.origin, "${it.fromShipName} -> ${it.toShipName} (${"$"}${Parser.priceFormatter(it.originalPrice)})"))
                    viewModel.fetchShipUpgradePath()
                    dialog.dismiss()
                }
                .show()
        })
        binding.shipUpgradePathRecyclerview.adapter = shipUpgradePathAdapter
        binding.shipUpgradePathRecyclerview.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
//        shipUpgradePathAdapter.submitList(shipUpgradePathAdapter.testUpgradeList)
//        shipUpgradePathAdapter.notifyDataSetChanged()
        binding.upgradeSettingsBtn.setOnClickListener {
//            viewModel.fetchShipUpgradePath()
            showSettingsBottomSheet()
        }
        showSettingsBottomSheet()
        bannedUpgradeList = mutableListOf()

        preferences = requireActivity().getSharedPreferences("vip.kirakira.starcitizenlite.kirakira",
            android.content.Context.MODE_PRIVATE
        )

        val bannedUpgradeString = preferences.getString("upgrade_search_banned_list", "")
        if (bannedUpgradeString != "") {
            bannedUpgradeString!!.split(",").map {
                bannedUpgradeList.add(ShipUpgradeCartViewModel.convertStringToBannedUpgrade(it))
            }
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
                binding.shipFromCheckSuccess.visibility = View.VISIBLE
                binding.shipFromCheckFailed.visibility = View.INVISIBLE
            } else {
                otherPrice += viewModel.fromShipAlias.getHighestSku()
                creditPrice += viewModel.fromShipAlias.getHighestSku()
                totalPrice += viewModel.fromShipAlias.getHighestSku()
                binding.shipFromCheckSuccess.visibility = View.INVISIBLE
                binding.shipFromCheckFailed.visibility = View.VISIBLE
            }

            binding.totalCreditCostTitle.text = "${"$"}${Parser.priceFormatter(creditPrice)}"
            binding.totalCashCost.text = "${"$"}${Parser.priceFormatter(cashPrice)}"
            binding.totalCost.text = "${"$"}${Parser.priceFormatter(totalPrice)}"
            binding.textviewHangarUpgradeCost.text = "${"$"}${Parser.priceFormatter(hangarPrice)}"
            binding.textviewOtherUpgradeCost.text = "${"$"}${Parser.priceFormatter(otherPrice)}"
            binding.textviewOriginalCost.text = "${"$"}${Parser.priceFormatter(viewModel.toShipAlias.getHighestSku() - viewModel.fromShipAlias.getHighestSku())}"
            binding.textviewFromShip.text = viewModel.fromShipAlias.chineseName
            binding.textviewToShip.text = viewModel.toShipAlias.chineseName
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

    private fun updateBannedUpgradeList() {
        bannedUpgradeList.clear()
        val bannedUpgradeString = preferences.getString("upgrade_search_banned_list", "")
        if (bannedUpgradeString != "") {
            bannedUpgradeString!!.split(",").map {
                bannedUpgradeList.add(ShipUpgradeCartViewModel.convertStringToBannedUpgrade(it))
            }
        }
    }

    private fun addBannedUpgrade(upgrade: BannedUpgrade) {
        updateBannedUpgradeList()
        for (item in bannedUpgradeList) {
            if (item.id == upgrade.id && item.type == upgrade.type) {
                return
            }
        }
        bannedUpgradeList.add(upgrade)
        val bannedUpgradeString = bannedUpgradeList.joinToString(",") {
            when (it.type) {
                UpgradeItemProperty.OriginType.NORMAL -> {
                    "${it.id}#1#${it.name}"
                }
                UpgradeItemProperty.OriginType.HANGAR -> {
                    "${it.id}#3#${it.name}"
                }
                UpgradeItemProperty.OriginType.HISTORY -> {
                    "${it.id}#2#${it.name}"
                }
                UpgradeItemProperty.OriginType.BUYBACK -> {
                    "${it.id}#4#${it.name}"
                }
                UpgradeItemProperty.OriginType.NOT_AVAILABLE -> {
                    TODO()
                }
            }
        }
        preferences.edit().putString("upgrade_search_banned_list", bannedUpgradeString).apply()
    }

}