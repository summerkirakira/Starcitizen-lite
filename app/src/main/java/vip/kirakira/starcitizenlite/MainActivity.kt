package vip.kirakira.starcitizenlite

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.github.vipulasri.timelineview.TimelineView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ImmersionBar
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.tapadoo.alerter.Alerter
import com.wyt.searchbox.SearchFragment
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import vip.kirakira.starcitizenlite.activities.LoginActivity
import vip.kirakira.starcitizenlite.activities.RefugeBaseActivity
import vip.kirakira.starcitizenlite.activities.SettingsActivity
import vip.kirakira.starcitizenlite.database.HangarLog
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.network.*
import vip.kirakira.starcitizenlite.network.CirnoProperty.ClientInfo
import vip.kirakira.starcitizenlite.network.CirnoProperty.RefugeInfo
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipUpgradePathPostBody
import vip.kirakira.starcitizenlite.network.shop.getCartSummary
import vip.kirakira.starcitizenlite.ui.ScreenSlidePagerAdapter
import vip.kirakira.starcitizenlite.ui.hangarlog.HangarLogBottomSheet
import vip.kirakira.starcitizenlite.ui.hangarlog.HangarLogViewModel
import vip.kirakira.starcitizenlite.ui.home.HomeFragment
import vip.kirakira.starcitizenlite.ui.home.HomeViewModel
import vip.kirakira.starcitizenlite.ui.home.Parser
import vip.kirakira.starcitizenlite.ui.loadUserAvatar
import vip.kirakira.starcitizenlite.ui.main.MainFragment
import vip.kirakira.starcitizenlite.ui.me.MeFragment
import vip.kirakira.starcitizenlite.ui.shopping.ShopItemFilter
import vip.kirakira.starcitizenlite.ui.widgets.RefugeVip
import vip.kirakira.viewpagertest.network.graphql.LoginBody
import vip.kirakira.viewpagertest.network.graphql.RsiLauncherSignInBody
import vip.kirakira.viewpagertest.ui.shopping.ShoppingFragment
import vip.kirakira.viewpagertest.ui.shopping.ShoppingViewModel
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread


var  PAGE_NUM = 4;

lateinit var shipAlias: List<ShipAlias>

class MainActivity : RefugeBaseActivity() {
    private lateinit var mPager: ViewPager2
    private lateinit var mMovingBar: View
    private lateinit var bottomShopIcon: ImageView
    private lateinit var bottomHangerIcon: ImageView
    private lateinit var bottomMainIcon: ImageView
    private lateinit var bottomMeIcon: ImageView
    private lateinit var userAvatar: ImageView
    private lateinit var drawerUserAvatar: AvatarView
    private lateinit var drawerUserName: TextView
    private lateinit var drawerUserCredit: TextView
    private lateinit var drawerUserUEC: TextView
    private lateinit var drawerUserREC: TextView
    private lateinit var drawerUserHangerValue: TextView
    private lateinit var firstLine: QMUIRoundButton
    private lateinit var secondLine: QMUIRoundButton
    private lateinit var thirdLine: QMUIRoundButton
    private lateinit var switchAccount: ConstraintLayout
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var shoppingViewModel: ShoppingViewModel
    private lateinit var hangarLogViewModel: HangarLogViewModel
    private lateinit var feedbackButton: ConstraintLayout
    private lateinit var settingsButton: ConstraintLayout
    private lateinit var logoutButton: ConstraintLayout
    lateinit var  shipUpgradeButton: ImageView


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

        shoppingViewModel = ViewModelProvider(this).get(ShoppingViewModel::class.java)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        hangarLogViewModel = ViewModelProvider(this).get(HangarLogViewModel::class.java)

        val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)

        var primaryUserId = sharedPreferences.getInt(getString(R.string.primary_user_key), 0)

        val database = getDatabase(application)

        val currentUser: LiveData<User> = database.userDao.getById(primaryUserId)

        val allUsers: LiveData<List<User>> = database.userDao.getAll()

        var typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)

        val colorPrimary = typedValue.data



        allUsers.observe(this) {

        }

