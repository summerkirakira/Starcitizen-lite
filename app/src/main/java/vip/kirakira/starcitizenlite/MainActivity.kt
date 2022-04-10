package vip.kirakira.starcitizenlite

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.slider.Slider
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.QMUIEmptyView
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wyt.searchbox.SearchFragment
import vip.kirakira.starcitizenlite.activities.WebLoginActivity
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.setRSICookie
import vip.kirakira.starcitizenlite.ui.ScreenSlidePagerAdapter
import vip.kirakira.starcitizenlite.ui.home.HomeFragment
import vip.kirakira.starcitizenlite.ui.loadUserAvatar
import vip.kirakira.starcitizenlite.ui.main.MainFragment
import vip.kirakira.starcitizenlite.ui.me.MeFragment
import vip.kirakira.viewpagertest.ui.shopping.ShoppingFragment
import vip.kirakira.viewpagertest.ui.shopping.ShoppingViewModel


var  PAGE_NUM = 4;

class MainActivity : AppCompatActivity() {
    private lateinit var mPager: ViewPager2
    private lateinit var mMovingBar: View
    private lateinit var bottomShopIcon: ImageView
    private lateinit var bottomHangerIcon: ImageView
    private lateinit var bottomMainIcon: ImageView
    private lateinit var bottomMeIcon: ImageView
    private lateinit var userAvatar: ImageView
    private lateinit var drawerUserAvatar: ImageView
    private lateinit var drawerUserName: TextView
    private lateinit var drawerUserCredit: TextView
    private lateinit var drawerUserUEC: TextView
    private lateinit var drawerUserREC: TextView
    private lateinit var drawerUserHangerValue: TextView

    private var  density: Float = 0f

    lateinit var searchButton: ImageView

    enum class FragmentType(val value: Int) {
        SHOPPING(0),
        MAIN(2),
        HANGER(1),
        ME(3)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() //隐藏标题栏

        val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)

        val primaryUserId = sharedPreferences.getInt(getString(R.string.primary_user_key), 0)

        val database = getDatabase(application)

        val currentUser: LiveData<User> = database.userDao.getById(primaryUserId)

        QMUIStatusBarHelper.translucent(this)
//        QMUIStatusBarHelper.setStatusBarLightMode(this)
        QMUIStatusBarHelper.getStatusbarHeight(this)
         //设置状态栏透明

        mMovingBar = findViewById(R.id.bottom_moving_bar) //底部滑动条
        mPager = findViewById(R.id.pager) //ViewPager
        bottomShopIcon = findViewById(R.id.bottom_shop_icon) //底部购物车图标
        bottomHangerIcon = findViewById(R.id.bottom_hanger_icon) //底部挂件图标
        bottomMainIcon = findViewById(R.id.bottom_main_icon) //底部主页图标
        bottomMeIcon = findViewById(R.id.bottom_me_icon) //底部我的图标
        searchButton = findViewById(R.id.search_icon) //搜索按钮
        userAvatar = findViewById(R.id.user_avatar) //用户头像
        drawerUserAvatar = findViewById(R.id.drawer_avatar_image) //用户头像
        drawerUserName = findViewById(R.id.drawer_handle_name) //用户名
        drawerUserCredit = findViewById(R.id.drawer_store_value) //用户积分
        drawerUserUEC = findViewById(R.id.drawer_uec_value) //用户UEC
        drawerUserREC = findViewById(R.id.drawer_rec_value) //用户REC
        drawerUserHangerValue = findViewById(R.id.drawer_hanger_value) //用户机库价值
        val drawerLayout: DrawerLayout = findViewById(R.id.root_drawer) //滑动菜单


        bottomShopIcon.setColorFilter(Color.GRAY)
        bottomHangerIcon.setColorFilter(Color.GRAY)
        bottomMainIcon.setColorFilter(getColor(R.color.bottom_icon_selected_color))
        mMovingBar.setBackgroundColor(getColor(R.color.bottom_icon_selected_color))

