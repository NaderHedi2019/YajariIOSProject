package com.app.yajari.ui.food
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.DispenseData
import com.app.yajari.databinding.BottomDialogFilterBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentFoodRequestBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.home.DispenseAdapter
import com.app.yajari.ui.home.RequestAdapter
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.search.SearchActivity
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.EndlessScrollListener
import com.app.yajari.utils.OnLoadMoreListener
import com.app.yajari.utils.SpacesItemDecoration
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getAddressFromLatLng
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class FoodRequestFragment : BaseFragment<FragmentFoodRequestBinding>(),
    DispenseAdapter.DispenseListener, RequestAdapter.RequestListener,
    BaseFragment.LocationListener,SearchActivity.SearchListener {
    private var mBottomFilterBinding: BottomDialogFilterBinding? = null
    private var userId = 0
    private var latitude =0.0
    private var longitude =0.0
    private var isLoader = false
    private var status = ""
    private var distance = ""
    private var search = ""
    private var distanceAddress = ""
    private var isSwipeRefresh = false
    private var isFilterApply = false
    private var isComeSearch = false
    private var isSwipeRefreshView = false
    private var isBottomDialogLocation=false
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var requestAdapter: RequestAdapter? = null
    private var requestList = mutableListOf<AnnouncementResponse.Data>()
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    private var statusList= mutableListOf<DispenseData>()
    private var isFromPlacePicker = false
    private var curLatitude =0.0
    private var curLongitude =0.0
    private var currentDistanceAddress = ""
    override fun getViewBinding() = FragmentFoodRequestBinding.inflate(layoutInflater)

    override fun initObj() {
        assignLocationListener(this@FoodRequestFragment)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
        binding.foodRequestRV.run {
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }
        statusList=Constant.Singleton.dispenseList(requireContext())
        if(activity is SearchActivity) {
            (activity as SearchActivity).assignSearchListener(this@FoodRequestFragment)
        }

    }

    override fun click() {
        binding.apply {
            filterIV.setSafeOnClickListener {
                showFilterBottomDialog()
            }
            noDataLayout.noDataTV.text = getString(R.string.no_food_object_found)
            swipeRefresh.setOnRefreshListener {
                isSwipeRefresh=true
                isSwipeRefreshView=true
                refreshData()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        isFilterApply = if(!isComeSearch) {
            Constant.foodRequestIsFilterApply
        } else{
            Constant.searchFoodRequestIsFilterApply
        }
        if(!isComeSearch && isFilterApply && !isFromPlacePicker) {
            status =  Constant.foodRequestStatus
            distance =  Constant.foodRequestDistance
            distanceAddress =  Constant.foodRequestDistanceAddress
            latitude =Constant.foodRequestLatitude
            longitude = Constant.foodRequestLongitude
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

    override fun setUpObserver() {
        setUpRequestRV()
        setRVScrollListener()
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            userId = it.id!!
            if (it.id == 0) {
                runTimePermission()
                mProgressDialog.showProgressDialog()
            } else {
                if(distanceAddress.isEmpty() && !isFilterApply) {
                    latitude = it.lat!!.toDouble()
                    longitude = it.lng!!.toDouble()
                }
                refreshData()
            }
        }

        announcementViewModel.foodRequestResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
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
                                    callGetRequestFood(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.logout.observe(this) {
            if (it) {
                requireActivity().start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        if(isComeSearch)
        {
            requireActivity().finish()
        }
        else {
            findNavController().navigate(R.id.navigation_home)
        }
    }


    private fun setUpRequestRV() {
        binding.foodRequestRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            requestAdapter = RequestAdapter(requireContext(), this@FoodRequestFragment)
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
                    callGetRequestFood(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.foodRequestRV.addOnScrollListener(scrollListener)
    }

    private fun callGetRequestFood(isLoadMore: Boolean) {
        isLoader = isLoadMore
        announcementViewModel.getFoodRequest(
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
            binding.foodRequestRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.foodRequestRV.visible()
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
            binding.foodRequestRV.smoothScrollToPosition(0)
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


    private fun refreshData() {
        requestList.clear()
        callGetRequestFood(true)
    }

    override fun onItemClick(announcementData: AnnouncementResponse.Data) {
        val intent = Intent(requireContext(), AnnouncementDetailsActivity::class.java)
        intent.putExtra(Constant.ANNOUNCEMENT_ID, announcementData.id.toString())
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

    private fun showFilterBottomDialog() {
        val mBottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomFilterBinding = BottomDialogFilterBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomFilterBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
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
                adapter=DispenseAdapter(statusList,this@FoodRequestFragment)
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
                isSwipeRefresh=true
                isFilterApply=true
                isSwipeRefreshView=false
                binding.filterApplyIV.visible()
                if(isComeSearch)
                {
                    Constant.searchFoodRequestIsFilterApply = true
                    Constant.searchFoodRequestStatus = status
                    Constant.searchFoodRequestDistance = distance
                    Constant.searchFoodRequestDistanceAddress = distanceAddress
                    Constant.searchFoodRequestLatitude = latitude
                    Constant.searchFoodRequestLongitude = longitude
                }
                else {
                    Constant.foodRequestIsFilterApply = true
                    Constant.foodRequestStatus = status
                    Constant.foodRequestDistance = distance
                    Constant.foodRequestDistanceAddress = distanceAddress
                    Constant.foodRequestLatitude = latitude
                    Constant.foodRequestLongitude = longitude
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
            getAddressFromLocation()
            isBottomDialogLocation=false
        }
        else {
           refreshData()
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


    private fun resetFilterData()
    {
        status=""
        search=""
        distance=""
        distanceAddress=""
        if(isComeSearch)
        {
            Constant.searchFoodRequestIsFilterApply = false
            Constant.searchFoodRequestStatus = status
            Constant.searchFoodRequestDistance = distance
            Constant.searchFoodRequestDistanceAddress = distanceAddress
            Constant.searchFoodRequestLatitude = 0.0
            Constant.searchFoodRequestLongitude = 0.0
        }
        else {
            Constant.foodRequestIsFilterApply = false
            Constant.foodRequestStatus = status
            Constant.foodRequestDistance = distance
            Constant.foodRequestDistanceAddress = distanceAddress
            Constant.foodRequestLatitude = 0.0
            Constant.foodRequestLongitude = 0.0
        }
    }
    override fun onSearchChange(isCome: Boolean, search: String,isSearch:Boolean) {
        isComeSearch=isCome
        this.search=search
        isFilterApply = Constant.searchFoodRequestIsFilterApply
        if(isComeSearch && isFilterApply)
        {
            latitude =Constant.searchFoodRequestLatitude
            longitude =Constant.searchFoodRequestLongitude
            status = Constant.searchFoodRequestStatus
            distance = Constant.searchFoodRequestDistance
            distanceAddress = Constant.searchFoodRequestDistanceAddress
        }
        if(isSearch) {
            isSwipeRefresh=true
            refreshData()
        }
    }


}