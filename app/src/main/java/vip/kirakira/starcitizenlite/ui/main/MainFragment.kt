package vip.kirakira.starcitizenlite.ui.main

import android.R.attr.banner
import android.R.attr.start
import android.content.ClipData.newIntent
import android.content.Intent
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
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.activities.PlayerSearch
import vip.kirakira.starcitizenlite.database.BannerImage
import vip.kirakira.starcitizenlite.databinding.MainFragmentBinding
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.network.main.getRandomBannerURL
import vip.kirakira.viewpagertest.ui.shopping.ShoppingViewModel


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }


    private lateinit var binding: MainFragmentBinding

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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