package com.app.yajari.ui.yajari
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityYajariBinding
import com.app.yajari.ui.faq.FAQActivity
import com.app.yajari.ui.help_support.HelpSupportActivity
import com.app.yajari.ui.pages.PagesActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start

class YajariActivity : BaseActivity<ActivityYajariBinding>() {
    override fun getViewBinding()= ActivityYajariBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.yajari)
            toolbar.backIV.setSafeOnClickListener {
               finish()
            }
            aboutUsTV.setSafeOnClickListener {
                start<PagesActivity>("2", Constant.KEY to Constant.ABOUT)
            }
            termsTV.setSafeOnClickListener {
                start<PagesActivity>("2", Constant.KEY to Constant.TERMS)
            }
            privacyTV.setSafeOnClickListener {
                start<PagesActivity>("2", Constant.KEY to Constant.PRIVACY)
            }
            helpTV.setSafeOnClickListener {
                start<HelpSupportActivity>("2")
            }
            faqTV.setSafeOnClickListener {
                start<FAQActivity>("2")
            }
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
      finish()
    }

}