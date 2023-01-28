package vip.kirakira.starcitizenlite.ui.hangarlog

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.HangarLog


class TimelineAdapter(private val hangarLogs: List<HangarLog>): RecyclerView.Adapter<TimelineAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = mLayoutInflater.inflate(R.layout.hangar_log_item_timeline, parent, false)
        return TimeLineViewHolder(view, viewType)
    }

    @SuppressLint("SimpleDateFormat", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        val timeLineModel = hangarLogs[position]
        holder.date.visibility = View.VISIBLE
        holder.date.text = SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(timeLineModel.time)
        holder.message.text = Html.fromHtml(formatMessage(timeLineModel))
        holder.timeline.markerSize = 120
        when(timeLineModel.type) {
            "CREATED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_shopping)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_shop_icon))
            }
            "RECLAIMED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_reclaim)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_reclaim_icon))
            }
            "GIVEAWAY" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_giveaway)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_giveaway_icon))
            }
            "APPLIED_UPGRADE" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_upgrade)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_upgrade_icon))
            }
            "CONSUMED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_consume)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_consume_icon))
            }
            "GIFT" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_gift)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_gift_icon))
            }
            "GIFT_CLAIMED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_gift_claimed)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_gift_claimed_icon))
            }
            "GIFT_CANCELLED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_gift_cancelled)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_gift_cancelled_icon))
            }
            "NAME_CHANGE" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_name_change)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_name_change_icon))
            }
            "BUYBACK" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_buyback)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_buyback_icon))
            }
            "NAME_CHANGE_RECLAIMED" -> {
                holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.ic_hangar_log_bottomsheet_reclaim)
                holder.timeline.setMarkerColor(holder.itemView.context.getColor(R.color.hangar_log_reclaim_icon))
            }
            else -> holder.timeline.marker = holder.itemView.context.getDrawable(R.drawable.marker)
        }
    }

    override fun getItemCount(): Int {
        return hangarLogs.size
    }

    inner class TimeLineViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val timeline: TimelineView = itemView.findViewById(R.id.timeline)
        val date: AppCompatTextView = itemView.findViewById(R.id.text_timeline_date)
        val message: AppCompatTextView = itemView.findViewById(R.id.text_timeline_title)
        init {
            timeline.initLine(viewType)

//            val theme: Theme = itemView.context.theme
//            val typedValue = TypedValue()
//            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)

//            timeline.setMarkerColor(typedValue.data)
//            timeline.setStartLineColor(typedValue.data, viewType)
//            timeline.setEndLineColor(typedValue.data, viewType)
        }
    }

    fun formatMessage(hangarLog: HangarLog): String {
        var message = ""
        when(hangarLog.type) {
            "CREATED" -> {
                if(hangarLog.price == null)
                    message = "购买了<b>${hangarLog.name}</b>(#${hangarLog.target})"
                else
                    message = "购买了<b>${hangarLog.name}</b>(#${hangarLog.target}), 价格为<b>${"$"}${(hangarLog.price!!.toFloat() / 100)} USD</b>"
            }
            "RECLAIMED" -> {
                message = "回收了<b>${hangarLog.name}</b>(#${hangarLog.target})"
            }
            "GIVEAWAY" -> {
                message = "获得了<b>${hangarLog.name}</b>(#${hangarLog.target})"
            }
            "APPLIED_UPGRADE" -> {
                message = "将升级包<b>${hangarLog.reason}</b>(#${hangarLog.target})应用于" +
                        "#${hangarLog.source} 当前价值为<b>${"$"}${(hangarLog.price!!.toFloat() / 100)} USD</b>"
            }
            "CONSUMED" -> {
                message = "消耗了<b>${hangarLog.name}</b>(#${hangarLog.target})"
            }
            "GIFT" -> {
                message = "将价值为<b>${"$"}${(hangarLog.price!!.toFloat() / 100)} USD</b>的<b>${hangarLog.name}</b>(#${hangarLog.target})发送到邮箱: <b>${hangarLog.operator}</b>"
            }
            "GIFT_CLAIMED" -> {
                message = "<b>${hangarLog.operator}</b>确认接收了<b>${hangarLog.name}</b>(#${hangarLog.target}), 价值为<b>${"$"}${(hangarLog.price!!.toFloat() / 100)} USD</b>"
            }
            "GIFT_CANCELLED" -> {
                message = "<b>${hangarLog.operator}</b>取消了<b>${hangarLog.name}</b>(#${hangarLog.target})的赠送"
            }
            "BUYBACK" -> {
                message = "回购了<b>${hangarLog.name}</b>(#${hangarLog.target})"
            }
            "NAME_CHANGE" -> {
                message = "将<b>${hangarLog.source!!.split("/")[0]}</b>(#${hangarLog.target})的名称修改为<b>${hangarLog.source!!.split("/")[1]}</b>"
            }
            "NAME_CHANGE_RECLAIMED" -> {
                message = "回收了<b>${hangarLog.source!!.split("/")[0]}</b>(#${hangarLog.target})的名称保留: <b>${hangarLog.source!!.split("/")[1]}</b>"
            }
            "UNKNOWN" -> {
                message = "获得了<b>${hangarLog.name}</b>(#${hangarLog.target})"
                Log.d("TimelineAdapter", hangarLog.toString())
            }
        }
        return message
    }

}