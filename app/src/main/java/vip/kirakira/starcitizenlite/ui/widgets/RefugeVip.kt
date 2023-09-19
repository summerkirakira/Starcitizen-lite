package vip.kirakira.starcitizenlite.ui.widgets

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.tapadoo.alerter.Alerter
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.RefugeApplication
import vip.kirakira.starcitizenlite.network.CirnoApi

class RefugeVip {
    companion object {
        fun createWarningAlert(activity: Activity, title: String = "缺少有效的避难所Premium订阅", detail: String = "点击此处解锁更多功能", button: String = "立即解锁") {
            Alerter.create(activity)
                .setTitle(title)
                .setText(detail)
                .setBackgroundColorRes(R.color.refuge_subscribe_more)
                .setIcon(R.drawable.ic_lack_refuge_vip)
                .setIconColorFilter(0)
                .setDuration(5000)
                .setOnClickListener {
                    val uri = Uri.parse(CirnoApi.getSubscribeUrl())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(activity, intent, null)
                }
                .show()
        }

        fun createTokenNotSufficientWarningAlert(activity: Activity, title: String = "缺少足够的避难所Token点数", detail: String = "点此获取更多token点数", button: String = "点此获取更多") {
            Alerter.create(activity)
                .setTitle(title)
                .setText(detail)
                .setBackgroundColorRes(R.color.refuge_subscribe_more)
                .setIcon(R.drawable.ic_lack_refuge_vip)
                .setIconColorFilter(0)
                .setDuration(5000)
                .setOnClickListener {
                    val uri = Uri.parse(CirnoApi.getSubscribeUrl())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(activity, intent, null)
                }
                .show()
        }

        fun isVip(): Boolean {
            val sharedPreferences = RefugeApplication.getInstance().getSharedPreferences(RefugeApplication.getInstance().getString(R.string.preference_file_key), 0)
            return sharedPreferences.getBoolean(RefugeApplication.getInstance().getString(R.string.IS_VIP), false)
        }
    }
}