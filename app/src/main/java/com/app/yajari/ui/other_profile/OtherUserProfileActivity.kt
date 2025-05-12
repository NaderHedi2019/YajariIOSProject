package com.app.yajari.ui.other_profile
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.AnnouncementType
import com.app.yajari.data.FavouriteUserResponse
import com.app.yajari.data.ReportData
import com.app.yajari.data.StatusType
import com.app.yajari.databinding.ActivityOtherUserProfileBinding
import com.app.yajari.databinding.BottomDialogReportBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.databinding.LayoutOptionPopupBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.announcement_details.ReportAdapter
import com.app.yajari.ui.home.DonationAdapter
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.ui.rate_review.RateReviewActivity
import com.app.yajari.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class OtherUserProfileActivity : BaseActivity<ActivityOtherUserProfileBinding>(),DonationAdapter.DonationListener,ReportAdapter.ReportListener {
    var from=""
    private var mPopupMenuBinding: LayoutOptionPopupBinding? = null
    private lateinit var popupWindow: PopupWindow
    private var isLoader = false
    private var loaderMore = false
    private var isUserLike=false
    private var isAnnouncementLike=false
    private var dummy = mutableListOf<AnnouncementResponse.Data>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var otherUserAdapter: DonationAdapter? = null
    var isFirstTime = true
    private var type=Constant.OBJECT
    private var status=""
    private var announcementId = ""
    private var reason = ""
    private var userId=0
    private var isSwipeRefresh = false
    private var donationList = mutableListOf<AnnouncementResponse.Data>()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this@OtherUserProfileActivity) }
    private var mBottomReportBinding: BottomDialogReportBinding? = null

    override fun getViewBinding()= ActivityOtherUserProfileBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        from=intent.getStringExtra(Constant.FROM).toString()
        userId=intent.getIntExtra(Constant.USER_ID,0)
    }

    private fun setUpDonationRV() {
        binding.noDataLayout.noDataTV.text=getString(R.string.no_announcement_found)
        binding.otherProfileRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            otherUserAdapter = DonationAdapter(this@OtherUserProfileActivity,this@OtherUserProfileActivity,Constant.OBJECT)
            adapter = otherUserAdapter
            (lManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (otherUserAdapter!!.getItemViewType(position) == 1) {
                            1
                        } else {
                            (lManager as GridLayoutManager).spanCount
                        }
                    }

                }
        }
        otherUserAdapter?.setObjectType(type,true)
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {

                if (!isFirstTime && !isLoader && loaderMore) {
                    callOtherUser(false)
                }
                isFirstTime = false
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.otherProfileRV.addOnScrollListener(scrollListener)
    }

    override fun click() {
        binding.apply {
            objectATV.setDropDownBackgroundDrawable(ContextCompat.getDrawable(this@OtherUserProfileActivity,R.drawable.shape_dropdown_radius))
            availableATV.setDropDownBackgroundDrawable(ContextCompat.getDrawable(this@OtherUserProfileActivity,R.drawable.shape_dropdown_radius))

            objectATV.setSafeOnClickListener {
                val arrayAdapter = object : ArrayAdapter<AnnouncementType>(this@OtherUserProfileActivity,  R.layout.adapter_dropdown, R.id.optionNameTV,   Constant.Singleton.typeList(this@OtherUserProfileActivity)) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val line = view.findViewById(R.id.lineView) as View
                        if(Constant.Singleton.typeList(this@OtherUserProfileActivity).size-1==position)
                        {
                            line.gone()
                        }
                        else{
                            line.visible()
                        }
                        return view
                    }
                }
                objectATV.setAdapter(arrayAdapter)
                objectATV.setOnItemClickListener { _, _, position, _ ->
                    val value =    Constant.Singleton.typeList(this@OtherUserProfileActivity)[position].value
                    objectATV.setText(Constant.Singleton.typeList(this@OtherUserProfileActivity)[position].name, false)
                    type=value
                    refreshData()
                    otherUserAdapter?.setObjectType(type,false)
                }
                objectATV.showDropDown()
            }
            availableATV.setSafeOnClickListener {
                val arrayAdapter = object : ArrayAdapter<StatusType>(this@OtherUserProfileActivity,  R.layout.adapter_dropdown, R.id.optionNameTV,  Constant.Singleton.otherStatusList(this@OtherUserProfileActivity)) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val line = view.findViewById(R.id.lineView) as View
                        if(Constant.Singleton.otherStatusList(this@OtherUserProfileActivity).size-1==position)
                        {
                            line.gone()
                        }
                        else{
                            line.visible()
                        }
                        return view
                    }
                }


                availableATV.setAdapter(arrayAdapter)
                availableATV.setOnItemClickListener { _, _, position, _ ->
                    val value =   Constant.Singleton.otherStatusList(this@OtherUserProfileActivity)[position].value
                    availableATV.setText(Constant.Singleton.otherStatusList(this@OtherUserProfileActivity)[position].name, false)
                    status=value
                    refreshData()
                }
                availableATV.showDropDown()
            }
            optionIV.setSafeOnClickListener {
                showOptionPopup(optionIV)
            }
            backIV.setSafeOnClickListener {
                if(isUserLike)
                {
                    setResult(RESULT_OK)
                }
                else if(isAnnouncementLike)
                {
                    val intent= Intent()
                    intent.putExtra(Constant.ANNOUNCEMENT_LIKE,isAnnouncementLike)
                    setResult(RESULT_OK,intent)
                }
                finish()
            }
            ratingTV.setSafeOnClickListener {
                start<RateReviewActivity>("2",Constant.USER_ID to userId.toString())
            }
            totalReviewTV.setSafeOnClickListener {
                start<RateReviewActivity>("2",Constant.USER_ID to userId.toString())
            }
        }
    }

    private fun refreshData()
    {
        isSwipeRefresh = true
        isFirstTime=true
        dummy.clear()
        callOtherUser(true)
    }

    override fun setUpObserver() {
            setUpDonationRV()
            setRVScrollListener()
            callOtherUser(true)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
            binding.otherProfileRV.run {
                addItemDecoration(SpacesItemDecoration(spacingInPixels))
            }
        profileViewModel.getUserFromPref()
        profileViewModel.userDataResponse.observe(this)
        {
            otherUserAdapter?.setUserId(it.id!!)
        }
        profileViewModel.favouriteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->           {
                   // mProgressDialog.showProgressDialog()
                }
                Status.SUCCESS -> {
                    isAnnouncementLike=true
                    //refreshData()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@OtherUserProfileActivity,
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
                                    callFavouriteAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.favResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    isUserLike=true
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@OtherUserProfileActivity,
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
                                    callFavouriteUser()
                                }
                            }
                        }
                    }
                }
            }
        }


        profileViewModel.otherUserProfileResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->
                {
                    if(isLoader && !isSwipeRefresh) {
                        mProgressDialog.showProgressDialog()
                    }
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    binding.otherUserCL.visible()
                    response.data?.data?.let {
                        bindData(it.announcement)
                        if(isFirstTime)
                        {
                            setupProfileData(it.user!!)
                        }
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@OtherUserProfileActivity,
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
                                    callOtherUser(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.reportUserResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->           {
                     mProgressDialog.showProgressDialog()
                }
                Status.SUCCESS -> {
                   showSuccessDialog()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@OtherUserProfileActivity,
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
                                    callReportUser()
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

    @SuppressLint("SetTextI18n")
    private fun setupProfileData(user: FavouriteUserResponse.Data) {
        binding.apply {
            likeIV.isSelected = user.isBookmark==1
            profileIV.loadImage(user.profileImage, R.drawable.img_user)
            donateNameTV.text = user.name
            ratingTV.text = user.avgRating.toString()
            totalReviewTV.text = "(${user.totalReview})"
            donationCountTV.text = user.donationCount.toString()
            collectionCountTV.text = user.collectionCount.toString()
            locationTV.text = user.address
            if (!user.distance.isNullOrEmpty()) {
                distanceTV.text = "${String.format("%.1f", user.distance.toFloat())} km"
            }
            addressTV.text = user.address
            if (!user.distance.isNullOrEmpty()) {
                kmTV.text = "${String.format("%.1f", user.distance.toFloat())} km"
            }
            if(!user.createdAt.isNullOrBlank()) {
                joiningDateTV.text = "${getString(R.string.member_since)} ${getDateFromMillis(getMilliSeconds(
                    user.createdAt,
                    Constant.backEndUTCFormat,
                    true
                )
                )}"
            }
            likeIV.setSafeOnClickListener {
                if (user.isBookmark == 1) {
                    user.isBookmark = 0
                } else {
                    user.isBookmark = 1
                }
                if (user.isBookmark == 1) {
                    animateLikeImage(this@OtherUserProfileActivity, likeIV, false)
                } else {
                    animateLikeImage(this@OtherUserProfileActivity, likeIV, true)
                }
                likeIV.isSelected= user.isBookmark ==1
                callFavouriteUser()
            }
        }
    }

    private fun callOtherUser(isLoadMore: Boolean) {
        isLoader=isLoadMore
        profileViewModel.otherUserProfile(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to dummy.size.toString(),
                Constant.TYPE to type.lowercase(),
                Constant.STATUS to status.lowercase(),
                Constant.USER_ID to userId.toString()
            )
        )
    }


    override fun onBaseBackPressed() {
        if(isUserLike)
        {
            setResult(RESULT_OK)
        }
        else if(isAnnouncementLike)
        {
            val intent= Intent()
            intent.putExtra(Constant.ANNOUNCEMENT_LIKE,isAnnouncementLike)
            setResult(RESULT_OK,intent)
        }
        finish()
    }

    private fun bindData(donationData: MutableList<AnnouncementResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.otherProfileRV.gone()
            return
        }
        loaderMore=true
        binding.noDataLayout.root.gone()
        binding.otherProfileRV.visible()
        donationList.addAll(donationData)
        dummy.addAll(donationData)
        otherUserAdapter?.removeLoading()
        otherUserAdapter?.addData(donationData, isSwipeRefresh)
        otherUserAdapter?.addLoading()
        if (donationData.isEmpty() || donationData.size < 10) {
            otherUserAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
    }


    override fun onItemLike(announcementData: AnnouncementResponse.Data) {

            announcementId = announcementData.id.toString()
            callFavouriteAnnouncement()
    }

    override fun onItemClick(announcementData: AnnouncementResponse.Data) {
        val intent = Intent(this@OtherUserProfileActivity, AnnouncementDetailsActivity::class.java)
        intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementData.id.toString())
        startLikeResult.launch(intent)
    }

    private val startLikeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    isSwipeRefresh=true
                    refreshData()
                }
            }
        }

    private fun callFavouriteAnnouncement() {
        profileViewModel.favouriteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }
    private fun callFavouriteUser()
    {
        profileViewModel.unFavouriteUser(mapOf(Constant.FRIEND_ID to userId.toString()))
    }

    private fun showOptionPopup(optionIV: AppCompatImageView)
    {
        mPopupMenuBinding = LayoutOptionPopupBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(this)
        popupWindow.isFocusable = true
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = mPopupMenuBinding!!.root
        popupWindow.setBackgroundDrawable(null)
        popupWindow.elevation = 5f
        mPopupMenuBinding?.apply {
            deleteTV.gone()
            view.gone()
            editTV.text= getString(R.string.report)
            editTV.setSafeOnClickListener {
                reportUserDialog()
                popupWindow.dismiss()
            }

        }
        popupWindow.showAsDropDown(optionIV, 0, -30)
    }

    private fun reportUserDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@OtherUserProfileActivity, R.style.AppBottomSheetDialogTheme)
        mBottomReportBinding = BottomDialogReportBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomReportBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomReportBinding?.apply {
            titleTV.text= getString(R.string.report_user)
            descTV.text=getString(R.string.report_user_desc)
            reportRV.apply {
                adapter = ReportAdapter(
                    this@OtherUserProfileActivity,
                    Constant.Singleton.reportAnnouncementList(this@OtherUserProfileActivity)
                )
            }
            confirmBTN.setSafeOnClickListener {
                if (reason.isEmpty()) {
                    showToasty(
                        this@OtherUserProfileActivity,
                        getString(R.string.please_select_reason),
                        "2"
                    )
                } else {
                    mBottomSheetDialog.dismiss()
                    callReportUser()
                }
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun callReportUser() {
      profileViewModel.reportUser(mapOf(Constant.OTHER_USER_ID to userId.toString(),Constant.REASON to reason))
    }

    override fun onItemReportClick(data: ReportData) {
        reason=data.value
    }
    private fun showSuccessDialog() {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                        msgTV.text = getStr(R.string.report_msg)
                        descTV.visible()
                    continueBTN.setSafeOnClickListener {
                        start<MainActivity>("1")
                        dismiss()
                    }
                }
            }
        }
    }


}