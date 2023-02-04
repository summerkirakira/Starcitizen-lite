package vip.kirakira.starcitizenlite.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.RefugeApplication
import vip.kirakira.starcitizenlite.createWarningAlerter
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.getDatabase
import vip.kirakira.starcitizenlite.ui.hangarlog.TimelineAdapter
import vip.kirakira.starcitizenlite.ui.loadImage

class HangarItemDetailBottomSheet(private val hangerItem: HangerItemProperty): RoundedCornerBottomSheet() {

    private lateinit var closeButton: AppCompatImageView

    companion object {

        fun showDialog(fragmentManager: FragmentManager, hangerItem: HangerItemProperty) {
            val dialog = HangarItemDetailBottomSheet(hangerItem)
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, theme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.bottom_sheet_hangar_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton = view.findViewById(R.id.image_toggle)
        closeButton.setOnClickListener {
            dismiss()
        }
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.pledge_upgrade_recycler_view)
        val titleTextView = view?.findViewById<TextView>(R.id.upgrade_text_attributes_heading)
        val application = RefugeApplication.getInstance()
        val database = getDatabase(application)
        val updateLogs = database.hangarLogDao.getByTargetDesc(hangerItem.idList.split(",")[0])
        val mLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//        titleTextView?.text = "${hangerItem.name}的机库日志"

        updateLogs.observeForever {
            if (it == null) {
                Log.d("HangarLogBottomSheet", "HangarLog is null")
                return@observeForever
            } else {
                recyclerView?.apply {
                    layoutManager = mLayoutManager
                    adapter = TimelineAdapter(it)
                }
            }
        }


    }
}