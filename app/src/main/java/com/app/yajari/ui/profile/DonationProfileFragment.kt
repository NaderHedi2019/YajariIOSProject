package com.app.yajari.ui.profile
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.AnnouncementType
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.data.StatusType
import com.app.yajari.databinding.BottomDialogDeleteConfirmationBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentDonationProfileBinding
import com.app.yajari.databinding.LayoutOptionPopupBinding
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.create_announcement.PublishAnnouncementActivity
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.my_announcement.MyAnnouncementAdapter
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class DonationProfileFragment : BaseFragment<FragmentDonationProfileBinding>(),MyAnnouncementAdapter.MyAnnouncementListener {
    private var mPopupMenuBinding: LayoutOptionPopupBinding? = null
    private lateinit var popupWindow: PopupWindow
    private var isLoader = false
    private var loaderMore = false
    private var dummy = mutableListOf<MyAnnouncementResponse.Data>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var donationAdapter: MyAnnouncementAdapter? = null
    var isFirstTime = true
    private var type=Constant.OBJECT
    private var status=""
    private var from=""
    private var announcementId=""
    private var donationList = mutableListOf<MyAnnouncementResponse.Data>()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    private var mBottomConfirmBinding: BottomDialogDeleteConfirmationBinding? = null


    override fun getViewBinding()= FragmentDonationProfileBinding.inflate(layoutInflater)

    companion object{
        fun newInstance(type: String): DonationProfileFragment {
            val data= Bundle()
            data.putString("type",type)
            return DonationProfileFragment().apply{
                arguments = data
            }
        }
    }

    override fun initObj() {
        from=arguments?.getString(Constant.TYPE).toString()
        setUpDonationRV()
        setRVScrollListener()
        callMyAnnouncement(true)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_5)
        binding.donationRV.run {
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }
    }
    private fun setUpDonationRV() {
        binding.noDataLayout.noDataTV.text=getString(R.string.no_donation_found)
        binding.donationRV.run {
            lManager = GridLayoutManager(context, 2)
            layoutManager = lManager
            donationAdapter = MyAnnouncementAdapter(requireContext(),this@DonationProfileFragment)
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
        donationAdapter?.setObjectType(Constant.DONATION)
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {

                if (!isFirstTime && !isLoader && loaderMore) {
                    callMyAnnouncement(false)
                }
                isFirstTime = false
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.donationRV.addOnScrollListener(scrollListener)
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
                val arrayAdapter = object : ArrayAdapter<StatusType>(requireContext(),  R.layout.adapter_dropdown, R.id.optionNameTV,  Constant.Singleton.statusList(requireContext())) {
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
        isFirstTime=true
        dummy.clear()
        setUpDonationRV()
        callMyAnnouncement(true)
    }

    override fun setUpObserver() {
        profileViewModel.myAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING ->
                    if(isLoader) {
                        mProgressDialog.showProgressDialog()
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
                                    callMyAnnouncement(true)
                                }
                            }
                        }
                    }
                }
            }
        }

        profileViewModel.deleteAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
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
                                   callDeleteAnnouncement()
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

    private fun callMyAnnouncement(isLoadMore: Boolean) {
        isLoader=isLoadMore
        profileViewModel.myAnnouncement(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to dummy.size.toString(),
                Constant.OBJECT_TYPE to Constant.DONATION,
                Constant.TYPE to type.lowercase(),
                Constant.STATUS to status.lowercase()
            )
        )
    }

    private fun callDeleteAnnouncement()
    {
        profileViewModel.deleteAnnouncement(mapOf(Constant.ANNOUNCEMENT_ID to announcementId))
    }

    override fun onBaseBackPressed() {
        if(from==Constant.PUSH &&  requireActivity().isTaskRoot)
        {
            requireActivity().start<MainActivity>("1")
        }
        else {
            requireActivity().finish()
        }
    }


    private fun bindData(donationData: MutableList<MyAnnouncementResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.donationRV.gone()
            return
        }
        loaderMore=true
        binding.noDataLayout.root.gone()
        binding.donationRV.visible()
        donationList.addAll(donationData)
        dummy.addAll(donationData)
        donationAdapter?.removeLoading()
        donationAdapter?.addData(donationData)
        donationAdapter?.addLoading()
        if (donationData.isEmpty() || donationData.size < 10) {
            donationAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
    }


    private fun showOptionPopup(
        optionIV: AppCompatImageView,
        announcementData: MyAnnouncementResponse.Data
    )
    {
        mPopupMenuBinding = LayoutOptionPopupBinding.inflate(layoutInflater)
        popupWindow = PopupWindow(requireContext())
        popupWindow.isFocusable = true
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = mPopupMenuBinding!!.root
        popupWindow.setBackgroundDrawable(null)
        popupWindow.elevation = 5f
        mPopupMenuBinding?.apply {
            editTV.setSafeOnClickListener {
                popupWindow.dismiss()
                val intent=Intent(requireContext(),PublishAnnouncementActivity::class.java)
                intent.putExtra(Constant.FROM ,Constant.EDIT)
                intent.putExtra(Constant.ANNOUNCEMENT_DATA ,announcementData)
                startUpdateResult.launch(intent)
            }
            deleteTV.setSafeOnClickListener {
                popupWindow.dismiss()
                announcementId=announcementData.id.toString()
                showConfirmDialog()

            }
        }
        popupWindow.showAsDropDown(optionIV, 0, -30)
    }

    private val startUpdateResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                   refreshData()
                }
            }
        }
    override fun onItemOptionClick(
        announcementData: MyAnnouncementResponse.Data,
        likeIV: AppCompatImageView,
    )
    {
        showOptionPopup(likeIV,announcementData)
    }

    override fun onItemClick(announcementData: MyAnnouncementResponse.Data) {
        val intent=Intent(requireContext(),AnnouncementDetailsActivity::class.java)
        intent.putExtra(Constant.FROM ,Constant.EDIT)
        intent.putExtra(Constant.ANNOUNCEMENT_ID ,announcementData.id.toString())
        intent.putExtra(Constant.ANNOUNCEMENT_DATA ,announcementData)
        startUpdateResult.launch(intent)
    }

    private fun showConfirmDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogDeleteConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            msgTV.text=getString(R.string.delete_announcement_msg)
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

}