package com.app.yajari.ui.language

import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.LanguageData
import com.app.yajari.databinding.ActivityChangeLangugaeBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeLanguage
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ChangeLanguageActivity : BaseActivity<ActivityChangeLangugaeBinding>(),
    LanguageAdapter.LanguageListener {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val languages=Constant.languageList()
    override fun getViewBinding() = ActivityChangeLangugaeBinding.inflate(layoutInflater)
    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        binding.toolbar.apply {
            backIV.setSafeOnClickListener {
                finish()
            }
            titleTV.text = getStr(R.string.change_language)
        }
    }

    override fun click() {
        binding.apply {
            changeLanguageBTN.setSafeOnClickListener {
                profileViewModel.setLanguage(Constant.DEFAULT_LANGUAGE)
                changeLanguage(Locale(Constant.DEFAULT_LANGUAGE))
                callChangeLanguage()
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.getLanguage()
        profileViewModel.language.observe(this)
        { it ->
            Constant.DEFAULT_LANGUAGE = it
            languages.map { it.isSelected=false }
            languages.find { it.countryCode == Constant.DEFAULT_LANGUAGE } ?.isSelected = true
            setLanguageRV()
        }
        profileViewModel.changeLanguageResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    start<MainActivity>("1")
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@ChangeLanguageActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
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
                                    callChangeLanguage()
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.logout.observe(this) {
            if (it) {
                start<LoginActivity>("1")
            }
        }
    }

    private fun callChangeLanguage() {
        profileViewModel.changeLanguage(mapOf("lang" to Constant.DEFAULT_LANGUAGE))
    }

    override fun onBaseBackPressed() {
        finish()
    }

    private fun setLanguageRV() {
        binding.languageRV.apply {
            adapter = LanguageAdapter(this@ChangeLanguageActivity, languages)
        }
    }

    override fun onItemLanguageClick(data: LanguageData) {
        Constant.DEFAULT_LANGUAGE = data.countryCode
    }
}