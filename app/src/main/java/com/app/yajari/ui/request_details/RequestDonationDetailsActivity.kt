package com.app.yajari.ui.request_details
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.ChatListResponse
import com.app.yajari.databinding.ActivityRequestDonationDetailsBinding
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.chat_detail.ChatDetailActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date
import java.util.Locale

class RequestDonationDetailsActivity : BaseActivity<ActivityRequestDonationDetailsBinding>(),RequestUserAdapter.RequestUserListener {
    private var objectType=""
    private var announcementID=""
    private var isLoader=true
    private var shareURL: Uri? = null
    private var requestUserAdapter:RequestUserAdapter?=null
    private var chatListResponse: ChatListResponse.Data?=null
    private val chatViewModel: ChatViewModel by viewModel()
    private var userList= mutableListOf<ChatListResponse.FriendRequest>()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null

    override fun getViewBinding()=ActivityRequestDonationDetailsBinding.inflate(layoutInflater)

    override fun initObj() {

        changeStatusBarBlackColor("#000000")
        objectType=intent.getStringExtra(Constant.OBJECT_TYPE).toString()
        chatListResponse=intent.serializable(Constant.CHAT_REQUEST_DATA)
        announcementID=chatListResponse?.announcementId.toString()
        binding.apply {
            productIV.loadImage(chatListResponse?.thumbImage?.file,R.drawable.ic_placeholder_yajari)
            titleTV.text=chatListResponse?.title
            if(chatListResponse?.status==Constant.RESERVED)
            {
                closedBTN.visible()
            }
            else{
                closedBTN.gone()
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.CHAT))
        createDynamicLink()
        setRequestUserRV()
    }
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isLoader=false
            callGetUserList()
        }
    }
    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    override fun click() {
        binding.apply {
            backIV.setSafeOnClickListener {
                setResult(RESULT_OK)
                finish()
            }
            viewFullTV.setSafeOnClickListener {
                start<AnnouncementDetailsActivity>("2",Constant.FROM to Constant.FULL_DETAILS,Constant.ANNOUNCEMENT_ID to announcementID)
            }
            if(objectType==Constant.DONATION)
            {
                requestTypeTV.text=getStr(R.string.request_n)
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
            closedBTN.setSafeOnClickListener {
               showConfirmDialog()
            }
        }
    }

    override fun setUpObserver() {
        callGetUserList()
        chatViewModel.chatUserResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->
                {
                    if(isLoader) {
                        mProgressDialog.showProgressDialog()
                    }
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data.let { it ->
                        userList= it!!.sortedByDescending { getUTCDate(it.messageLatest?.createdAt,Constant.backEndUTCFormat, true) }.toMutableList()
                        requestUserAdapter?.addData(userList,true,chatListResponse?.objectId,chatListResponse?.status.toString())
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@RequestDonationDetailsActivity,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
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
                                    callGetUserList()
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

        chatViewModel.logout.observe(this){
            if(it)
            {
                start<LoginActivity>("1")
            }
        }
    }

    private fun callGetUserList() {
       chatViewModel.userChatList(mapOf(Constant.ANNOUNCEMENT_ID to announcementID,Constant.TYPE to objectType ))
    }

    override fun onBaseBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    private fun setRequestUserRV()
    {
        binding.requestedUserRV.apply {
            requestUserAdapter=RequestUserAdapter(this@RequestDonationDetailsActivity,this@RequestDonationDetailsActivity)
            adapter=requestUserAdapter
        }
    }

    override fun onItemRequestUserClick(position: Int, userData: ChatListResponse.FriendRequest) {
        val intent = Intent(this@RequestDonationDetailsActivity, ChatDetailActivity::class.java)
        intent.putExtra(Constant.THREAD_ID,userData.threadId.toString())
        intent.putExtra(Constant.TYPE,objectType)
        startChatResult.launch(intent)
    }

    private fun createDynamicLink() {
        val caption = getString(R.string.view_announcement)
        val url = if(chatListResponse?.thumbImage==null) {
            "https://hexeros.com/dev/yajari/uploads/no_image.png"
        } else{
            chatListResponse?.thumbImage?.file!!
        }
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("${Constant.DEEPLINK_URL}?announcement_id=${announcementID}}"))
            .setDomainUriPrefix(getStr(R.string.firebase_deeplink))
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(packageName)
                    .setFallbackUrl(
                        Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                    ).build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder().setTitle(caption)
                    .setImageUrl(Uri.parse(url)).build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener {
                shareURL = it.shortLink!!
            }
            .addOnFailureListener {
                Log.i("LOG_", "createDynamicLink: FAIL ${it.localizedMessage}")
            }
    }

    private val startChatResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    isLoader=false
                    callGetUserList()
                }
            }
        }

    private fun callChangeStatus() {
        chatViewModel.changeStatus(
            mapOf(
                Constant.ANNOUNCEMENT_ID to announcementID,
                Constant.OBJECT_ID to chatListResponse?.objectId.toString(),
                Constant.STATUS to Constant.FINALIZED
            )
        )
    }

    private fun showSuccessDialog() {
        commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text = getString(R.string.closed_success_msg)
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
    private fun showConfirmDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(this@RequestDonationDetailsActivity, R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {

            msgTV.text = getStr(R.string.closed_announcement_msg)
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()

                callChangeStatus()
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

}