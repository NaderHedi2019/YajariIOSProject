package com.app.yajari.ui.community_impact
import androidx.navigation.fragment.findNavController
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentCommunityImpactBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener


class CommunityImpactFragment : BaseFragment<FragmentCommunityImpactBinding>() {
    override fun getViewBinding()= FragmentCommunityImpactBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.community_impact)
            toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
             //   (activity as MainActivity).openDrawer()
            }
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

}