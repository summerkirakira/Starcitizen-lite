package vip.kirakira.starcitizenlite.ui.ship_info

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vip.kirakira.starcitizenlite.R

class ShipInfoFragment : Fragment() {

    companion object {
        fun newInstance() = ShipInfoFragment()
    }

    private lateinit var viewModel: ShipInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ship_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShipInfoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}