        currentUser.observe(this) {
            if (it != null) {
                setRSICookie(it.rsi_token, it.rsi_device)
                loadUserAvatar(userAvatar, it.profile_image)
                loadUserAvatar(bottomMeIcon, it.profile_image)
                loadUserAvatar(drawerUserAvatar, it.profile_image)
                val userCredit = "${it.store.toFloat() / 100.0f} USD"
                val userUEC = "${it.uec} UEC"
                val userREC = "${it.rec} REC"
                val userHangerValue = "${it.hanger_value.toFloat() / 100.0f} USD"
                drawerUserName.text = it.handle
                drawerUserCredit.text = userCredit
                drawerUserUEC.text = userUEC
                drawerUserREC.text = userREC
                drawerUserHangerValue.text = userHangerValue
            }
        }

        userAvatar.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //设置底部图标的点击事件
        searchButton.setOnClickListener(View.OnClickListener {
            val searchFragment = SearchFragment.newInstance()
            searchFragment.show(supportFragmentManager, "search")
            searchFragment.setOnSearchClickListener {
                keyword -> Toast.makeText(this, keyword, Toast.LENGTH_SHORT).show()
            }
        })

//        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.app_name; identifier = 1 }
//
//        slider.itemAdapter.add(item1)



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
        bottomMeIcon.setOnClickListener {
            mPager.currentItem = FragmentType.ME.value
        }

        density = resources.displayMetrics.density

        val shopFragment = ShoppingFragment.newInstance()
        val homeFragment = HomeFragment.newInstance()
        val meFragment = MeFragment.newInstance()
        val hangerFragment = MainFragment.newInstance()

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        val fragment_list: MutableList<Fragment> = mutableListOf(shopFragment, homeFragment, hangerFragment, meFragment)
        pagerAdapter.setList(fragment_list)
        mPager.adapter = pagerAdapter
        mPager.setCurrentItem(2, false)
        mPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mMovingBar.x = (mPager.width * positionOffset + position * mPager.width) / 4 + mPager.width / 8 - 20 * density //设置滑动条的位置
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                when(position) {
                    FragmentType.SHOPPING.value -> {
                        bottomShopIcon.setColorFilter(getColor(R.color.bottom_icon_selected_color))
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(Color.GRAY)
                    }
                    FragmentType.HANGER.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(getColor(R.color.bottom_icon_selected_color))
                        bottomMainIcon.setColorFilter(Color.GRAY)
                    }
                    FragmentType.MAIN.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(getColor(R.color.bottom_icon_selected_color))
                    }
                    FragmentType.ME.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(Color.GRAY)
                    }
                }
                super.onPageSelected(position)
            }
        })

        val filterButton = findViewById<ImageView>(R.id.filter_icon)

        val shoppingViewModel = ViewModelProvider(this).get(ShoppingViewModel::class.java)

        filterButton.setOnClickListener {
            when(mPager.currentItem) {
                FragmentType.SHOPPING.value -> {
                    val itemTypes = listOf("单船", "涂装", "装备", "附加包", "信用点", "UEC", "礼品卡")
                    val builder = QMUIDialog.MultiCheckableDialogBuilder(this)
                    builder.setTitle("请选择商品种类")
                        .setCheckedItems(arrayOf(0).toIntArray())
                        .addItems(itemTypes.toTypedArray(), null)
                        .addAction("取消") { dialog, index -> dialog.dismiss() }
                        .addAction("提交") { dialog, index ->
                            val filterList = mutableListOf<String>()
                            builder.checkedItemIndexes.forEach {
                                when(it) {
                                    0 -> filterList.add(ShopItemType.SHIP.itemName)
                                    1 -> filterList.add(ShopItemType.PAINT.itemName)
                                    2 -> filterList.add(ShopItemType.GEAR.itemName)
                                    3 -> filterList.add(ShopItemType.PACKS.itemName)
                                    4 -> filterList.add(ShopItemType.ADDON.itemName)
                                    5 -> filterList.add(ShopItemType.CREDITS.itemName)
                                    6 -> filterList.add(ShopItemType.UEC.itemName)
                                    7 -> filterList.add(ShopItemType.GIFT.itemName)
                                }
                                shoppingViewModel.setFilter(filterList)
                            }
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

}