package com.app.yajari.ui.change_password
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityChangePasswordBinding
import com.app.yajari.databinding.DialogCommonMsgBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()=ActivityChangePasswordBinding.inflate(layoutInflater)
    override fun initObj() {
       changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            toolbar.titleTV.text=getString(R.string.change_password)

            changePasswordBTN.setSafeOnClickListener {
             if(isValid())
             {
                 callChangePassword()
             }
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.changePasswordResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showMessageDialog()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@ChangePasswordActivity,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    profileViewModel.logoutUser()
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
                                    callChangePassword()
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.logout.observe(this){
            if(it)
            {
                start<LoginActivity>("1")
            }
        }
    }

    private fun callChangePassword() {
        profileViewModel.changePassword(mapOf(Constant.OLD_PASS to binding.passwordEDT.asString(),
            Constant.NEW_PASS to binding.newPasswordEDT.asString(),
            Constant.CONF_NEW_PASS to binding.confirmPasswordEDT.asString()))
    }

    override fun onBaseBackPressed() {
        finish()
    }
    private fun showMessageDialog()
    {
        commonDialog(R.layout.dialog_common_msg) {
            run {
                val msgDialog = DialogCommonMsgBinding.inflate(layoutInflater)
                setContentView(msgDialog.root)
                msgDialog.apply {
                    msgTV.text=getStr(R.string.change_pwd_msg)
                    descTV.gone()
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        finish()
                    }
                }
            }
        }
    }

    private fun isValid():Boolean{
        if (binding.passwordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_current_password).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.passwordEDT.requestFocus()
            return false
        }
        else if (binding.newPasswordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_new_password).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.newPasswordEDT.requestFocus()
            return false
        }
        else if(!isValidPasswordFormat(binding.newPasswordEDT.asString()))
        {
            showToasty(this, getStr(R.string.new_password_invalid).ifNotNullOrElse({it }, { "" }), "2")
            binding.newPasswordEDT.requestFocus()
            return false
        }
        else if(binding.passwordEDT.asString()==binding.newPasswordEDT.asString())
        {
            showToasty(
                this,
                getStr(R.string.current_new_password_not_same).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.newPasswordEDT.requestFocus()
            return false
        }
        else if (binding.confirmPasswordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_confirm_password).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.confirmPasswordEDT.requestFocus()
            return false
        }
        else if(!isValidPasswordFormat(binding.confirmPasswordEDT.asString()))
        {
            showToasty(this, getStr(R.string.confirm_password_invalid).ifNotNullOrElse({it }, { "" }), "2")
            binding.confirmPasswordEDT.requestFocus()
            return false
        }
        else if (binding.confirmPasswordEDT.asString()!=binding.newPasswordEDT.asString()) {
            showToasty(
                this,
                getStr(R.string.change_pwd_miss_match).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.newPasswordEDT.requestFocus()
            return false
        }
        return true
    }

}