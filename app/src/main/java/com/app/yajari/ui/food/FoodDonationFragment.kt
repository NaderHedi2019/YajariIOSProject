package com.app.yajari.ui.food
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
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
import com.app.yajari.databinding.FragmentFoodDonationBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.home.DispenseAdapter
import com.app.yajari.ui.home.DonationAdapter
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

class FoodDonationFragment : BaseFragment<FragmentFoodDonationBinding>(),
    DispenseAdapter.DispenseListener, DonationAdapter.DonationListener,
    BaseFragment.LocationListener,SearchActivity.SearchListener {
    private var mBottomFilterBinding: BottomDialogFilterBinding? = null
    private var announcementId = ""
    private var status = ""
    private var distance = ""
    private var search = ""
    private var distanceAddress = ""
    private var isSwipeRefresh = false
    private var isFilterApply = false
    private var isBottomDialogLocation=false
    private var userId = 0
    private var latitude = 0.0
    private var longitude =0.0
    private var isLoader = false
    private var isComeSearch = false
    private var isSwipeRefreshView = false
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var donationAdapter: DonationAdapter? = null
    private var donationList = mutableListOf<AnnouncementResponse.Data>()
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private lateinit var mProgressDialog: CustomProgressDialog
    private var statusList= mutableListOf<DispenseData>()
    private var isFromPlacePicker = false
    private var curLatitude =0.0
    private var curLongitude =0.0
    private var currentDistanceAddress = ""
    override fun getViewBinding() = FragmentFoodDonationBinding.inflate(layoutInflater)

    override fun initObj() {
        assignLocationListener(this@FoodDonationFragment)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
        binding.foodDonationRV.run {
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }
        statusList=Constant.Singleton.dispenseList(requireContext())
        if(activity is SearchActivity) {
            (activity as SearchActivity).assignSearchListener(this@FoodDonationFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = CustomProgressDialog(requireActivity())
    }

    override fun onDestroy() {
        mProgressDialog.dismissProgressDialog()
        super.onDestroy()
    }

    override fun onPause() {
        mProgressDialog.dismissProgressDialog()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        isFilterApply = if(!isComeSearch) {
            Constant.foodDonationIsFilterApply
        } else{
            Constant.searchFoodDonationIsFilterApply
        }
        if(!isComeSearch && isFilterApply && !isFromPlacePicker) {
            status =  Constant.foodDonationStatus
            distance =  Constant.foodDonationDistance
            distanceAddress =  Constant.foodDonationDistanceAddress
            latitude = Constant.foodDonationLatitude
            longitude = Constant.foodDonationLongitude
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

    override fun setUpObserver() {
        setUpDonationRV()
        setRVScrollListener()
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            userId = it.id!!
            donationAdapter?.setUserId(userId)
            if (it.id == 0) {
                mProgressDialog.showProgressDialog()
                runTimePermission()
            } else {
                if(distanceAddress.isEmpty() && !isFilterApply) {
                    latitude = it.lat!!.toDouble()
                    longitude = it.lng!!.toDouble()
                }
                refreshData()
            }
        }

        announcementViewModel.foodDonationResponse.observe(this) { response ->
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
                                    callGetFoodDonation(true)
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
                                    callFavouriteAnnouncement()
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
                adapter=DispenseAdapter(statusList,this@FoodDonationFragment)
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
                    Constant.searchFoodDonationIsFilterApply = true
                    Constant.searchFoodDonationStatus = status
                    Constant.searchFoodDonationDistance = distance
                    Constant.searchFoodDonationDistanceAddress = distanceAddress
                    Constant.searchFoodDonationLatitude = latitude
                    Constant.searchFoodDonationLongitude = longitude
                }
                else {
                    Constant.foodDonationIsFilterApply = true
                    Constant.foodDonationStatus = status
                    Constant.foodDonationDistance = distance
                    Constant.foodDonationDistanceAddress = distanceAddress
                    Constant.foodDonationLatitude = latitude
                    Constant.foodDonationLongitude = longitude
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

    private fun setUpDonationRV() {
        binding.foodDonationRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            donationAdapter =
                DonationAdapter(requireContext(), this@FoodDonationFragment, Constant.FOOD)
            adapter = donationAdapter
            (lManager as GridLayoutManager).spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (donationAdapter!!.getItemViewType(position) == 1) {
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
                    callGetFoodDonation(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.foodDonationRV.addOnScrollListener(scrollListener)
    }

    private fun callGetFoodDonation(isLoadMore: Boolean) {
        isLoader = isLoadMore
        announcementViewModel.getFoodDonation(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to donationList.size.toString(),
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
            binding.foodDonationRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.foodDonationRV.visible()
        donationList.addAll(donationData)
        donationAdapter?.removeLoading()
        donationAdapter?.addData(donationData, isSwipeRefresh)
        donationAdapter?.addLoading()

        if (donationData.isEmpty() || donationData.size < 10) {
            donationAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
        if(isSwipeRefresh && isSwipeRefreshView)
        {
            binding.foodDonationRV.smoothScrollToPosition(0)
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

    override fun onItemLike(announcementData: AnnouncementResponse.Data) {
        if (userId == 0) {
            loginContinueDialog()
        } else {
            announcementId = announcementData.id.toString()
            callFavouriteAnnouncement()
        }
    }

    private fun refreshData() {
        donationList.clear()
        callGetFoodDonation(true)
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
                    isSwipeRefresh=true
                    refreshData()
                }
            }
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

    private fun callFavouriteAnnouncement() {
        announcementViewModel.favouriteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }

    private fun resetFilterData()
    {
        status=""
        search=""
        distance=""
        distanceAddress=""
        if(isComeSearch)
        {
            Constant.searchFoodDonationIsFilterApply = false
            Constant.searchFoodDonationStatus = status
            Constant.searchFoodDonationDistance = distance
            Constant.searchFoodDonationDistanceAddress = distanceAddress
            Constant.searchFoodDonationLatitude = 0.0
            Constant.searchFoodDonationLongitude = 0.0
        }
        else {
            Constant.foodDonationIsFilterApply = false
            Constant.foodDonationStatus = status
            Constant.foodDonationDistance = distance
            Constant.foodDonationDistanceAddress = distanceAddress
            Constant.foodDonationLatitude = 0.0
            Constant.foodDonationLongitude = 0.0
        }
    }

    override fun onSearchChange(isCome: Boolean, search: String,isSearch:Boolean) {
        isComeSearch=isCome
        this.search=search
        isFilterApply = Constant.searchFoodDonationIsFilterApply

        if(isComeSearch && isFilterApply)
        {
            status = Constant.searchFoodDonationStatus
            distance = Constant.searchFoodDonationDistance
            distanceAddress = Constant.searchFoodDonationDistanceAddress
            latitude = Constant.searchFoodDonationLatitude
            longitude =Constant.searchFoodDonationLongitude
        }
        if(isSearch) {
            isSwipeRefresh=true
            refreshData()
        }
    }

}