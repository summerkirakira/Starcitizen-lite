package com.github.vipulasri.timelineview.sample.widgets

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vip.kirakira.starcitizenlite.R

open class RoundedCornerBottomSheet: BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()

        view?.post {
            val parent = view?.parent as View
            parent.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet))
        }

    }

}