package com.app.yajari.ui.community_impact
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.CommunityImpactResponse
import com.app.yajari.databinding.ActivityCommunityImpactBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.other_profile.OtherUserProfileActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale

class CommunityImpactActivity : BaseActivity<ActivityCommunityImpactBinding>() {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this@CommunityImpactActivity) }
    override fun getViewBinding()= ActivityCommunityImpactBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.community_impact)
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
        }
    }
    override fun setUpObserver() {
        callGetCommunityImpact()
        profileViewModel.communityImpactResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                        mProgressDialog.showProgressDialog()
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    binding.communityImpactSV.visible()
                    response.data?.data?.let {
                        setupCommunityImpact(it)
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@CommunityImpactActivity,
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
                                    callGetCommunityImpact()
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

    private fun setupCommunityImpact(it: CommunityImpactResponse.Data) {
        binding.apply {
            if(it.user!!.isNotEmpty()) {
                goldUserNameTV.text=it.user[0].name
                goldUserCountTV.text=it.user[0].donationCount.toString()
                goldUserIV.loadImage(it.user[0].profileImage,R.drawable.img_user)

                silverUserNameTV.text=it.user[1].name
                silverUserCountTV.text=it.user[1].donationCount.toString()
                silverUserIV.loadImage(it.user[1].profileImage,R.drawable.img_user)

                bronzUserNameTV.text=it.user[2].name
                bronzUserCountTV.text=it.user[2].donationCount.toString()
                bronzUserIV.loadImage(it.user[2].profileImage,R.drawable.img_user)
            }

            totalUserCountTV.text=formatAsCommaSeparated(it.totalUser!!)
            totalDonationCountTV.text=formatAsCommaSeparated(it.totalDonation!!)
            goldCL.setSafeOnClickListener { _ ->
                start<OtherUserProfileActivity>("2",Constant.USER_ID to it.user[0].id)
            }
            silverCL.setSafeOnClickListener { _ ->
                start<OtherUserProfileActivity>("2",Constant.USER_ID to it.user[1].id)
            }
            bronzCL.setSafeOnClickListener { _ ->
                start<OtherUserProfileActivity>("2",Constant.USER_ID to it.user[2].id)
            }
        }
    }

    private fun callGetCommunityImpact() {
       profileViewModel.communityImpact()
    }

    override fun onBaseBackPressed() {
        finish()
    }
    fun formatAsCommaSeparated(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }

}