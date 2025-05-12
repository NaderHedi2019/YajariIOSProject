package com.app.yajari.ui.my_favourite
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityMyFavouriteBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyFavouriteActivity : BaseActivity<ActivityMyFavouriteBinding>() {
    override fun getViewBinding()=ActivityMyFavouriteBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            setupViewPager(myFavouriteVP,myFavouriteTL)
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            toolbar.titleTV.text=getStr(R.string.my_fav)

        }
    }

    override fun setUpObserver() {
    }


    private fun setupViewPager(vpFav: ViewPager2, taFav: TabLayout) {

        vpFav.adapter = MyFavouritePagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(taFav, vpFav) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        vpFav.isUserInputEnabled = false
    }
    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FIRST_PAGE_INDEX -> getString(R.string.announcement)
            SECOND_PAGE_INDEX -> getString(R.string.users)
            else -> null
        }
    }

    override fun onBaseBackPressed() {
        finish()
    }

}