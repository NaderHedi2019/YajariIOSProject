package com.app.yajari.ui.rate_review

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.RateReviewResponse
import com.app.yajari.databinding.AdapterRateReviewBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.loadImage
import kotlin.properties.Delegates

class RateReviewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(), AutoUpdatableAdapter {
    companion object {
        private const val LOAD_ITEM_VIEW_TYPE = 0
        private const val ITEM = 1
    }


    override fun getItemCount() =reviewList.size

    inner class MyViewHolder(val binding : AdapterRateReviewBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding : LayoutDialogMoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> MyViewHolder(AdapterRateReviewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> ProgressViewHolder(LayoutDialogMoreBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (reviewList[position].id == 0) {
            LOAD_ITEM_VIEW_TYPE
        } else
            ITEM
    }

    //Add Loading Progress At Bottom
    internal fun addLoading() {
        reviewList.add(RateReviewResponse.RateReview())
        notifyItemInserted(reviewList.size - 1)
    }

    //Remove Loading progress After Loading More Item
    internal fun removeLoading() {
        if (reviewList.size != 0) {
            val position = reviewList.size - 1
            if (reviewList[position].id == 0) {
                reviewList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /*Add API data to list*/
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        donation: MutableList<RateReviewResponse.RateReview>?
    ) {
        if (donation == null) return
        val newItems = reviewList
        newItems.addAll(donation)
        reviewList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val reviewData=reviewList[position]
            holder.binding.apply {
                val params: ViewGroup.MarginLayoutParams =
                    rateCV.layoutParams as ViewGroup.MarginLayoutParams

                if (position == 0) {
                    params.topMargin = 40

                } else if (position == reviewList.size-1) {
                    params.bottomMargin = 40
                }
                rateCV.layoutParams = params
                rateTV.text=reviewData.rating
                reviewTV.text=reviewData.comment
                rateUserNameTV.text=reviewData.user?.name
                rateProfileIV.loadImage(reviewData.user?.profileImage, R.drawable.img_user)
            }
        }
    }
    private var reviewList: MutableList<RateReviewResponse.RateReview> by Delegates.observable(mutableListOf()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }


}
