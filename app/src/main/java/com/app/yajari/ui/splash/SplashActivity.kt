package com.app.yajari.ui.splash
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivitySplashBinding
import com.app.yajari.databinding.BottomDialogLocationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogVersionCheckerBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.language.ChooseLanguageActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.splash.viewmodel.SplashViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.Status
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.hideKeyboard
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val splashViewModel: SplashViewModel by viewModel()
    private var sFrom = ""
    private var announcementId = ""
    private var mBottomLocationBinding: BottomDialogLocationBinding? = null

    override fun getViewBinding()= ActivitySplashBinding.inflate(layoutInflater)

    override fun initObj() {

    }
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission()
        }
        else{
            getReceiveLink()
            callVersionCheck()
        }
    }

    override fun click() {
    }

    override fun setUpObserver() {
        splashViewModel.versionCheckResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    hideKeyboard()
                }
                Status.SUCCESS -> {
                    binding.help2TV.text=getString(R.string.help_desc_2,it.data?.data?.totalUser.toString())
                    binding.help1TV.text=getString(R.string.help_desc_1,it.data?.data?.totalDonation.toString())
                    if (sFrom != "") {
                        isLoggedIn()
                    }
                    else {
                        isLoggedIn()
                    }
                }
                Status.ERROR -> {
                    it.data?.let { data ->
                        binding.help2TV.text=getString(R.string.help_desc_2,data.data?.totalUser.toString())
                        binding.help1TV.text=getString(R.string.help_desc_1,data.data?.totalDonation.toString())
                        commonDialog(data.data?.isForceUpdate!!, "error")
                    }

                }
                Status.UNAUTHORIZED -> {

                }
                Status.NETWORK_ERROR -> {
                    it.message?.let {
                        runOnUiThread {
                            fullScreenDialog(R.layout.dialog_no_internet) {
                                val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                                setContentView(commonDialog.root)
                                commonDialog.apply {
                                    tryAgainBTN.setSafeOnClickListener {
                                        dismiss()
                                        callVersionCheck()
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

    }

    override fun onBaseBackPressed() {

    }
    private fun callVersionCheck() {
        val map = mapOf("type" to Constant.APP_TYPE, "version" to getAppVersionName())
        splashViewModel.versionCheck(map)
    }
    private fun getAppVersionName(): String {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Version not found"
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun isLoggedIn() {
        splashViewModel.isLoggedIn.observe(this) { it ->
            if (it) {
                GlobalScope.launch {
                    delay(4000)
                    if(sFrom.isNotEmpty())
                    {
                        start<AnnouncementDetailsActivity>("0",Constant.ANNOUNCEMENT_ID to announcementId,Constant.FROM to sFrom)
                    }
                    else {
                        start<MainActivity>("1")
                    }
                }
            }
            else if(splashViewModel.getGuestUser())   {
                GlobalScope.launch {
                    delay(4000)
                    if(sFrom.isNotEmpty())
                    {
                        start<AnnouncementDetailsActivity>("0",Constant.ANNOUNCEMENT_ID to announcementId,Constant.FROM to sFrom)
                    }
                    else if(splashViewModel.getSelectedLanguage()=="")
                    {
                        start<ChooseLanguageActivity>("1")
                    }
                    else {
                        start<MainActivity>("1")
                    }
                }
            }
            else if(splashViewModel.getSelectedLanguage()=="")
            {
                start<ChooseLanguageActivity>("1")
            }
            else {
                GlobalScope.launch {
                    delay(3000)
                    start<LoginActivity>("1")
                }
            }
        }
    }

    private fun commonDialog(isForceUpdate: Int, sType: String) {
        runOnUiThread {
            fullScreenDialog(R.layout.dialog_version_checker) {
                val commonDialog = DialogVersionCheckerBinding.inflate(layoutInflater)
                setContentView(commonDialog.root)
                commonDialog.apply {
                    if (sType == "error"){
                        if (isForceUpdate == 1){
                            skipTV.gone()
                            updateBTN.setSafeOnClickListener {
                                dismiss()
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}"))
                                startActivity(intent)
                            }
                        }else{
                            skipTV.setOnClickListener {
                                dismiss()
                                isLoggedIn()
                            }
                            updateBTN.setSafeOnClickListener {
                                dismiss()
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}"))
                                startActivity(intent)
                            }
                        }
                    }else{
                        skipTV.gone()
                        updateBTN.setSafeOnClickListener {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getReceiveLink()
            callVersionCheck()
        }
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission()
    {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                getReceiveLink()
                callVersionCheck()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationSetting()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun getReceiveLink(): String {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.e("===>", deepLink.toString())
                    announcementId= deepLink?.getQueryParameter(Constant.ANNOUNCEMENT_ID).toString()
                    if(announcementId.isEmpty())
                    {
                        announcementId=""
                        sFrom = ""
                    }
                    else{
                        sFrom = Constant.SPLASH
                    }
                }
            }
            .addOnFailureListener(this) { e -> Log.i("===>", "getDynamicLink:onFailure", e) }

        return announcementId
    }

    private fun showNotificationSetting()
    {
        val mBottomSheetDialog =
            BottomSheetDialog(this@SplashActivity, R.style.AppBottomSheetDialogTheme)
        mBottomLocationBinding = BottomDialogLocationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomLocationBinding!!.root)
        mBottomSheetDialog.setCanceledOnTouchOutside(false)
        mBottomSheetDialog.setCancelable(false)
        mBottomLocationBinding?.apply {
            titleTV.text=getStr(R.string.notification_permission_required)
            descTV.text=getStr(R.string.notification_push_message)
            goBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }
        }
        mBottomSheetDialog.show()
    }

}