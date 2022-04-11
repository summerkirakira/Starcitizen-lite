package vip.kirakira.starcitizenlite.ui.home

import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        binding.contentContainer.setOnClickListener(View.OnClickListener {
            binding.popupLayout.visibility = View.GONE
        })

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

        val adapter = HangerViewAdapter(
            HangerViewAdapter.OnClickListener {
                if(binding.popupLayout.visibility == View.VISIBLE) {
                    binding.popupLayout.visibility = View.GONE
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
                binding.popupLayout.visibility = View.VISIBLE
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
                                val message = HangerService().reclaimPledge(item.id.toString(), viewModel.currentUser.value!!.password)
                                if (message.code == "OK") {
                                    Alerter.create(activity!!)
                                        .setTitle("回收成功")
                                        .setText("已返还${item.price.toFloat() / 100f}信用点")
                                        .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                        .show()
                                    viewModel.refresh()
                                } else {
                                    Alerter.create(activity!!)
                                        .setTitle("回收失败")
                                        .setText(message.msg)
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
                                val message = HangerService().giftPledge(item.id.toString(), viewModel.currentUser.value!!.password, "Powered by Starcitizen lite", builder.getEditText().getText().toString())
                                if (message.code == "OK") {
                                    Alerter.create(activity!!)
                                        .setTitle("赠送成功")
                                        .setText("已赠送${item.price.toFloat() / 100f}信用点")
                                        .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                        .show()
                                    viewModel.refresh()
                                } else {
                                    Alerter.create(activity!!)
                                        .setTitle("赠送失败")
                                        .setText(message.msg)
                                        .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                        .show()
                                }
                            }
                        }
                        .show()
                }
                else if (tag == "status") {
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
                                            .setTitle("撤回成功")
                                            .setText("已撤回${item.name}")
                                            .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background))
                                            .show()
                                        viewModel.refresh()
                                    } else {
                                        Alerter.create(activity!!)
                                            .setTitle("撤回失败")
                                            .setText(message.msg)
                                            .setBackgroundColorInt(getColor(context!!, R.color.alert_dialog_background_failure))
                                            .show()
                                    }
                                }
                            }
                            .show()
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


        return binding.root
    }

}