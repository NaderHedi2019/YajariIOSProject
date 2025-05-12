package com.app.yajari.ui.my_announcement
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentMyAnnouncementBinding
import com.app.yajari.ui.profile.FIRST_PAGE_INDEX
import com.app.yajari.ui.profile.ProfilePagerAdapter
import com.app.yajari.ui.profile.SECOND_PAGE_INDEX
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MyAnnouncementFragment : BaseFragment<FragmentMyAnnouncementBinding>() {
    override fun getViewBinding()= FragmentMyAnnouncementBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text=getString(R.string.my_announcement)
             toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
              //  (activity as MainActivity).openDrawer()
            }
            setupViewPager(vpMyAnnouncement = MyAnnouncementVP, tabMyAnnouncement = MyAnnouncementTL)
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setupViewPager(vpMyAnnouncement: ViewPager2, tabMyAnnouncement: TabLayout) {

        vpMyAnnouncement.adapter = ProfilePagerAdapter(childFragmentManager, lifecycle, "normal")
        TabLayoutMediator(tabMyAnnouncement, vpMyAnnouncement) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        vpMyAnnouncement.isUserInputEnabled = false
    }
    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FIRST_PAGE_INDEX -> getString(R.string.donation)
            SECOND_PAGE_INDEX -> getString(R.string.request)
            else -> null
        }
    }

}