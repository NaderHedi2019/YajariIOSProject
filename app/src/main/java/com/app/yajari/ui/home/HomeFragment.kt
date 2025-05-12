package com.app.yajari.ui.home

import android.graphics.Typeface
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.DialogAppRatingBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.databinding.FragmentHomeBinding
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.notification.NotificationActivity
import com.app.yajari.ui.search.SearchActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.NotificationCountListener
import com.app.yajari.utils.changeStatusBarBlackColor
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.getClr
import com.app.yajari.utils.gone
import com.app.yajari.utils.makeLinks
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>(),NotificationCountListener {
    private var appRating:Float=0f
    private var userId=0
    private val reviewManager: FakeReviewManager? = null
    private var reviewInfo: ReviewInfo? = null
    private val announcementViewModel: AnnouncementViewModel by viewModel()

    override fun getViewBinding()=FragmentHomeBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarBlackColor("#D60E67")
        binding.apply {
            setupViewPager(homeVP,homeTL)
           // showAppRateDialog()
        }
        if(activity is MainActivity) {
            (activity as MainActivity).assignNotificationListener(this@HomeFragment)
        }
        inAppRequest()
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
                requireActivity().start<SearchActivity>("2",Constant.TYPE to Constant.OBJECT)
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
    }

    private fun setupViewPager(vpHome: ViewPager2, tabHome: TabLayout) {

        vpHome.adapter = HomePagerAdapter(childFragmentManager, lifecycle, Constant.OBJECT)
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

    private fun showAppRateDialog()
    {

        requireActivity().commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogAppRatingBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    val typeface = Typeface.createFromAsset(requireContext().assets, "rale_way_bold.ttf")
                    expMsgTV.makeLinks(
                        requireActivity().getClr(R.color.color_151515),
                        typeface,
                        false,
                        Pair("Ya Jari", View.OnClickListener {
                        })
                    )
                    appRating=appRB.rating
                    appRB.setOnRatingChangeListener { _, rating, _ ->
                     appRating=rating
                    }
                    submitBTN.setSafeOnClickListener {
                        dismiss()
                        if(appRating>3)
                        {
                           launchInAppRating()
                        }
                        else {
                            showRateSuccessDialog()
                        }
                    }
                }
            }
        }
    }
    private fun showRateSuccessDialog()
    {
        requireActivity().commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text=getString(R.string.review_success_msg)
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                    }
                }
            }
        }
    }
    private fun inAppRequest()
    {
        val request = reviewManager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                 reviewInfo = task.result
            } else {
                @ReviewErrorCode
                val reviewErrorCode = (task.exception as ReviewException).errorCode
                showToasty(requireContext(), task.exception?.message.toString(),"2")
            }
        }
    }

    private fun launchInAppRating()
    {
        val flow = reviewManager?.launchReviewFlow(requireActivity(), reviewInfo!!)
        flow?.addOnCompleteListener { task ->
            if(task.isSuccessful)
            {
                showRateSuccessDialog()
            }
        }

    }

    override fun onUpdateCount() {
        Log.e("Call update0","@@@")
        setCount()
    }


}