package com.app.yajari.ui.pages

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityPagesBinding
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PagesActivity : BaseActivity<ActivityPagesBinding>() {
    private var key = ""
    private var sUrl = ""
    private var language=""
    private val profileViewModel: ProfileViewModel by viewModel()
    override fun getViewBinding()= ActivityPagesBinding.inflate(layoutInflater)
    override fun initObj() {
            changeStatusBarColor("#FFFFFF")
            key=intent.getStringExtra(Constant.KEY).toString()
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }

        }
    }

    override fun setUpObserver() {

        profileViewModel.getLanguage()
        profileViewModel.language.observe(this)
        {
            language=it
            setWebView(binding.webView,binding.progressBar)
            setWebClient(binding.webView,binding.progressBar)
        }
    }

    override fun onBaseBackPressed() {
       finish()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView(webView: WebView, progressBar: ProgressBar) {
        progressBar.visible()

        webView.apply {
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            when (key) {
               Constant.PRIVACY -> {
                    binding.toolbar.titleTV.text = getStr(R.string.privacy_policy)
                    sUrl = Constant.PRIVACY_URL
                }
               Constant.TERMS -> {
                    binding.toolbar.titleTV.text = getStr(R.string.terms_amp_conditions)
                    sUrl = Constant.TERMS_URL
                }
                Constant.ABOUT -> {
                    binding.logoIV.visible()
                    binding.toolbar.titleTV.text = getStr(R.string.about_us)
                    sUrl = Constant.ABOUT_US_URL
                }
            }
            val map=HashMap<String,String>()
            map["localization"] = language
            loadUrl(sUrl,map)
        }
    }

    private fun setWebClient(webView: WebView, progressBar: ProgressBar) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (progressBar.isVisible) {
                    progressBar.gone()
                }

            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                if (progressBar.isVisible) {
                    progressBar.gone()
                }

                showToasty(
                    this@PagesActivity,
                    "Error:- $description".ifNotNullOrElse({ _it -> _it }, { "" }),
                    "2"
                )
            }
        }
    }

}