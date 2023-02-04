package vip.kirakira.starcitizenlite.ui.hangarlog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.ui.home.HomeViewModel

class HangarLogFragment : Fragment() {

    companion object {
        fun newInstance() = HangarLogFragment()
    }

    private lateinit var viewModel: HangarLogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: HomeViewModel by activityViewModels()
        return inflater.inflate(R.layout.fragment_hangar_log, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HangarLogViewModel::class.java)
        // TODO: Use the ViewModel
    }

}