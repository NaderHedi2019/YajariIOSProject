package com.app.yajari.ui.my_favourite
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.AnnouncementType
import com.app.yajari.data.StatusType
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentAnnouncementBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.home.DonationAdapter
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.EndlessScrollListener
import com.app.yajari.utils.OnLoadMoreListener
import com.app.yajari.utils.SpacesItemDecoration
import com.app.yajari.utils.Status
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class AnnouncementFragment : BaseFragment<FragmentAnnouncementBinding>(),DonationAdapter.DonationListener {
    private var isLoader = false
    private var loaderMore = false
    private var dummy = mutableListOf<AnnouncementResponse.Data>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var favAnnouncementAdapter: DonationAdapter? = null
    var isFirstTime = true
    private var type=Constant.OBJECT
    private var status=""
    private var announcementId = ""
    private var isSwipeRefresh = false
    private var userId=0
    private var donationList = mutableListOf<AnnouncementResponse.Data>()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    override fun getViewBinding()= FragmentAnnouncementBinding.inflate(layoutInflater)

    override fun initObj() {

    }
    private fun setUpDonationRV() {
        binding.noDataLayout.noDataTV.text=getString(R.string.no_fav_announcement_found)
        binding.announcementRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            favAnnouncementAdapter = DonationAdapter(requireContext(),this@AnnouncementFragment,Constant.OBJECT)
            adapter = favAnnouncementAdapter
            (lManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (favAnnouncementAdapter!!.getItemViewType(position) == 1) {
                            1
                        } else {
                            (lManager as GridLayoutManager).spanCount
                        }
                    }

                }
        }
        favAnnouncementAdapter?.setObjectType(type,true)
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                if (!isFirstTime && !isLoader && loaderMore) {
                    callFavouriteAnnouncementList(false)
                }
                isFirstTime = false
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.announcementRV.addOnScrollListener(scrollListener)
    }

    override fun click() {
        binding.apply {
            objectATV.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.shape_dropdown_radius))
            availableATV.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.shape_dropdown_radius))

            objectATV.setSafeOnClickListener {
                val arrayAdapter = object : ArrayAdapter<AnnouncementType>(requireContext(),  R.layout.adapter_dropdown, R.id.optionNameTV,   Constant.Singleton.typeList(requireContext())) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val line = view.findViewById(R.id.lineView) as View
                        if(Constant.Singleton.typeList(requireContext()).size-1==position)
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
                    val value =    Constant.Singleton.typeList(requireContext())[position].value
                    objectATV.setText(Constant.Singleton.typeList(requireContext())[position].name, false)
                    type=value
                    refreshData()

                }
                objectATV.showDropDown()
            }
            availableATV.setSafeOnClickListener {
                val arrayAdapter = object : ArrayAdapter<StatusType>(requireContext(),  R.layout.adapter_dropdown, R.id.optionNameTV,   Constant.Singleton.statusList(requireContext())) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val line = view.findViewById(R.id.lineView) as View
                        if(Constant.Singleton.statusList(requireContext()).size-1==position)
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
                    val value =   Constant.Singleton.statusList(requireContext())[position].value
                    availableATV.setText(Constant.Singleton.statusList(requireContext())[position].name, false)
                    status=value
                    refreshData()
                }
                availableATV.showDropDown()
            }
        }
    }

    private fun refreshData()
    {
        isSwipeRefresh=true
        isFirstTime=true
        dummy.clear()
        setUpDonationRV()
        callFavouriteAnnouncementList(true)
    }

    override fun setUpObserver() {
        profileViewModel.getUserFromPref()
        profileViewModel.userDataResponse.observe(this)
        {
            userId= it.id!!
            setUpDonationRV()
            setRVScrollListener()
            callFavouriteAnnouncementList(true)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
            binding.announcementRV.run {
                addItemDecoration(SpacesItemDecoration(spacingInPixels))
            }
        }
        profileViewModel.favouriteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->           {
                    mProgressDialog.showProgressDialog()
                }
                Status.SUCCESS -> {
                    refreshData()
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
                                    callFavouriteAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }

        profileViewModel.favAnnouncementListResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    if(isLoader) {
                        mProgressDialog.showProgressDialog()
                    }
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data?.let {
                        bindData(it)
                    }
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
                                    callFavouriteAnnouncementList(true)
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

    private fun callFavouriteAnnouncementList(isLoadMore: Boolean) {
        isLoader=isLoadMore
        profileViewModel.favAnnouncementList(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to dummy.size.toString(),
                Constant.TYPE to type.lowercase(),
                Constant.STATUS to status.lowercase()
            )
        )
    }


    override fun onBaseBackPressed() {
        requireActivity().finish()
    }

    private fun bindData(donationData: MutableList<AnnouncementResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.announcementRV.gone()
            return
        }
        loaderMore=true
        binding.noDataLayout.root.gone()
        binding.announcementRV.visible()
        donationList.addAll(donationData)
        dummy.addAll(donationData)
        favAnnouncementAdapter?.removeLoading()
        favAnnouncementAdapter?.addData(donationData, isSwipeRefresh)
        favAnnouncementAdapter?.addLoading()
        if (donationData.isEmpty() || donationData.size < 10) {
            favAnnouncementAdapter?.removeLoading()
            scrollListener.setLoaded()
            loaderMore=false
        }
    }


    override fun onItemLike(announcementData: AnnouncementResponse.Data) {
        if(userId==0)
        {
            loginContinueDialog()
        }
        else {
            announcementId = announcementData.id.toString()
            callFavouriteAnnouncement()
        }
    }


    override fun onItemClick(announcementData: AnnouncementResponse.Data) {
        val intent= Intent(requireContext(), AnnouncementDetailsActivity::class.java)
        intent.putExtra(Constant.ANNOUNCEMENT_ID , announcementData.id.toString())
        startLikeResult.launch(intent)
    }
    private val startLikeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    refreshData()
                }
            }
        }

    private fun callFavouriteAnnouncement() {
        profileViewModel.favouriteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }
}