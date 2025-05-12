package com.app.yajari.ui.account_parameter
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityAccountParameterBinding
import com.app.yajari.databinding.BottomDialogDeleteConfirmationBinding
import com.app.yajari.databinding.BottomDialogPasswordBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.change_password.ChangePasswordActivity
import com.app.yajari.ui.edit_profile.EditProfileActivity
import com.app.yajari.ui.language.ChangeLanguageActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.dismissProgressDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showProgressDialog
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class AccountParameterActivity : BaseActivity<ActivityAccountParameterBinding>() {
    private var mBottomConfirmBinding: BottomDialogDeleteConfirmationBinding? = null
    private var mBottomPasswordBinding: BottomDialogPasswordBinding? = null
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding()=ActivityAccountParameterBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.account_parameter)
            toolbar.backIV.setSafeOnClickListener {
               finish()
            }
            changePasswordTV.setSafeOnClickListener {
                start<ChangePasswordActivity>("2")
            }
            infoTV.setSafeOnClickListener {
                start<EditProfileActivity>("2")
            }
            deleteAccountTV.setSafeOnClickListener {
                showConfirmDialog()
            }
            languageTV.setSafeOnClickListener {
                start<ChangeLanguageActivity>("2")
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.deleteAccountResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    setFilterBlank()
                   start<LoginActivity>("1")
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@AccountParameterActivity,
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
                                    callDeleteAccount()
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

    private fun callDeleteAccount() {
        profileViewModel.deleteAccount(mapOf(Constant.PASSWORD to mBottomPasswordBinding?.passwordEDT!!.asString()))
    }

    override fun onBaseBackPressed() {
       finish()
    }
    private fun showConfirmDialog() {
        val mBottomSheetDialog = BottomSheetDialog(this@AccountParameterActivity, R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogDeleteConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                showPasswordDialog()
            }
            noBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun showPasswordDialog()
    {
        val mBottomSheetDialog = BottomSheetDialog(this@AccountParameterActivity, R.style.AppBottomSheetDialogTheme)
        mBottomPasswordBinding = BottomDialogPasswordBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomPasswordBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomPasswordBinding?.apply {
            deleteBTN.setSafeOnClickListener {
                if(passwordEDT.asString()=="")
                {
                    showToasty(this@AccountParameterActivity,getStr(R.string.please_enter_password),"2")
                }
                else{
                    mBottomSheetDialog.dismiss()
                    callDeleteAccount()
                }
            }

            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun setFilterBlank()
    {
        //Filter
        /* Object Donation Filter */
         Constant.objectDonationStatus= ""
         Constant.objectDonationCondition=""
         Constant.objectDonationDistance=""
         Constant.objectDonationDistanceAddress=""
         Constant.objectDonationLatitude=0.0
         Constant.objectDonationLongitude=0.0
         Constant.objectDonationIsFilterApply=false

        /* Object Request Filter*/
         Constant.objectRequestStatus= ""
         Constant.objectRequestDistance=""
         Constant.objectRequestDistanceAddress=""
         Constant.objectRequestLatitude=0.0
         Constant.objectRequestLongitude=0.0
         Constant.objectRequestIsFilterApply=false

        /* Food Donation Filter */
        Constant.foodDonationStatus= ""
        Constant.foodDonationDistance=""
        Constant.foodDonationDistanceAddress=""
        Constant.foodDonationLatitude=0.0
        Constant.foodDonationLongitude=0.0
        Constant.foodDonationIsFilterApply=false

        /* Food Request Filter */
        Constant.foodRequestStatus= ""
        Constant.foodRequestDistance=""
        Constant.foodRequestDistanceAddress=""
        Constant.foodRequestLatitude=0.0
        Constant.foodRequestLongitude=0.0
        Constant.foodRequestIsFilterApply=false

        //Search
        /* Object Donation Filter */
        Constant.searchObjectDonationStatus= ""
        Constant.searchObjectDonationCondition=""
        Constant.searchObjectDonationDistance=""
        Constant.searchObjectDonationDistanceAddress=""
        Constant.searchObjectDonationLatitude=0.0
        Constant.searchObjectDonationLongitude=0.0
        Constant.searchObjectDonationIsFilterApply=false

        /* Object Request Filter*/
        Constant.searchObjectRequestStatus= ""
        Constant.searchObjectRequestDistance=""
        Constant.searchObjectRequestDistanceAddress=""
        Constant.searchObjectRequestLatitude=0.0
        Constant.searchObjectRequestLongitude=0.0
        Constant.searchObjectRequestIsFilterApply=false

        /* Food Donation Filter */
        Constant.searchFoodDonationStatus= ""
        Constant.searchFoodDonationDistance=""
        Constant.searchFoodDonationDistanceAddress=""
        Constant.searchFoodDonationLatitude=0.0
        Constant.searchFoodDonationLongitude=0.0
        Constant.searchFoodDonationIsFilterApply=false

        /* Food Request Filter */
        Constant.searchFoodRequestStatus= ""
        Constant.searchFoodRequestDistance=""
        Constant.searchFoodRequestDistanceAddress=""
        Constant.searchFoodRequestLatitude=0.0
        Constant.searchFoodRequestLongitude=0.0
        Constant.searchFoodRequestIsFilterApply=false
    }

}