package vip.kirakira.starcitizenlite.ui.widgets

import android.app.Activity
import android.util.Log
import com.tapadoo.alerter.Alerter
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.RefugeApplication

class RefugeVip {
    companion object {
        fun createWarningAlert(activity: Activity, title: String = "缺失有效的避难所Premium订阅", detail: String = "点击此处解锁更多功能", button: String = "立即解锁") {
            Alerter.create(activity)
                .setTitle(title)
                .setText(detail)
                .setBackgroundColorRes(R.color.refuge_subscribe_more)
                .setIcon(R.drawable.ic_lack_refuge_vip)
                .setIconColorFilter(0)
                .setDuration(5000)
                .setOnClickListener {
                    Log.d("RefugeVip", "Alert hide")
                }
                .show()
        }

        fun isVip(): Boolean {
            val sharedPreferences = RefugeApplication.getInstance().getSharedPreferences(RefugeApplication.getInstance().getString(R.string.preference_file_key), 0)
            return sharedPreferences.getBoolean(RefugeApplication.getInstance().getString(R.string.IS_VIP), false)
        }
    }
}