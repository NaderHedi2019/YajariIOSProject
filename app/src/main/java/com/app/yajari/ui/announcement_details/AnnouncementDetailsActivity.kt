package com.app.yajari.ui.announcement_details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.AnnouncementDetailsResponse
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.data.ReportData
import com.app.yajari.databinding.ActivityAnnouncementDetailsBinding
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.BottomDialogReportBinding
import com.app.yajari.databinding.BottomDialogSendRequestBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.databinding.LayoutOptionPopupBinding
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.chat_detail.ChatDetailActivity
import com.app.yajari.ui.create_announcement.PublishAnnouncementActivity
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.other_profile.OtherUserProfileActivity
import com.app.yajari.ui.rate_review.RateReviewActivity
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.gson.Gson
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class AnnouncementDetailsActivity : BaseActivity<ActivityAnnouncementDetailsBinding>(),
    ReportAdapter.ReportListener, BaseActivity.LocationListener {
    private var mBottomSendRequestBinding: BottomDialogSendRequestBinding? = null
    private var from = ""
    private var type = ""
    private var userId = 0
    private var threadId = 0
    private var latitude = 0.00
    private var longitude = 0.00
    private var announcementId = ""
    private var otherUserId = ""
    private var reason = ""
    private var isLikeAnnouncement = false
    private var announcementData: MyAnnouncementResponse.Data? = null
    private var isFilterApply = false
    private var shareURL: Uri? = null
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null
    private var mBottomReportBinding: BottomDialogReportBinding? = null
    private var mPopupMenuBinding: LayoutOptionPopupBinding? = null
    private lateinit var popupWindow: PopupWindow
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private var objectID = ""
    private var pushType = ""

    override fun getViewBinding() = ActivityAnnouncementDetailsBinding.inflate(layoutInflater)
    override fun initObj() {
        changeStatusBarBlackColor("#000000")
        from = intent.getStringExtra(Constant.FROM).toString()
        pushType = intent.getStringExtra(Constant.PUSH_TYPE).toString()
        announcementId = intent.getStringExtra(Constant.ANNOUNCEMENT_ID).toString()
        isFilterApply = intent.getBooleanExtra(Constant.IsFILTER, false)
        if (isFilterApply) {
            latitude = intent.getDoubleExtra(Constant.LAT, 0.0)
            longitude = intent.getDoubleExtra(Constant.LNG, 0.0)
        }
        assignLocationListener(this@AnnouncementDetailsActivity)
    }

    override fun click() {
        binding.apply {
            ratingTV.underline()
            totalReviewTV.underline()
            backIV.setSafeOnClickListener {
                if (from == Constant.SPLASH || from ==Constant.PUSH && isTaskRoot) {
                    start<MainActivity>("1")
                } else if (isLikeAnnouncement) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    finish()
                }
            }
            sendRequestBTN.setSafeOnClickListener {
                if (userId == 0) {
                    loginContinueDialog()
                } else if (threadId == 0) {
                    showSendRequestBottomDialog()
                } else {
                    start<ChatDetailActivity>("2", Constant.THREAD_ID to threadId.toString())
                }
            }
            donateCV.setSafeOnClickListener {
                if (userId == 0) {
                    loginContinueDialog()
                } else {
                    val intent = Intent(
                        this@AnnouncementDetailsActivity,
                        OtherUserProfileActivity::class.java
                    )
                    intent.putExtra(Constant.USER_ID, otherUserId.toInt())
                    startUserLikeResult.launch(intent)
                }
            }
            totalReviewTV.setSafeOnClickListener {
                start<RateReviewActivity>("2",Constant.USER_ID to otherUserId)
            }
            ratingTV.setSafeOnClickListener {
                start<RateReviewActivity>("2",Constant.USER_ID to otherUserId)
            }
            optionIV.setSafeOnClickListener {
                showOptionPopup(optionIV)
            }
            if (from == Constant.EDIT || pushType=="1" || pushType=="2" || pushType=="7") {
                announcementData = intent.serializable(Constant.ANNOUNCEMENT_DATA)
                donateTV.gone()
                donateCV.gone()
                optionIV.gone()
                sendRequestBTN.gone()
                editBTN.visible()
                cancelBTN.visible()
                cancelBTN.setSafeOnClickListener {
                    showConfirmDialog()
                }
                editBTN.setSafeOnClickListener {
                    val intent = Intent(
                        this@AnnouncementDetailsActivity,
                        PublishAnnouncementActivity::class.java
                    )
                    intent.putExtra(Constant.FROM, Constant.EDIT)
                    intent.putExtra(Constant.ANNOUNCEMENT_DATA, announcementData)
                    startUpdateResult.launch(intent)
                }
            } else if (from == Constant.FULL_DETAILS) {
                donateTV.gone()
                donateCV.gone()
                sendRequestBTN.gone()
                likeIV.gone()
                optionIV.gone()
            }


        }

    }

    private val startUpdateResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    private val startUserLikeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val isAnnouncementLike =
                        result.data?.getBooleanExtra(Constant.ANNOUNCEMENT_LIKE, false)
                    if (!isAnnouncementLike!!) {
                        callAnnouncementDetails()
                    } else {
                        isLikeAnnouncement = isAnnouncementLike
                    }
                }
            }
        }

    override fun setUpObserver() {
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            userId = it.id!!
            if (userId == 0) {
                mProgressDialog.showProgressDialog()
                runTimePermission()
            } else {
                if (!isFilterApply) {
                    latitude = it.lat!!.toDouble()
                    longitude = it.lng!!.toDouble()
                }
                callAnnouncementDetails()
            }
        }
        announcementViewModel.announcementDetailsResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    if (userId != 0) {
                        mProgressDialog.showProgressDialog()
                    }
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    binding.announcementDetailsCL.visible()
                    response.data?.data?.let {
                        response.data.data.isThreadId.let {
                            threadId = it!!
                        }

                        response.data.data.finalized.let {
                            objectID = response.data.data.finalized?.id.toString()
                        }

                        setupDetails(it)
                    }
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                    finish()
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callAnnouncementDetails()
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.sendRequestResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    mProgressDialog.showProgressDialog()
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showSuccessDialog(false)
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callSendRequest()
                                }
                            }
                        }
                    }
                }
            }
        }

        announcementViewModel.favouriteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                }

                Status.SUCCESS -> {
                    isLikeAnnouncement = true
                    mProgressDialog.dismissProgressDialog()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
        announcementViewModel.userFavouriteResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
        announcementViewModel.deleteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    setResult(RESULT_OK)
                    finish()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callDeleteAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }

        announcementViewModel.reportAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showSuccessDialog(true)
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@AnnouncementDetailsActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callReportAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }
        chatViewModel.changeStatusResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    mProgressDialog.showProgressDialog()
                }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showSuccessDialog(false)
                    finish()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this,
                                msg,
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    chatViewModel.logoutUser()
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
                                    callChangeStatus(Constant.FINALIZED)
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.logout.observe(this) {
            if (it) {
                start<LoginActivity>("1")
            }
        }
    }

    private fun callReportAnnouncement() {
        announcementViewModel.reportAnnouncement(
            mapOf(
                Constant.REASON to reason,
                Constant.ANNOUNCEMENT_ID to announcementId
            )
        )
    }

    override fun onBaseBackPressed() {
        if (from == Constant.SPLASH || from ==Constant.PUSH && isTaskRoot) {
            start<MainActivity>("1")
        } else if (isLikeAnnouncement) {
            setResult(RESULT_OK)
            finish()
        } else {
            finish()
        }
    }

    private fun setSlider(gallery: MutableList<AnnouncementDetailsResponse.Gallery>?) {
        if(gallery!!.isEmpty())
        {
            gallery.add(AnnouncementDetailsResponse.Gallery())
        }
        binding.apply {
            sliderVP.run {
                adapter = ImageSliderAdapter(gallery)
                isUserInputEnabled = true
            }
            dotsIndicator.setupWithViewPager(sliderVP)
            dotsIndicator.setIndicatorGap(resources.getDimension(R.dimen.padding_6))
            dotsIndicator.setSliderWidth(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setSliderHeight(resources.getDimension(R.dimen.margin_10))
            dotsIndicator.setIndicatorStyle(IndicatorStyle.CIRCLE)
        }

    }

    private fun showSendRequestBottomDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@AnnouncementDetailsActivity, R.style.AppBottomSheetDialogTheme)
        mBottomSendRequestBinding = BottomDialogSendRequestBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomSendRequestBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomSendRequestBinding?.apply {
            sendRequestDescTV.text = getString(R.string.send_request_to_take_this_food, type)
            sendNowBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                if (messageEDT.asString() == "") {
                    showToasty(
                        this@AnnouncementDetailsActivity,
                        getString(R.string.please_enter_your_message),
                        "2"
                    )
                } else {
                    callSendRequest()
                }
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun showSuccessDialog(isReport: Boolean) {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    if (isReport) {
                        msgTV.text = getStr(R.string.report_msg)
                        descTV.visible()
                    }
                    continueBTN.setSafeOnClickListener {
                        if(from ==Constant.PUSH && isTaskRoot)
                        {
                            start<MainActivity>("1")
                        }
                        else if (!isReport) {
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            start<MainActivity>("1")
                        }
                        dismiss()
                    }
                }
            }
        }
    }

    private fun showConfirmDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@AnnouncementDetailsActivity, R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            msgTV.text = getStr(R.string.delete_announcement_msg)
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                callDeleteAnnouncement()
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

    private fun reportAnnouncementDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@AnnouncementDetailsActivity, R.style.AppBottomSheetDialogTheme)
        mBottomReportBinding = BottomDialogReportBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomReportBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomReportBinding?.apply {
            reportRV.apply {
                adapter = ReportAdapter(
                    this@AnnouncementDetailsActivity,
                    Constant.Singleton.reportAnnouncementList(this@AnnouncementDetailsActivity)
                )
            }
            confirmBTN.setSafeOnClickListener {
                if (reason.isEmpty()) {
                    showToasty(
                        this@AnnouncementDetailsActivity,
                        getString(R.string.please_select_reason),
                        "2"
                    )
                } else {
                    mBottomSheetDialog.dismiss()
                    callReportAnnouncement()
                }
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }


    private fun showOptionPopup(optionIV: AppCompatImageView) {
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
            editTV.text = getString(R.string.report)
            editTV.setSafeOnClickListener {
                popupWindow.dismiss()
                if (userId == 0) {
                    loginContinueDialog()
                } else {
                    reportAnnouncementDialog()
                }
            }

        }
        popupWindow.showAsDropDown(optionIV, 0, -30)
    }

    override fun onItemReportClick(data: ReportData) {
        reason = data.value
    }

    private fun callAnnouncementDetails() {
        announcementViewModel.announcementDetails(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementId,
                Constant.LAT to latitude.toString(),
                Constant.LNG to longitude.toString()
            )
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setupDetails(data: AnnouncementDetailsResponse.Data) {
        if(pushType=="1" || pushType=="2")
        {
            announcementData=MyAnnouncementResponse.Data()
            announcementData?.id=data.id
            announcementData?.category=data.category
            announcementData?.condition=data.condition
            announcementData?.createdAt=data.createdAt
            announcementData?.distance=data.distance
            val gallery= mutableListOf<MyAnnouncementResponse.Gallery>()
            for (galleryData in data.gallery!!) {
                gallery.add(MyAnnouncementResponse.Gallery(announcementId = galleryData.announcementId, file = galleryData.file, id = galleryData.id))
            }
            announcementData?.gallery=gallery
            announcementData?.image=data.image
            announcementData?.isBookmark=data.isBookmark
            announcementData?.objectType=data.objectType
            announcementData?.status=data.status
            announcementData?.title=data.title
            announcementData?.type=data.type
            announcementData?.expirationDate=data.expirationDate
            announcementData?.commercial=data.commercial
            announcementData?.description=data.description
            announcementData?.lat=data.lat
            announcementData?.lng=data.lng
            announcementData?.conditionId=data.conditionId
            announcementData?.categoryId=data.categoryId
            announcementData?.location=data.location
        }
        createDynamicLink(data)
        threadId = data.isThreadId!!
        type = data.type.toString()
        binding.apply {
            if (threadId == 0) {
                sendRequestBTN.text = getStr(R.string.contact)

            } else {
                sendRequestBTN.text = getStr(R.string.chat)
            }
            if(data.objectType==Constant.REQUEST)
            {
                donateTV.text= getString(R.string.request_by)
            }
            else{
                donateTV.text= getString(R.string.donate_by)
            }
            if (data.type == Constant.FOOD && data.objectType == Constant.DONATION) {
                likeIV.visible()
                tagTV.gone()
            } else if (data.type == Constant.OBJECT && data.objectType == Constant.DONATION) {
                likeIV.visible()
                tagTV.visible()
            } else {
                likeIV.gone()
                tagTV.gone()
            }
            if (from == Constant.FULL_DETAILS) {
                likeIV.gone()
            }

            tagTV.text = data.condition
            likeIV.isSelected = data.isBookmark == 1
            titleTV.text = data.title
            typeTV.text = data.category
            if (!data.distance.isNullOrEmpty()) {
                distanceTV.text = "${String.format("%.1f", data.distance.toFloat())} km"
            }
            locationTV.text = data.location
            if (!data.createdAt.isNullOrEmpty()) {
                agoTV.text = TimeAgo(context = this@AnnouncementDetailsActivity).timeAgo(
                    getMilliSeconds(
                        data.createdAt,
                        Constant.backEndUTCFormat,
                        true
                    )
                )
            }
            detailDescTV.text = data.description
            //User Details
            otherUserId = data.user?.id.toString()
            profileIV.loadImage(data.user?.profileImage, R.drawable.img_user)
            donateNameTV.text = data.user?.name
            ratingTV.text = data.user?.avgRating
            totalReviewTV.text = "(${data.user?.totalReview})"
            donationCountTV.text = data.user?.donationCount.toString()
            collectionCountTV.text = data.user?.collectionCount.toString()
            userAddressTV.text = data.user?.address
            if (!data.user?.distance.isNullOrEmpty()) {
                kmTV.text = "${String.format("%.1f", data.user?.distance?.toFloat())} km"
            }
            likeUserIV.isSelected = data.user?.isBookmark == 1
            val mapURL =
                "http://maps.google.com/maps/api/staticmap?center=" + data.lat + "," + data.lng + "&zoom=15&size=644x300&sensor=false&markers=color:red|"+data.lat+","+data.lng+"&key=${
                    getStr(R.string.map_key)
                }"

            mapIV.loadImage(mapURL, R.drawable.ic_placeholder_yajari)
            setSlider(data.gallery)
            when (data.status) {
                Constant.AVAILABLE -> {
                    announcementTV.text = getStr(R.string.available)
                    announcementTV.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_available_announcement,
                        0,
                        0,
                        0
                    )
                    announcementTV.setTextColor(getClr(R.color.color_available))
                }
                Constant.REJECTED -> {
                    announcementTV.text=getStr(R.string.reject_by_admin)
                    announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject_announcement,0,0,0)
                    announcementTV.setTextColor(getClr(R.color.color_rejected))
                }
                Constant.PENDING -> {
                    announcementTV.text=getStr(R.string.pending)
                    announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pending_announcement,0,0,0)
                    announcementTV.setTextColor(getClr(R.color.color_pending))
                    editBTN.visible()
                    cancelBTN.visible()
                    cancelReservedBTN.gone()
                    finalizedBTN.gone()
                    takerTV.gone()
                    takerCV.gone()
                }

                Constant.RESERVED -> {
                    announcementTV.text = getStr(R.string.reserved)
                    announcementTV.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_reserved_announcement,
                        0,
                        0,
                        0
                    )
                    announcementTV.setTextColor(getClr(R.color.color_reserved))
                    if (from == Constant.EDIT) {
                        editBTN.gone()
                        cancelBTN.gone()
                        cancelReservedBTN.visible()
                        finalizedBTN.visible()
                        takerTV.gone()
                        takerCV.gone()
                    }
                    finalizedBTN.setSafeOnClickListener {
                        callChangeStatus(Constant.FINALIZED)
                    }
                    cancelReservedBTN.setSafeOnClickListener {
                        callChangeStatus(Constant.AVAILABLE)
                    }
                }

                Constant.FINALIZED -> {
                    announcementTV.text = getStr(R.string.finalized)
                    announcementTV.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_announcement,
                        0,
                        0,
                        0
                    )
                    announcementTV.setTextColor(getClr(R.color.color_finalize))
                    if (from == Constant.EDIT) {
                        editBTN.gone()
                        cancelBTN.gone()
                        cancelReservedBTN.gone()
                        finalizedBTN.gone()
                        takerTV.visible()
                        takerCV.visible()
                        takerNameTV.text = data.finalized?.name.toString()
                        takerImageIV.loadImage(data.finalized?.profile_image.toString(),R.drawable.ic_placeholder_yajari)
                        takerCV.setSafeOnClickListener {
                            if (userId == 0) {
                                loginContinueDialog()
                            } else {
                                val intent = Intent(
                                    this@AnnouncementDetailsActivity,
                                    OtherUserProfileActivity::class.java
                                )
                                intent.putExtra(Constant.USER_ID, data.finalized?.id)
                                startUserLikeResult.launch(intent)
                            }
                        }
                    }
                }
            }
            shareIV.setSafeOnClickListener {
                shareURL?.let {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            getStr(R.string.app_name)
                        )
                        var shareMessage = getString(R.string.view_this_announcement)
                        shareMessage += shareURL
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: Exception) {
                        Log.e("TAG", "setValues::::  " + e.message)
                    }
                }
            }
            likeIV.setSafeOnClickListener {
                if (userId == 0) {
                    loginContinueDialog()
                } else {
                    if (data.isBookmark == 1) {
                        data.isBookmark = 0
                    } else {
                        data.isBookmark = 1
                    }
                    if (data.isBookmark == 1) {
                        animateLikeImage(this@AnnouncementDetailsActivity, likeIV, false)
                    } else {
                        animateLikeImage(this@AnnouncementDetailsActivity, likeIV, true)
                    }
                    likeIV.isSelected = data.isBookmark == 1
                    callFavouriteAnnouncement()
                }
            }
            likeUserIV.setSafeOnClickListener {
                if (userId == 0) {
                    loginContinueDialog()
                } else {
                    if (data.user?.isBookmark == 1) {
                        data.user.isBookmark = 0
                    } else {
                        data.user?.isBookmark = 1
                    }
                    if (data.user?.isBookmark == 1) {
                        animateLikeImage(this@AnnouncementDetailsActivity, likeUserIV, false)
                    } else {
                        animateLikeImage(this@AnnouncementDetailsActivity, likeUserIV, true)
                    }
                    likeUserIV.isSelected = data.user?.isBookmark == 1
                    callFavouriteUser()
                }
            }

        }
    }

    private fun callChangeStatus(status: String) {
        chatViewModel.changeStatus(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementId,
                Constant.OBJECT_ID to objectID,
                Constant.STATUS to status,
                Constant.THREAD_ID to threadId.toString()
            )
        )
    }

    private fun callSendRequest() {
        announcementViewModel.sendRequest(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementId,
                Constant.MESSAGE to mBottomSendRequestBinding?.messageEDT!!.asString()
            )
        )
    }

    private fun callFavouriteAnnouncement() {
        announcementViewModel.favouriteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }


    private fun callFavouriteUser() {
        announcementViewModel.userFavourite(mapOf(Constant.FRIEND_ID to otherUserId))
    }

    private fun createDynamicLink(data: AnnouncementDetailsResponse.Data) {
        val caption = getString(R.string.view_announcement)
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("${Constant.DEEPLINK_URL}?announcement_id=${data.id}}"))
            .setDomainUriPrefix(getStr(R.string.firebase_deeplink))
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(packageName)
                    .setFallbackUrl(
                        Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                    ).build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder().setTitle(caption)
                    .setImageUrl(Uri.parse(data.image)).build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener {
                shareURL = it.shortLink!!
            }
            .addOnFailureListener {
                Log.i("LOG_", "createDynamicLink: FAIL ${it.localizedMessage}")
            }
    }

    override fun onLocationGet(location: Location) {
        if (!isFilterApply) {
            latitude = location.latitude
            longitude = location.longitude
        }
        callAnnouncementDetails()
    }

    private fun callDeleteAnnouncement() {
        announcementViewModel.deleteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }


}