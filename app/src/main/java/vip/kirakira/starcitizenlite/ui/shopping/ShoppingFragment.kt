package vip.kirakira.viewpagertest.ui.shopping

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.*
import vip.kirakira.starcitizenlite.*
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.databinding.ShoppingFragmentBinding
import vip.kirakira.starcitizenlite.network.*
import vip.kirakira.starcitizenlite.network.CirnoProperty.RecaptchaList
import vip.kirakira.starcitizenlite.network.shop.*
import vip.kirakira.starcitizenlite.ui.shopping.ShopItemFilter
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip
import vip.kirakira.viewpagertest.network.graphql.ApplyTokenBody
import vip.kirakira.viewpagertest.network.graphql.FilterShipsQuery
import vip.kirakira.viewpagertest.network.graphql.SearchFromShipQuery
import vip.kirakira.viewpagertest.network.graphql.UpgradeAddToCartQuery
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class ShoppingFragment : Fragment() {

    companion object {
        fun newInstance() = ShoppingFragment()
    }

    lateinit var binding: ShoppingFragmentBinding
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val viewModel: ShoppingViewModel by activityViewModels()
    private var isAdding = false
    private var autoBuying = ""


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.shopping_fragment, container, false)
        val adapter = ShoppingAdapter(
            ShoppingAdapter.OnClickListener {
                    item ->
                run {
                    viewModel.popUpItemDetail(item)
                }
            }
        )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity() /* Activity context */)
        var toId: Int? = null
        var toName: String? = null
        var upgradeCount: Int? = null


        binding.lifecycleOwner = this

        binding.catalogRecyclerView.adapter = adapter
        binding.catalogRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        var selectedItem: ShopItem? = null
        viewModel.itemsAfterFilter.observeForever {
            adapter.submitList(it)
        }


        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            if(!it) {
                binding.catalogRecyclerView.scrollToPosition(0)
            }
        }

        binding.fab.hide()
        binding.leftFab.hide()

        viewModel.isDetailShowing.observe(viewLifecycleOwner) {
            if(binding.popupLayout.visibility == View.VISIBLE) {
                binding.popupLayout.visibility = View.INVISIBLE
                binding.fab.hide()
                binding.leftFab.hide()
            }
            if(it) {
                binding.popupLayout.visibility = View.VISIBLE
                when (viewModel.currentUpgradeStage.value) {
                    ShoppingViewModel.UpgradeStage.UNDEFINED -> {
                        binding.fab.show()
                        ObjectAnimator.ofFloat(binding.fab, "rotation", 0f, 90f).apply {
                            duration = 300
                            start()
                        }
                    }
                    ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP -> {
                        binding.fab.show()
                        ObjectAnimator.ofFloat(binding.fab, "rotation", 0f, 90f).apply {
                            duration = 300
                            start()
                        }
                    }

                    ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP -> {
                        binding.fab.show()
                        ObjectAnimator.ofFloat(binding.fab, "rotation", 0f, 90f).apply {
                            duration = 300
                            start()
                        }
                        binding.leftFab.show()
                        ObjectAnimator.ofFloat(binding.leftFab, "rotation", 0f, -90f).apply {
                            duration = 300
                            start()
                        }
                    }

                    else -> {}
                }
            }
            }

        viewModel.popUpItem.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            binding.textViewShopItemDetailTitle.text = it.chineseName?:it.name
            binding.textViewShopItemDetailSubtitle.text = it.chineseSubtitle?:it.subtitle
            viewModel.isDetailShowing.value = binding.popupLayout.visibility == View.INVISIBLE
            selectedItem = it
            when(viewModel.currentUpgradeStage.value) {
                ShoppingViewModel.UpgradeStage.UNDEFINED -> {
                    binding.textViewShopItemDetailDescription.text = it.chineseDescription?:it.excerpt
                }
                ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP -> {
                    binding.textViewShopItemDetailSubtitle.text = getString(R.string.ship_upgrade)
                    val descriptionText = "${getString(R.string.click_arrow_to_choose_upgrade_to_ship)}。\n\n${getString(R.string.buy_upgrade_warning)}"
                    binding.textViewShopItemDetailDescription.text = descriptionText
                }

                ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP -> {
                    binding.textViewShopItemDetailSubtitle.text = getString(R.string.ship_upgrade)
                    val descriptionText = "${getString(R.string.click_arrow_to_choose_upgraded_ship)}。\n\n${getString(R.string.buy_upgrade_warning)}"
                    binding.textViewShopItemDetailDescription.text = descriptionText
                }

                else -> {}
            }
        })

        viewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.UNDEFINED

        binding.swipeRefreshLayout.setOnRefreshListener {
            when(viewModel.currentUpgradeStage.value) {
                ShoppingViewModel.UpgradeStage.UNDEFINED -> {
                    viewModel.getCatalog()
                }
                ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                else -> {}
            }
        }

        viewModel.currentUpgradeStage.observe(viewLifecycleOwner) {
            when (it) {
                ShoppingViewModel.UpgradeStage.UNDEFINED -> {
                    binding.fab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.leftFab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.fragmentTitle.text = getString(R.string.ship_shop)

                }
                ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP -> {
                    binding.fab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.leftFab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.fragmentTitle.text = getString(R.string.choose_to_ship)
                }

                ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP -> {
                    binding.fab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.leftFab.setImageDrawable(resources.getDrawable(R.drawable.ic_upgrade_arrow))
                    binding.fragmentTitle.text = getString(R.string.choose_from_ship)
                }

                else -> {}
            }
        }

        binding.fab.setOnClickListener {
            val isAutoAddCredits = sharedPreferences.getBoolean("auto_add_credits", false)
            if(selectedItem != null) {
                if (csrf_token.isEmpty()) {
                    getRSIToken()
                    if (csrf_token.isEmpty()) {
                        createWarningAlerter(
                            requireActivity(),
                            "添加购物车失败",
                            getString(R.string.network_error)
                        ).show()
                        return@setOnClickListener
                    }
                }

                if (viewModel.currentUpgradeStage.value == ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP) {
                    scope.launch {
                        val skus = selectedItem!!.skus!!.split(",")
                        if (skus.size > 1) {
                            val checkItems: List<String> = skus.map {
                                val title = it.split("#&")[0]
                                val price = it.split("#&")[1].toInt() / 100
                                return@map "$title - $price"
                            }
                            val checkBuilder = QMUIDialog.CheckableDialogBuilder(context)
                            checkBuilder
                                .setTitle("请选择你要升级到的版本")
                                .addItems(checkItems.toTypedArray()) { dialog, which ->
                                    toId = skus[which].split("#&")[2].toInt()
                                    toName = skus[which].split("#&")[0]
                                    dialog.dismiss()
                                    createMessageDialog(requireContext(), "确认升级","你确定要购买${selectedItem!!.name}(${toName})的升级包吗？" )
                                        .addAction("取消") { dialog, _ ->
                                            upgradeCount = null
                                            dialog.dismiss()
                                        }
                                        .addAction("确定") { dialog, _ ->
                                            dialog.dismiss()
                                            scope.launch {
                                                try {
                                                    RSIApi.setAuthToken()
                                                    RSIApi.setUpgradeToken()
                                                    val shipFrom = RSIApi.retrofitService.filterShips(SearchFromShipQuery().getRequestBody(toId=toId!!))
                                                    if (shipFrom.errors != null) {
                                                        createWarningAlerter(requireActivity(), getString(R.string.search_can_upgrade_error), shipFrom.errors[0].message).show()
                                                        return@launch
                                                    }
                                                    if (shipFrom.data.from == null || shipFrom.data.from.ships.isEmpty()) {
                                                        createWarningAlerter(requireActivity(), getString(R.string.search_can_upgrade_error), "未找到可升级的船只").show()
                                                        return@launch
                                                    }
                                                    val shipFromList: List<Int> = shipFrom.data.from.ships.map { it.id + 100000 }
                                                    viewModel.setFilter(ShopItemFilter("", listOf("Upgrade"), ids=shipFromList))
                                                    viewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP
                                                    viewModel.isDetailShowing.value = false
                                                } catch (e: Exception) {
                                                    createWarningAlerter(requireActivity(), getString(R.string.search_can_upgrade_error), getString(R.string.network_error)).show()
                                                }
                                            }
                                            upgradeCount = null
                                        }
                                        .show()
                                }.show()
                                return@launch
                        } else {
                            toId = skus[0].split("#&")[2].toInt()
                            toName = selectedItem!!.name + ' ' + skus[0].split("#&")[0]
                            createMessageDialog(requireContext(), "确认升级","你确定要购买${selectedItem!!.name}(${toName})的升级包吗？" )
                                .addAction("取消") { dialog, _ ->
                                    upgradeCount = null
                                    dialog.dismiss()
                                }
                                .addAction("确定") { dialog, _ ->
                                    dialog.dismiss()
                                    scope.launch {
                                        RSIApi.setAuthToken()
                                        RSIApi.setUpgradeToken()
                                        val shipFrom = RSIApi.retrofitService.filterShips(SearchFromShipQuery().getRequestBody(toId=toId!!))
                                        if (shipFrom.errors != null) {
                                            createWarningAlerter(requireActivity(), "查询可升级船只失败", shipFrom.errors[0].message).show()
                                            return@launch
                                        }
                                        if (shipFrom.data.from == null || shipFrom.data.from.ships.isEmpty()) {
                                            createWarningAlerter(requireActivity(), "查询可升级船只失败", "未找到可升级的船只").show()
                                            return@launch
                                        }
                                        val shipFromList: List<Int> = shipFrom.data.from.ships.map { it.id + 100000 }
                                        viewModel.setFilter(ShopItemFilter("", listOf("Upgrade"), ids=shipFromList))
                                        viewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP
                                        viewModel.isDetailShowing.value = false
                                    }
                                    upgradeCount = null
                                }
                                .show()
                        }

                    }
                }
                else if (viewModel.currentUpgradeStage.value == ShoppingViewModel.UpgradeStage.UNDEFINED) {
                    val numberBuilder = QMUIDialog.EditTextDialogBuilder(context)
                    var number = 1
                    numberBuilder
                        .setTitle("请输入购买数量")
                        .setPlaceholder("1")
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .addAction("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .addAction("确定") { dialog, _ ->
                            try {
                                number = numberBuilder.editText.text.toString().toInt()
                            } catch (e: Exception) {
                                number = 1
                            }
                            dialog.dismiss()
                            val builder = QMUIDialog.CheckBoxMessageDialogBuilder(context)
                            builder
                                .setTitle("是否添加${number}件${selectedItem!!.name}(${selectedItem!!.price * number / 100f}USD)\n到购物车？")
                                .setMessage("自动补全信用点")
                                .setChecked(isAutoAddCredits)
                                .addAction("取消") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .addAction("确定") { dialog, _ ->
                                    dialog.dismiss()
                                    scope.launch {
                                        try {
                                            val canNextStep = true
                                            var maxCartNumber = 5
                                            clearCart()
                                            try {
                                                var cartTest = addToCart(selectedItem!!.id, number)
                                                if (cartTest.errors != null) {
                                                    if (cartTest.errors!![0].message == "Invalid cart") {
                                                        if (number < 5) {
                                                            maxCartNumber = 1
                                                        } else {
                                                            cartTest = addToCart(selectedItem!!.id, 5)
                                                            if (cartTest.errors != null) {
                                                                if (cartTest.errors!![0].message == "Invalid cart") {
                                                                    maxCartNumber = 1
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                clearCart()
                                            } catch (e: Exception) {
                                                createWarningAlerter(
                                                    requireActivity(),
                                                    "添加购物车失败",
                                                    "此商品未开放或已不再可用"
                                                ).show()
                                                return@launch
                                            }
                                            val requiredTokenNumber: Int = if (maxCartNumber < number) {
                                                number / maxCartNumber + 1
                                            } else {
                                                1
                                            }

                                            var totalItemNumber = number
//                                        if (cartInfo.data.store.cart.totals.subTotal == 0) {
//                                            createWarningAlerter(
//                                                requireActivity(),
//                                                "添加购物车失败",
//                                                "此商品未开放或已不再可用"
//                                            ).show()
//                                            canNextStep = false
//                                        }
                                            if (canNextStep) {
                                                var tokenList: MutableList<String> = mutableListOf()
                                                try {
                                                    while (tokenList.size < requiredTokenNumber) {
                                                        val token =
                                                            CirnoApi.getReCaptchaV3(requiredTokenNumber)
                                                        if (token.isEmpty()) {
                                                            RefugeVip.createWarningAlert(requireActivity(), "获取Token失败", "未知错误")
                                                            return@launch
                                                        }
                                                        tokenList.addAll(token)
                                                    }
                                                } catch (e: Exception) {
                                                    createWarningAlerter(
                                                        requireActivity(),
                                                        "获取Token失败",
                                                        getString(R.string.cirno_token_error)
                                                    ).show()
                                                    return@launch
                                                }
                                                while (totalItemNumber > 0) {
                                                    if (maxCartNumber < totalItemNumber) {
                                                        addToCart(selectedItem!!.id, maxCartNumber)
                                                    } else {
                                                        addToCart(selectedItem!!.id, totalItemNumber)
                                                    }
                                                    val cartInfo = getCartSummary()
                                                    if (cartInfo.data.store.cart.totals.total == 0) {
                                                        createWarningAlerter(
                                                            requireActivity(),
                                                            "添加购物车失败",
                                                            "此商品未开放或已不再可用"
                                                        ).show()
                                                        return@launch
                                                    }
                                                    if (builder.isChecked) {
                                                        val applicableCredit: Int =
                                                            cartInfo.data.store.cart.totals.credits.maxApplicable - cartInfo.data.store.cart.totals.credits.amount
                                                        if (applicableCredit == 0) {
                                                            Toast.makeText(
                                                                context,
                                                                "可添加信用点为零",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            jumpToCartActivity(requireActivity())
                                                            return@launch
                                                        }
                                                        val addCredits = addCredit(applicableCredit / 100f)
                                                        if (addCredits.errors == null) {
                                                            Toast.makeText(
                                                                context,
                                                                getString(R.string.credits_add_success),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                getString(R.string.credits_add_failed),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                    nextStep()
                                                    val token = tokenList[0]
                                                    tokenList.removeAt(0)
                                                    val address = cartAddressQuery()
                                                    val assignAddress = cartAddressAssign(address.data.store.addressBook.first().id)
                                                    val validateData = cartValidation(token)
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
                                                        return@launch
                                                    }
                                                    totalItemNumber -= maxCartNumber
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.d("AddToCart", e.toString())
                                            createWarningAlerter(requireActivity(), "添加到购物车失败", getString(R.string.network_error)).show()
                                        }

                                    }
                                }
                                .create()
                                .show()
                        }
                        .show()
                }
                else if (viewModel.currentUpgradeStage.value == ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP) {
                    val fromId = selectedItem!!.id - 100000
                    val fromName = selectedItem!!.name
                    if (upgradeCount == null) {
                        val numberBuilder = QMUIDialog.EditTextDialogBuilder(context)
                        numberBuilder
                            .setTitle("请输入购买数量")
                            .setPlaceholder("1")
                            .setInputType(InputType.TYPE_CLASS_NUMBER)
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                                upgradeCount = null
                            }
                            .addAction("确定") { dialog, _ ->
                                upgradeCount = try {
                                    numberBuilder.editText.text.toString().toInt()
                                } catch (e: Exception) {
                                    1
                                }
                                dialog.dismiss()
                                binding.fab.performClick()
                            }
                            .create()
                            .show()
                    } else {
                        val message = "确定要购买${upgradeCount}个${fromName}到${toName}的升级包吗？"
                        val builder = QMUIDialog.CheckBoxMessageDialogBuilder(context)
                        builder
                            .setTitle(message)
                            .setMessage("自动补全信用点")
                            .setChecked(isAutoAddCredits)
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction("确定") { dialog, _ ->
                                dialog.dismiss()
                                scope.launch {
                                    try {
                                        clearCart()
                                        val totalCount = upgradeCount!!
                                        while (upgradeCount!! > 0) {
                                            val cartToken = RSIApi.retrofitService.addUpgradeToCart(
                                                UpgradeAddToCartQuery().getRequestBody(
                                                    fromId,
                                                    toId!!
                                                )
                                            )
                                            if (cartToken.data.addToCart != null) {
                                                val code =
                                                    RSIApi.retrofitService.applyToken(ApplyTokenBody(cartToken.data.addToCart.jwt))
                                                if (code.success == 0) {
                                                    createWarningAlerter(requireActivity(), "购买失败", "应用Token失败").show()
                                                }
                                                if (builder.isChecked) {
                                                    val cartInfo = getCartSummary()
//                                                    Log.d("CartInfo", cartInfo.toString())
                                                    val applicableCredit: Int =
                                                        cartInfo.data.store.cart.totals.credits.maxApplicable - cartInfo.data.store.cart.totals.credits.amount
                                                    val addCredits = addCredit(applicableCredit / 100f)
//                                                    Log.d("AddCredits", addCredits.toString())
//                                                    if (addCredits.errors == null) {
//                                                        Toast.makeText(
//                                                            context,
//                                                            getString(R.string.credits_add_success),
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                    } else {
//                                                        Toast.makeText(
//                                                            context,
//                                                            getString(R.string.credits_add_failed),
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                    }
                                                }
                                                nextStep()
                                                val tokenList: ArrayList<String>?
                                                val recaptchaList: ArrayList<String>
                                                try {
                                                    recaptchaList = CirnoApi.getReCaptchaV3(1)
                                                    tokenList = recaptchaList
                                                } catch (e: Exception) {
                                                    createWarningAlerter(requireActivity(), "获取Token失败", getString(R.string.cirno_token_error)).show()
                                                    return@launch
                                                }
                                                if (tokenList.isEmpty()) {
                                                    RefugeVip.createWarningAlert(requireActivity(), "获取Token失败", "未知错误")
                                                    return@launch
                                                }
                                                val token = tokenList[0]

                                                val billingAddress = cartAddressQuery()

                                                if (billingAddress.data.store.addressBook.isEmpty()) {
                                                    createWarningAlerter(requireActivity(), "购物车添加失败", "请先在网页端添加至少一个账单地址").show()
                                                    return@launch
                                                }

                                                cartAddressAssign(billingAddress.data.store.addressBook.first().id)

                                                val cartValidateData = cartValidation(token)
                                                val cartStatus = getCartSummary()
                                                if (cartStatus.data.store.cart.totals.total == 0 && cartStatus.data.store.cart.totals.subTotal == 0) {
                                                    if (upgradeCount!! > 1) {
                                                        Toast.makeText(
                                                            context,
                                                            "已购买${totalCount - (upgradeCount!! - 1)} / $totalCount 个${fromName}到${toName}的升级包",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    else {
                                                        createSuccessAlerter(
                                                            requireActivity(),
                                                            "购买成功",
                                                            "请在机库中查看"
                                                        ).show()
                                                        upgradeCount = null
                                                        return@launch
                                                    }
                                                } else {

                                                    if (cartValidateData.data != null && cartValidateData.data.store.cart.mutations.validate != null) {
                                                        performAliPay(requireContext(), cartValidateData.data.store.cart.mutations.validate)
                                                    }
                                                    return@launch
                                                }
                                                upgradeCount = upgradeCount!! - 1
                                            } else {
                                                createWarningAlerter(requireActivity(), "购物车添加失败", "Token失效").show()
                                                return@launch
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.d("UpgradeError", e.toString())
                                        createWarningAlerter(requireActivity(), "添加购物车失败", getString(R.string.network_error)).show()
                                    }
                                }
                            }
                            .create()
                            .show()
                    }

                }
            }
        }
        binding.fab.setOnLongClickListener {
            if(selectedItem != null) {
                isAdding = !isAdding
                if (!isAdding) {
                    return@setOnLongClickListener true
                }

            val builder = QMUIDialog.MessageDialogBuilder(requireContext())
            val dialog = builder
                .setTitle("是否进入抢船模式？即将添加${selectedItem!!.name}")
                .setMessage("当前此功能为测试版, 不保证能正常运作~")
                .addAction("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .addAction("确定") { dialog, _ ->
                    dialog.dismiss()
                    scope.launch {
                        var canNextStep = false
                        val token: ArrayList<String>

//                        if (!RefugeVip.isVip()) {
//                            RefugeVip.createWarningAlert(activity = requireActivity())
//                            return@launch
//                        }

                        try {
                            token =
                                CirnoApi.getReCaptchaV3(1)
                        } catch (e: Exception) {
                            createWarningAlerter(requireActivity(), "Token获取失败", getString(R.string.network_error)).show()
                            return@launch
                        }

                        if (token.isEmpty()) {
                            RefugeVip.createWarningAlert(title = "Token获取失败", detail = "未知错误", activity = requireActivity())
                        }

                        autoBuying = token.first()
                        clearCart()
                        while (!canNextStep && isAdding) {
                            canNextStep = true
                            try {
                                addToCart(selectedItem!!.id, 1)
                            } catch (e: Exception) {
                                createWarningAlerter(requireActivity(), "添加购物车失败", "此商品未开放或已不再可用").show()
                                canNextStep = false
                                continue
                            }
                            val cartInfo = getCartSummary()
                            if(cartInfo.data.store.cart.totals.subTotal == 0) {
                                createWarningAlerter(requireActivity(), "添加购物车失败", "此商品未开放或已不再可用").show()
                                canNextStep = false
                            }
                            if(canNextStep) {
                                val applicableCredit: Int = cartInfo.data.store.cart.totals.credits.maxApplicable - cartInfo.data.store.cart.totals.credits.amount
                                val addCredits = addCredit(applicableCredit / 100f)
                                if(addCredits.errors == null) {
                                    Toast.makeText(context, getString(R.string.credits_add_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, getString(R.string.credits_add_failed), Toast.LENGTH_SHORT)
                                        .show()
                                }
                                nextStep()
//                                if (autoBuying.isNotEmpty()) {
                                cartValidation(autoBuying)
//                                    autoBuying = ""
//                                }
//                            val address = cartAddressQuery()
//                            val assignAddress = cartAddressAssign(address.data.store.addressBook.first().id)
                               val bundle = Bundle()
                                bundle.putString("url", "https://robertsspaceindustries.com/store/pledge/cart")
                                val intent = Intent(context, CartActivity::class.java)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }

                        }
                    }

                }
                .create()
            dialog.show()
//                val autoBuyBuilder = QMUIDialog.EditTextDialogBuilder(context)
//                autoBuyBuilder.setTitle("是否启用自动确认模式？")
//                    .setPlaceholder("请输入自动确认所需token")
//                    .setInputType(InputType.TYPE_CLASS_TEXT)
//                    .addAction(getString(R.string.cancel)) { dialog, _ ->
//                        dialog.dismiss()
//                        autoBuying = ""
//                    }
//                    .addAction(0, getString(R.string.confirm)) { dialog, index ->
//                        dialog.dismiss()
//                        autoBuying = autoBuyBuilder.editText.text.toString()
//                    }
//                    .create()
//                    .show()
        }
            return@setOnLongClickListener true
        }

        binding.leftFab.setOnClickListener() {
            when(viewModel.currentUpgradeStage.value) {
                ShoppingViewModel.UpgradeStage.CHOOSE_FROM_SHIP -> {
                    viewModel.setFilter(ShopItemFilter("", listOf("Upgrade"), onlyCanUpgradeTo = true))
                    viewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP
                    viewModel.isDetailShowing.value = false
                    upgradeCount = null
                }
                ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP -> {

                }
                ShoppingViewModel.UpgradeStage.UNDEFINED -> {

                }
                else -> {}
            }
        }
        return binding.root
    }

//    fun shopItemFilter(filters: List<String>) {
//        viewModel.filterBySubtitle(filters)
//    }
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



}