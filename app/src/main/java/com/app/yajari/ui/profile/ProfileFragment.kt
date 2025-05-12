package com.app.yajari.ui.profile
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentProfileBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.rate_review.RateReviewActivity
import com.app.yajari.utils.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale


class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    override fun getViewBinding()= FragmentProfileBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")

    }

    override fun click() {
        binding.apply {
            setupViewPager(vpProfile = profileVP, tabProfile = ProfileTL)
            toolbar.backIV.gone()
            toolbar.titleTV.text=requireContext().getStr(R.string.my_profile)

            totalReviewTV.underline()
            ratingTV.underline()

            totalReviewTV.setSafeOnClickListener {
                requireActivity().start<RateReviewActivity>("2")
            }
            ratingTV.setSafeOnClickListener {
                requireActivity().start<RateReviewActivity>("2")
            }
        }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setupViewPager(vpProfile: ViewPager2, tabProfile: TabLayout) {

        vpProfile.adapter = ProfilePagerAdapter(childFragmentManager, lifecycle, "normal")
        TabLayoutMediator(tabProfile, vpProfile) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        vpProfile.isUserInputEnabled = false
    }
    private fun getTabTitle(position: Int): String? {
        return when (position) {
          FIRST_PAGE_INDEX -> getString(R.string.donation)
           SECOND_PAGE_INDEX -> getString(R.string.request)
            else -> null
        }
    }


}