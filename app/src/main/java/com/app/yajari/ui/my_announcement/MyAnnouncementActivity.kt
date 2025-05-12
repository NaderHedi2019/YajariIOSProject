package com.app.yajari.ui.my_announcement
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityMyAnnouncementBinding
import com.app.yajari.ui.profile.FIRST_PAGE_INDEX
import com.app.yajari.ui.profile.ProfilePagerAdapter
import com.app.yajari.ui.profile.SECOND_PAGE_INDEX
import com.app.yajari.utils.Constant
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyAnnouncementActivity : BaseActivity<ActivityMyAnnouncementBinding>() {
    private var from=""
    override fun getViewBinding()=ActivityMyAnnouncementBinding.inflate(layoutInflater)
    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        from=intent.getStringExtra(Constant.FROM).toString()
    }
    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        if(from==Constant.PUSH &&  isTaskRoot)
        {
            start<MainActivity>("1")
        }
        else {
            finish()
        }
    }
    override fun click() {
        binding.apply {
            toolbar.titleTV.text=getString(R.string.my_announcement)
            toolbar.backIV.setSafeOnClickListener {
                if(from==Constant.PUSH &&  isTaskRoot)
                {
                    start<MainActivity>("1")
                }
                else {
                    finish()
                }
            }
            setupViewPager(vpMyAnnouncement = MyAnnouncementVP, tabMyAnnouncement = MyAnnouncementTL)
        }
    }


    private fun setupViewPager(vpMyAnnouncement: ViewPager2, tabMyAnnouncement: TabLayout) {

        vpMyAnnouncement.adapter = ProfilePagerAdapter(supportFragmentManager, lifecycle, from)
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