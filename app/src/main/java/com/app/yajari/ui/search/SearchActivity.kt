package com.app.yajari.ui.search
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivitySearchBinding
import com.app.yajari.ui.food.FoodPagerAdapter
import com.app.yajari.ui.home.FIRST_PAGE_INDEX
import com.app.yajari.ui.home.HomePagerAdapter
import com.app.yajari.ui.home.SECOND_PAGE_INDEX
import com.app.yajari.utils.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    private val animationDuration = 500L
    var type=""
    private var listener:SearchListener?=null
    override fun getViewBinding()= ActivitySearchBinding.inflate(layoutInflater)

    override fun initObj() {
        type=intent.getStringExtra(Constant.TYPE).toString()
        changeStatusBarColor("#FFFFFF")
        binding.apply {
            if(type==Constant.OBJECT) {
                setupObjectViewPager(searchVP, searchTL)
            }
            else {
                setupFoodViewPager(searchVP, searchTL)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun click() {
        binding.apply {
            backIV.setSafeOnClickListener {
                finish()
            }
            searchEDT.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    if(searchEDT.asString().isNotEmpty()) {
                        clearIV.visible()
                    }
                    else{
                        clearIV.gone()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        listener?.onSearchChange(true,searchEDT.asString(),true)
                    }, 500)

                }
                true
            }
            searchEDT.doAfterTextChanged {
                if(searchEDT.asString().isNotEmpty()) {
                    clearIV.visible()
                }
                else{
                    clearIV.gone()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    listener?.onSearchChange(true,searchEDT.asString(),true)
                }, 500)
            }
            clearIV.setSafeOnClickListener {
                searchEDT.setText("")
                listener?.onSearchChange(true,searchEDT.asString(),true)
                hideKeyboard()
            }

          //  searchIV.setSafeOnClickListener {
                searchEDT.visible()
                searchIV.gone()
                if (searchEDT.visibility == View.GONE) {
                    searchEDT.visibility = View.VISIBLE
                    searchEDT.alpha = 0f
                    searchEDT.animate()
                        .alpha(1f)
                        .setDuration(animationDuration)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                searchIV.visibility = View.GONE
                            }
                        })
                }
         //   }


        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
       finish()
    }
    private fun setupObjectViewPager(vpHome: ViewPager2, tabHome: TabLayout) {

        vpHome.adapter = HomePagerAdapter(supportFragmentManager, lifecycle,type)
        TabLayoutMediator(tabHome, vpHome) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        vpHome.isUserInputEnabled = false
    }
    private fun setupFoodViewPager(vpHome: ViewPager2, tabHome: TabLayout) {

        vpHome.adapter = FoodPagerAdapter(supportFragmentManager, lifecycle)
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
    fun assignSearchListener(searchListener: SearchListener)
    {
        listener=searchListener
        listener?.onSearchChange(true,binding.searchEDT.asString(),false)
    }

    interface SearchListener{
        fun onSearchChange(isCome:Boolean,search:String,isSearch:Boolean)
    }
}