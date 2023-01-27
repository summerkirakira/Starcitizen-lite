package vip.kirakira.starcitizenlite.ui.hangarlog

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import vip.kirakira.starcitizenlite.R

class HangarLogBottomSheet: RoundedCornerBottomSheet() {

    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var hangarLogViewModel: HangarLogViewModel
    private lateinit var closeButton: AppCompatImageView

    override fun onStart() {
        super.onStart()

    }
    companion object {

        fun showDialog(fragmentManager: FragmentManager) {
            val dialog = HangarLogBottomSheet()
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hangarLogViewModel = ViewModelProvider(this).get(HangarLogViewModel::class.java)
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppBaseTheme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.bottom_sheet_hangar_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton = view.findViewById(R.id.image_toggle)
        closeButton.setOnClickListener {
                    dismiss()
        }
        initAdapter()
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.hangar_log_recycler_view)

        hangarLogViewModel.hangarLogs.observe(viewLifecycleOwner) {
            if (it == null) {
                Log.d("HangarLogBottomSheet", "HangarLog is null")
                return@observe
            } else {
                recyclerView?.apply {
                    layoutManager = mLayoutManager
                    adapter = TimelineAdapter(it)
                }
            }
        }

    }

}