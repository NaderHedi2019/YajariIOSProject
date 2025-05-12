package com.app.yajari.ui.forgot_password
import android.graphics.Typeface
import android.view.View
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityForgotPasswordBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogVerifyBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ForgotPasswordActivity : BaseActivity<ActivityForgotPasswordBinding>() {
    private val authViewModel: AuthViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()= ActivityForgotPasswordBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
    binding.apply {
        sendBTN.setSafeOnClickListener {
            if(isValid())
            {
                callForgotPassword()
            }
        }
        backIV.setSafeOnClickListener {
            finish()
        }
    }
    }

    override fun setUpObserver() {
        authViewModel.forgotPasswordResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showVerifyDialog()
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
                                    callForgotPassword()
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

    private fun showVerifyDialog()
    {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogVerifyBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    val typeface = Typeface.createFromAsset(applicationContext.assets, "rale_way_semi_bold.ttf")
                    verifyDescTV.text=getString(R.string.verify_desc,binding.emailEDT.asString())
                    verifyDescTV.makeLinks(
                        getClr(R.color.color_151515),
                        typeface,
                        false,
                        Pair(binding.emailEDT.asString(), View.OnClickListener {
                        })
                    )
                    verifyBTN.setSafeOnClickListener {
                        dismiss()
                        start<LoginActivity>("1")
                    }
                }
            }
        }
    }
    private fun callForgotPassword()
    {
        authViewModel.forgotPassword(mapOf(Constant.EMAIL to binding.emailEDT.asString()))
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
        return true
    }


}