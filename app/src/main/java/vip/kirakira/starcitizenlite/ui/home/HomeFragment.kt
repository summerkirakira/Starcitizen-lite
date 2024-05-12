package vip.kirakira.starcitizenlite.ui.home

import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nambimobile.widgets.efab.FabOption
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.*
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.RefugeApplication
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.activities.LoginActivity
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.createSuccessAlerter
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.databinding.HomeFragmentBinding
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.network.hanger.HangerService
import vip.kirakira.starcitizenlite.network.performAliPay
import vip.kirakira.starcitizenlite.network.shop.*
import vip.kirakira.starcitizenlite.ui.loadImage
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip
import vip.kirakira.viewpagertest.network.graphql.ApplyTokenBody
import vip.kirakira.viewpagertest.network.graphql.BuyBackPledgeBody
import vip.kirakira.viewpagertest.network.graphql.SetContextTokenBody
import vip.kirakira.viewpagertest.network.graphql.UpgradeAddToCartQuery


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding: HomeFragmentBinding

    private lateinit var viewModel: HomeViewModel

    private val scope = CoroutineScope(Job() + Dispatchers.Main)



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: HomeViewModel by activityViewModels()

        val theme: Resources.Theme = context!!.theme
        val textFillColor = TypedValue()
        theme.resolveAttribute(R.attr.textFillColor, textFillColor, true)

        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this
        binding.fabReset.setOnClickListener {
            onResetFabClick(viewModel)
        }
        binding.fabUpgrade.setOnClickListener {
            onUpgradeFabClick(viewModel)
        }
        binding.fabShip.setOnClickListener {
            onShipFabClick(viewModel)
        }

        binding.fabSubscription.setOnClickListener {
            onSubscribeFabClick(viewModel)
        }

        binding.fabPaint.setOnClickListener {
            onPaintFabClick(viewModel)
        }
        binding.fabTrash.setOnClickListener {
            onTrashFabClick(viewModel)
        }

