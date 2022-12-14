package vip.kirakira.starcitizenlite.ui.main

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import kotlinx.coroutines.*
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.activities.PlayerSearch
import vip.kirakira.starcitizenlite.activities.ThreePartySiteActivity
import vip.kirakira.starcitizenlite.createSuccessAlerter
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.database.BannerImage
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.databinding.MainFragmentBinding
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.RSIApi


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: MainFragmentBinding

    val viewModel: MainViewModel by activityViewModels()

    lateinit var database: ShopItemDatabase

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        binding.lifecycleOwner = this



        viewModel.takeBanners.observe(viewLifecycleOwner) {
            if (it.size >= 6) {
                imageBanner(it)
            }
        }
        binding.playerSearchLayout.setOnClickListener {
            val intent = Intent(context, PlayerSearch::class.java)
            startActivity(intent)
        }

        binding.spectrumLayout.setOnClickListener {
            val intent = Intent(context,CartActivity::class.java)
            intent.putExtra("url", "https://robertsspaceindustries.com/spectrum/community/SC")
            startActivity(intent)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {}

        binding.organizationSearchLayout.setOnClickListener {
            // 修改为促销码查询
//            if (viewModel.currentUser.value == null){
//                createWarningAlerter(requireActivity(), getString(R.string.please_login), getString(R.string.operate_after_login)).show()
//                return@setOnClickListener
//            }
            scope.launch {
                val promotionCode = CirnoApi.retrofitService.getAllPromotion()
                val promotionNameList = promotionCode.map { it.chinese_title }
                val builder = QMUIDialog.MultiCheckableDialogBuilder(requireContext())
                builder.setTitle("请选择要兑换的礼物")
                    .addItems(promotionNameList.toTypedArray(), null)
                    .addAction("取消") { dialog, index -> dialog.dismiss() }
                    .addAction("确认") { dialog, index ->
                        dialog.dismiss()
                        builder.checkedItemIndexes.forEach {
                            scope.launch {
                                val result = RSIApi.redeemPromoCode(promotionCode[it].promo, promotionCode[it].code, promotionCode[it].currency)
                                if (result.success != 0) {
                                    createSuccessAlerter(requireActivity(), "\"${promotionCode[it].chinese_title}\"兑换成功", result.msg).show()
                                } else {
                                    createWarningAlerter(requireActivity(), "\"${promotionCode[it].chinese_title}\"兑换失败", result.msg).show()
                                }
                            }
                        }

                    }
                    .show()
            }

        }

        binding.shipSearchLayout.setOnClickListener {
            if (viewModel.currentUser.value == null){
                createWarningAlerter(requireActivity(), getString(R.string.please_login), getString(R.string.operate_after_login)).show()
                return@setOnClickListener
            }
            val intent: Intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("url", "https://robertsspaceindustries.com/account/pledges")
            startActivity(intent)
//            val intent = Intent(context,CartActivity::class.java)
//            intent.putExtra("url", "https://www.erkul.games/live/calculator")
//            startActivity(intent)
        }

        binding.componentSearchLayout.setOnClickListener {
            val intent = Intent(context,CartActivity::class.java)
            intent.putExtra("url", "https://fleetyards.net/search")
            startActivity(intent)
        }

        binding.resetCharacterIcon.setOnClickListener {
            if (viewModel.currentUser.value == null){
                createWarningAlerter(requireActivity(),getString(R.string.please_login),getString(R.string.operate_after_login)).show()
                return@setOnClickListener
            }
            QMUIDialog.MessageDialogBuilder(activity)
                .setTitle(getString(R.string.is_agree_to_reset))
                .setMessage(getString(R.string.reset_warning))
                .addAction(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .addAction(getString(R.string.confirm)) { dialog, _ ->
                    dialog.dismiss()
                    CoroutineScope(Dispatchers.IO).launch {
                        val message = RSIApi.resetCharacter(viewModel.currentUser.value!!.password, issue_council = viewModel.currentUser.value!!.email)
                        if (message.code == "OK") {
                            createSuccessAlerter(requireActivity(),getString(R.string.reset_success),getString(R.string.please_login_after_15_mins)).show()
                        } else {
                            createWarningAlerter(requireActivity(),getString(R.string.reset_failed),message.msg).show()
                        }
                    }
                }.show()
        }

        binding.ptuLayout.setOnClickListener {
            if (viewModel.currentUser.value == null){
                createWarningAlerter(requireActivity(),getString(R.string.please_login),getString(R.string.operate_after_login)).show()
                return@setOnClickListener
            }
            val items = arrayOf("将账号拷贝到PTU", "重置PTU账号")
            QMUIDialog.MenuDialogBuilder(activity)
                .setTitle(getString(R.string.access_ptu))
                .addItems(items, DialogInterface.OnClickListener {
                    dialog, which ->
                        when(which) {
                            0 -> {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val message = RSIApi.copyAccount()
                                    if (message.code == "OK") {
                                        createSuccessAlerter(requireActivity(),getString(R.string.copy_success),getString(R.string.please_login_ptu)).show()
                                    } else {
                                        createWarningAlerter(requireActivity(),getString(R.string.copy_failed),message.msg).show()
                                    }
                                }
                            }
                            1 -> {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val message = RSIApi.eraseCopyAccount()
                                    if (message.code == "OK") {
                                        createSuccessAlerter(requireActivity(),getString(R.string.reset_success),getString(R.string.please_login_ptu)).show()
                                    } else {
                                        createWarningAlerter(requireActivity(),getString(R.string.reset_failed),message.msg).show()
                                    }

                                }
                            }
                        }
                }).show()
        }

        binding.referralCodeLayout.setOnClickListener {
            if (viewModel.currentUser.value == null){
                createWarningAlerter(requireActivity(),getString(R.string.please_login),getString(R.string.operate_after_login)).show()
                return@setOnClickListener
            }
            val intent: Intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("url", "https://robertsspaceindustries.com/account/referral-program")
            startActivity(intent)
        }

        binding.tutorialLayout.setOnClickListener {
            val intent: Intent = Intent(activity, ThreePartySiteActivity::class.java)
            intent.putExtra("url", "https://www.biaoju.site/")
            startActivity(intent)
        }

        binding.upgradeLayout.setOnClickListener {
            val intent: Intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("url", "https://biaoju.site/star-refuge/docs/install-localization")
            startActivity(intent)
        }

        return binding.root
    }

    fun imageBanner(banners: List<BannerImage>) {
        binding.banner.addBannerLifecycleObserver(this)
            .setAdapter( object : BannerImageAdapter<BannerImage>(banners) {
                override fun onBindView(holder: BannerImageHolder?, data: BannerImage?, position: Int, size: Int) {
                    Glide.with(holder!!.itemView)
                        .load(data!!.url)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                        .into(holder.imageView);
                }

            }).indicator = CircleIndicator(context)
    }
}




data class Banner(
    val imageUrl: String,
    val title: String,
)