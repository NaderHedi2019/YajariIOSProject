package com.app.yajari

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.AskData
import com.app.yajari.databinding.ActivityMainBinding
import com.app.yajari.databinding.BottomDialogPublishAskBinding
import com.app.yajari.ui.create_announcement.AskSliderAdapter
import com.app.yajari.ui.create_announcement.PublishAnnouncementActivity
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>(),AskSliderAdapter.AskListener {
    private var mBottomAskBinding: BottomDialogPublishAskBinding? = null
    private lateinit var  mBottomSheetDialog:BottomSheetDialog
    private val authViewModel: AuthViewModel by viewModel()
    private var type=""
    private var from =""
    private var pushType =""
    private var objectType=""
    private var userId=0
     var listener: NotificationCountListener?=null

    override fun getViewBinding()= ActivityMainBinding.inflate(layoutInflater)

    override fun initObj() {
        from=intent.getStringExtra(Constant.FROM).toString()
        pushType=intent.getStringExtra(Constant.PUSH_TYPE).toString()
        setBottomMenu()
        if(from ==Constant.PUSH && pushType=="4")
        {
            findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_chat)
        }
        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.CHAT))
    }
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Constant.CHAT_COUNT=intent.getStringExtra(Constant.MSG_C)!!.toInt()
            Constant.NOTIFICATION_COUNT=intent.getStringExtra(Constant.NOTIFICATION_C)!!.toInt()
            setBadgeChatCount()
            listener?.onUpdateCount()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this@MainActivity).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    override fun click() {
        binding.apply {
            addIV.setSafeOnClickListener {
                if(userId==0)
                {
                    loginContinueDialog()
                }
                else {
                    showAskDialog()
                }
            }
            val homeMenu: View = bottomNavigation.findViewById(R.id.navigation_home)
            homeMenu.setSafeOnClickListener {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_home)
            }
            val foodMenu: View = bottomNavigation.findViewById(R.id.navigation_food)
            foodMenu.setSafeOnClickListener {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_food)
            }
            val chatMenu: View = bottomNavigation.findViewById(R.id.navigation_chat)
            chatMenu.setSafeOnClickListener {
                if(userId==0)
                {
                    loginContinueDialog()
                }
                else {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_chat)
                }
            }
            val profileMenu: View = bottomNavigation.findViewById(R.id.navigation_profile)
            profileMenu.setSafeOnClickListener {
                if(userId==0)
                {
                    loginContinueDialog()
                }
                else {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_profile)
                }
            }

        }
    }

    override fun setUpObserver() {
        setProfileImage()
    }
    fun setProfileImage()
    {
        authViewModel.getUserFromPref()
        authViewModel.userDataResponse.observe(this)
        {
            userId= it.id!!
            binding.bottomProfileIV.loadImage(it.profileImage,R.drawable.img_user)
        }
    }


    override fun onBaseBackPressed() {
    }
    private fun setBottomMenu() {
        binding.apply {
            val navController = findNavController(R.id.nav_host_fragment)
            val graphInflater = navController.navInflater
            val navGraph = graphInflater.inflate(R.navigation.navigation_menu)
            navController.graph = navGraph
            bottomNavigation.setupWithNavController(navController)
            bottomNavigation.itemIconTintList = null
        }
    }

    private fun showAskDialog() {
        mBottomSheetDialog = BottomSheetDialog(this@MainActivity, R.style.AppBottomSheetDialogTheme)
        mBottomAskBinding = BottomDialogPublishAskBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomAskBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomAskBinding?.apply {
            val askOptionList= mutableListOf<AskData>()
            askOptionList.add(AskData(getString(R.string.what_type_announcement),getString(R.string.i_give),R.drawable.i_ask_selector,getString(R.string.i_ask_for),R.drawable.i_ask_selector,
                selected1 = false,
                selected2 = false
            ))
            askOptionList.add(AskData(getString(R.string.what_kind_thing),getString(R.string.`object`),R.drawable.object_selector,getString(R.string.food),R.drawable.food_selector,
                selected1 = false,
                selected2 = false
            ))
            sliderVP.run {
                adapter = AskSliderAdapter(askOptionList,this@MainActivity)
                isUserInputEnabled = false
            }
            dotsIndicator.setupWithViewPager(sliderVP)
            dotsIndicator.setIndicatorGap(resources.getDimension(R.dimen.padding_6))
            dotsIndicator.setSliderWidth(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setSliderHeight(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setIndicatorStyle(IndicatorStyle.CIRCLE)
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    override fun onItemOptionClick(askData: AskData,askList:MutableList<AskData>) {
        var currentItem= mBottomAskBinding?.sliderVP?.currentItem
        if(currentItem==1)
        {
            objectType = if(askList[0].selected1) {
                 Constant.DONATION
            } else{
                 Constant.REQUEST
            }
            type = if(askList[1].selected1) {
               Constant.OBJECT
            } else{
               Constant.FOOD
            }
            if(this::mBottomSheetDialog.isInitialized) {
                mBottomSheetDialog.dismiss()
            }
           start<PublishAnnouncementActivity>("2",Constant.FROM to Constant.CREATE,Constant.TYPE to type,Constant.OBJECT_TYPE to objectType)

        }
        else {
             currentItem=+1
            mBottomAskBinding?.sliderVP!!.currentItem=currentItem
        }
    }

    private fun getOrCreateBadge(bottomBar: View, tabResId: Int): TextView? {
        val parentView = bottomBar.findViewById<ViewGroup>(tabResId)
        return parentView?.let {
            var badge = parentView.findViewById<TextView>(R.id.countTV)
            if (badge == null) {
                LayoutInflater.from(parentView.context).inflate(R.layout.layout_badge_count, parentView, true)
                badge = parentView.findViewById(R.id.countTV)
            }
            badge
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    fun BottomNavigationView.setBadge(tabResId: Int, badgeValue: Int) {
        getOrCreateBadge(this, tabResId)?.let { badge ->
            badge.visibility = if (badgeValue > 0) {
                badge.text = "$badgeValue"
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    fun setBadgeChatCount()
    {
        binding.bottomNavigation.setBadge(R.id.navigation_chat,Constant.CHAT_COUNT)
    }
    fun assignNotificationListener(notificationListener: NotificationCountListener)
    {
        this.listener=notificationListener
    }


}