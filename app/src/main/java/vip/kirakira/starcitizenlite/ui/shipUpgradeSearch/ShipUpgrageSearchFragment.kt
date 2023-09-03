package vip.kirakira.starcitizenlite.ui.shipUpgradeSearch

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vip.kirakira.starcitizenlite.R

class ShipUpgrageSearchFragment : Fragment() {

    companion object {
        fun newInstance() = ShipUpgrageSearchFragment()
    }

    private lateinit var viewModel: ShipUpgradeSearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ship_upgrade_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShipUpgradeSearchViewModel::class.java)
        // TODO: Use the ViewModel
    }

}