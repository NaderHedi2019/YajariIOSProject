package com.app.yajari.ui.profile
import android.annotation.SuppressLint
import androidx.navigation.fragment.findNavController
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.LoginResponse
import com.app.yajari.data.ProfileOptionData
import com.app.yajari.databinding.DialogLogoutBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentNewProfileBinding
import com.app.yajari.ui.account_parameter.AccountParameterActivity
import com.app.yajari.ui.alert_settings.AlertSettingsActivity
import com.app.yajari.ui.community_impact.CommunityImpactActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.my_announcement.MyAnnouncementActivity
import com.app.yajari.ui.my_favourite.MyFavouriteActivity
import com.app.yajari.ui.my_package.MyPackageActivity
import com.app.yajari.ui.our_partner.OurPartnerActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.ui.rate_review.RateReviewActivity
import com.app.yajari.ui.yajari.YajariActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getDateFromMillis
import com.app.yajari.utils.getMilliSeconds
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.underline
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class NewProfileFragment : BaseFragment<FragmentNewProfileBinding>(),ProfileOptionAdapter.ProfileListener {
    private var userId=""
    override fun getViewBinding()= FragmentNewProfileBinding.inflate(layoutInflater)
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
        binding.apply {
            toolbar.backIV.gone()
            toolbar.titleTV.text=requireContext().getStr(R.string.my_profile)
            totalReviewTV.underline()
            ratingTV.underline()
        }
        setProfileRV()

    }

    override fun click() {
        binding.apply {
            totalReviewTV.setSafeOnClickListener {
                requireActivity().start<RateReviewActivity>("2",Constant.USER_ID to userId)
            }
            ratingTV.setSafeOnClickListener {
                requireActivity().start<RateReviewActivity>("2",Constant.USER_ID to userId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.getUserFromPref()
    }

    override fun setUpObserver() {
        profileViewModel.userDataResponse.observe(this){
           userId=it.id.toString()
            binding.apply {
                if(it.id!=0) {
                    setupProfileData(it)
                    getProfile()
                }
            }
        }
        profileViewModel.logoutResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    setFilterBlank()
                    requireActivity().start<LoginActivity>("1")
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    requireActivity().runOnUiThread { response.message?.let { msg -> showToasty(requireContext(),
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    profileViewModel.logoutUser()
                }
                Status.NETWORK_ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    requireActivity().runOnUiThread {
                        requireActivity().fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callLogout()
                                }
                            }
                        }
                    }
                }
            }
        }

        profileViewModel.profileResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    response.data!!.data?.let {
                        binding.apply {
                            setupProfileData(it)
                            (activity as MainActivity).setProfileImage()
                        }
                    }
                }
                Status.ERROR -> {
                    requireActivity().runOnUiThread { response.message?.let { msg -> showToasty(requireContext(),
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    profileViewModel.logoutUser()
                }
                Status.NETWORK_ERROR -> {
                    requireActivity().runOnUiThread {
                        requireActivity().fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callLogout()
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
                requireActivity().start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setProfileRV()
    {
        val optionList= mutableListOf<ProfileOptionData>()
        optionList.add(ProfileOptionData(R.drawable.ic_alert_warning,getString(R.string.alert_setting)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_my_announcement,getString(R.string.my_announcement)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_my_favourite,getString(R.string.my_fav)))
     //   optionList.add(ProfileOptionData(R.drawable.ic_profile_my_package,getString(R.string.my_package)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_community_impact,getString(R.string.community_impact)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_yajari,getString(R.string.yajari)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_our_partner,getString(R.string.out_partner)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_account_parameter,getString(R.string.account_parameter)))
        optionList.add(ProfileOptionData(R.drawable.ic_profile_logout,getString(R.string.logout)))
        binding.profileOptionRV.apply {
            adapter=ProfileOptionAdapter(listener = this@NewProfileFragment, optionList = optionList)
        }
    }

    override fun onItemOptionClick(position: Int) {
        when(position)
        {
            0 ->{
                requireActivity().start<AlertSettingsActivity>("2")
            }
            1 -> {
                requireActivity().start<MyAnnouncementActivity>("2")
            }
            2 -> {
                requireActivity().start<MyFavouriteActivity>("2")
            }
//            3 -> {
//                requireActivity().start<MyPackageActivity>("2")
//            }
            3 -> {
                requireActivity().start<CommunityImpactActivity>("2")
            }
            4 -> {
                requireActivity().start<YajariActivity>("2")
            }
            5 -> {
                requireActivity().start<OurPartnerActivity>("2")
            }
            6 -> {
                requireActivity().start<AccountParameterActivity>("2")
            }
            7 -> {
                showLogoutDialog()
            }
        }
    }
    private fun showLogoutDialog()
    {
        requireActivity().commonDialog(R.layout.dialog_logout) {
            run {
                val verifyDialog = DialogLogoutBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    yesBTN.setSafeOnClickListener {
                        dismiss()
                        callLogout()
                    }
                    noBTN.setSafeOnClickListener {
                        dismiss()
                    }
                }
            }
        }
    }

    private fun callLogout()
    {
        profileViewModel.logout()
    }

    private fun getProfile(){
        profileViewModel.getProfile()
    }

    @SuppressLint("SetTextI18n")
    private fun setupProfileData(loginResponse: LoginResponse.Data)
    {
        loginResponse.let {
            binding.apply {
                profileIV.loadImage(it.profileImage,R.drawable.img_user)
                donateNameTV.text = it.name
                ratingTV.text=it.avgRating
                totalReviewTV.text="(${it.totalReview})"
                donationCountTV.text=it.donationCount.toString()
                collectionCountTV.text=it.collectionCount.toString()
                if(!it.createdAt.isNullOrBlank()) {
                    joiningDateTV.text = "${getString(R.string.member_since)} ${getDateFromMillis(getMilliSeconds(
                        it.createdAt,
                            Constant.backEndUTCFormat,
                            true
                        )
                    )}"
                }
            }

        }
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