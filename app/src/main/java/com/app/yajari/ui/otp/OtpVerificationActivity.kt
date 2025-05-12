package com.app.yajari.ui.otp
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.CountDownTimer
import android.view.View
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityOtpVerificationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogVerifyBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import com.aabhasjindal.otptextview.OtpTextView
import com.aabhasjindal.otptextview.OTPListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class OtpVerificationActivity : BaseActivity<ActivityOtpVerificationBinding>() {
    private var name=""
    private var countryCode=""
    private var mobile=""
    private var email=""
    private var dob=""
    private var address=""
    private var latitude=""
    private var longitude=""
    private var password=""
    private var otpValue=""
    private val authViewModel: AuthViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()= ActivityOtpVerificationBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        name=intent.getStringExtra(Constant.NAME).toString()
        mobile=intent.getStringExtra(Constant.MOBILE).toString()
        countryCode=intent.getStringExtra(Constant.COUNTRY_CODE).toString()
        email=intent.getStringExtra(Constant.EMAIL).toString()
        dob=intent.getStringExtra(Constant.DOB).toString()
        address=intent.getStringExtra(Constant.ADDRESS).toString()
        latitude=intent.getStringExtra(Constant.LAT).toString()
        longitude=intent.getStringExtra(Constant.LNG).toString()
        password=intent.getStringExtra(Constant.PASSWORD).toString()
        hideKeyboard()
        binding.apply {
            emailTV.text=email
            otpView.otpListener = object : OTPListener {
                override fun onInteractionListener() {
                    if (otpView.otp?.length != 6) {
                        verifyBTN.isEnabled = false
                        verifyBTN.isSelected=false
                    }
                }

                @SuppressLint("ResourceType")
                override fun onOTPComplete(otp: String) {
                    hideKeyboard()
                    otpValue = otp
                    verifyBTN.isEnabled = true
                    verifyBTN.isSelected = true
                }
            }
        }
        setTimer()
    }

    override fun click() {
        binding.apply {
            backIV.setSafeOnClickListener {
                finish()
            }
            verifyBTN.setSafeOnClickListener {
                hideKeyboard()
                callSignup()
            }
            otpCL.setSafeOnClickListener {
                hideKeyboard()
            }
            resendTV.setSafeOnClickListener {
                otpValue=""
                otpView.setOTP("")
                verifyBTN.isEnabled=false
                verifyBTN.isSelected=false
                callSendOTP()
            }
        }
    }

    override fun setUpObserver() {
        authViewModel.sendOTPResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    setTimer()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    showToasty(this, response.message.toString(), "2")
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
                                    callSendOTP()
                                }
                            }
                        }
                    }
                }
            }
        }
        authViewModel.signUpResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    start<MainActivity>("1")
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    showToasty(this, response.message.toString(), "2")
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
                                    callSignup()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBaseBackPressed() {
      finish()
    }


    private fun callSignup(){
        authViewModel.signup(mapOf(Constant.NAME to name,Constant.COUNTRY_CODE to countryCode,
            Constant.MOBILE to mobile,Constant.EMAIL to email,Constant.BOD to dob,
            Constant.ADDRESS to address,Constant.LAT to latitude,Constant.LNG to longitude,
            Constant.PASSWORD to password,Constant.DEVICE_ID to Constant.sDeviceId,
            Constant.DEVICE_TYPE to Constant.APP_TYPE,Constant.PUSH_TOKEN to Constant.sFcmKey,Constant.OTP to otpValue
        ))
    }
    private fun callSendOTP()
    {
        authViewModel.sendOTP(mapOf(Constant.EMAIL to email))
    }
    private fun setTimer() {
        binding.apply {
            resendTV.gone()
            circularProgress.visible()

            circularProgress.maxProgress = 60.0
            circularProgress.setCurrentProgress(60.0)
            val countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    circularProgress.setCurrentProgress((millisUntilFinished / 1000).toDouble())
                }

                override fun onFinish() {
                    circularProgress.visibility = View.INVISIBLE
                    resendTV.visible()
                }
            }
            countDownTimer.start()
        }
    }
}