//        binding.contentContainer.setOnClickListener(View.OnClickListener {
//            binding.popupLayout.visibility = View.GONE
//            viewModel.isDetailShowing.value = false
//        })

        binding.popupLayout.maxHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()

        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.swipeRefreshLayout.visibility = View.GONE
                binding.errorBox.setTitleText(getString(R.string.not_login_error_box_title))
                binding.errorBox.setDetailText(getString(R.string.not_login_error_box_detail))
                binding.errorBox.setButton(
                    getString(R.string.click_to_login)
                ) {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
                binding.errorBox.show()
            } else {
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.errorBox.hide()
            }
        }

        viewModel.isDetailShowing.observe(viewLifecycleOwner) {
            if (it) {
                binding.popupLayout.visibility = View.VISIBLE
            } else {
                binding.popupLayout.visibility = View.GONE
            }
        }


        val adapter = HangerViewAdapter(
            HangerViewAdapter.OnClickListener {
//                vibrate()
                if(binding.popupLayout.visibility == View.VISIBLE) {
                    binding.popupLayout.visibility = View.GONE
                    viewModel.isDetailShowing.value = false
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

                val hangerItem = it

                binding.contentDetail.setOnClickListener {
//                    if(!RefugeVip.isVip()) {
//                        RefugeVip.createWarningAlert(requireActivity())
//                        return@setOnClickListener
//                    }
                    HangarItemDetailBottomSheet.showDialog(
                        requireActivity().supportFragmentManager,
                        hangerItem
                    )
                }


                val pref = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val isTranslationEnabled = pref?.getBoolean(getString(R.string.enable_localization), false) ?: false
                for(item in it.items) {
                    val deatail_item = inflater.inflate(R.layout.hanger_detail_item, binding.itemsLinearLayout, false)
                    val itemImage = deatail_item.findViewById<ImageView>(R.id.detail_imageview)
                    val itemName = deatail_item.findViewById<TextView>(R.id.detail_title)
                    val itemKind = deatail_item.findViewById<TextView>(R.id.detail_kind)
                    val itemSubtitle = deatail_item.findViewById<TextView>(R.id.detail_subtitle)
                    loadImage(itemImage, item.image)
                    if (isTranslationEnabled) {
                        itemName.text = item.chineseTitle?:item.title
                        itemKind.text = item.chineseSubtitle?:item.kind
                    } else {
                        itemName.text = item.title
                        itemKind.text = item.kind
                    }
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
                val contains = it.chineseAlsoContains?:it.alsoContains
                for(alsoContains in contains.split("#")) {
                    val alsoContainsView = TextView(context)
                    alsoContainsView.text = alsoContains
                    alsoContainsView.textSize = 16f
                    alsoContainsView.setPadding(15, 4, 0, 4)
                    alsoContainsView.setTextColor(textFillColor.data)
                    binding.alsoContainsLinearLayout.addView(alsoContainsView)
                }
                // set max height

                binding.popupLayout.visibility = View.VISIBLE
                viewModel.isDetailShowing.value = true
            },
            HangerViewAdapter.OnTagClickListener {
                tag, item ->
                if(tag == "can_reclaim") {
                    val pref = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                    val isBannedReclaim = pref?.getBoolean(getString(R.string.banned_reclaim_key), false) ?: false
                    if (isBannedReclaim) {
                        createWarningAlerter(requireActivity(), getString(R.string.already_banned_reclaim), getString(R.string.please_switch_off_banned_reclaim)).show()
                        return@OnTagClickListener
                    }
                    QMUIDialog.MessageDialogBuilder(activity)
                        .setTitle(getString(R.string.reclaim_warning))
                        .setMessage("即将对${item.name}进行回收操作，结束后将会返还${item.price.toFloat() / 100f}信用点\n此操作不可逆！是否继续？")
                        .addAction("全部回收") { dialog, _ ->
                            dialog.dismiss()
//                            if (!RefugeVip.isVip()) {
//                                RefugeVip.createWarningAlert(requireActivity())
//                                return@addAction
//                            }
                            QMUIDialog.MessageDialogBuilder(activity)
                                .setTitle(getString(R.string.reclaim_warning))
                                .setMessage("确定要对${item.name}进行全部回收吗？\n此操作不可逆！！！")
                                .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                                .addAction(getString(R.string.accept)) {
                                    dialog, _ ->
                                    dialog.dismiss()
                                    scope.launch {
                                        for (id in item.idList.split(","))
                                            try {
                                                val message = HangerService().reclaimPledge(id, viewModel.currentUser.value!!.password)
                                                if (message.code == "OK") {
                                                    Toast.makeText(context, "已返还${item.price.toFloat() / 100f}信用点", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    createWarningAlerter(requireActivity(), getString(R.string.reclaim_failed), message.msg).show()
                                                    viewModel.refresh()
                                                    return@launch
                                                }
                                            } catch (e: Exception) {
                                                createWarningAlerter(requireActivity(), getString(R.string.reclaim_failed), getString(R.string.network_error)).show()
                                                viewModel.refresh()
                                                return@launch
                                            }
                                        viewModel.refresh()
                                    }
                                }.show()

                        }
                        .addAction(getString(R.string. cancel)) { dialog, _ -> dialog.dismiss() }
                        .addAction(0, getString(R.string.confirm)) { dialog, _ ->
                            dialog.dismiss()
                            val builder = QMUIDialog.EditTextDialogBuilder(activity)
                            builder
                                .setTitle(getString(R.string.reclaim_warning))
                                .setPlaceholder(getString(R.string.please_enter_reclaim_password))
                                .setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                                .addAction(getString(R.string.accept)) {
                                    dialog, _ ->
                                    val password = builder.editText.text.toString()
                                    if (viewModel.currentUser.value!!.password != password) {
                                        createWarningAlerter(requireActivity(), getString(R.string.reclaim_failed), getString(R.string.password_not_match)).show()
                                        return@addAction
                                    }
                                    dialog.dismiss()
                                    scope.launch {
                                        try {
                                            val message = HangerService().reclaimPledge(item.id.toString(), viewModel.currentUser.value!!.password)
                                            if (message.code == "OK") {
                                                Alerter.create(activity!!)
                                                    .setTitle(getString(R.string.reclaim_success))
                                                    .setText("已返还${item.price.toFloat() / 100f}信用点")
                                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                                    .show()
                                                viewModel.refresh()
                                            } else {
                                                Alerter.create(activity!!)
                                                    .setTitle(getString(R.string.reclaim_failed))
                                                    .setText(message.msg)
                                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                                    .show()
                                            }
                                        } catch (e: Exception) {
                                            Alerter.create(activity!!)
                                                .setTitle(getString(R.string.reclaim_failed))
                                                .setText(getString(R.string.network_error))
                                                .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                                .show()
                                        }

                                    }
                                }.show()
                        }
                        .show()

                }
                else if (tag == "can_gift"){
                    var builder = QMUIDialog.EditTextDialogBuilder(activity)
                    builder
                        .setTitle(getString(R.string.gifit_pledge))
                        .setPlaceholder(getString(R.string.please_input_email))
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                        .addAction(0, getString(R.string.confirm)) { dialog, index ->
                            dialog.dismiss()
                            scope.launch {
                                try {
                                    val message = HangerService().giftPledge(item.id.toString(), viewModel.currentUser.value!!.password, "Powered by Starcitizen lite", builder.getEditText().getText().toString())
                                    if (message.code == "OK") {
                                        Alerter.create(activity!!)
                                            .setTitle(getString(R.string.gift_success))
                                            .setText("已赠送${item.price.toFloat() / 100f}信用点")
                                            .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                            .show()
                                        viewModel.refresh()
                                    } else {
                                        Alerter.create(activity!!)
                                            .setTitle(getString(R.string.gift_failed))
                                            .setText(message.msg)
                                            .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                            .show()
                                    }
                                } catch (e: Exception) {
                                    Alerter.create(activity!!)
                                        .setTitle(getString(R.string.gift_failed))
                                        .setText(getString(R.string.network_error))
                                        .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                        .show()
                                }

                            }
                        }.show()
                }
                else if (tag == "status") {
                    try {
                        if (item.status == "已礼物") {
                            val builder = QMUIDialog.MessageDialogBuilder(activity)
                            builder
                                .setTitle(getString(R.string.cancel_gift))
                                .setMessage("要撤回礼物${item.name}吗？")
                                .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                                .addAction(getString(R.string.confirm)) { dialog, _ ->
                                    dialog.dismiss()
                                    scope.launch {
                                        val message = HangerService().cancelPledge(item.id.toString())
                                        if (message.code == "OK") {
                                            Alerter.create(activity!!)
                                                .setTitle(getString(R.string.cancel_gift_success))
                                                .setText("已撤回${item.name}")
                                                .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                                .show()
                                            viewModel.refresh()
                                        } else {
                                            Alerter.create(activity!!)
                                                .setTitle(getString(R.string.cancel_gift_success))
                                                .setText(message.msg)
                                                .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                                .show()
                                        }
                                    }
                                }.show()
                        }
                    } catch (e: Exception) {
                        createWarningAlerter(activity!!, getString(R.string.cancel_gift_failed), getString(R.string.network_error)).show()
                    }
                    if (item.status == "回购中") {
                        val builder = QMUIDialog.MessageDialogBuilder(activity)
                        builder
                            .setTitle("回购舰船")
                            .setMessage("要回购${item.name}吗？")
                            .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                            .addAction(getString(R.string.confirm)) { dialog, _ ->
                                dialog.dismiss()
                                try {
                                    scope.launch {
                                        try {
                                            clearCart()
                                            if (!item.isUpgrade) {
                                                val message = RSIApi.retrofitService.buyBackPledge(BuyBackPledgeBody(item.id))
                                                val recaptcha =CirnoApi.getReCaptchaV3(1)[0]
                                                nextStep()
                                                val address = cartAddressQuery()
                                                val assignAddress = cartAddressAssign(address.data.store.addressBook.first().id)
                                                val validateData = cartValidation(recaptcha)
                                                if (validateData.errors != null) {
                                                    createWarningAlerter(
                                                        requireActivity(),
                                                        "购买失败",
                                                        validateData.errors[0].message
                                                    ).show()
                                                    return@launch
                                                }
                                                val cartStatus = getCartSummary()
                                                if (cartStatus.data.store.cart.totals.total == 0 && cartStatus.data.store.cart.totals.subTotal == 0) {
                                                    createSuccessAlerter(
                                                        requireActivity(),
                                                        "购买成功",
                                                        "请在机库中查看"
                                                    ).show()
                                                } else {
                                                    performAliPay(requireContext(), validateData.data!!.store.cart.mutations.validate!!)
                                                    viewModel.refresh()
                                                    return@launch
                                                }
                                                if (message.success == 1) {
                                                    createSuccessAlerter(requireActivity(), getString(R.string.buy_back_success), "已添加回购${item.name}")
                                                    jumpToCartActivity(requireContext())
                                                } else {
                                                    createWarningAlerter(requireActivity(), getString(R.string.buy_back_failed), message.msg).show()
                                                }
                                            } else {
                                                RSIApi.setBuybackAuthToken()
                                                RSIApi.getBuybackContextToken(
                                                    fromShipId = item.formShipId,
                                                    pledgeId = item.id,
                                                    toShipId = item.toShipId,
                                                    toSkuId = item.toSkuId
                                                )
                                                val addToCartPostBody = UpgradeAddToCartQuery().getRequestBody(
                                                    item.formShipId,
                                                    item.toSkuId
                                                )
                                                val token = RSIApi.retrofitService.addUpgradeToCart(
                                                    addToCartPostBody
                                                )
                                                if (token.data.addToCart == null) {
                                                    createWarningAlerter(activity!!, getString(R.string.buy_back_failed), "Token error").show()
                                                    return@launch
                                                }
                                                RSIApi.retrofitService.applyToken(ApplyTokenBody(token.data.addToCart.jwt))
                                                val recaptcha =CirnoApi.getReCaptchaV3(1)[0]
                                                nextStep()
                                                val address = cartAddressQuery()
                                                val assignAddress = cartAddressAssign(address.data.store.addressBook.first().id)
                                                val validateData = cartValidation(recaptcha)
                                                if (validateData.errors != null) {
                                                    createWarningAlerter(
                                                        requireActivity(),
                                                        "购买失败",
                                                        validateData.errors[0].message
                                                    ).show()
                                                    return@launch
                                                }
                                                val cartStatus = getCartSummary()
                                                if (cartStatus.data.store.cart.totals.total == 0 && cartStatus.data.store.cart.totals.subTotal == 0) {
                                                    createSuccessAlerter(
                                                        requireActivity(),
                                                        "购买成功",
                                                        "请在机库中查看"
                                                    ).show()
                                                } else {
                                                    performAliPay(requireContext(), validateData.data!!.store.cart.mutations.validate!!)
                                                    viewModel.refresh()
                                                    return@launch
                                                }
                                            }
                                        } catch (e: Exception) {
                                            createWarningAlerter(activity!!, getString(R.string.buy_back_failed), e.toString()).show()
                                            Log.d("buyback", e.toString())
                                        }
                                    }
                                } catch (e: Exception) {
                                    createWarningAlerter(activity!!, getString(R.string.buy_back_failed), getString(R.string.network_error)).show()
                                    Log.d("buyback", e.toString())
                                }
                            }.show()

                    }

                } else if(tag == "can_upgrade") {
                    scope.launch {
                        try {
                            val upgradeTarget = RSIApi.chooseUpgradeTarget(item.id.toString())
                            Log.d("upgrade", upgradeTarget.toString())
                            if (upgradeTarget == null) {
                                createWarningAlerter(requireActivity(), getString(R.string.request_upgrade_failed), getString(R.string.network_error)).show()
                                return@launch
                            }
                            if (upgradeTarget.size == 0) {
                                Alerter.create(requireActivity())
                                    .setTitle(getString(R.string.request_upgrade_failed))
                                    .setText(getString(R.string.no_upgrade_target))
                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                    .show()
                                return@launch
                            }
                            val builder = QMUIDialog.MenuDialogBuilder(activity)
                            builder.setTitle(getString(R.string.choose_upgrade_target))
                            val items = mutableListOf<String>()
                            for (i in upgradeTarget) {
                                items.add(
                                    i.name.split("(#")[0]
                                        .replace("Standalone Ship", "单船")
                                        .replace("Package", "船包中")
                                )
                            }
                            builder.addItems(items.toTypedArray()) { dialog, which ->
                                dialog.dismiss()
                                scope.launch {
                                    val doubleCheckBuilder = QMUIDialog.MessageDialogBuilder(activity)
                                    doubleCheckBuilder.setTitle(getString(R.string.confirm_upgrade))
                                        .setMessage("你确定要使用:\n${item.name}\n升级:\n${upgradeTarget[which].name} 吗？\n注意，此操作不可逆！")
                                        .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                                        .addAction(getString(R.string.confirm)) { dialog, _ ->
                                            dialog.dismiss()
                                            scope.launch {
                                                val message = RSIApi.applyUpgrade(
                                                    item.id.toString(),
                                                    upgradeTarget[which].pledge_id,
                                                    viewModel.currentUser.value!!.password
                                                )
                                                if (message.code == "OK") {
                                                    Alerter.create(activity!!)
                                                        .setTitle(getString(R.string.request_upgrade_success))
                                                        .setText("舰船已升级")
                                                        .setBackgroundColorInt(
                                                            getColor(
                                                                context!!,
                                                                R.color.alert_dialog_background
                                                            )
                                                        )
                                                        .show()
                                                    viewModel.refresh()
                                                } else {
                                                    Alerter.create(activity!!)
                                                        .setTitle(getString(R.string.upgrade_failed))
                                                        .setText(message.msg)
                                                        .setBackgroundColorInt(
                                                            getColor(
                                                                context!!,
                                                                R.color.alert_dialog_background_failure
                                                            )
                                                        )
                                                        .show()
                                                }
//                                            val message = RSIApi.upgradeShip(item.id.toString(), upgradeTarget[which].id.toString())
//                                            if (message.code == "OK") {
//                                                Alerter.create(activity!!)
//                                                    .setTitle(getString(R.string.upgrade_success))
//                                                    .setText("已升级为${upgradeTarget[which].name}")
//                                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
//                                                    .show()
//                                                viewModel.refresh()
//                                            } else {
//                                                Alerter.create(activity!!)
//                                                    .setTitle(getString(R.string.upgrade_failed))
//                                                    .setText(message.msg)
//                                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
//                                                    .show()
                                                Toast.makeText(getActivity(), "升级成功", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .show()
                                    dialog.dismiss()
                                }
                            }.show()

                        } catch (e: Exception) {
                            Alerter.create(requireActivity())
                                .setTitle(getString(R.string.request_upgrade_failed))
                                .setText(getString(R.string.network_error))
                                .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                .show()
                        }
                    }
                }
            }
        )

//        adapter.setHasStableIds(true)

        binding.hangerRecyclerView.adapter = adapter
        binding.hangerRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        viewModel.hangerItems.observe(viewLifecycleOwner) {
            if (viewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                adapter.submitList(it.toItemPropertyList())
            } else if (viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                adapter.submitList(viewModel.buybackItems.value?.toItemProperty())
            }
//            binding.hangerRecyclerView.post {
//                Log.d("scroll", "scroll to top")
//                // delay 100ms to scroll to top
////                Thread.sleep(2000)
//                binding.hangerRecyclerView.scrollToPosition(0)
//            }
        }
        viewModel.currentMode.observe(viewLifecycleOwner) {
            if (it == HomeViewModel.Mode.HANGER) {
                binding.fragmentTitle.text = getString(R.string.my_hanger)
            } else if (it == HomeViewModel.Mode.BUYBACK) {
                binding.fragmentTitle.text = getString(R.string.my_buyback)
            }
        }

        viewModel.buybackItems.observe(viewLifecycleOwner) {
            if (viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                adapter.submitList(it.toItemProperty())

            } else if (viewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                adapter.submitList(viewModel.hangerItems.value?.toItemPropertyList())
            }
//            binding.hangerRecyclerView.post {
//                // delay 100ms to scroll to top
////                Thread.sleep(2000)
//                binding.hangerRecyclerView.scrollToPosition(0)
//            }
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

        viewModel.refreshBuybackError.observe(viewLifecycleOwner) {
            if (it == true) {
                createWarningAlerter(
                    requireActivity(),
                    getString(R.string.buyback_refresh_failed),
                    getString(R.string.network_error)
                ).show()
            }
        }

        viewModel.refreshHangerError.observe(viewLifecycleOwner) {
            if (it == true) {
                createWarningAlerter(
                    requireActivity(),
                    getString(R.string.hanger_refresh_failed),
                    getString(R.string.network_error)
                ).show()
            }
        }


        return binding.root
    }
    fun jumpToCartActivity(context: Context) {
        val bundle = Bundle()
        bundle.putString(
            "url",
            "https://robertsspaceindustries.com/store/pledge/cart"
        )
        val intent = Intent(context, CartActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun onResetFabClick(homeViewModel: HomeViewModel) {
        homeViewModel.setFilter("")
    }

    private fun onUpgradeFabClick(homeViewModel: HomeViewModel) {
//        if(!RefugeVip.isVip()) {
//            RefugeVip.createWarningAlert(requireActivity())
//            return
//        }
        homeViewModel.setFilter("Upgrade - ")
    }

    private fun onShipFabClick(homeViewModel: HomeViewModel) {
//        if(!RefugeVip.isVip()) {
//            RefugeVip.createWarningAlert(requireActivity())
//            return
//        }
        homeViewModel.setFilter("Ship")
    }

    private fun onSubscribeFabClick(homeViewModel: HomeViewModel) {
//        if(!RefugeVip.isVip()) {
//            RefugeVip.createWarningAlert(requireActivity())
//            return
//        }
        homeViewModel.setFilter("Subscribe")
    }

    private fun onPaintFabClick(homeViewModel: HomeViewModel) {
//        if(!RefugeVip.isVip()) {
//            RefugeVip.createWarningAlert(requireActivity())
//            return
//        }
        homeViewModel.setFilter("paint")
    }

    private fun onTrashFabClick(homeViewModel: HomeViewModel) {
//        if(!RefugeVip.isVip()) {
//            RefugeVip.createWarningAlert(requireActivity())
//            return
//        }
        homeViewModel.setFilter("Trash")
    }

    private fun vibrate() {
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (vibrator.hasVibrator()) {
            val vibratorEffect = android.os.VibrationEffect.createOneShot(50, 70)
            vibrator.vibrate(vibratorEffect)
        }
    }

}