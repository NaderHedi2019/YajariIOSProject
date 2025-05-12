package com.app.yajari.ui.home
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.DispenseData
import com.app.yajari.databinding.BottomDialogCategoryBinding
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.BottomDialogFilterBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentDonationBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.create_announcement.CategoryAdapter
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.search.SearchActivity
import com.app.yajari.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import okhttp3.internal.trimSubstring
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DonationFragment : BaseFragment<FragmentDonationBinding>(),DonationAdapter.DonationListener,DispenseAdapter.DispenseListener,
    BaseFragment.LocationListener,SearchActivity.SearchListener,NotificationCountListener, CategoryAdapter.CategoryListener {
    private var mBottomFilterBinding: BottomDialogFilterBinding? = null
    private var status = ""
    private var condition = ""
    private var distance =""
    private var search = ""
    private var distanceAddress = ""
    private var userId = 0
    private var announcementId = ""
    private var latitude =0.0
    private var longitude =0.0
    private var isLoader = false
    private var isSwipeRefresh = false
    private var isSwipeRefreshView = false
    private var isFilterApply =false
    private var isComeSearch = false
    private var isBottomDialogLocation=false
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var donationAdapter: DonationAdapter? = null
    private var donationList = mutableListOf<AnnouncementResponse.Data>()
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private lateinit var  mProgressDialog: CustomProgressDialog
    private var statusList= mutableListOf<DispenseData>()
    private var categoryAdapter: CategoryAdapter? = null
    private var isSubmit = false
    private var isFromPlacePicker = false
    private var curLatitude =0.0
    private var curLongitude =0.0
    private var currentDistanceAddress = ""


    override fun getViewBinding()= FragmentDonationBinding.inflate(layoutInflater)

    companion object{
        fun newInstance(type: String): DonationFragment {
            val data=Bundle()
            data.putString("type",type)
            return DonationFragment().apply{
                arguments = data
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog=CustomProgressDialog(requireActivity())
    }



    override fun onDestroy() {
        mProgressDialog.dismissProgressDialog()
        super.onDestroy()
    }

    override fun onPause() {
        mProgressDialog.dismissProgressDialog()
        super.onPause()
    }

    override fun initObj() {
        assignLocationListener(this)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
        binding.donationRV.run {
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }
        statusList=Constant.Singleton.dispenseList(requireContext())
        if(activity is SearchActivity) {
            (activity as SearchActivity).assignSearchListener(this@DonationFragment)
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

    override fun onResume() {
        super.onResume()
        isFilterApply = if(!isComeSearch) {
            Constant.objectDonationIsFilterApply
        } else{
            Constant.searchObjectDonationIsFilterApply
        }
        if(!isComeSearch && isFilterApply && !isFromPlacePicker) {
            status = Constant.objectDonationStatus
            condition = Constant.objectDonationCondition
            distance = Constant.objectDonationDistance
            distanceAddress = Constant.objectDonationDistanceAddress
            latitude = Constant.objectDonationLatitude
            longitude = Constant.objectDonationLongitude
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
        callGetCategory()
        setUpDonationRV()
        setRVScrollListener()
        announcementViewModel.getUserFromPref()
        announcementViewModel.userDataResponse.observe(this)
        {
            //Reset Data from filter OR first time call
            userId= it.id!!
            donationAdapter?.setUserId(userId)

            if(it.id==0)
            {
               mProgressDialog.showProgressDialog()
               runTimePermission()
            }
            else
            {
                if(distanceAddress.isEmpty() && !isFilterApply) {
                    latitude = it.lat!!.toDouble()
                    longitude = it.lng!!.toDouble()
                }
                refreshData()
            }
        }

        announcementViewModel.categoryResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    //mProgressDialog.showProgressDialog()
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data?.let {
                        Constant.categoryList= it.category!!
                        Constant.conditionList= it.condition!!
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
                                    callGetCategory()
                                }
                            }
                        }
                    }
                }
            }
        }

        announcementViewModel.favouriteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->           {

                }
                Status.SUCCESS -> {

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
                                    callFavouriteAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }

        announcementViewModel.objectDonationResponse.observe(this) { response ->
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
                    Constant.NOTIFICATION_COUNT= response.data?.notificationCount!!
                    Constant.CHAT_COUNT= response.data.messageCount!!
                    if(activity is MainActivity) {
                        (activity as MainActivity).setBadgeChatCount()
                        (activity as MainActivity).listener?.onUpdateCount()
                    }
                    response.data.data?.let {

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
                                    callGetDonationObject(true)
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

    private fun callFavouriteAnnouncement() {
       announcementViewModel.favouriteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
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


    private fun showFilterBottomDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomFilterBinding = BottomDialogFilterBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomFilterBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomFilterBinding?.apply {
            objectTV.visible()
            objectATV.visible()
            currentDistanceAddress=""
            curLatitude=0.0
            curLongitude=0.0
            if(condition.isNotEmpty())
            {
                val categoryIds=condition.split(",").map { it.trim()}
                for (i in Constant.categoryList.indices) {
                    for (categoryId in categoryIds) {
                        if (categoryId == Constant.categoryList[i].uniqueId) {
                            Constant.categoryList[i].isSelected = true
                        }
                    }
                }
                objectATV.setText(Constant.categoryList.filter {it.isSelected }.joinToString { it.title!!.trim()})
            }
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
                adapter=DispenseAdapter(statusList,this@DonationFragment)
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
                isSwipeRefresh=true
                isSwipeRefreshView=false
                if(isComeSearch)
                {
                    Constant.searchObjectDonationIsFilterApply = true
                    Constant.searchObjectDonationStatus = status
                    Constant.searchObjectDonationCondition = condition
                    Constant.searchObjectDonationDistance = distance
                    Constant.searchObjectDonationDistanceAddress = distanceAddress
                    Constant.searchObjectDonationLatitude =latitude
                    Constant.searchObjectDonationLongitude = longitude
                }
                else {
                    Constant.objectDonationIsFilterApply = true
                    Constant.objectDonationStatus = status
                    Constant.objectDonationCondition = condition
                    Constant.objectDonationDistance = distance
                    Constant.objectDonationDistanceAddress = distanceAddress
                    Constant.objectDonationLatitude = latitude
                    Constant.objectDonationLongitude = longitude
                }
                binding.filterApplyIV.visible()
                announcementViewModel.getUserFromPref()
                mBottomSheetDialog.dismiss()
            }
            objectATV.setSafeOnClickListener {
                showCategoryDialog()
            }
            resetTV.setSafeOnClickListener {
                isSwipeRefresh=true
                isSwipeRefreshView=false
                isFilterApply=false
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

    private fun resetFilterData()
    {
        status=""
        condition=""
        distance=""
        distanceAddress=""
        search=""
        if(isComeSearch)
        {
            Constant.searchObjectDonationIsFilterApply = false
            Constant.searchObjectDonationStatus = status
            Constant.searchObjectDonationCondition = condition
            Constant.searchObjectDonationDistance = distance
            Constant.searchObjectDonationDistanceAddress = distanceAddress
            Constant.searchObjectDonationLatitude = 0.0
            Constant.searchObjectDonationLongitude = 0.0
        }
        else {
            Constant.objectDonationIsFilterApply = false
            Constant.objectDonationStatus = status
            Constant.objectDonationCondition = condition
            Constant.objectDonationDistance = distance
            Constant.objectDonationDistanceAddress = distanceAddress
            Constant.objectDonationLatitude = 0.0
            Constant.objectDonationLongitude = 0.0
        }
    }

    override fun onItemDispenseClick(dispenseData: DispenseData) {
        status=dispenseData.value
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

    private fun setUpDonationRV() {

        binding.donationRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            donationAdapter = DonationAdapter(requireContext(),this@DonationFragment,Constant.OBJECT)
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
                    Log.e("EE scroll status","Status scroll :"+scrollListener.getLoaded())
                    callGetDonationObject(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.donationRV.addOnScrollListener(scrollListener)
    }

    private fun callGetDonationObject(isLoadMore: Boolean) {
        isLoader=isLoadMore
//        var conditionAll=condition
//        if(condition=="ID0302010")
//        {
//            conditionAll=""
//        }
        announcementViewModel.getObjectDonation(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to donationList.size.toString(),
                Constant.LAT to latitude.toString(),
                Constant.LNG to longitude.toString(),
                Constant.STATUS to status,
                Constant.DISTANCE to distance,
                Constant.CATEGORY to condition,
                Constant.SEARCH to search
            )
        )
    }


    private fun bindData(donationData: MutableList<AnnouncementResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.donationRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.donationRV.visible()
        donationList.addAll(donationData)
        donationAdapter?.removeLoading()
        donationAdapter?.addData(donationData,isSwipeRefresh)
        donationAdapter?.addLoading()

       if (donationData.isEmpty() || donationData.size < 10) {
           donationAdapter?.removeLoading()
           scrollListener.setLoaded()
       }
        if(isSwipeRefresh && isSwipeRefreshView)
        {
            isSwipeRefresh=false
            isSwipeRefreshView=false
            binding.donationRV.smoothScrollToPosition(0)
            binding.swipeRefresh.isRefreshing=false
            if(scrollListener.getLoaded()) {
                scrollListener.setLoaded(false)
            }
            if (donationData.isEmpty() || donationData.size < 10) {
                scrollListener.setLoaded()
            }
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

    private fun refreshData()
    {
        donationList.clear()
        callGetDonationObject(true)
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

    private fun callGetCategory() {
        announcementViewModel.getCategory()
    }

    override fun onSearchChange(isCome: Boolean, search: String,isSearch:Boolean) {
        isComeSearch=isCome
        this.search=search
        isFilterApply = Constant.searchObjectDonationIsFilterApply
        if(isComeSearch && isFilterApply)
        {
           status = Constant.searchObjectDonationStatus
           condition = Constant.searchObjectDonationCondition
           distance = Constant.searchObjectDonationDistance
           distanceAddress = Constant.searchObjectDonationDistanceAddress
           latitude =Constant.searchObjectDonationLatitude
           longitude =Constant.searchObjectDonationLongitude
        }
        if(isSearch) {
            isSwipeRefresh=true
            refreshData()
        }
    }

    override fun onUpdateCount() {
    }
    private fun showCategoryDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val mBottomCategoryBinding = BottomDialogCategoryBinding.inflate(layoutInflater)
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomSheetDialog.setCancelable(false)
        mBottomSheetDialog.setContentView(mBottomCategoryBinding.root)
        val categoryIds=condition.split(",").map { it.trim()}
        for (i in Constant.categoryList.indices) {
            for (categoryId in categoryIds) {
                if (categoryId == Constant.categoryList[i].uniqueId) {
                    Constant.categoryList[i].isSelected = true
                }
            }
        }
        mBottomCategoryBinding.apply {
            commonListRV.apply {
                categoryAdapter = CategoryAdapter(this@DonationFragment, Constant.categoryList, requireContext(),false)
                adapter = categoryAdapter
            }
            closeIV.setSafeOnClickListener {
                if (!isSubmit && !categoryAdapter?.categoryList!!.any { it.isSelected }) {
                    condition=""
                    mBottomFilterBinding?.objectATV?.setText("")
                    categoryAdapter?.categoryList!!.map { it.isSelected = false }
                }
                mBottomSheetDialog.dismiss()
            }
            submitBTN.setSafeOnClickListener {
                if (categoryAdapter?.categoryList!!.any { it.isSelected }) {
                    condition= categoryAdapter?.categoryList!!.filter { it.isSelected }.joinToString(separator = ",") {it.uniqueId?.trim()!!}
                    mBottomFilterBinding?.objectATV?.setText(categoryAdapter?.categoryList!!.filter { it.isSelected }
                        .joinToString { it.title?.trim()!! })
                    isSubmit = true
                    mBottomSheetDialog.dismiss()
                } else {
                    showToasty(
                        requireContext(),
                        getString(R.string.please_select_category), "2"
                    )
                }
            }
            searchEDT.addTextChangedListener {
                if (it.toString().isNotEmpty()) {
                    clearIV.visible()
                } else {
                    clearIV.gone()
                }
                categoryAdapter?.filter?.filter(it.toString())
            }
            clearIV.setSafeOnClickListener {
                searchEDT.setText("")
            }
        }
        mBottomSheetDialog.show()
    }

    override fun onItemClickCategory(categoryData: CategoryResponse.Category) {
        isSubmit = false
    }


}