package com.app.yajari.ui.chat

import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentChatBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ChatFragment : BaseFragment<FragmentChatBinding>() {
    override fun getViewBinding()= FragmentChatBinding.inflate(layoutInflater)
    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            setupViewPager(chatVP,chatTL)
            toolbar.backIV.gone()
            toolbar.titleTV.text=requireContext().getStr(R.string.chat)
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
       findNavController().navigate(R.id.navigation_home)
    }

    private fun setupViewPager(chatVP: ViewPager2, chatTL: TabLayout) {

        chatVP.adapter = ChatPagerAdapter(childFragmentManager, lifecycle)
        TabLayoutMediator(chatTL, chatVP) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
        chatVP.isUserInputEnabled = false
    }
    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FIRST_PAGE_INDEX -> getString(R.string.my_request)
            SECOND_PAGE_INDEX -> getString(R.string.my_donation)
            else -> null
        }
    }


}