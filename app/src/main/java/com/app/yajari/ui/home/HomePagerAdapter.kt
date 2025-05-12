package com.app.yajari.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

const val FIRST_PAGE_INDEX = 0
const val SECOND_PAGE_INDEX = 1
class HomePagerAdapter(
    fragment: FragmentManager,
    lifecycle: Lifecycle,
    type: String
):

    FragmentStateAdapter(fragment, lifecycle) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        FIRST_PAGE_INDEX to { DonationFragment.newInstance(type) },
        SECOND_PAGE_INDEX to { RequestFragment.newInstance(type) }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}