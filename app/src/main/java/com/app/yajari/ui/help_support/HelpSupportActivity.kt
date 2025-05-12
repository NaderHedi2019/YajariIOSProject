package com.app.yajari.ui.help_support
import android.view.View
import androidx.core.content.ContextCompat
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityHelpSupportBinding
import com.app.yajari.databinding.DialogCommonMsgBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class HelpSupportActivity : BaseActivity<ActivityHelpSupportBinding>() {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()= ActivityHelpSupportBinding.inflate(layoutInflater)

    override fun initObj() {
       changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text=getStr(R.string.help_amp_support)
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            submitBTN.setSafeOnClickListener {
               if(isValid())
               {
                   callHelpSupport()
               }
            }
            msgEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                {
                    msgCL.background= ContextCompat.getDrawable(this@HelpSupportActivity, R.drawable.shape_edt_selected)
                    aboutIV.setImageResource(R.drawable.ic_write_msg_selected)
                }
                else{
                    msgCL.background= ContextCompat.getDrawable(this@HelpSupportActivity, R.drawable.shape_edt_unselected)
                    aboutIV.setImageResource(R.drawable.ic_write_msg_unselected)
                }
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.helpSupportResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showMessageDialog()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@HelpSupportActivity,
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
                                    callHelpSupport()
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

    private fun callHelpSupport() {
       profileViewModel.helpSupport(mapOf(Constant.NAME to binding.nameEDT.asString(),Constant.EMAIL to binding.emailEDT.asString(),
           Constant.MESSAGE to binding.msgEDT.asString()))
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
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        finish()
                    }
                }
            }
        }
    }

    private fun isValid():Boolean{
        if (binding.nameEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_name).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.nameEDT.requestFocus()
            return false
        }
        else if (!isNameValid(binding.nameEDT.asString())) {
            showToasty(
                this,
                getStr(R.string.please_enter_valid_name).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.nameEDT.requestFocus()
            return false
        }
        else if (binding.emailEDT.asString() == "") {
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
        else if (binding.msgEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_message).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.msgEDT.requestFocus()
            return false
        }
        return true
    }

}