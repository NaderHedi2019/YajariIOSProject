package com.app.yajari.ui.my_favourite
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.FavouriteUserResponse
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.databinding.FragmentUsersBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.other_profile.OtherUserProfileActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.ui.rate_review.RateReviewActivity
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


class UsersFragment : BaseFragment<FragmentUsersBinding>(),UserAdapter.UserListener {
    private var isLoader = false
    private var loaderMore = false
    private var unFavUserId=""
    private var dummy = mutableListOf<FavouriteUserResponse.Data>()
    private var userFavList = mutableListOf<FavouriteUserResponse.Data>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var userFavAdapter: UserAdapter? = null
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity()) }
    var isFirstTime = true
    override fun getViewBinding()=FragmentUsersBinding.inflate(layoutInflater)

    override fun initObj() {
    }

    override fun click() {
    }

    override fun setUpObserver() {
        setUpUserRV()
        setRVScrollListener()
        callUserFav(true)

        profileViewModel.favUserResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
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
                                    callUserFav(true)
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.favResponse.observe(this) { response ->
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
                                   callUnFavouriteUser()
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

    override fun onBaseBackPressed() {
       requireActivity().finish()
    }

    private fun setUpUserRV() {
        binding.noDataLayout.noDataTV.text=getString(R.string.no_fav_user_found)
        binding.userRV.run {
            lManager = LinearLayoutManager(context)
            layoutManager = lManager
            userFavAdapter = UserAdapter(requireContext(),this@UsersFragment)
            adapter = userFavAdapter
        }
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {

                if (!isFirstTime && !isLoader && loaderMore) {
                    callUserFav(false)
                }
                isFirstTime = false
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.userRV.addOnScrollListener(scrollListener)
    }
    private fun refreshData()
    {
        isFirstTime=true
        dummy.clear()
        setUpUserRV()
        callUserFav(true)
    }


    private fun callUserFav(isLoadMore: Boolean) {
        isLoader=isLoadMore
        profileViewModel.favouriteUser(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to dummy.size.toString()
            )
        )
    }

    private fun callUnFavouriteUser()
    {
        profileViewModel.unFavouriteUser(mapOf(Constant.FRIEND_ID to unFavUserId))
    }

    private fun bindData(donationData: MutableList<FavouriteUserResponse.Data>?) {
        if (donationData!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.userRV.gone()
            return
        }
        loaderMore=true
        binding.noDataLayout.root.gone()
        binding.userRV.visible()
        userFavList.addAll(donationData)
        dummy.addAll(donationData)
        userFavAdapter?.removeLoading()
        userFavAdapter?.addData(donationData)
        userFavAdapter?.addLoading()
        if (donationData.isEmpty() || donationData.size < 10) {
            userFavAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
    }


    override fun onItemUserClick(userResponse: FavouriteUserResponse.Data) {
        val intent = Intent(requireContext(), OtherUserProfileActivity::class.java)
        intent.putExtra(Constant.USER_ID, userResponse.id)
        intent.putExtra(Constant.FROM, "fav")
        startUserLikeResult.launch(intent)
    }
    private val startUserLikeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    refreshData()
                }
            }
        }

    override fun onItemUserUnlike(userResponse: FavouriteUserResponse.Data) {
        unFavUserId=userResponse.id.toString()
        callUnFavouriteUser()
    }

    override fun onItemRatingClick(userResponse: FavouriteUserResponse.Data) {
        requireActivity().start<RateReviewActivity>("2",Constant.USER_ID to userResponse.id.toString())
    }



}