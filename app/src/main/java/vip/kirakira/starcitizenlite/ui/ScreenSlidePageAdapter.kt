package vip.kirakira.starcitizenlite.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vip.kirakira.starcitizenlite.PAGE_NUM

class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    lateinit var fragmentList: List<Fragment>

    fun setList(fragmentList: List<Fragment>) {
        this.fragmentList = fragmentList
    }

    override fun getItemCount(): Int = PAGE_NUM

    override fun createFragment(position: Int): Fragment = fragmentList.get(position)


}