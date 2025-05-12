package com.app.yajari.ui.rate_review

import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivitySubmitReviewBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.hideKeyboard
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubmitReviewActivity : BaseActivity<ActivitySubmitReviewBinding>() {
    private var announcementId = ""
    private var communicationRating = ""
    private var punctualRating = ""
    private var endPoint=""
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val chatViewModel: ChatViewModel by viewModel()
    override fun getViewBinding() = ActivitySubmitReviewBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        announcementId=intent.getStringExtra(Constant.ANNOUNCEMENT_ID).toString()
        endPoint=intent.getStringExtra(Constant.END_PONT).toString()
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            toolbar.titleTV.text = getStr(R.string.rate_review)
            punctualRating = punctualityRB.rating.toString()
            communicationRating = communicationRB.rating.toString()
            communicationRB.setOnRatingChangeListener { _, rating, _ ->
                if(rating==0f)
                {
                    communicationRB.rating=1.0f
                }
                communicationRating = rating.toString()
            }
            punctualityRB.setOnRatingChangeListener { _, rating, _ ->
                if(rating==0f)
                {
                    punctualityRB.rating=1.0f
                }
                punctualRating = rating.toString()
            }
            submitBTN.setSafeOnClickListener {
//                if(messageEDT.asString()=="")
//                {
//                    showToasty(this@SubmitReviewActivity,
//                        getString(R.string.review_description_msg),"2")
//                }
//                else{
                    callSubmitReview()
                //}
            }
            submitReviewCL.setOnClickListener {
                hideKeyboard()
            }
        }
    }

    override fun setUpObserver() {
        chatViewModel.submitRateReviewResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    mProgressDialog.showProgressDialog()
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showSuccessDialog()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this,
                                msg,
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    chatViewModel.logoutUser()
                }

                Status.NETWORK_ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callSubmitReview()
                                }
                            }
                        }
                    }
                }
            }
        }

        chatViewModel.logout.observe(this) {
            if (it) {
                start<LoginActivity>("1")
            }
        }
    }

    private fun callSubmitReview() {
        chatViewModel.submitRateReview(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementId,
                Constant.COMMENT to binding.messageEDT.asString(),
                Constant.COMMUNICATION_RATE to communicationRating,
                Constant.PUNCTUAL_RATE to punctualRating
            )
            ,endPoint
        )
    }

    override fun onBaseBackPressed() {
        finish()
    }

    private fun showSuccessDialog() {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text = getStr(R.string.review_success_msg)
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}