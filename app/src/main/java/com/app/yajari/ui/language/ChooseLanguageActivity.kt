package com.app.yajari.ui.language
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.LanguageData
import com.app.yajari.databinding.ActivityChooseLanguageBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.splash.viewmodel.SplashViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.changeLanguage
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ChooseLanguageActivity : BaseActivity<ActivityChooseLanguageBinding>(),LanguageAdapter.LanguageListener {
    private val splashViewModel: SplashViewModel by viewModel()

    override fun getViewBinding()= ActivityChooseLanguageBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        setLanguageRV()
    }

    override fun click() {
        binding.apply {
            continueBTN.setSafeOnClickListener {
                splashViewModel.setLanguage(Constant.DEFAULT_LANGUAGE)
                changeLanguage(Locale(Constant.DEFAULT_LANGUAGE))
                start<LoginActivity>("1")
            }
        }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
       finish()
    }

    private fun setLanguageRV()
    {
        binding.languageRV.apply {
            adapter=LanguageAdapter(this@ChooseLanguageActivity,Constant.languageList())
        }
    }

    override fun onItemLanguageClick(data: LanguageData) {
        Constant.DEFAULT_LANGUAGE=data.countryCode
    }

}