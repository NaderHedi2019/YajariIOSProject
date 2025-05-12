package com.app.yajari.ui.chat
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.ChatListResponse
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentMyRequestBinding
import com.app.yajari.ui.chat.viewmodel.ChatViewModel
import com.app.yajari.ui.chat_detail.ChatDetailActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.request_details.RequestDonationDetailsActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.EndlessScrollListener
import com.app.yajari.utils.OnLoadMoreListener
import com.app.yajari.utils.Status
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class MyRequestFragment : BaseFragment<FragmentMyRequestBinding>(),MyRequestAdapter.MyRequestListener {
    private var isSwipeRefresh = false
    private var isLoader = false
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var requestAdapter: MyRequestAdapter? = null
    private var chatList = mutableListOf<ChatListResponse.Data>()
    private var limitCount=10
    private val chatViewModel: ChatViewModel by viewModel()
    private lateinit var mProgressDialog: CustomProgressDialog
    override fun getViewBinding()= FragmentMyRequestBinding.inflate(layoutInflater)

    override fun initObj() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.CHAT))
    }
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isSwipeRefresh=true
            refreshData()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = CustomProgressDialog(requireActivity())
    }

    override fun onDestroy() {
        mProgressDialog.dismissProgressDialog()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    override fun onPause() {
        mProgressDialog.dismissProgressDialog()
        super.onPause()
    }
    override fun click() {
        binding.apply {
            noDataLayout.noDataTV.text=getString(R.string.no_request_chat_found)
//            swipeRefresh.setOnRefreshListener {
//                isSwipeRefresh=true
//                refreshData()
//            }
        }
    }

    private fun refreshData() {
        limitCount=10
        chatList.clear()
        callGetChatList(true)
    }

    override fun setUpObserver() {
        setMyRequestRV()
        setRVScrollListener()
        callGetChatList(true)
        chatViewModel.chatRequestListResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    if(isLoader && !isSwipeRefresh)
                    {
                        mProgressDialog.showProgressDialog()
                    }
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    Constant.NOTIFICATION_COUNT= response.data?.notificationCount!!
                    Constant.CHAT_COUNT= response.data.messageCount!!
                    if(activity is MainActivity) {
                        (activity as MainActivity).setBadgeChatCount()
                    }
                    response.data.data?.let {
                        if(isSwipeRefresh && it.isEmpty())
                        {
                          //  binding.swipeRefresh.isRefreshing=false
                            isSwipeRefresh=false
                        }
                        if(it.isEmpty())
                        {
                            scrollListener.setLoaded()
                        }
                        bindData(it)
                    }
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    requireActivity().runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(requireContext(),
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
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
                    requireActivity().runOnUiThread {
                        requireActivity().fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callGetChatList(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        chatViewModel.logout.observe(this) {
            if (it) {
                requireActivity().start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setMyRequestRV()
    {
        binding.myRequestRV.run {
            lManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutManager = lManager
            requestAdapter = MyRequestAdapter(requireContext(),this@MyRequestFragment)
            adapter = requestAdapter
        }
    }
    private fun setRVScrollListener() {
      //  binding.swipeRefresh.isEnabled=false
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                if (!isLoader && !scrollListener.getLoaded()) {
                    callGetChatList(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
//                    binding.swipeRefresh.isEnabled =
//                        (lManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0
            }
        })
        binding.myRequestRV.addOnScrollListener(scrollListener)
    }

    private fun callGetChatList(isLoadMore: Boolean) {
        isLoader=isLoadMore
        chatViewModel.requestChatList(
            mapOf(
                Constant.LIMIT_PARAM to  chatList.size.toString(),
                Constant.OFFSET_PARAM to limitCount.toString()
            )
        )
    }

    override fun onItemMyRequestClick(data: ChatListResponse.Data) {
        if(data.friendRequest!!.isEmpty())
        {
            val intent = Intent(requireContext(), ChatDetailActivity::class.java)
            intent.putExtra(Constant.THREAD_ID, data.id.toString())
            intent.putExtra(Constant.FROM,Constant.DIRECT_CHAT)
            startChatResult.launch(intent)
        }
        else {
            val intent = Intent(requireContext(), RequestDonationDetailsActivity::class.java)
            intent.putExtra(Constant.OBJECT_TYPE, Constant.REQUEST)
            intent.putExtra(Constant.CHAT_REQUEST_DATA,data)
            startChatResult.launch(intent)
        }
    }
    private val startChatResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    isSwipeRefresh=true
                    refreshData()
                }
            }
        }


    private fun bindData(requestData: MutableList<ChatListResponse.Data>?) {
        if (requestData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.myRequestRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.myRequestRV.visible()
        chatList.addAll(requestData)
        requestAdapter?.removeLoading()
        requestAdapter?.addData(requestData, isSwipeRefresh)
        requestAdapter?.addLoading()
        limitCount += chatList.size
        if (requestData.isEmpty() || requestData.size < 10) {
            requestAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
//        if(isSwipeRefresh && isLoader)
//        {
//          //  binding.swipeRefresh.isRefreshing=false
//            isSwipeRefresh=false
//            if(scrollListener.getLoaded() && requestData.size >= 10) {
//                scrollListener.setLoaded(false)
//            }
//            if (requestData.isEmpty() || requestData.size < 10) {
//                scrollListener.setLoaded()
//            }
//        }
    }
}