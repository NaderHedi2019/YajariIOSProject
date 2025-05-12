package com.app.yajari.ui.faq
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FaqActivityBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.dismissProgressDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showProgressDialog
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class FAQActivity : BaseActivity<FaqActivityBinding>(){
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()= FaqActivityBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        callFaq()
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text=getString(R.string.faqs)
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.faqResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data.let {
                        if(it!!.isEmpty())
                        {
                            binding.noDataLayout.noDataTV.text= getString(R.string.no_faq_found)
                            binding.noDataLayout.root.visible()
                            binding.faqRV.gone()
                        }
                        else {
                            binding.noDataLayout.root.gone()
                            binding.faqRV.visible()
                            binding.faqRV.apply {
                                adapter = FaqAdapter(it)
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@FAQActivity,
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
                                    callFaq()
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

    override fun onBaseBackPressed() {
        finish()
    }

    private fun callFaq()
    {
        profileViewModel.faq()
    }

}