//        QMUIStatusBarHelper.translucent(this)
//        QMUIStatusBarHelper.setStatusBarLightMode(this)
//        QMUIStatusBarHelper.getStatusbarHeight(this)
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
        switchAccount = findViewById(R.id.switch_account_layout) //切换账号按钮
        feedbackButton = findViewById(R.id.drawer_feedback_loyout) //反馈按钮
        settingsButton = findViewById(R.id.drawer_settings_layout) //设置按钮
        logoutButton = findViewById(R.id.drawer_logout_loyout) //登出按钮
        val drawerLayout: DrawerLayout = findViewById(R.id.root_drawer) //滑动菜单
        firstLine = findViewById(R.id.avatar_first_line)
        secondLine = findViewById(R.id.avatar_second_line)
        thirdLine = findViewById(R.id.avatar_third_line)
        val filterButton = findViewById<ImageView>(R.id.filter_icon)
        shipUpgradeButton = findViewById(R.id.ship_upgrade_icon)

        val immersionBar = ImmersionBar.with(this)



        bottomShopIcon.setColorFilter(Color.GRAY)
        bottomHangerIcon.setColorFilter(Color.GRAY)
        if(primaryUserId == 0) bottomMeIcon.setColorFilter(Color.GRAY)
        bottomMainIcon.setColorFilter(colorPrimary)
        mMovingBar.setBackgroundColor(colorPrimary)

        currentUser.observe(this) {
            if (it != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
//                        CirnoApi.getShipDetail("https://image.biaoju.site/starcitizen/ship_detail.0.1.json")
                        CirnoApi.retrofitService.updateClientInfo(
                            ClientInfo(
                                primaryUser = it.handle
                            )
                        )
                    } catch (e: Exception) {
                        Log.d("Cirno", "Error updating client info", e)
                    }
                }

            }
        }

        currentUser.observe(this) {
            if (it != null) {
                setRSICookie(it.rsi_token, it.rsi_device)
                sharedPreferences.edit().putString(getString(R.string.device_id_key), it.rsi_device).commit()
                if("default" !in it.profile_image) {
                    drawerUserAvatar.loadImage(it.profile_image)
                    loadUserAvatar(userAvatar, it.profile_image)
                    loadUserAvatar(bottomMeIcon, it.profile_image)
                } else {
                    drawerUserAvatar.loadImage("https://cdn.robertsspaceindustries.com/static/images/account/avatar_default_big.jpg")
                    loadUserAvatar(userAvatar, "https://cdn.robertsspaceindustries.com/static/images/account/avatar_default_big.jpg")
                    loadUserAvatar(bottomMeIcon, "https://cdn.robertsspaceindustries.com/static/images/account/avatar_default_big.jpg")
                }

                val userCredit = "${Parser.priceFormatter(it.store)} USD"
                val userUEC = "${it.uec} UEC"
                val userREC = "${it.rec} REC"
                val userHangerValue = "${Parser.priceFormatter(it.hanger_value)} USD"
                drawerUserName.text = it.handle
                drawerUserCredit.text = userCredit
                drawerUserUEC.text = userUEC
                drawerUserREC.text = userREC
                drawerUserHangerValue.text = userHangerValue
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        try {
                            getRSIToken(it.rsi_token, it.rsi_device)
                            while (csrf_token.isEmpty()) {
                                Thread.sleep(100)
                            }
                            val data = getCartSummary()
                            if(data.data.account.isAnonymous) {
                                throw Exception("Anonymous")
                            }
                            return@launch
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "检测到IP变动，RSI账号登录失效，正在尝试重新登录...", Toast.LENGTH_LONG).show()
                            }
                        }
                        try {
                            val reLogin = RSIApi.retrofitService.rsiLauncherSignIn(
                                RsiLauncherSignInBody(
                                    it.email,
                                    it.password
                                )
                            )
                            if (reLogin.data != null) {
                                try{
                                    val newUser = saveUserData(
                                        uid = reLogin.data.account_id,
                                        rsi_device = it.rsi_device,
                                        rsi_token = reLogin.data.session_id,
                                        email = it.email,
                                        password = it.password)
                                    setRSICookie(newUser.rsi_token, newUser.rsi_device)
                                    database.userDao.insert(newUser)
                                    sharedPreferences.edit().putInt(getString(R.string.primary_user_key), reLogin.data.account_id).apply()
                                    primaryUserId = reLogin.data.account_id
                                    val alerter = createSuccessAlerter(this@MainActivity, "重新登录成功", "检测到IP变动，自动登录成功")
                                    alerter
                                        .enableSwipeToDismiss()
                                        .setOnClickListener {
                                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .setOnHideListener {
                                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .show()
                                    return@launch
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
//                            getRSIToken()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            getRSIToken()
                            val token = CirnoApi.getReCaptchaV3(1)[0]
                            val response = RSIApi.retrofitService.login(
                                LoginBody(
                                    variables = LoginBody.Variables(
                                        email = it.email,
                                        password = it.password,
                                        captcha = token
                                    )
                                )
                            )
                            val loginDetail = response.body()!!
                            if (loginDetail.errors == null) {
                                val headers = response.headers().toMultimap()["set-cookie"]!!
                                for (header in headers) {
                                    if (header.contains("Rsi-Token")) {
                                        rsi_token = header.split(";")[0].split("=")[1]
                                    }
                                }
                                val newUser = saveUserData(
                                    loginDetail.data.account_signin!!.id,
                                    rsi_device,
                                    rsi_token,
                                    it.email,
                                    it.password
                                )
                                Thread {
                                    val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                                    with(pref.edit()) {
                                        putInt(getString(R.string.primary_user_key), loginDetail.data.account_signin.id)
                                        putBoolean(getString(R.string.is_log_in), true)
                                        apply()
                                    }
                                    database.userDao.insert(newUser)
                                    createSuccessAlerter(
                                        this@MainActivity,
                                        "检测到IP变动",
                                        "自动登录成功"
                                    )
                                        .enableSwipeToDismiss()
                                        .setOnClickListener {
                                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .setOnHideListener {
                                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .show()
                                }.start()
                                return@launch
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        sharedPreferences.edit().putInt(getString(R.string.primary_user_key), 0).apply()
                        database.userDao.delete(primaryUserId)
                        val alerter = createWarningAlerter(this@MainActivity, "警告", "RSI账号登录失效，点击此处重新登录")
                        alerter
                            .enableSwipeToDismiss()
                            .setOnClickListener {
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .setOnHideListener {
                                val intent = Intent(this@MainActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    }
                }
            else {
                val rsiDevice = sharedPreferences.getString(getString(R.string.device_id_key), "")
                Log.d("Cirno", "RSI Device: $rsiDevice")
                if (rsiDevice != null) {
                    setRSICookie("", rsiDevice)
                }
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
                keyword -> when(mPager.currentItem){
                    FragmentType.SHOPPING.value -> {
                        shoppingViewModel.setFilter(ShopItemFilter(keyword, listOf("")))
                    }
                    FragmentType.HANGER.value -> {
                        homeViewModel.setFilter(keyword)
                    }
                }
            }
        })



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

        density = resources!!.displayMetrics.density

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
                        bottomShopIcon.setColorFilter(colorPrimary)
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(Color.GRAY)
                        if(primaryUserId == 0) bottomMeIcon.setColorFilter(Color.GRAY)
                        searchButton.setColorFilter(getColor(R.color.avatar_left_line))
                        filterButton.setImageDrawable(getDrawable(R.drawable.ic_filter))
                        filterButton.setColorFilter(getColor(R.color.avatar_left_line))
                        filterButton.visibility = View.VISIBLE

                        if(shoppingViewModel.currentUpgradeStage.value == ShoppingViewModel.UpgradeStage.UNDEFINED) {
                            shipUpgradeButton.setColorFilter(getColor(R.color.avatar_left_line))
                        } else {
                            shipUpgradeButton.setColorFilter(getColor(R.color.upgrade_is_selected))
                        }
                        shipUpgradeButton.visibility = View.VISIBLE
                        shipUpgradeButton.setImageDrawable(getDrawable(R.drawable.ic_ship_upgrade))
                        setAvatarLine(ColorStateList.valueOf(getColor(R.color.avatar_left_line)))
                        immersionBar.statusBarDarkFont(true).init()
                    }
                    FragmentType.HANGER.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(colorPrimary)
                        bottomMainIcon.setColorFilter(Color.GRAY)
                        if(primaryUserId == 0) bottomMeIcon.setColorFilter(Color.GRAY)
                        filterButton.setImageDrawable(getDrawable(R.drawable.ic_exchange))
                        searchButton.setColorFilter(getColor(R.color.avatar_left_line))
                        filterButton.setColorFilter(getColor(R.color.avatar_left_line))
                        filterButton.visibility = View.VISIBLE

                        shipUpgradeButton.setImageDrawable(getDrawable(R.drawable.baseline_history_24))
                        shipUpgradeButton.setColorFilter(getColor(R.color.avatar_left_line))
                        shipUpgradeButton.visibility = View.VISIBLE

                        setAvatarLine(ColorStateList.valueOf(getColor(R.color.avatar_left_line)))
                        immersionBar.statusBarDarkFont(true).init()
                    }
                    FragmentType.MAIN.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(colorPrimary)
                        if(primaryUserId == 0) bottomMeIcon.setColorFilter(Color.GRAY)
                        searchButton.setColorFilter(Color.WHITE)
                        setAvatarLine(ColorStateList.valueOf(Color.WHITE))
                        filterButton.visibility = View.GONE

                        shipUpgradeButton.visibility = View.GONE

                        immersionBar.statusBarDarkFont(false).init()
                    }
                    FragmentType.ME.value -> {
                        bottomShopIcon.setColorFilter(Color.GRAY)
                        bottomHangerIcon.setColorFilter(Color.GRAY)
                        bottomMainIcon.setColorFilter(Color.GRAY)
                        if(primaryUserId == 0) bottomMeIcon.setColorFilter(colorPrimary)
                        filterButton.visibility = View.GONE

                        shipUpgradeButton.visibility = View.GONE

                        setAvatarLine(ColorStateList.valueOf(Color.WHITE))
                        immersionBar.statusBarDarkFont(false).init()
                    }
                }
                super.onPageSelected(position)
            }
        })


