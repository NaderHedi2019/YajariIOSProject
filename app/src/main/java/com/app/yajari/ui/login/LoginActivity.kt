package com.app.yajari.ui.login

import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityLoginBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.forgot_password.ForgotPasswordActivity
import com.app.yajari.ui.signup.SignUpActivity
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.dismissProgressDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.ifNotNullOrElse
import com.app.yajari.utils.isValidEmail
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showProgressDialog
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private val authViewModel: AuthViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }


    override fun getViewBinding()= ActivityLoginBinding.inflate(layoutInflater)

    override fun initObj() {
    }

    override fun click() {
        binding.apply {
            signupTV.setSafeOnClickListener {
                start<SignUpActivity>("2")
            }
            forgotTV.setSafeOnClickListener {
                start<ForgotPasswordActivity>("2")
            }
            loginBTN.setSafeOnClickListener {
                if(isValid())
                {
                    callLogin()
                }

            }
            guestTV.setSafeOnClickListener {
                authViewModel.setGuest(true)
                start<MainActivity>("1")
            }
        }
    }

    override fun setUpObserver() {
        authViewModel.loginResponse.observe(this) { response ->
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
                                    callLogin()
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

    private fun isValid():Boolean{
        if (binding.emailEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_email).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.emailEDT.requestFocus()
            return false
        } else if (!binding.emailEDT.asString().isValidEmail()) {
            showToasty(
                this,
                getStr(R.string.please_enter_valid_email).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.emailEDT.requestFocus()
            return false
        }
        else if (binding.passwordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_password).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.passwordEDT.requestFocus()
            return false
        }
        return true
    }

    private fun callLogin(){
        authViewModel.login(mapOf(
            Constant.EMAIL to binding.emailEDT.asString(),
            Constant.PASSWORD to binding.passwordEDT.asString(),
            Constant.DEVICE_ID to Constant.sDeviceId,
            Constant.DEVICE_TYPE to Constant.APP_TYPE,
            Constant.PUSH_TOKEN to Constant.sFcmKey
        ))
    }

}