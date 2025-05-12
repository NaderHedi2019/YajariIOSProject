package com.app.yajari.ui.chat_detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.ChatDetailResponse
import com.app.yajari.databinding.ActivityChatDetailBinding
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.other_profile.OtherUserProfileActivity
import com.app.yajari.ui.rate_review.SubmitReviewActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getClr
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.invisible
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.underline
import com.app.yajari.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatDetailActivity : BaseActivity<ActivityChatDetailBinding>() {
    private var type = ""
    private var from = ""
    private var isLoader = false
    private var isCancel = false
    private var isFinalize = false
    private var isReserved = false
    private var loginUserId = ""
    private var announcementUserId = ""
    private var threadId = ""
    private var receiverId = ""
    private var otherUserId = ""
    private var receiverImage = ""
    private var receiverName = ""
    private var status = ""
    private var announcementId = ""
    private var objectId = ""
    private var moreRequest = ""
    private var chatDetailsAdapter: ChatDetailsAdapter? = null
    private var chatMessageList = mutableListOf<ChatDetailResponse.Data>()
    private var isFromNewMSG = false
    private var isMessageSend = false
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val chatViewModel: ChatViewModel by viewModel()
    private var isEnd = false
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var isScrolledToBottom: Boolean = true
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null
    private var mBottomMoreRequestBinding: BottomDialogConfirmationBinding? = null

    private var reviewManager: ReviewManager? = null

    override fun getViewBinding() = ActivityChatDetailBinding.inflate(layoutInflater)

    override fun initObj() {
        reviewManager = ReviewManagerFactory.create(this)
        changeStatusBarColor("#FFFFFF")
        from = intent.getStringExtra(Constant.FROM).toString()
        type = intent.getStringExtra(Constant.TYPE).toString()
        threadId = intent.getStringExtra(Constant.THREAD_ID).toString()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.CHAT))
        Constant.IS_IN_CHAT = true
        Log.e("Data tye","Type${type}")
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            chatMessageList.clear()
            isFromNewMSG = true
            callChatMessageList(false)
        }
    }

    private fun callChatMessageList(isLoadMore: Boolean) {
        isLoader = isLoadMore
        chatViewModel.chatDetails(
            mapOf(
                Constant.THREAD_ID to threadId,
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to chatMessageList.size.toString()
            )
        )
    }

    private fun callSendMessage() {
        chatViewModel.sendMessage(
            mapOf(
                Constant.RECEIVER_ID to receiverId,
                Constant.THREAD_ID to threadId,
                Constant.MESSAGE to binding.sendEDT.asString()
            )
        )
        binding.sendEDT.setText("")
        isMessageSend = true
    }

    private fun callChangeStatus() {
        chatViewModel.changeStatus(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementId,
                Constant.OBJECT_ID to objectId,
                Constant.STATUS to status,
                Constant.MORE_REQ to moreRequest,
                Constant.THREAD_ID to threadId
            )
        )
    }

    private fun populateSendMessage(index: Int, chatData: ChatDetailResponse.Data) {
        chatDetailsAdapter?.updateMessage(
            index,
            chatData
        )
    }

    override fun onDestroy() {
        Constant.IS_IN_CHAT = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    private fun setUpMessageRV() {
        binding.chatRV.run {
            val lManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
            layoutManager = lManager
            setHasFixedSize(true)
            chatDetailsAdapter = ChatDetailsAdapter(this@ChatDetailActivity, loginUserId)
            adapter = chatDetailsAdapter
            scrollToPosition(0)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItemCount = lManager.itemCount - 1
                    lastVisibleItem = lManager.findLastVisibleItemPosition()
                    if (!isLoader && totalItemCount == lastVisibleItem && !isEnd) {
                        callChatMessageList(false)
                        //  isLoader = false
                        //isEnd = false
                    }
                }
            })

        }
    }


    override fun click() {
        binding.apply {
            backIV.setSafeOnClickListener {
                if (isTaskRoot) {
                    start<MainActivity>("1")
                } else if (from == Constant.DIRECT_CHAT) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    setResult(RESULT_OK)
                    finish()
                }
            }
            writeReviewTV.underline()
            writeReviewTV.setSafeOnClickListener {
                val intent = Intent(this@ChatDetailActivity, SubmitReviewActivity::class.java)
                intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementId)
                if (loginUserId == announcementUserId) {
                    intent.putExtra(Constant.END_PONT, "rate_review_user")
                } else {
                    intent.putExtra(Constant.END_PONT, "rate_review")
                }
                startChatResult.launch(intent)
            }
            writeReviewCloseTV.underline()
            writeReviewCloseTV.setSafeOnClickListener {
                val intent = Intent(this@ChatDetailActivity, SubmitReviewActivity::class.java)
                intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementId)
                if (loginUserId == announcementUserId) {
                    intent.putExtra(Constant.END_PONT, "rate_review_user")
                } else {
                    intent.putExtra(Constant.END_PONT, "rate_review")
                }
                startChatResult.launch(intent)
            }
            writeReviewReservedTV.underline()
            writeReviewReservedTV.setSafeOnClickListener {
                val intent = Intent(this@ChatDetailActivity, SubmitReviewActivity::class.java)
                intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementId)
                if (loginUserId == announcementUserId) {
                    intent.putExtra(Constant.END_PONT, "rate_review_user")
                } else {
                    intent.putExtra(Constant.END_PONT, "rate_review")
                }
                startChatResult.launch(intent)
            }
            reservedBTN.setSafeOnClickListener {
                isReserved = true
                isFinalize = false
                isCancel = false
                showMoreRequestConfirm()
            }
            cancelBTN.setSafeOnClickListener {
                isReserved = false
                isFinalize = false
                isCancel = true
                showConfirmDialog()
            }
            closedBTN.setSafeOnClickListener {
                isReserved = false
                isFinalize = true
                isCancel = false
                showConfirmDialog()
            }
            finalizeBTN.setSafeOnClickListener {
                isReserved = false
                isFinalize = true
                isCancel = false
                showConfirmDialog()
            }
            sendIV.setSafeOnClickListener {
                if (sendEDT.asString() == "") {
                    showToasty(this@ChatDetailActivity, getString(R.string.message_error), "2")
                } else {

                    val chatMessage = ChatDetailResponse.Data(
                        createdAt = getString(R.string.sending),
                        id = 0,
                        senderId = loginUserId.toInt(),
                        receiverId = 0,
                        message = binding.sendEDT.asString()
                    )

                    chatDetailsAdapter?.addMessageToFirst(mutableListOf(chatMessage))
                    binding.chatRV.scrollToPosition(0)
                    callSendMessage()
                }
            }
        }
    }

    private val startChatResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    isReserved = false
                    isFinalize = false
                    isCancel = false
                    chatMessageList.clear()
                    setUpMessageRV()
                    callChatMessageList(true)
                }
            }
        }

    override fun setUpObserver() {
        chatViewModel.getUserFromPref()

        chatViewModel.userDataResponse.observe(this)
        {
            loginUserId = it.id.toString()
            setUpMessageRV()
            observeKeyBoard()
            callChatMessageList(true)
        }

        chatViewModel.chatDetailsResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->
                    if (isLoader) {
                        mProgressDialog.showProgressDialog()
                    }

                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()

                    response.data?.let {
                        if (isFromNewMSG) {
                            chatMessageList.clear()
                            chatMessageList.addAll(it.data!!)
                            if (chatMessageList.size > 0) {
                                binding.noDataLayout.root.gone()
                                binding.chatRV.visible()
                            }
                            for (i in chatMessageList.indices) {
                                populateSendMessage(i, chatMessageList[i])
                            }
                            binding.chatRV.scrollToPosition(0)
                            isFromNewMSG = false
                        } else {
                            bindData(it.data)
                        }
                        announcementId = it.announcement?.id.toString()
                        setupChatView(it.announcement, it.isReview)
                    }
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
                                    callChatMessageList(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        chatViewModel.sendMessageResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    binding.progressBar.visible()
                    binding.sendIV.isEnabled = false
                }

                Status.SUCCESS -> {
                    binding.progressBar.gone()
                    binding.sendIV.isEnabled = true
                    mProgressDialog.dismissProgressDialog()
                    chatMessageList.clear()
                    isFromNewMSG = true
                    callChatMessageList(false)
                }

                Status.ERROR -> {
                    binding.progressBar.gone()
                    binding.sendIV.isEnabled = true
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
                    binding.progressBar.gone()
                    binding.sendIV.isEnabled = true
                    mProgressDialog.dismissProgressDialog()
                    chatViewModel.logoutUser()
                }

                Status.NETWORK_ERROR -> {
                    binding.progressBar.gone()
                    binding.sendIV.isEnabled = true
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callSendMessage()
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
                    showSuccessDialog()
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
                                    callChangeStatus()
                                }
                            }
                        }
                    }
                }
            }
        }

        chatViewModel.logout.observe(this) {
            if (it) {
                start<LoginActivity>("1")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupChatView(announcement: ChatDetailResponse.Announcement?, review: Int?) {
        binding.apply {
            announcement?.let { it ->
                type=it.type.toString()
                announcementUserId = it.userId.toString()
                if (it.objectType == Constant.REQUEST && it.objectId == null && it.userId == loginUserId.toInt()) {
                    profileCloseCL.visible()
                    productIV.visible()
                    profileIV.gone()
                    productIV.loadImage(it.thumbImage?.file, R.drawable.ic_placeholder_yajari)
                    nameTV.text = it.title
                    profileCloseIV.loadImage(receiverImage, R.drawable.img_user)
                    closeNameTV.text = receiverName
                } else if (it.objectType == Constant.REQUEST && it.objectId != loginUserId.toInt() && it.userId == loginUserId.toInt()) {
                    profileCloseCL.visible()
                    productIV.visible()
                    profileIV.gone()
                    productIV.loadImage(it.thumbImage?.file, R.drawable.ic_placeholder_yajari)
                    nameTV.text = it.title
                    profileCloseIV.loadImage(receiverImage, R.drawable.img_user)
                    closeNameTV.text = receiverName
                    if (it.status == Constant.FINALIZED) {
                        closeTV.visible()
                        closedBTN.gone()
                        sendEDT.gone()
                        sendIV.gone()
                        if (review == 0) {
                            writeReviewCloseTV.visible()
                        } else {
                            writeReviewCloseTV.gone()
                        }
                    } else {
                        closedBTN.visible()
                        closeTV.gone()
                    }

                } else if (it.objectType == Constant.REQUEST && it.objectId == null && it.userId != loginUserId.toInt()) {
                    finalizeCL.visible()
                    productIV.gone()
                    profileIV.visible()
                    profileIV.loadImage(receiverImage, R.drawable.img_user)
                    nameTV.text = receiverName
                    productFinalizeIV.loadImage(
                        it.thumbImage?.file,
                        R.drawable.ic_placeholder_yajari
                    )
                    finalizeNameTV.text = it.title
                    when (it.status) {
                        Constant.AVAILABLE -> {
                            statusTV.text = getStr(R.string.available)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_available_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_available))
                        }

                        Constant.RESERVED -> {
                            statusTV.text = getStr(R.string.reserved)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_reserved_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_reserved))
                        }

                        Constant.FINALIZED -> {
                            statusTV.text = getStr(R.string.finalized)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_finalize))
                            if (review == 0) {
                                writeReviewTV.visible()
                            } else {
                                writeReviewTV.gone()
                            }
                        }
                    }

                } else if (it.objectType == Constant.REQUEST && it.objectId == loginUserId.toInt() && it.userId != loginUserId.toInt()) {
                    finalizeCL.visible()
                    productIV.gone()
                    profileIV.visible()
                    profileIV.loadImage(receiverImage, R.drawable.img_user)
                    nameTV.text = receiverName
                    productFinalizeIV.loadImage(
                        it.thumbImage?.file,
                        R.drawable.ic_placeholder_yajari
                    )
                    finalizeNameTV.text = it.title

                    when (it.status) {
                        Constant.AVAILABLE -> {
                            statusTV.visible()
                            finalizeLabelTV.gone()
                            statusTV.text = getStr(R.string.available)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_available_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_available))
                        }

                        Constant.RESERVED -> {
                            statusTV.visible()
                            finalizeLabelTV.gone()
                            statusTV.text = getStr(R.string.reserved)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_reserved_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_reserved))
                        }

                        Constant.FINALIZED -> {
                            statusTV.text = getStr(R.string.finalized)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_finalize))
                            finalizeLabelTV.visible()
                            statusTV.invisible()
                            sendEDT.gone()
                            sendIV.gone()
                            if (review == 0) {
                                writeReviewTV.visible()
                            } else {
                                writeReviewTV.gone()
                            }
                        }
                    }

                } else if (it.objectType == Constant.REQUEST && it.objectId != loginUserId.toInt() && it.userId != loginUserId.toInt()) {
                    finalizeCL.visible()
                    productIV.gone()
                    profileIV.visible()
                    profileIV.loadImage(receiverImage, R.drawable.img_user)
                    nameTV.text = receiverName
                    productFinalizeIV.loadImage(
                        it.thumbImage?.file,
                        R.drawable.ic_placeholder_yajari
                    )
                    it.title.toString().also { finalizeNameTV.text = it }
                    when (it.status) {
                        Constant.AVAILABLE -> {
                            statusTV.text = getStr(R.string.available)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_available_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_available))
                        }

                        Constant.RESERVED -> {
                            statusTV.text = getStr(R.string.reserved)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_reserved_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_reserved))
                        }

                        Constant.FINALIZED -> {
                            statusTV.text = getStr(R.string.finalized)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_finalize))
                            sendEDT.gone()
                            sendIV.gone()
                            if (review == 0) {
                                writeReviewTV.visible()
                            } else {
                                writeReviewTV.gone()
                            }
                        }
                    }

                } else if (it.objectType == Constant.DONATION && it.userId == loginUserId.toInt()) {
                    reservedCL.visible()
                    productIV.visible()
                    profileIV.gone()
                    productIV.loadImage(it.thumbImage?.file, R.drawable.ic_placeholder_yajari)
                    nameTV.text = it.title
                    profileReservedIV.loadImage(receiverImage, R.drawable.ic_placeholder_yajari)
                    reservedNameTV.text = receiverName
                    if (it.objectId == null) {
                        reservedBTN.visible()
                    }
                    when (it.status) {
                        Constant.AVAILABLE -> {
                            reservedStatusTV.text = getStr(R.string.available)
                            reservedStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_available_announcement,
                                0,
                                0,
                                0
                            )
                            reservedStatusTV.setTextColor(getClr(R.color.color_available))
                            if (it.objectId == null) {
                                reservedBTN.visible()
                                cancelBTN.gone()
                                finalizeBTN.gone()
                            }
                        }

                        Constant.RESERVED -> {
                            reservedStatusTV.text = getStr(R.string.reserved)
                            reservedStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_reserved_announcement,
                                0,
                                0,
                                0
                            )
                            reservedStatusTV.setTextColor(getClr(R.color.color_reserved))
                            reservedBTN.gone()
                            if (it.objectId == otherUserId.toInt()) {
                                reservedBTN.gone()
                                cancelBTN.visible()
                                finalizeBTN.visible()
                            }

                        }

                        Constant.FINALIZED -> {
                            reservedStatusTV.text = getStr(R.string.finalized)
                            reservedStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_announcement,
                                0,
                                0,
                                0
                            )
                            reservedStatusTV.setTextColor(getClr(R.color.color_finalize))
                            if (it.objectId != null) {
                                reservedBTN.gone()
                                cancelBTN.gone()
                                finalizeBTN.gone()
                            }
                            sendEDT.gone()
                            sendIV.gone()
                            if (review == 0) {
                                writeReviewReservedTV.visible()
                            } else {
                                writeReviewReservedTV.gone()
                            }

                        }
                    }
                } else if (it.objectType == Constant.DONATION && it.userId != loginUserId.toInt()) {
                    finalizeCL.visible()
                    productIV.gone()
                    profileIV.visible()
                    profileIV.loadImage(receiverImage, R.drawable.img_user)
                    nameTV.text = receiverName
                    productFinalizeIV.loadImage(
                        it.thumbImage?.file,
                        R.drawable.ic_placeholder_yajari
                    )
                    it.title.toString().also { finalizeNameTV.text = it }


                    when (it.status) {
                        Constant.AVAILABLE -> {
                            statusTV.text = getStr(R.string.available)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_available_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_available))
                        }

                        Constant.RESERVED -> {
                            statusTV.text = getStr(R.string.reserved)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_reserved_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_reserved))

                        }

                        Constant.FINALIZED -> {
                            statusTV.text = getStr(R.string.finalized)
                            statusTV.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_announcement,
                                0,
                                0,
                                0
                            )
                            statusTV.setTextColor(getClr(R.color.color_finalize))
                            sendEDT.gone()
                            sendIV.gone()
                            if (it.objectId == loginUserId.toInt()) {
                                statusTV.invisible()
                                finalizeLabelTV.visible()
                                if (review == 0) {
                                    writeReviewTV.visible()
                                } else {
                                    writeReviewTV.gone()
                                }
                            }
                        }
                    }

                }
                productIV.setSafeOnClickListener {
                    start<AnnouncementDetailsActivity>(
                        "2",
                        Constant.ANNOUNCEMENT_ID to announcementId
                    )
                }
                productFinalizeIV.setSafeOnClickListener {
                    start<AnnouncementDetailsActivity>(
                        "2",
                        Constant.ANNOUNCEMENT_ID to announcementId
                    )
                }
                profileIV.setSafeOnClickListener {
                    start<OtherUserProfileActivity>("2", Constant.USER_ID to otherUserId.toInt())
                }
                profileCloseIV.setSafeOnClickListener {
                    start<OtherUserProfileActivity>("2", Constant.USER_ID to otherUserId.toInt())
                }
                profileReservedIV.setSafeOnClickListener {
                    start<OtherUserProfileActivity>("2", Constant.USER_ID to otherUserId.toInt())
                }
            }
        }
    }

    override fun onBaseBackPressed() {
        if (isTaskRoot) {
            start<MainActivity>("1")
        } else if (from == Constant.DIRECT_CHAT) {
            setResult(RESULT_OK)
            finish()
        } else {
            setResult(RESULT_OK)
            finish()
        }
    }


    private fun showConfirmDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@ChatDetailActivity, R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            if (isCancel) {
                msgTV.text = getStr(R.string.cancel_reservation_msg)
            } else if (isFinalize) {
                msgTV.text = getStr(R.string.closed_announcement_msg)
            } else if (isReserved && type==Constant.FOOD) {
                msgTV.text = getStr(R.string.reserve_food_msg)
            }
            else if (isReserved && type==Constant.OBJECT) {
                msgTV.text = getStr(R.string.reserve_object_msg)
            }
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                if (isCancel) {
                    status = Constant.AVAILABLE
                } else if (isReserved) {
                    status = Constant.RESERVED
                } else if (isFinalize) {
                    status = Constant.FINALIZED
                }
                callChangeStatus()
            }
            noBTN.setSafeOnClickListener {
                moreRequest = ""
                isCancel = false
                isReserved = false
                mBottomSheetDialog.dismiss()
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun showMoreRequestConfirm() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@ChatDetailActivity, R.style.AppBottomSheetDialogTheme)
        mBottomMoreRequestBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomMoreRequestBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomMoreRequestBinding?.apply {
            msgTV.text = getStr(R.string.more_request_msg)
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                moreRequest = "Yes"
                status = Constant.RESERVED
                showConfirmDialog()
            }
            noBTN.setSafeOnClickListener {
                moreRequest = "No"
                mBottomSheetDialog.dismiss()
                showConfirmDialog()
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }

    private fun showSuccessDialog() {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    if (isFinalize) {
                        msgTV.text = getString(R.string.closed_success_msg)

                    } else if (isReserved) {
                        msgTV.text = getStr(R.string.reservation_success_msg)
                    } else if (isCancel) {
                        msgTV.text = getStr(R.string.announcement_cancel_success)
                    }

                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        isReserved = false
                        if (isFinalize) {
                            isFinalize = false
                            val intent = Intent(this@ChatDetailActivity, SubmitReviewActivity::class.java)
                            intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementId)
                            if (loginUserId == announcementUserId) {
                                intent.putExtra(Constant.END_PONT, "rate_review_user")
                            } else {
                                intent.putExtra(Constant.END_PONT, "rate_review")
                            }
                            startChatResult.launch(intent)
                        } else {
                            isFinalize = false
                        }
                        isCancel = false
                        chatMessageList.clear()
                        setUpMessageRV()
                        callChatMessageList(true)
                    }
                }
            }
        }
    }

    private fun showReviewDialog() {
        isFinalize = false
        val request = reviewManager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("TAG", "Review Flow Success")
                val reviewInfo = task.result
                val flow = reviewManager?.launchReviewFlow(this, reviewInfo)
                flow?.addOnCompleteListener {
                    Log.e("TAG", "Launcher Review Flow")
                }
            } else {
                val errorCode = ((task.exception) as ReviewException).errorCode
                Log.e("TAG", "Review Flow Unsuccessful - ErrorCode :- $errorCode")
            }
        }
    }

    private fun bindData(messageData: MutableList<ChatDetailResponse.Data>?) {
        if (messageData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.chatRV.gone()
            isEnd = true
            return
        }
        binding.noDataLayout.root.gone()
        binding.chatRV.visible()
        chatMessageList.addAll(messageData)
        chatDetailsAdapter?.removeLoading()
        chatDetailsAdapter?.addData(messageData, isLoader)
        chatDetailsAdapter?.addLoading()
        isEnd = false
        isLoader = false

        if (messageData.isEmpty() || messageData.size < 10) {
            isEnd = true
            totalItemCount = chatMessageList.size - 1
            chatDetailsAdapter?.removeLoading()
        }
        if (chatMessageList.isNotEmpty()) {
            for (i in chatMessageList.indices) {
                if (chatMessageList[i].senderId == loginUserId.toInt()) {
                    otherUserId = chatMessageList[i].receiverId.toString()
                    receiverImage = chatMessageList[i].receiverImg.toString()
                    receiverName = chatMessageList[i].receiverName.toString()
                    objectId = chatMessageList[i].receiverId.toString()
                    receiverId = chatMessageList[i].receiverId.toString()
                } else if (chatMessageList[i].receiverId == loginUserId.toInt()) {
                    otherUserId = chatMessageList[i].senderId.toString()
                    receiverImage = chatMessageList[i].senderImg.toString()
                    receiverName = chatMessageList[i].senderName.toString()
                    objectId = chatMessageList[i].senderId.toString()
                    receiverId = chatMessageList[i].senderId.toString()
                }
                return
            }
        }
        Log.e("Login Id :${loginUserId}", "Receiver ID:${receiverId}")
    }

    private fun observeKeyBoard() {

        binding.chatRV.post {
            binding.chatRV.scrollToPosition(0)
        }

        val scrollBounds = Rect()
        binding.chatRV.getHitRect(scrollBounds)

        binding.chatRV.viewTreeObserver?.addOnScrollChangedListener {
            val view = binding.chatRV.getChildAt(binding.chatRV.childCount - 1) as View
            val diff = view.bottom - (binding.chatRV.height + binding.chatRV.scrollY)
            isScrolledToBottom = (diff == 0)
        }

        KeyboardVisibilityEvent.setEventListener(
            this
        ) { isOpen ->
            if (isOpen && isScrolledToBottom) binding.chatRV.smoothScrollBy(
                0,
                binding.chatRV.bottom
            )
            else if (isScrolledToBottom) binding.chatRV.smoothScrollBy(
                0,
                binding.chatRV.bottom
            )
        }
    }
}