//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)




        filterButton.setOnClickListener {
            when(mPager.currentItem) {
                FragmentType.SHOPPING.value -> {
                    val itemTypes = listOf("单船", "涂装", "装备", "附加包", "UEC", "礼品卡", "资格包", "组合包")
                    val builder = QMUIDialog.MultiCheckableDialogBuilder(this)
                    builder.setTitle("请选择商品种类")
                        .setCheckedItems(arrayOf(0).toIntArray())
                        .addItems(itemTypes.toTypedArray(), null)
                        .addAction("取消") { dialog, index -> dialog.dismiss() }
                        .addAction("确认") { dialog, index ->
                            val filterList = mutableListOf<String>()
                            builder.checkedItemIndexes.forEach {
                                when(it) {
                                    0 -> filterList.add(ShopItemType.SHIP.itemName)
                                    1 -> filterList.add(ShopItemType.PAINT.itemName)
                                    2 -> filterList.add(ShopItemType.GEAR.itemName)
                                    3 -> filterList.add(ShopItemType.ADDON.itemName)
                                    4 -> filterList.add(ShopItemType.UEC.itemName)
                                    5 -> filterList.add(ShopItemType.GIFT.itemName)
                                    6 -> filterList.add(ShopItemType.PACKAGE.itemName)
                                    7 -> filterList.add(ShopItemType.PACKS.itemName)
                                }
                                shoppingViewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.UNDEFINED
                                shipUpgradeButton.setColorFilter(getColor(R.color.avatar_left_line))
                                shoppingViewModel.setFilter(ShopItemFilter("", filterList))
                            }
                            dialog.dismiss()
                        }
                        .show()
                }
                FragmentType.HANGER.value -> {
                    if(homeViewModel.currentMode.value == HomeViewModel.Mode.HANGER) {
                        homeViewModel.currentMode.value = HomeViewModel.Mode.BUYBACK
                        homeViewModel.refreshBuybackItems()
                    } else {
                        homeViewModel.currentMode.value = HomeViewModel.Mode.HANGER
                        homeViewModel.refresh()
                    }
                }
            }
        }

        shipUpgradeButton.setOnClickListener {
            when(mPager.currentItem) {
                FragmentType.SHOPPING.value -> {
                    shoppingViewModel.isDetailShowing.value = false
                    if (shoppingViewModel.currentUpgradeStage.value == ShoppingViewModel.UpgradeStage.UNDEFINED) {
                        shoppingViewModel.setFilter(ShopItemFilter("", listOf("Upgrade"), onlyCanUpgradeTo = true))
                        shoppingViewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.CHOOSE_TO_SHIP
                        shipUpgradeButton.setColorFilter(getColor(R.color.upgrade_is_selected))
                    } else {
                        shoppingViewModel.setFilter(ShopItemFilter("", listOf("Standalone Ship")))
                        shoppingViewModel.currentUpgradeStage.value = ShoppingViewModel.UpgradeStage.UNDEFINED
                        shipUpgradeButton.setColorFilter(getColor(R.color.avatar_left_line))
                    }
                }
                FragmentType.HANGER.value -> {
//                    if(RefugeVip.isVip())
                    HangarLogBottomSheet.showDialog(supportFragmentManager)

                }
            }

        }

        switchAccount.setOnClickListener {
            if (allUsers.value != null) {
                var items = allUsers.value!!.map { it.handle }.toTypedArray()
                items += "登陆/注册新账号"
                val builder = QMUIDialog.CheckableDialogBuilder(this)
                builder.setTitle("请选择操作")
                    .setCheckedIndex(items.size - 1)
                    .addItems(items) { dialog, which -> builder.checkedIndex = which }
                    .addAction("确定") { dialog, index ->
                        thread {
                            database.shopItemDao.deleteAll()
                            database.buybackItemDao.deleteAllOldItems(System.currentTimeMillis())
                            database.hangarLogDao.deleteAll()
                            sharedPreferences.edit().putInt(getString(R.string.crawled_page_key), 0).apply()
                            sharedPreferences.edit().putInt(getString(R.string.current_hanger_value_key), 0).apply()
                        }
                        if(builder.checkedIndex == items.size - 1){
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            dialog.dismiss()
                            primaryUserId = allUsers.value!![builder.checkedIndex].id
                            sharedPreferences.edit().putInt(getString(R.string.primary_user_key), primaryUserId).apply()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }

                    }
                    .show()
            }
        }

        feedbackButton.setOnClickListener {
            joinQQGroup()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }, 1000)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }

        logoutButton.setOnClickListener {
            if (currentUser.value == null) {
                Toast.makeText(this, "当前未登录账号哟~", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val builder = QMUIDialog.MessageDialogBuilder(this)
            builder.setTitle("确认登出")
                .setMessage("确认要从避难所登出并删除账号${currentUser.value!!.handle}吗？")
                .addAction("取消") { dialog, index -> dialog.dismiss() }
                .addAction("确认") { dialog, index ->
                    thread {
                        database.userDao.delete(primaryUserId)
                    }
                    dialog.dismiss()
                    Toast.makeText(this, "登出成功...", Toast.LENGTH_SHORT).show()
                    sharedPreferences.edit().putInt(getString(R.string.primary_user_key), 0).apply()
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .show()
        }


        if(sharedPreferences.getBoolean(getString(R.string.CHECK_UPDATE_KEY), true)) {
            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    getRSIToken()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
                try {
                    loadInitialData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    checkStartUp()
                    checkUpdate()
                    checkSubscription()
                    checkAnnouncement(sharedPreferences.getInt(getString(R.string.CURRENT_ANNOUNCEMENT_ID), 0))
                    sharedPreferences.edit().putBoolean(getString(R.string.CHECK_UPDATE_KEY), false).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val result = ApkUtil.deleteOldApk(this, "${externalCacheDir?.path}/refuge_update.apk")
        }
    }

    private fun loadInitialData() {
        shipAlias = loadAliasFromStorage()
        translateShipName(shipAlias)
    }

    private fun loadAliasFromStorage(): List<ShipAlias> {
        val file = File("${filesDir?.path}/ship_alias.json")
        if(file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<ShipAlias>>() {}.type
            return Gson().fromJson(json, type)
        } else {
            return listOf()
        }
    }

    private fun translateShipName(shipAlias: List<ShipAlias>) {
        for (ship in shipAlias) {
            if (ship.chineseName == null) {
                ship.chineseName = translateShipName(ship.name)
            }
        }
    }

    private fun translateShipName(name: String): String {
        getDatabase(application).translationDao.getByEnglishTitle(name)?.let {
            return it.title
        }
        return name
    }

    private suspend fun checkUpdate() {
        val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        val latestVersion = CirnoApi.retrofitService.getVersion(
            RefugeInfo(
                version = BuildConfig.VERSION_NAME,
                androidVersion = android.os.Build.VERSION.RELEASE,
                systemModel = android.os.Build.MODEL
            )
        )

        if(compareVersion(latestVersion.version, BuildConfig.VERSION_NAME)) {
            val manager = DownloadManager.Builder(this).run {
                apkUrl(latestVersion.url)
                apkName("refuge_update.apk")
                smallIcon(R.mipmap.ic_launcher)
                apkDescription("更新星河避难所...")
                //省略一些非必须参数...
                build()
            }
            manager.download()
        }
        try {
            updateAlias(latestVersion.shipAliasUrl)
            homeViewModel.refresh()
            homeViewModel.refreshBuybackItems()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val shipDetailVersion = sharedPreferences.getString(getString(R.string.SHIP_DETAIL_VERSION_KEY), "0.0.0")
        if(compareVersion(latestVersion.shipDetailVersion, shipDetailVersion!!)) {
            Log.d("ShipDetail", "Updating ship detail...")
            val shipDetails = CirnoApi.getShipDetail(latestVersion.shipDetailUrl)
            val gameTranslations = CirnoApi.getGameTranslation("https://image.biaoju.site/starcitizen/localization.${latestVersion.shipDetailVersion}.json")
            if(shipDetails.isNotEmpty()) {
                val database = getDatabase(application)
                database.shipDetailDao.insertAll(shipDetails)
                sharedPreferences.edit().putString(getString(R.string.SHIP_DETAIL_VERSION_KEY), latestVersion.shipDetailVersion).apply()
            }
            if (gameTranslations.isNotEmpty()) {
                val database = getDatabase(application)
                database.gameTranslationDao.insertAll(gameTranslations)
                sharedPreferences.edit().putBoolean(getString(R.string.GAME_TRANSLATION_KEY), true).apply()
                Toast.makeText(this, "游戏翻译数据已更新", Toast.LENGTH_SHORT).show()
            }
        }
        if(!sharedPreferences.getBoolean(getString(R.string.GAME_TRANSLATION_KEY), false)) {
            val database = getDatabase(application)
            val gameTranslations = CirnoApi.getGameTranslation("https://image.biaoju.site/starcitizen/localization.${latestVersion.shipDetailVersion}.json")
            database.gameTranslationDao.insertAll(gameTranslations)
            sharedPreferences.edit().putBoolean(getString(R.string.GAME_TRANSLATION_KEY), true).apply()
            Toast.makeText(this, "游戏翻译数据已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAlias(url: String) {
        shipAlias = CirnoApi.getShipAliasFromUrl(url)
        if (shipAlias.isNotEmpty()) {
            val file = File("${filesDir?.path}/ship_alias.json")
            file.writeText(Gson().toJson(shipAlias))
        }
        translateShipName(shipAlias)
    }

    private suspend fun checkAnnouncement(currentAnnouncementId: Int = 0) {
        val latestAnnouncement = CirnoApi.retrofitService.getAnnouncement()
//        val shipUpgrade = CirnoApi.retrofitService.getUpgradePath(
//            ShipUpgradePathPostBody(
//                from_ship_id = 1,
//                to_ship_id = 37,
//                banned_list = listOf(),
//                hangar_upgrade_list = listOf(),
//                buyback_upgrade_list = listOf(),
//                use_history_ccu = true,
//                only_can_buy_ships = true,
//                upgrade_multiplier = 1.2f
//            )
//        )
//        Log.d("ShipUpgrade", shipUpgrade.toString())
        if(latestAnnouncement != null && latestAnnouncement.id > currentAnnouncementId) {
            this.runOnUiThread {
                val builder = QMUIDialog.MessageDialogBuilder(this)
                builder.setTitle(latestAnnouncement.title)
                    .setMessage(latestAnnouncement.content)
                    .addAction("确定") { dialog, index ->
                        dialog.dismiss()
                        getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).edit().putInt(getString(R.string.CURRENT_ANNOUNCEMENT_ID), latestAnnouncement.id).apply()
                    }
                    .show()
            }
        }
    }

    private suspend fun checkStartUp() {
        val preference = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        if (preference.getString(getString(R.string.UUID), "DEFAULT")!! == "DEFAULT") {
            val uniqueID = UUID.randomUUID().toString()
            preference.edit().putString(getString(R.string.UUID), uniqueID).apply()
            uuid = uniqueID
        } else {
            uuid = preference.getString(getString(R.string.UUID), "DEFAULT")!!
        }
        if(preference.getBoolean(getString(R.string.FIRST_START_KEY), true)) {
            val startUpMessage = CirnoApi.retrofitService.getStartup()
            val uniqueID = UUID.randomUUID().toString()
            preference.edit().apply {
                putString(getString(R.string.UUID), uniqueID)
                putBoolean(getString(R.string.enable_localization_key), true)
                putBoolean(getString(R.string.AUTO_ADD_CREDITS_KEY), true)
            }.apply()
            uuid = uniqueID

            this.runOnUiThread {
                val builder = QMUIDialog.MessageDialogBuilder(this)
                builder.setTitle(startUpMessage.title)
                    .setMessage(startUpMessage.content)
                    .addAction("取消") { dialog, index ->
                        dialog.dismiss()
                        finish()
                    }
                    .addAction("确定") { dialog, index ->
                        dialog.dismiss()
                        preference.edit().putBoolean(getString(R.string.FIRST_START_KEY), false).apply()
                    }
                    .show()
            }
        } else {
            uuid = preference.getString(getString(R.string.UUID), "DEFAULT")?: "DEFAULT"
        }
    }


    fun setAvatarLine(colorStateList: ColorStateList) {
        firstLine.setBgData(colorStateList)
        firstLine.setStrokeData(4, colorStateList)
        secondLine.setBgData(colorStateList)
        secondLine.setStrokeData(4, colorStateList)
        thirdLine.setBgData(colorStateList)
        thirdLine.setStrokeData(4, colorStateList)
    }

    override fun onBackPressed() {
        when(mPager.currentItem) {
            FragmentType.HANGER.value -> {
                if (homeViewModel.isDetailShowing.value == true){
                    homeViewModel.isDetailShowing.value = false
                    return
                }
            }
            FragmentType.SHOPPING.value -> {
                if (shoppingViewModel.isDetailShowing.value == true){
                    shoppingViewModel.isDetailShowing.value = false
                    return
                }
            }
        }
        super.onBackPressed()
    }


    override fun getResources(): Resources? {
        val res: Resources = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.getDisplayMetrics())
        return res
    }


    /****************
     *
     * 发起添加群流程。群号：星河避难所(696608010) 的 key 为： LzRUVOyetWjRlVyNh24tN2gU8KZZvDcB
     * 调用 joinQQGroup(LzRUVOyetWjRlVyNh24tN2gU8KZZvDcB) 即可发起手Q客户端申请加群 星河避难所(696608010)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(): Boolean {
        val intent = Intent()
        val key = "LzRUVOyetWjRlVyNh24tN2gU8KZZvDcB"
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: java.lang.Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    private suspend fun checkSubscription() {
            val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
            val latestVersion = CirnoApi.retrofitService.getVersion(
                RefugeInfo(
                    version = BuildConfig.VERSION_NAME,
                    androidVersion = android.os.Build.VERSION.RELEASE,
                    systemModel = android.os.Build.MODEL
                )
            )
            val isVip = sharedPreferences.getBoolean(getString(R.string.IS_VIP), false)

            sharedPreferences.edit().apply {
                putBoolean(getString(R.string.IS_VIP), latestVersion.isVip)
                putInt(getString(R.string.VIP_EXPIRE), latestVersion.vipExpire)
                putInt(getString(R.string.TOTAL_VIP_TIME), latestVersion.totalVipTime)
                putInt(getString(R.string.REFUGE_CREDIT), latestVersion.credit)
                apply()
            }
            if (isVip != latestVersion.isVip) {
//            if (latestVersion.isVip) {
//                Alerter.create(this)
//                    .setTitle("星河避难所 Premium已激活")
//                    .setText("有效期至${Date(Date().time + latestVersion.vipExpire.toLong() * 1000).toLocaleString()}")
//                    .setBackgroundColorRes(R.color.alerter_default_success_background)
//                    .show()
//            } else {
//                Alerter.create(this)
//                    .setTitle("星河避难所 Premium已到期")
//                    .setText("您的Premium有效期至${Date(Date().time + latestVersion.vipExpire.toLong() * 1000).toLocaleString()}")
//                    .setBackgroundColorRes(R.color.alert_dialog_background_failure)
//                    .show()
//            }
                //更新主界面
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
            }

    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibratorEffect = android.os.VibrationEffect.createOneShot(50, 70)
            vibrator.vibrate(vibratorEffect)
        }
    }
}