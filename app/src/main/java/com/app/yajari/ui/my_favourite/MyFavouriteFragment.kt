package com.app.yajari.ui.my_favourite
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentMyFavouriteBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MyFavouriteFragment : BaseFragment<FragmentMyFavouriteBinding>() {
    override fun getViewBinding()= FragmentMyFavouriteBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            setupViewPager(myFavouriteVP,myFavouriteTL)
            toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
             //   (activity as MainActivity).openDrawer()
            }
            toolbar.titleTV.text=requireContext().getStr(R.string.my_fav)

        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }
    private fun setupViewPager(vpFav: ViewPager2, taFav: TabLayout) {

        vpFav.adapter = MyFavouritePagerAdapter(childFragmentManager, lifecycle)
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


}