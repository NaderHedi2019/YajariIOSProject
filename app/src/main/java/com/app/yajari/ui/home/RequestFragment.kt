package com.app.yajari.ui.home
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.DispenseData
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.BottomDialogFilterBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentRequestBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.search.SearchActivity
import com.app.yajari.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class RequestFragment: BaseFragment<FragmentRequestBinding>(),RequestAdapter.RequestListener,DispenseAdapter.DispenseListener,BaseFragment.LocationListener,SearchActivity.SearchListener {
    private var mBottomFilterBinding: BottomDialogFilterBinding? = null
    private var type = ""
    private var search = ""
    private var status = ""
    private var distance = ""
    private var distanceAddress = ""
    private var statusList= mutableListOf<DispenseData>()
    private var userId = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var isLoader = false
    private var isSwipeRefresh = false
    private var isFilterApply = false
    private var isComeSearch = false
    private var isBottomDialogLocation=false
    private var isSwipeRefreshView = false
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var requestAdapter: RequestAdapter? = null
    private var requestList = mutableListOf<AnnouncementResponse.Data>()
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    private var isFromPlacePicker = false
    private var curLatitude =0.0
    private var curLongitude =0.0
    private var currentDistanceAddress = ""
    override fun getViewBinding()= FragmentRequestBinding.inflate(layoutInflater)

    override fun initObj() {
        type=arguments?.getString("type").toString()
        assignLocationListener(this@RequestFragment)
        statusList=Constant.Singleton.dispenseList(requireContext())
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
        binding.requestRV.run {
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }
        if(activity is SearchActivity) {
            (activity as SearchActivity).assignSearchListener(this@RequestFragment)
        }

    }
    override fun onResume() {
        super.onResume()
        isFilterApply = if(!isComeSearch) {
            Constant.objectRequestIsFilterApply
        } else{
            Constant.searchObjectRequestIsFilterApply
        }
        if(!isComeSearch && isFilterApply && !isFromPlacePicker) {
            status =  Constant.objectRequestStatus
            distance = Constant.objectRequestDistance
            distanceAddress =  Constant.objectRequestDistanceAddress
            latitude = Constant.objectRequestLatitude
            longitude =  Constant.objectRequestLongitude
        }
        if(isFilterApply)
        {
            binding.filterApplyIV.visible()
        }
        else{
            binding.filterApplyIV.gone()
        }
        isFromPlacePicker=false
    }


    companion object{
        fun newInstance(type: String): RequestFragment {
            val data= Bundle()
            data.putString("type",type)
            return RequestFragment().apply{
                arguments = data
            }
        }
    }

    override fun click() {
        binding.apply {
            filterIV.setSafeOnClickListener {
                showFilterBottomDialog()
            }
            noDataLayout.noDataTV.text=getString(R.string.no_food_object_found)
            swipeRefresh.setOnRefreshListener {
                isSwipeRefresh=true
                isSwipeRefreshView=true
                refreshData()
            }
        }
    }

    override fun setUpObserver() {
        setUpRequestRV()
        setRVScrollListener()
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            userId= it.id!!
            if(it.id==0)
            {
              mProgressDialog.showProgressDialog()
               runTimePermission()
            }
            else{
                if(distanceAddress.isEmpty() && !isFilterApply) {
                    latitude = it.lat!!.toDouble()
                    longitude = it.lng!!.toDouble()
                }
                refreshData()
            }
        }

        announcementViewModel.objectRequestResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->           {
                    if(userId!=0 && !isSwipeRefresh && isLoader)
                    {
                        mProgressDialog.showProgressDialog()
                    }
                    if(isFilterApply && isLoader && !isSwipeRefreshView)
                    {
                        mProgressDialog.showProgressDialog()
                    }

                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data?.let {
                        if(isSwipeRefresh && it.isEmpty())
                        {
                            binding.swipeRefresh.isRefreshing=false
                            isSwipeRefresh=false
                        }
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
                    announcementViewModel.logoutUser()
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
                                    callGetRequestObject(true)
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
                requireActivity().start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        if (isComeSearch)
        {
            requireActivity().finish()
        }
        else {
            showConfirmDialog()
        }
    }
    private fun setUpRequestRV() {
        binding.requestRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            requestAdapter = RequestAdapter(requireContext(),this@RequestFragment)
            adapter = requestAdapter
            (lManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (requestAdapter!!.getItemViewType(position) == 1) {
                            1
                        } else {
                            (lManager as GridLayoutManager).spanCount
                        }
                    }

                }
        }
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                if (!isLoader && !scrollListener.getLoaded()) {
                    callGetRequestObject(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.requestRV.addOnScrollListener(scrollListener)
    }

    private fun callGetRequestObject(isLoadMore: Boolean) {
        isLoader=isLoadMore
        announcementViewModel.getObjectRequest(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to requestList.size.toString(),
                Constant.LAT to latitude.toString(),
                Constant.LNG to longitude.toString(),
                Constant.STATUS to status,
                Constant.DISTANCE to distance,
                Constant.SEARCH to search
            )
        )
    }


    private fun bindData(donationData: MutableList<AnnouncementResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.requestRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.requestRV.visible()
        requestList.addAll(donationData)
        requestAdapter?.removeLoading()
        requestAdapter?.addData(donationData,isSwipeRefresh)
        requestAdapter?.addLoading()

        if (donationData.isEmpty() || donationData.size < 10) {
            requestAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
        if(isSwipeRefresh && isSwipeRefreshView)
        {
            binding.requestRV.smoothScrollToPosition(0)
            binding.swipeRefresh.isRefreshing=false
            isSwipeRefresh=false
            isSwipeRefreshView=false
            if(scrollListener.getLoaded()) {
                scrollListener.setLoaded(false)
            }
            if (donationData.isEmpty() || donationData.size < 10) {
                scrollListener.setLoaded()
            }
        }
    }



    override fun onItemClick(announcementData: AnnouncementResponse.Data) {
        val intent= Intent(requireContext(), AnnouncementDetailsActivity::class.java)
        intent.putExtra(Constant.ANNOUNCEMENT_ID , announcementData.id.toString())
        intent.putExtra(Constant.LAT, latitude)
        intent.putExtra(Constant.LNG, longitude)
        intent.putExtra(Constant.IsFILTER, isFilterApply)
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
    private fun refreshData()
    {
        requestList.clear()
        callGetRequestObject(true)
    }

    private fun showFilterBottomDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomFilterBinding = BottomDialogFilterBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomFilterBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomFilterBinding?.apply {
            currentDistanceAddress=""
            curLatitude=0.0
            curLongitude=0.0
            if(status.isNotEmpty())
            {
                for (data in statusList)
                {
                    data.selected = data.value==status
                }
            }
            else{
                statusList=Constant.Singleton.dispenseList(requireContext())
            }

            if(distanceAddress.isNotEmpty())
            {
                locationEDT.setText(distanceAddress)
            }
            if (distance.isNotEmpty()) {
                kmRS.value = distance.toFloat()
                endKmTV.text = getString(R.string.km, distance)
            }
            dispenseRV.apply {
                adapter=DispenseAdapter(statusList,this@RequestFragment)
            }
            applyBTN.setSafeOnClickListener {
                if(currentDistanceAddress.isNotEmpty())
                {
                    distanceAddress=currentDistanceAddress
                }
                if(curLatitude!=0.0 && curLongitude!=0.0)
                {
                    latitude=curLatitude
                    longitude=curLongitude
                }
                isFilterApply=true
                isSwipeRefreshView=false
                isSwipeRefresh=true
                binding.filterApplyIV.visible()
                if(isComeSearch)
                {
                    Constant.searchObjectRequestIsFilterApply = true
                    Constant.searchObjectRequestStatus = status
                    Constant.searchObjectRequestDistance = distance
                    Constant.searchObjectRequestDistanceAddress = distanceAddress
                    Constant.searchObjectRequestLatitude = latitude
                    Constant.searchObjectRequestLongitude = longitude
                }
                else {
                    Constant.objectRequestIsFilterApply = true
                    Constant.objectRequestStatus = status
                    Constant.objectRequestDistance = distance
                    Constant.objectRequestDistanceAddress = distanceAddress
                    Constant.objectRequestLatitude = latitude
                    Constant.objectRequestLongitude = longitude
                }
                announcementViewModel.getUserFromPref()
                mBottomSheetDialog.dismiss()
            }
            resetTV.setSafeOnClickListener {
                isSwipeRefresh=true
                isFilterApply=false
                isSwipeRefreshView=false
                binding.filterApplyIV.gone()
                resetFilterData()
                announcementViewModel.getUserFromPref()
                mBottomSheetDialog.dismiss()
            }
            closeIV.setSafeOnClickListener {
                if(!isFilterApply) {
                    resetFilterData()
                }
                mBottomSheetDialog.dismiss()
            }
            locationEDT.setSafeOnClickListener {
                startPlacesActivity()
            }
            currentLocationIV.setSafeOnClickListener {
                isBottomDialogLocation=true
                runTimePermission()
            }
            kmRS.setLabelFormatter { value ->
                String.format(Locale.US, "%.0f", value)
            }

            kmRS.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }
                override fun onStopTrackingTouch(slider: Slider) {
                    val startValue = String.format(Locale.US, "%.0f", slider.value)
                    distance = startValue
                    endKmTV.text = getString(R.string.km, distance)
                }

            })

        }
        mBottomSheetDialog.show()
    }

    override fun onItemDispenseClick(dispenseData: DispenseData) {
        status=dispenseData.value
    }
    override fun onLocationGet(location: Location) {
        if(!isFilterApply) {
            latitude = location.latitude
            longitude = location.longitude
        }
        if(isBottomDialogLocation)
        {
            curLatitude = location.latitude
            curLongitude = location.longitude
            getAddressFromLocation()
            isBottomDialogLocation=false
        }
        else {
            refreshData()
        }
    }


    private fun showConfirmDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            msgTV.text=requireContext().getStr(R.string.app_exit)
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                requireActivity().finishAffinity()
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

    private fun resetFilterData()
    {
        status=""
        distance=""
        distanceAddress=""
        search=""
        if(isComeSearch)
        {
            Constant.searchObjectRequestIsFilterApply = false
            Constant.searchObjectRequestStatus = status
            Constant.searchObjectRequestDistance = distance
            Constant.searchObjectRequestDistanceAddress = distanceAddress
            Constant.searchObjectRequestLatitude = 0.0
            Constant.searchObjectRequestLongitude = 0.0
        }
        else {
            Constant.objectRequestIsFilterApply = false
            Constant.objectRequestStatus = status
            Constant.objectRequestDistance = distance
            Constant.objectRequestDistanceAddress = distanceAddress
            Constant.objectRequestLatitude = 0.0
            Constant.objectRequestLongitude = 0.0
        }
    }
    override fun onGetAddressFromPlacePicker(latitude: Double, longitude: Double, address: String) {
        this.latitude=latitude
        this.longitude=longitude
        distanceAddress=address
        mBottomFilterBinding?.locationEDT?.setText(address)
        isFromPlacePicker=true
    }

    @Suppress("DEPRECATION")
    private fun getAddressFromLocation() {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList=if(curLatitude!=0.0 && curLongitude!=0.0) {
            geocoder.getFromLocation(curLatitude, curLongitude, 1)
        }
        else{
            geocoder.getFromLocation(latitude, longitude, 1)
        }
        mBottomFilterBinding?.locationEDT?.setText(getAddressFromLatLng(addressList!!.toMutableList()))
        currentDistanceAddress=mBottomFilterBinding?.locationEDT!!.asString()
    }

    override fun onSearchChange(isCome: Boolean, search: String,isSearch:Boolean) {
        isComeSearch=isCome
        this.search=search
        isFilterApply = Constant.searchObjectRequestIsFilterApply
        if(isComeSearch && isFilterApply)
        {
            status = Constant.searchObjectRequestStatus
            distance = Constant.searchObjectRequestDistance
            distanceAddress = Constant.searchObjectRequestDistanceAddress
            latitude = Constant.searchObjectRequestLatitude
            longitude = Constant.searchObjectRequestLongitude
        }
        if(isSearch) {
            isSwipeRefresh=true
            refreshData()
        }
    }
}