package vip.kirakira.starcitizenlite.ui.hangarlog

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
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

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        val timeLineModel = hangarLogs[position]
        holder.date.visibility = View.VISIBLE
        holder.date.text = SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(timeLineModel.time)
        holder.message.text = formatMessage(timeLineModel)
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
        }
    }

    fun formatMessage(hangarLog: HangarLog): String {
        var message = ""
        when(hangarLog.type) {
            "CREATED" -> {
                if(hangarLog.price == null)
                    message = "购买了${hangarLog.name}"
                else
                    message = "购买了${hangarLog.name}, 价格为${"$"}${(hangarLog.price!!.toFloat() / 100)} USD"
            }
            "RECLAIMED" -> {
                message = "回收了${hangarLog.name}"
            }
            "UNKNOWN" -> {
                message = "获得了${hangarLog.name}"
                Log.d("TimelineAdapter", hangarLog.toString())
            }
        }
        return message
    }

}