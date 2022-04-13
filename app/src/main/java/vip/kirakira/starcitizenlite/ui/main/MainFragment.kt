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
import com.tapadoo.alerter.Alerter
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.activities.PlayerSearch
import vip.kirakira.starcitizenlite.activities.ThreePartySiteActivity
import vip.kirakira.starcitizenlite.database.BannerImage
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.databinding.MainFragmentBinding
import vip.kirakira.starcitizenlite.network.RSIApi
import kotlin.concurrent.thread


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }


    private lateinit var binding: MainFragmentBinding

    val viewModel: MainViewModel by activityViewModels()

    lateinit var database: ShopItemDatabase

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
            if (viewModel.currentUser.value == null){
                Alerter.create(requireActivity())
                    .setTitle(getString(R.string.please_login))
                    .setText(getString(R.string.operate_after_login))
                    .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                    .setDuration(3000)
                    .show()
                return@setOnClickListener
            }
            if (viewModel.currentUser.value!!.organization.isEmpty()) {
                val intent = Intent(context, CartActivity::class.java)
                intent.putExtra("url", "https://robertsspaceindustries.com/community/orgs")
                startActivity(intent)
                return@setOnClickListener
            }
            val intent = Intent(context,CartActivity::class.java)
            intent.putExtra("url", "https://robertsspaceindustries.com/orgs/${viewModel.currentUser.value!!.organization}")
            startActivity(intent)
        }

        binding.shipSearchLayout.setOnClickListener {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val url = Uri.parse("https://www.erkul.games/live/calculator")
            intent.data = url
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
                Alerter.create(requireActivity())
                    .setTitle(getString(R.string.please_login))
                    .setText(getString(R.string.operate_after_login))
                    .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                    .setDuration(3000)
                    .show()
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
                            Alerter.create(requireActivity())
                                .setTitle(getString(R.string.reset_success))
                                .setText(getString(R.string.please_login_after_15_mins))
                                .setBackgroundColorRes(R.color.alerter_default_success_background)
                                .setDuration(3000)
                                .show()
                        } else {
                            Alerter.create(requireActivity())
                                .setTitle(getString(R.string.reset_failed))
                                .setText(message.msg)
                                .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                                .setDuration(3000)
                                .show()
                        }
                    }
                }.show()
        }

        binding.ptuLayout.setOnClickListener {
            if (viewModel.currentUser.value == null){
                Alerter.create(requireActivity())
                    .setTitle(getString(R.string.please_login))
                    .setText(getString(R.string.operate_after_login))
                    .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                    .setDuration(3000)
                    .show()
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
                                        Alerter.create(requireActivity())
                                            .setTitle(getString(R.string.copy_success))
                                            .setText(getString(R.string.please_login_ptu))
                                            .setBackgroundColorRes(R.color.alerter_default_success_background)
                                            .setDuration(3000)
                                            .show()
                                    } else {
                                        Alerter.create(requireActivity())
                                            .setTitle(getString(R.string.copy_failed))
                                            .setText(message.msg)
                                            .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                                            .setDuration(3000)
                                            .show()
                                    }
                                }
                            }
                            1 -> {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val message = RSIApi.eraseCopyAccount()
                                    if (message.code == "OK") {
                                        Alerter.create(requireActivity())
                                            .setTitle(getString(R.string.reset_success))
                                            .setText(getString(R.string.please_login_ptu_again))
                                            .setBackgroundColorRes(R.color.alerter_default_success_background)
                                            .setDuration(3000)
                                            .show()
                                    } else {
                                        Alerter.create(requireActivity())
                                            .setTitle(getString(R.string.reset_failed))
                                            .setText(message.msg)
                                            .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                                            .setDuration(3000)
                                            .show()
                                    }

                                }
                            }
                        }
                }).show()
        }

        binding.referralCodeLayout.setOnClickListener {
            if (viewModel.currentUser.value == null){
                Alerter.create(requireActivity())
                    .setTitle(getString(R.string.please_login))
                    .setText(getString(R.string.operate_after_login))
                    .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                    .setDuration(3000)
                    .show()
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
            intent.putExtra("url", "https://robertsspaceindustries.com/account/pledges")
            startActivity(intent)
        }

        return binding.root
    }

    fun imageBanner(banners: List<BannerImage>) {
        binding.banner.addBannerLifecycleObserver(this)
            .setAdapter( object : BannerImageAdapter<BannerImage>(banners) {
                override fun onBindView(holder: BannerImageHolder?, data: BannerImage?, position: Int, size: Int) {
                    Glide.with(holder!!.itemView)
                        .load(data!!.image)
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