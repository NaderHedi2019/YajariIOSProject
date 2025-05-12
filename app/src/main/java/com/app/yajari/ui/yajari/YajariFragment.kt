package com.app.yajari.ui.yajari
import androidx.navigation.fragment.findNavController
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.FragmentYajariBinding
import com.app.yajari.ui.faq.FAQActivity
import com.app.yajari.ui.help_support.HelpSupportActivity
import com.app.yajari.ui.pages.PagesActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start


class YajariFragment : BaseFragment<FragmentYajariBinding>() {
    override fun getViewBinding()= FragmentYajariBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")

    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.yajari)
            toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
             //   (activity as MainActivity).openDrawer()
            }
            aboutUsTV.setSafeOnClickListener {
                requireActivity().start<PagesActivity>("2",Constant.KEY to Constant.ABOUT)
            }
            termsTV.setSafeOnClickListener {
                requireActivity().start<PagesActivity>("2",Constant.KEY to Constant.TERMS)
            }
            privacyTV.setSafeOnClickListener {
                requireActivity().start<PagesActivity>("2",Constant.KEY to Constant.PRIVACY)
            }
            helpTV.setSafeOnClickListener {
                requireActivity().start<HelpSupportActivity>("2")
            }
            faqTV.setSafeOnClickListener {
                requireActivity().start<FAQActivity>("2")
            }
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

}