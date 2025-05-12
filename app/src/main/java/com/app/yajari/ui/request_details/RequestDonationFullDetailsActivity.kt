package com.app.yajari.ui.request_details

import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityRequestDonationFullDetailsBinding
import com.app.yajari.ui.announcement_details.ImageSliderAdapter
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.zhpan.indicator.enums.IndicatorStyle

class RequestDonationFullDetailsActivity : BaseActivity<ActivityRequestDonationFullDetailsBinding>() {


    override fun getViewBinding()= ActivityRequestDonationFullDetailsBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#000000")

        setSlider()
    }

    override fun click() {
        binding.apply {
            backIV.setSafeOnClickListener {
                finish()
            }

        }
    }
    private fun setSlider() {
        binding.apply {
            sliderVP.run {
               // adapter = ImageSliderAdapter(gallery)
                isUserInputEnabled = true
            }
            dotsIndicator.setupWithViewPager(sliderVP)
            dotsIndicator.setIndicatorGap(resources.getDimension(R.dimen.padding_6))
            dotsIndicator.setSliderWidth(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setSliderHeight(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setIndicatorStyle(IndicatorStyle.CIRCLE)
        }

    }


    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
        finish()
    }

}