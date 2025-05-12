package com.app.yajari.ui.create_announcement

import com.app.yajari.MainActivity
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityAnnouncementSubmissionBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start

class AnnouncementSubmissionActivity : BaseActivity<ActivityAnnouncementSubmissionBinding>() {
    override fun getViewBinding()=ActivityAnnouncementSubmissionBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.gone()
            toolbar.titleTV.gone()
            continueBTN.setOnClickListener {
                start<MainActivity>("1")
            }
        }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
        start<MainActivity>("1")
    }

}