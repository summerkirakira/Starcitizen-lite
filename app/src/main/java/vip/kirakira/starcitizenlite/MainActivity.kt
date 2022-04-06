package vip.kirakira.starcitizenlite

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.wyt.searchbox.SearchFragment
import vip.kirakira.starcitizenlite.ui.ScreenSlidePagerAdapter
import vip.kirakira.starcitizenlite.ui.home.HomeFragment
import vip.kirakira.starcitizenlite.ui.main.MainFragment
import vip.kirakira.viewpagertest.ui.shopping.ShoppingFragment
import vip.kirakira.viewpagertest.ui.shopping.ShoppingViewModel


var  PAGE_NUM = 3;

class MainActivity : AppCompatActivity() {
    private lateinit var mPager: ViewPager2
    private lateinit var mMovingBar: View
    private lateinit var bottomShopIcon: ImageView
    private lateinit var bottomHangerIcon: ImageView
    private lateinit var bottomMainIcon: ImageView
    private var  density: Float = 0f

    val shoppingViewModel: ShoppingViewModel by viewModels()
    lateinit var popupLayout: View

    enum class FragmentType(val value: Int) {
        SHOPPING(0),
        MAIN(1),
        HANGER(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMovingBar = findViewById(R.id.bottom_moving_bar)
        mPager = findViewById(R.id.pager)
        bottomShopIcon = findViewById(R.id.bottom_shop_icon)
        bottomHangerIcon = findViewById(R.id.bottom_hanger_icon)
        bottomMainIcon = findViewById(R.id.bottom_main_icon)




        val searchFragment: SearchFragment = SearchFragment.newInstance()
        searchFragment.setOnSearchClickListener { keyword ->
            Toast.makeText(this, keyword, Toast.LENGTH_SHORT).show()
        }
//        searchFragment.showFragment(supportFragmentManager,SearchFragment.TAG);

        bottomMainIcon.setOnClickListener {
            mPager.currentItem = FragmentType.MAIN.value
        }
        bottomHangerIcon.setOnClickListener {
            mPager.currentItem = FragmentType.HANGER.value
        }

        bottomShopIcon.setOnClickListener {
            mPager.currentItem = FragmentType.SHOPPING.value
        }

        density = resources.displayMetrics.density

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        val fragment_list: List<Fragment> = listOf(ShoppingFragment(), MainFragment(), HomeFragment())
        pagerAdapter.setList(fragment_list)
        mPager.adapter = pagerAdapter
        mPager.setCurrentItem(1, false)
        mPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mMovingBar.x = (mPager.width * positionOffset + position * mPager.width) / 3 + mPager.width / 6 - 25 * density //设置滑动条的位置
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })

    }

}