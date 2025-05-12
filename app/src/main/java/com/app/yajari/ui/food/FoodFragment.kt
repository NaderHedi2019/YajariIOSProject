package com.app.yajari.ui.food

import android.util.Log
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentFoodBinding
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.home.FIRST_PAGE_INDEX
import com.app.yajari.ui.home.SECOND_PAGE_INDEX
import com.app.yajari.ui.notification.NotificationActivity
import com.app.yajari.ui.search.SearchActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.NotificationCountListener
import com.app.yajari.utils.changeStatusBarBlackColor
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class FoodFragment : BaseFragment<FragmentFoodBinding>(),NotificationCountListener {
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private var userId=0
    override fun getViewBinding()= FragmentFoodBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarBlackColor("#D60E67")
        binding.apply {
            setupViewPager(homeVP,homeTL)
        }
        if(activity is MainActivity) {
            (activity as MainActivity).assignNotificationListener(this@FoodFragment)
        }
    }

    override fun click() {
        binding.apply {
            notificationIV.setSafeOnClickListener {
                if(userId==0)
                {
                    loginContinueDialog()
                }
                else {
                    requireActivity().start<NotificationActivity>("2")
                }
            }
            searchIV.setSafeOnClickListener {
                requireActivity().start<SearchActivity>("2",Constant.TYPE to Constant.FOOD)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        setCount()
    }

    private fun setCount()
    {
        binding.apply {
            if (Constant.NOTIFICATION_COUNT>0)
            {
                notificationCountTV.visible()
                notificationCountTV.text=Constant.NOTIFICATION_COUNT.toString()
            }
            else{
                notificationCountTV.gone()
            }
        }

    }

    override fun setUpObserver() {
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            userId = it.id!!
        }
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setupViewPager(vpHome: ViewPager2, tabHome: TabLayout) {

        vpHome.adapter = FoodPagerAdapter(childFragmentManager, lifecycle)
        TabLayoutMediator(tabHome, vpHome) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        vpHome.isUserInputEnabled = false
    }
    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FIRST_PAGE_INDEX -> getString(R.string.donation)
            SECOND_PAGE_INDEX -> getString(R.string.request)
            else -> null
        }
    }

    override fun onUpdateCount() {
        Log.e("Call update0","@@@")
        setCount()
    }

}