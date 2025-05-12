package com.app.yajari.ui.rate_review

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.RateReviewResponse
import com.app.yajari.databinding.ActivityRateReviewBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
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

class RateReviewActivity : BaseActivity<ActivityRateReviewBinding>() {
    private var isLoader = false
    private var isFirstTime = true
    private var userId=""
    private var from=""
    private var rateReviewList = mutableListOf<RateReviewResponse.RateReview>()
    private lateinit var lManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: EndlessScrollListener
    private var rateReviewAdapter: RateReviewAdapter? = null
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this@RateReviewActivity) }
    override fun getViewBinding()= ActivityRateReviewBinding.inflate(layoutInflater)
    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        from=intent.getStringExtra(Constant.FROM).toString()
        if(from!=Constant.PUSH && from!=Constant.NOTIFICATION) {
            userId = intent.getStringExtra(Constant.USER_ID).toString()
        }
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                if (from==Constant.PUSH && isTaskRoot)
                {
                    start<MainActivity>("1")
                }
                else {
                    finish()
                }
            }
            toolbar.titleTV.text=getString(R.string.rate_review)
        }
    }

    override fun setUpObserver() {
        setRateReviewRV()
        setRVScrollListener()
        callGetRateReview(true)
        profileViewModel.rateReviewResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    if(isLoader) {
                        mProgressDialog.showProgressDialog()
                    }
                }
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data?.let {
                        bindData(it.list)
                        if(isFirstTime)
                        {
                            setupReviewData(it.review)
                        }
                    }
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@RateReviewActivity,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    profileViewModel.logoutUser()
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
                                    callGetRateReview(true)
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
                start<LoginActivity>("1")
            }
        }
    }

    private fun setupReviewData(review: RateReviewResponse.Review?) {
        review?.let {
            binding.apply {
                avgRatingRB.rating=review.avgRating!!.toFloat()
                avgRatingTV.text=review.avgRating
                totalReviewTV.text=getString(R.string.v_rating_amp_reviews,review.totalReview)
                fiveStarCountTV.text=review.rating5Count
                fourStarCountTV.text=review.rating4Count
                threeStarCountTV.text=review.rating3Count
                twoStarCountTV.text=review.rating2Count
                oneStarCountTV.text=review.rating1Count
                if(review.totalReview=="0")
                {
                    fiveStarPB.progress = 0
                    fourStarPB.progress = 0
                    threeStarPB.progress = 0
                    twoStarPB.progress = 0
                    oneStarPB.progress = 0
                }
                else {
                    val fivePercentage =
                        (100 * review.rating5Count?.toInt()!!) / review.totalReview?.toInt()!!
                    fiveStarPB.progress = fivePercentage
                    val fourPercentage =
                        (100 * review.rating4Count?.toInt()!!) / review.totalReview.toInt()
                    fourStarPB.progress = fourPercentage
                    val threePercentage =
                        (100 * review.rating3Count?.toInt()!!) / review.totalReview.toInt()
                    threeStarPB.progress = threePercentage
                    val twoPercentage =
                        (100 * review.rating2Count?.toInt()!!) / review.totalReview.toInt()
                    twoStarPB.progress = twoPercentage
                    val onePercentage =
                        (100 * review.rating1Count?.toInt()!!) / review.totalReview.toInt()
                    oneStarPB.progress = onePercentage
                }
            }
        }
    }

    override fun onBaseBackPressed() {
        if (from==Constant.PUSH && isTaskRoot)
        {
            start<MainActivity>("1")
        }
        else {
            finish()
        }
    }

    private fun setRateReviewRV() {
        binding.noDataLayout.noDataTV.text= getString(R.string.no_rate_review_found)
        binding.rateReviewRV.run {
            lManager = LinearLayoutManager(context)
            layoutManager = lManager
            rateReviewAdapter = RateReviewAdapter()
            adapter = rateReviewAdapter
        }
    }

    private fun setRVScrollListener() {
        scrollListener = EndlessScrollListener(lManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {

                if (!isLoader && !scrollListener.getLoaded()) {
                    callGetRateReview(false)
                }
                isLoader = false
            }

            override fun onScrollChange(dy: Int, scrollState: Int) {
            }
        })
        binding.rateReviewRV.addOnScrollListener(scrollListener)
    }

    private fun callGetRateReview(isLoadMore: Boolean) {
        isLoader=isLoadMore
        profileViewModel.rateReview(
            mapOf(
                Constant.LIMIT_PARAM to Constant.LIMIT,
                Constant.OFFSET_PARAM to rateReviewList.size.toString(),
                Constant.USER_ID to userId
            )
        )
    }
    private fun bindData(rateReviews: MutableList<RateReviewResponse.RateReview>?) {
        if (rateReviews!!.isEmpty() && isLoader) {
            binding.noDataLayout.root.visible()
            binding.rateReviewRV.gone()
            return
        }
        binding.noDataLayout.root.gone()
        binding.rateReviewRV.visible()
        rateReviewList.addAll(rateReviews)
        rateReviewAdapter?.removeLoading()
        rateReviewAdapter?.addData(rateReviews)
        rateReviewAdapter?.addLoading()
        if (rateReviews.isEmpty() || rateReviews.size < 10) {
            rateReviewAdapter?.removeLoading()
            scrollListener.setLoaded()
        }
    }

}