package vip.kirakira.starcitizenlite.ui.me

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import io.getstream.avatarview.coil.loadImage
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.databinding.MeFragmentBinding
import vip.kirakira.starcitizenlite.network.saveUserData
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
                    val intent = Intent(context, WebLoginActivity::class.java)
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
            }

            binding.errorBox.hide()
            binding.rootLayout.visibility = View.VISIBLE
            binding.avatar.loadImage(it.profile_image)
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
            val hangerValue = "${it.hanger_value.toFloat()/100f} USD"
            val credits = "${it.store.toFloat()/100f} USD"
            val totalSpent = "${it.total_spent.toFloat()/100f} USD"
            val uecValue = "${it.uec} UEC"
            val recValue = "${it.rec} REC"
            val referralValue = "${it.referral_count} 人"
            val referralCode = it.referral_code

            binding.hangerValueNumber.text = hangerValue
            binding.creditsValue.text = credits
            binding.totalSpendValue.text = totalSpent
            binding.uecValue.text = uecValue
            binding.recValue.text = recValue
            binding.referralCodeValue.text = referralValue
            binding.referralCodeValueText.text = referralCode
        }

        return binding.root
    }



}