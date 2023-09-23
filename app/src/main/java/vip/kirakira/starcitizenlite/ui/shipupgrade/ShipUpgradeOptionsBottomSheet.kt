package vip.kirakira.starcitizenlite.ui.shipupgrade

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.databinding.UpgradeBottomSheetOptionsBinding
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.shipAlias
import java.util.prefs.Preferences

class ShipUpgradeOptionsBottomSheet: RoundedCornerBottomSheet() {

    private lateinit var binding: UpgradeBottomSheetOptionsBinding
    private lateinit var preferences: SharedPreferences
    private var mCallbacks: Callbacks? = null
    private lateinit var fromShipAliasList: List<ShipAlias>
    private lateinit var toShipAliasList: List<ShipAlias>
    private var fromShipAlias: ShipAlias? = null
    private var toShipAlias: ShipAlias? = null


    companion object {

        fun showDialog(fragmentManager: FragmentManager, callbacks: Callbacks) {
            val dialog = ShipUpgradeOptionsBottomSheet()
            dialog.setCallback(callbacks)
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }
    }

    interface Callbacks {
        fun onApplyButtonClicked()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UpgradeBottomSheetOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = requireActivity().getSharedPreferences("vip.kirakira.starcitizenlite.kirakira",
        Context.MODE_PRIVATE
        )
        setOptions()
        fromShipAliasList = shipAlias.filter { it.getHighestSku() != 0 }.sortedBy { it.getHighestSku() }
        toShipAliasList = shipAlias.filter { it.getHighestSku() != 0 }.sortedBy { it.getHighestSku() }
    }

