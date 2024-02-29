package vip.kirakira.starcitizenlite.ui.me

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import io.getstream.avatarview.coil.loadImage
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.LoginActivity
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.databinding.MeFragmentBinding
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.saveUserData
import vip.kirakira.starcitizenlite.repositories.HangerItemRepository
import vip.kirakira.starcitizenlite.repositories.RepoUtil
import vip.kirakira.starcitizenlite.ui.home.Parser
import kotlin.concurrent.thread

class MeFragment : Fragment() {

    companion object {
        fun newInstance() = MeFragment()
    }

    private lateinit var viewModel: MeViewModel
    private lateinit var binding: MeFragmentBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.me_fragment, container, false)
        binding.lifecycleOwner = this

        val database = getDatabase(requireContext())
        val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        val primaryUserId = sharedPreferences.getInt(getString(R.string.primary_user_key), 0)
        val currentHangerValue = sharedPreferences.getInt(getString(R.string.current_hanger_value_key), 0)
        val currentUser: LiveData<User> = database.userDao.getById(primaryUserId)
        println(primaryUserId)
        currentUser.observe(viewLifecycleOwner){
            if(it == null){
                binding.swipeRefresh.visibility = View.GONE
                binding.errorBox.setTitleText(getString(R.string.only_login_account_can_check_user_info))
                binding.errorBox.setDetailText(getString(R.string.not_login_error_box_detail))
                binding.errorBox.setButton(
                    getString(R.string.click_to_login)
                ) {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
                binding.errorBox.show()
                return@observe
            }

            binding.swipeRefresh.setOnRefreshListener {
                if(currentUser.value != null){
                    thread {
                        val newUser = saveUserData(
                            currentUser.value!!.id,
                            currentUser.value!!.rsi_device,
                            currentUser.value!!.rsi_token,
                            currentUser.value!!.email,
                            currentUser.value!!.password
                        )
                        newUser.hanger_value = currentUser.value!!.hanger_value
                        database.userDao.insert(newUser)
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
                binding.swipeRefresh.isRefreshing = false
                binding.currentHangerValueNumber.text = "${Parser.priceFormatter(currentHangerValue)} USD"

            }

            binding.errorBox.hide()
            binding.rootLayout.visibility = View.VISIBLE
            if (it.profile_image.contains("default")) {
                binding.avatar.loadImage("https://cdn.robertsspaceindustries.com/static/images/account/avatar_default_big.jpg")
            } else {
                binding.avatar.loadImage(it.profile_image)
            }
            if(it.organization_image.isNotEmpty()){
                binding.organizationImage.loadImage(it.organization_image)
            }
            binding.name.text = it.name
            binding.handle.text = it.handle
            binding.registerDateValue.text = it.registerTime
            binding.organizationNameValue.text = it.orgName
            if(it.orgName.isNotEmpty()){
                val positionValue = "${it.orgRankName}(${it.orgRank}级权限)"
                binding.organizationPositionValue.text = positionValue
            }
            val hangerValue = "${Parser.priceFormatter(it.hanger_value)} USD"
            val credits = "${Parser.priceFormatter(it.store)} USD"
            val totalSpent = "${Parser.priceFormatter(it.total_spent)} USD"
            val currentHangerValueText = "${Parser.priceFormatter(currentHangerValue)} USD"
            val uecValue = "${it.uec} UEC"
            val recValue = "${it.rec} REC"
            val referralValue = "${it.referral_count} 人"
            val referralCode = it.referral_code

            binding.hangerValueNumber.text = hangerValue
            binding.currentHangerValueNumber.text = currentHangerValueText
            binding.creditsValue.text = credits
            binding.totalSpendValue.text = totalSpent
            binding.uecValue.text = uecValue
            binding.recValue.text = recValue
            binding.referralCodeValue.text = referralValue
            binding.referralCodeValueText.text = referralCode
            initRefugeVip(binding)

            binding.referralCodeLayout.setOnClickListener {
                // click to copy referral code
                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Referral Code", referralCode)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "邀请码已复制到剪切板~", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    fun initRefugeVip(binding: MeFragmentBinding) {
        val sharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        val isVip = sharedPreferences.getBoolean(getString(R.string.IS_VIP), false)
        val vipExpire = sharedPreferences.getInt(getString(R.string.VIP_EXPIRE), 0)
        val totalVipTime = sharedPreferences.getInt(getString(R.string.TOTAL_VIP_TIME), 0)
        val credit = sharedPreferences.getInt(getString(R.string.REFUGE_CREDIT), 0)

        if (isVip) {
            binding.refugeVipTotalTimeText.text = Html.fromHtml("已陪伴避难所<b>${(totalVipTime - vipExpire) / (3600 * 24)}</b>天")
            binding.refugeVipTokenNumText.text = credit.toString()
            binding.refugeVipRemainDayText.text = (vipExpire / (3600 * 24) + 1).toString()
            binding.refugeVipProgressBar.progress = (totalVipTime.toFloat() - vipExpire.toFloat()) / totalVipTime.toFloat() * 100
            if(binding.refugeVipProgressBar.progress < 85) {
                if(binding.refugeVipProgressBar.progress < 10) {
                    binding.refugeVipProgressBar.progress = 10f
                }
                binding.refugeVipProgressBar.secondaryProgress = binding.refugeVipProgressBar.progress + 15
            } else {
                binding.refugeVipProgressBar.secondaryProgress = 100f
            }
        } else {
            binding.vipIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_me_not_vip_avatar))
            binding.refugeVipTotalTimeText.text = "避难所 Premium已过期"
            binding.refugeVipTokenNumText.text = credit.toString()
            binding.refugeVipRemainDayText.text = "0"
            binding.refugeVipProgressBar.setIconBackgroundColor(resources.getColor(R.color.colorDeepGrey100))
            binding.refugeVipProgressBar.progressColor = resources.getColor(R.color.colorDeepGrey80)
            binding.refugeVipProgressBar.secondaryProgressColor = resources.getColor(R.color.colorDeepGrey30)
        }

        binding.getSubscription.setOnClickListener {
            val uri = Uri.parse(CirnoApi.getSubscribeUrl())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }


}