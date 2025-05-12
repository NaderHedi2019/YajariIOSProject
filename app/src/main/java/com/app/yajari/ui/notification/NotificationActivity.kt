package com.app.yajari.ui.notification
import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.NotificationResponse
import com.app.yajari.databinding.ActivityNotificationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.chat_detail.ChatDetailActivity
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.my_announcement.MyAnnouncementActivity
import com.app.yajari.ui.rate_review.RateReviewActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.EndlessScrollListener
import com.app.yajari.utils.OnLoadMoreListener
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class NotificationActivity : BaseActivity<ActivityNotificationBinding>(),NotificationAdapter.NotificationListener {
    private var isLoader = false
    private var from=""
    private var notificationList = mutableListOf<NotificationResponse.Data>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var notificationAdapter: NotificationAdapter? = null
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this@NotificationActivity) }
    override fun getViewBinding()= ActivityNotificationBinding.inflate(layoutInflater)
    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        from=intent.getStringExtra(Constant.FROM).toString()
        setNotificationRV()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun click() {
        binding.apply {
        toolbar.titleTV.text=getString(R.string.notifications)
            toolbar.backIV.setSafeOnClickListener {
                if(from==Constant.PUSH && isTaskRoot)
                {
                    start<MainActivity>("1")
                }
                else {
                    finish()
                }
            }
        }
    }

    override fun setUpObserver() {
        setNotificationRV()
        setRVScrollListener()
        callNotification(true)
        announcementViewModel.notificationResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    if(isLoader) {
                        mProgressDialog.showProgressDialog()
                    }
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    Constant.NOTIFICATION_COUNT=0
                    response.data?.data?.let {
                        bindData(it)
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@NotificationActivity,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
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
                                    callNotification(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.logout.observe(this){
            if(it)
            {
                start<LoginActivity>("1")
            }
        }

    }

    override fun onBaseBackPressed() {
        if(from==Constant.PUSH && isTaskRoot)
        {
            start<MainActivity>("1")
        }
        else {
            finish()
        }
    }


    private fun setNotificationRV() {
        binding.noDataLayout.noDataTV.text=getString(R.string.no_notification_found)
        binding.notificationRV.run {
            lManager = LinearLayoutManager(context)
            layoutManager = lManager
            notificationAdapter = NotificationAdapter(this@NotificationActivity,this@NotificationActivity)
            adapter = notificationAdapter
        }
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {

                if (!isLoader && !scrollListener.getLoaded()) {
                    callNotification(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.notificationRV.addOnScrollListener(scrollListener)
    }

    private fun callNotification(isLoadMore: Boolean) {
        isLoader=isLoadMore
        announcementViewModel.notifications(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to notificationList.size.toString()
            )
        )
    }
    private fun bindData(notifications: MutableList<NotificationResponse.Data>?) {
        if (notifications!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.notificationRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.notificationRV.visible()
        notificationList.addAll(notifications)
        notificationAdapter?.removeLoading()
        notificationAdapter?.addData(notifications)
        notificationAdapter?.addLoading()
        if (notifications.isEmpty() || notifications.size < 10) {
            notificationAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
    }

    override fun onItemClickNotification(notificationData: NotificationResponse.Data) {
        when (notificationData.pushType) {
            1 -> {
                start<AnnouncementDetailsActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.ANNOUNCEMENT_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            2 -> {
                start<AnnouncementDetailsActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.ANNOUNCEMENT_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            3 -> {
                start<ChatDetailActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.THREAD_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            4 -> {
                start<AnnouncementDetailsActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.ANNOUNCEMENT_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            5 -> {
                start<MainActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.THREAD_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            6 -> {
                start<RateReviewActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            7 -> {
                start<AnnouncementDetailsActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.ANNOUNCEMENT_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
            8 -> {
                start<AnnouncementDetailsActivity>(
                    "2",
                    Constant.FROM to Constant.NOTIFICATION,
                    Constant.ANNOUNCEMENT_ID to notificationData.objectId.toString(),
                    Constant.PUSH_TYPE to notificationData.pushType.toString(),
                )
            }
        }

    }


}