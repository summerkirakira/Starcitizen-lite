package vip.kirakira.viewpagertest.ui.shopping

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.CartActivity
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.databinding.ShoppingFragmentBinding
import vip.kirakira.starcitizenlite.network.shop.*
import java.util.*
import kotlin.concurrent.schedule

class ShoppingFragment : Fragment() {

    companion object {
        fun newInstance() = ShoppingFragment()
    }

    lateinit var binding: ShoppingFragmentBinding
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    val viewModel: ShoppingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.shopping_fragment, container, false)
        val adapter = ShoppingAdapter(
            ShoppingAdapter.OnClickListener {
                    item -> viewModel.popUpItemDetail(item)
            }
        )


        binding.lifecycleOwner = this

        binding.catalogRecyclerView.adapter = adapter
        binding.catalogRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        var selectedItem: ShopItem? = null
        viewModel.itemsAfterFilter.observeForever {
            adapter.submitList(it)
        }


        viewModel.needRefresh.observe(viewLifecycleOwner) {
            if (it) {

            }
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            if(!it) {
                binding.catalogRecyclerView.scrollToPosition(0)
            }
        }

        binding.fab.hide()

        viewModel.popUpItem.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            binding.textViewShopItemDetailTitle.text = it.name
            binding.textViewShopItemDetailSubtitle.text = it.subtitle
            binding.textViewShopItemDetailDescription.text = it.excerpt
            if(binding.popupLayout.visibility == View.INVISIBLE) {
                binding.popupLayout.visibility = View.VISIBLE
                binding.fab.show()
                ObjectAnimator.ofFloat(binding.fab, "rotation", 0f, 90f).apply {
                    duration = 300
                    start()
                }
                binding.fab.elevation = 15f
                selectedItem = it
            } else {
                binding.popupLayout.visibility = View.INVISIBLE
                binding.fab.hide()
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCatalog()
        }

        binding.fab.setOnClickListener {
            if(selectedItem != null) {
                val dialog = QMUIDialog.CheckBoxMessageDialogBuilder(context)
                    .setTitle("是否添加${selectedItem!!.name}(${selectedItem!!.price / 100f}USD)\n到购物车？")
                    .setMessage("自动补全信用点")
                    .setChecked(true)
                    .addAction("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .addAction("确定") { dialog, _ ->
                        dialog.dismiss()
                        scope.launch {
                            clearCart()
                            addToCart(selectedItem!!.id, 3)
                            val cartInfo = getCartSummary()
                            val applicableCredit: Int = cartInfo.data.store.cart.totals.credits.maxApplicable - cartInfo.data.store.cart.totals.credits.amount
                            val addCredits = addCredit(applicableCredit / 100f)
                            nextStep()
//                            val address = cartAddressQuery()
//                            val assignAddress = cartAddressAssign(address.data.store.addressBook.first().id)
//                            println(cartValidation("03AGdBq27y9ldoo5gKZTkYZ2S_LEDgEI_iw1UUp9nMjsIW_Z0pywtluUGpXyisHFpdGB9sG84Zryqy3woCnhqGOR1mdIF6aGf1GGoGHe9C3QhpBk7S25IfLHbc2ticW4iw1fuUD6RLT-00vkTwZFwULI_p16BLRAHFLOwwFaJViXuXhlYpjc5Ot-CoQkbf2d-0MP9EpNiSWqF2gsZrje58g8AFF3rgRb19DSfDnVKQS0kE-Tl9zvrJwU-pLKIu_CaPiR2L5jVOItBMhCWe5iMInJN1rck-jUoLONhIc1MummaiyGrdVDHLYu2xb7_V2ipDSNqc_IZ30fJN_bT6RUUnFzFBJeea6hwR91VX34IrN1Tu3eok_7M6ZHRyk5IB_fGu2q5e4G0WfkR5QlEAOGwC6_5LCwlHyI5lRtMirIG2hsBjsbuoVzvYrPhalHHXr9zfxk6t-IDYAayQx3yApVLSBbbai0jGdjkqHeWUwJ3vI85MJhM5mYQRpH1bUCeJLY0snFpF5Lnli5GmeQwuxOiGOuw07JKwbXc1iyNWil9OXnBfGI9_1e4ijHJq0GIZQtg6N5cE1D1MMa37QYbUb8fy96NGurih1EezNDX-AmxWZzhsJ74O72CMIitn0BhpctSrcKHy-beFl6c1zz72vpMRVSgsH9Zg5Zb0QlN9zs6LA60ikLscWyyszeeup__oktpHGsjGE8D1N1zgOTjv2rBW8CsxQq7Q3FU2OJwBej5tZ8eW2udlGEWsHugamTGkdh1osrtgHvcwTsacvoxZXKeV_j2vzA2_iYsgAaNOQpzJzKMDApJd20lXMFyzT--CUZgYhfI0OpU5IUjOgUr0TmVGmouDwmlRUWFFu4Ui_9Cur65SgfE-saWHwyXIxHW72Uy6mQ97PslG69sydlIq-d-8v1350ZaZNJoA9kG_9bNJ7fhxGtjQWvraer8FphO5o8cGrA2ODjOesmYJP6VxeNe7TEhwMKwD3IwUdw1HxW6-2an1NYNMFRisX0Z2PyESqVn6hp1pqGufF6zIecYu0K9VR2MPbEJlktgKzT-pej95E857cimHU0OWgchmq5LYaKIkC95cPoZtIpg9llZ1o2lZ0wtZTFqWEzpJizZsX676cWEEvAaNMG-ksH18C1reNJcpq_QG4ICESXOBZHZbaBA252OnDA4p_Sfir-lYLIQOTYNyuw79Ml3hXnaDkvJHs3Tjv-bmHLoFTuZFaKKbfwG3ai9h_GRaKO6A4cTr0dO7osDDqMtmIXrcAWlgBtKwS8a8X-oOj1dYtwk-1JGFyYEqA5sidnLXLVFyvFJbdLfH88y1Ocy-yWoso5QfVimHQyYkQViNa0GitLcuWE3Q1u15nmnpsZ_mvV4-j-7GZPRFJoR-K5kGFHGmwciU-FrTxOWtTnxuBXqTKNLZPLPIV5IR5P8PIhV2Ur2RwWpaNFqGA9epnv-yJoQtfXewWFWrkH77Y29rUvqM1k04asj5xmyzSSbi43_hJnJVRV6Fo3dnzymgfgJ8tbbBSZ0EOdKTJL4KPeDs-_nZYycmnwwzsEWDqdfo9io7v4GgCrDbEY5GIIyb8Ixnc0KG6Gw"))
                            if(addCredits.errors == null) {
                                Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show()
                            }
                            val bundle = Bundle()
                            bundle.putString("url", "https://robertsspaceindustries.com/store/pledge/cart")
                            val intent = Intent(context, CartActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                    }
                    .create()
                dialog.show()
            }
        }
        return binding.root
    }

    fun shopItemFilter(filters: List<String>) {
        viewModel.filterBySubtitle(filters)
    }



}