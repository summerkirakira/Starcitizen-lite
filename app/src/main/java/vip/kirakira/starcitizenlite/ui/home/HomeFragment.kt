package vip.kirakira.starcitizenlite.ui.home

import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.databinding.HomeFragmentBinding
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.network.hanger.HangerService
import vip.kirakira.starcitizenlite.ui.loadImage


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding: HomeFragmentBinding

    private lateinit var viewModel: HomeViewModel

    val scope = CoroutineScope(Job() + Dispatchers.Main)



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: HomeViewModel by activityViewModels()
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this

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
                    val intent = Intent(context, WebLoginActivity::class.java)
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
                // set max height

                binding.popupLayout.visibility = View.VISIBLE
                viewModel.isDetailShowing.value = true
            },
            HangerViewAdapter.OnTagClickListener {
                tag, item ->
                if(tag == "can_reclaim") {
                    QMUIDialog.MessageDialogBuilder(activity)
                        .setTitle(getString(R.string.reclaim_warning))
                        .setMessage("即将对${item.name}进行回收操作，结束后将会返还${item.price.toFloat() / 100f}信用点\n此操作不可逆！是否继续？")
                        .addAction(getString(R.string. cancel)) { dialog, _ -> dialog.dismiss() }
                        .addAction(0, getString(R.string.confirm)) { dialog, _ ->
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
                        Alerter.create(activity!!)
                            .setTitle(getString(R.string.cancel_gift_failed))
                            .setText(getString(R.string.network_error))
                            .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                            .show()
                    }

                } else if(tag == "can_upgrade") {
                    scope.launch {
                        try {
                            val upgradeTarget = RSIApi.chooseUpgradeTarget(item.id.toString())
                            Log.d("upgrade", upgradeTarget.toString())
                            if (upgradeTarget == null) {
                                Alerter.create(requireActivity())
                                    .setTitle(getString(R.string.request_upgrade_failed))
                                    .setText(getString(R.string.network_error))
                                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                    .show()
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

        binding.hangerRecyclerView.adapter = adapter
        binding.hangerRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        viewModel.hangerItems.observe(viewLifecycleOwner) {
            if (viewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                adapter.submitList(it.toItemPropertyList())
            } else if (viewModel.currentMode.value == HomeViewModel.Mode.BUYBACK) {
                adapter.submitList(viewModel.buybackItems.value?.toItemProperty())

            }
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
                Alerter.create(activity!!)
                    .setTitle(getString(R.string.buyback_refresh_failed))
                    .setText(getString(R.string.network_error))
                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                    .show()
            }
        }

        viewModel.refreshHangerError.observe(viewLifecycleOwner) {
            if (it == true) {
                Alerter.create(activity!!)
                    .setTitle(getString(R.string.hanger_refresh_failed))
                    .setText(getString(R.string.network_error))
                    .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                    .show()
            }
        }


        return binding.root
    }
}