    private fun setOptions() {
        val fromShipId = preferences.getInt("upgrade_search_from_ship_id", 1)
        val toShipId = preferences.getInt("upgrade_search_to_ship_id", 37)
        val useHistoryCcu = preferences.getBoolean("upgrade_search_use_history_ccu", true)
        val onlyCanBuyShips = preferences.getBoolean("upgrade_search_only_can_buy_ships", true)
        val upgradeMultiplier = preferences.getFloat("upgrade_search_upgrade_multiplier", 1.5f) // A float value ranging form 1 to 20
        val useBuyback = preferences.getBoolean("upgrade_search_use_buyback", true)
        val useHangar = preferences.getBoolean("upgrade_search_use_hangar", true)

        binding.upgradeOption = UpgradeOptions(
            useHistoryCcu=useHistoryCcu,
            onlyCanBuyShips=onlyCanBuyShips,
            upgradeMultiplier=upgradeMultiplier,
            useBuyBack=useBuyback,
            bannedList= listOf(),
            useHangarCcu = useHangar
        )

        binding.imageCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.selectFromShipBtn.setOnClickListener {
            val builder = QMUIBottomSheet.BottomListSheetBuilder(context)
            for (ship in fromShipAliasList) builder.addItem(ship.chineseName)

            builder.setOnSheetItemClickListener { dialog, _, position, _ ->
                fromShipAlias = fromShipAliasList[position]
                toShipAliasList = shipAlias.filter {
                    it.getHighestSku() > fromShipAlias!!.getHighestSku() && it.getHighestSku() != 0
                }.sortedBy { it.getHighestSku() }
                binding.textviewFromShip.text = fromShipAlias!!.chineseName
                dialog.dismiss()
            }.build().show()
        }

        binding.selectBannedCcuBtn.setOnClickListener {
            val bannedCcuList = getBannedUpgradeList()
            if (bannedCcuList.size == 0) {
                createWarningAlerter(requireActivity(), "没有屏蔽任何升级哦", "在规划器中点击升级包右上方的删除按钮可以屏蔽升级包~").show()
                return@setOnClickListener
            }
            val builder = QMUIBottomSheet.BottomListSheetBuilder(context, false)
            for (upgrade in bannedCcuList) {
                val itemName = when(upgrade.type) {
                    UpgradeItemProperty.OriginType.NORMAL -> {
                        upgrade.name
                    }
                    UpgradeItemProperty.OriginType.HANGAR -> {
                        "[机库中] ${upgrade.name}"
                    }
                    UpgradeItemProperty.OriginType.HISTORY -> {
                        "[历史升级] ${upgrade.name}"
                    }
                    UpgradeItemProperty.OriginType.BUYBACK -> {
                        "[回购中] ${upgrade.name}"
                    }
                    UpgradeItemProperty.OriginType.NOT_AVAILABLE -> {
                        TODO()
                    }
                }
                builder.addItem(itemName)
            }
            builder.setOnSheetItemClickListener { dialog, _, position, _ ->
                removeBannedUpgrade(position)
                Toast.makeText(context, "已将${bannedCcuList.get(position).name}移出CCU链黑名单哦~", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
                .setAddCancelBtn(true)
                .addContentHeaderView(
                    LayoutInflater.from(context).inflate(
                        R.layout.remove_banned_ccu_bottomsheet_header,
                        null
                    )
                )
                .build().show()
        }

        binding.selectToShipBtn.setOnClickListener {
            val builder = QMUIBottomSheet.BottomListSheetBuilder(context)
            for (ship in toShipAliasList) builder.addItem(ship.chineseName)

            builder.setOnSheetItemClickListener { dialog, _, position, _ ->
                toShipAlias = toShipAliasList[position]
                fromShipAliasList = shipAlias.filter {
                    it.getHighestSku() < toShipAlias!!.getHighestSku() && it.getHighestSku() != 0
                }.sortedBy { it.getHighestSku() }
                binding.textviewToShip.text = toShipAlias!!.chineseName
                dialog.dismiss()
            }.build().show()
        }
        fromShipAlias = getShipAliasById(fromShipId)
        toShipAlias = getShipAliasById(toShipId)
        if (fromShipAlias == null || toShipAlias == null) {
            createWarningAlerter(requireActivity(), "数据加载错误", "请回到主界面等待避难所加载数据哦~").show()
            return
        }
        binding.textviewFromShip.text = fromShipAlias!!.chineseName
        binding.textviewToShip.text = toShipAlias!!.chineseName

        binding.buttonApply.setOnClickListener {
            if (fromShipAlias!!.getHighestSku() >= toShipAlias!!.getHighestSku()) {
                createWarningAlerter(requireActivity(), "设置错误", "待升级舰船的价格要小于目标舰船哦~").show()
                return@setOnClickListener
            }
            preferences.edit().apply {
                putInt("upgrade_search_to_ship_id", toShipAlias!!.id)
                putInt("upgrade_search_from_ship_id", fromShipAlias!!.id)
                putBoolean("upgrade_search_use_history_ccu", binding.checkboxUseHistoryCcu.isChecked)
                putBoolean("upgrade_search_only_can_buy_ships", binding.checkboxOnlyCanBuyShips.isChecked)
                putBoolean("upgrade_search_use_buyback", binding.checkboxUseBuybackUpgrade.isChecked)
                putBoolean("upgrade_search_use_hangar", binding.checkboxUseHangarUpgrade.isChecked)
                putFloat("upgrade_search_upgrade_multiplier", convertBarValueToMultiplier(binding.seekUpgradeMultiplier.progress))
                commit()
            }
            mCallbacks?.onApplyButtonClicked()
            dismiss()
        }

        binding.seekUpgradeMultiplier.progress = convertMultiplierToBarValue(upgradeMultiplier)


    }

    private fun setCallback(callbacks: Callbacks) {
        mCallbacks = callbacks
    }

    private fun convertBarValueToMultiplier(value: Int): Float {
        return -0.04f * value.toFloat() + 5.0f
    }

    private fun convertMultiplierToBarValue(value: Float): Int {
        return (125f - 25 * value).toInt()
    }

    private fun getShipAliasById(id: Int): ShipAlias? {
        for (ship in shipAlias) {
            if (ship.id == id) return ship
        }
        return null
    }

    private fun getBannedUpgradeList(): MutableList<BannedUpgrade> {
        val bannedUpgradeList = mutableListOf<BannedUpgrade>()
        val bannedUpgradeString = preferences.getString("upgrade_search_banned_list", "")
        if (bannedUpgradeString == "") return bannedUpgradeList

        bannedUpgradeString!!.split(",").map {
            bannedUpgradeList.add(ShipUpgradeCartViewModel.convertStringToBannedUpgrade(it))
        }
        return bannedUpgradeList
    }

    private fun removeBannedUpgrade(index: Int) {
        val bannedUpgradeList = getBannedUpgradeList()
        bannedUpgradeList.removeAt(index)
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
        preferences.edit().putString("upgrade_search_banned_list", bannedUpgradeString).commit()
    }


}