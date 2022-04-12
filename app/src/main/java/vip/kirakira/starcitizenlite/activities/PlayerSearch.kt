package vip.kirakira.starcitizenlite.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import coil.clear
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.tapadoo.alerter.Alerter
import com.wyt.searchbox.SearchFragment
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.network.search.getPlayerSearchResult
import kotlin.concurrent.thread

class PlayerSearch : AppCompatActivity() {

    val scope = CoroutineScope(Job() + Dispatchers.Main)
    lateinit var avatar: AvatarView
    lateinit var organizationImage: AvatarView
    lateinit var handleText: TextView
    lateinit var nameText: TextView
    lateinit var registerText: TextView
    lateinit var fleetText: TextView
    lateinit var organizationRankText: TextView
    lateinit var registerLocationText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_search)
        avatar = findViewById(R.id.avatar)
        organizationImage = findViewById(R.id.organization_image)
        handleText = findViewById(R.id.handle)
        nameText = findViewById(R.id.name)
        registerText = findViewById(R.id.register_date_value)
        fleetText = findViewById(R.id.organization_name_value)
        organizationRankText = findViewById(R.id.organization_position_value)
        registerLocationText = findViewById(R.id.register_location_value)
        supportActionBar?.hide()
        QMUIStatusBarHelper.translucent(this)
//        QMUIStatusBarHelper.setStatusBarLightMode(this)
        QMUIStatusBarHelper.getStatusbarHeight(this)
        //设置状态栏透明

        val searchButton: ImageView = findViewById(R.id.top_search)
        searchButton.setOnClickListener {
            val searchFragment = SearchFragment.newInstance()
            searchFragment.show(supportFragmentManager, "search")
            searchFragment.setOnSearchClickListener {
                    keyword -> thread {
                        val player = getPlayerSearchResult(keyword)
                runOnUiThread {
                    if (player == null) {
                        Alerter.create(this@PlayerSearch)
                            .setTitle("搜索失败")
                            .setText("没有找到玩家")
                            .setBackgroundColorRes(R.color.alert_dialog_background_failure)
                            .show()
                        return@runOnUiThread
                    }
                    handleText.text = player.handle
                    nameText.text = player.name
                    avatar.loadImage("https://robertsspaceindustries.com/${player.image}")
                    if (player.organization != null) {
                        organizationImage.loadImage("https://robertsspaceindustries.com/${player.organization.logo}")
                        fleetText.text = player.organization.name
                        val rankText = "${player.organization.rankName}(${player.organization.rank}级权限)"
                        organizationRankText.text = rankText
                    } else if (player.isHidden) {
                        fleetText.text = "[数据删除]"
                        organizationRankText.text = "[数据删除]"
                        organizationImage.clear()
                    } else {
                        fleetText.text = "无"
                        organizationRankText.text = "无"
                    }
                    registerText.text = player.enlisted
                    registerLocationText.text = player.location
                }
            }
            }
        }

    }
}