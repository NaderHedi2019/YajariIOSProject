package com.app.yajari.ui.my_favourite

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.FavouriteUserResponse
import com.app.yajari.databinding.AdapterUserBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.ui.home.DonationAdapter
import com.app.yajari.utils.animateLikeImage
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setMarginBottom
import com.app.yajari.utils.setMarginTop
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.underline

class UserAdapter(private var mContext:Context,private var listener:UserListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var userList = mutableListOf<FavouriteUserResponse.Data?>()

    companion object {
        private const val LOAD_ITEM_VIEW_TYPE = 0
        private const val ITEM = 1
    }

    inner class MyViewHolder(val binding : AdapterUserBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding : LayoutDialogMoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> MyViewHolder(AdapterUserBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> ProgressViewHolder(LayoutDialogMoreBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (userList[position]?.id == 0) {
            LOAD_ITEM_VIEW_TYPE
        } else
            ITEM
    }

    //Add Loading Progress At Bottom
    internal fun addLoading() {
        userList.add(FavouriteUserResponse.Data())
        notifyItemInserted(userList.size - 1)
    }

    //Remove Loading progress After Loading More Item
    internal fun removeLoading() {
        if (userList.size != 0) {
            val position = userList.size - 1
            if (userList[position]?.id == 0) {
                userList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /*Add API data to list*/
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        favUser: MutableList<FavouriteUserResponse.Data>?
    ) {
        if (favUser == null) return
        val newItems = userList
        newItems.addAll(favUser)
        userList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    )
    {
        if (payloads.isNotEmpty()) {
            for (payload in payloads) {
                when (payload) {
                    is LikeImage -> {
                        if (holder is MyViewHolder) {
                            holder.binding.likeIV.isSelected =false
                        }
                    }

                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }

    }
    data class LikeImage(val isLike: Int)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val data = userList[position]!!
            holder.binding.apply {

                profileIV.loadImage(data.profileImage, R.drawable.img_user)
                donateNameTV.text=data.name
                ratingTV.text=data.avgRating.toString()
                totalReviewTV.text=data.totalReview.toString()
                donationCountTV.text=data.totalReview.toString()
                collectionCountTV.text=data.collectionCount.toString()
                userAddressTV.text=data.address
                if (!data.distance.isNullOrEmpty()) {
                    kmTV.text = "${String.format("%.1f", data.distance.toFloat())} km"
                }
                if (position == 0) {
                    userCV.setMarginTop(20)

                } else if (position == 10) {
                    userCV.setMarginBottom(20)
                }
                ratingTV.underline()
                totalReviewTV.underline()
                likeIV.setSafeOnClickListener {
                    animateLikeImage(mContext, likeIV, false)
                    notifyItemChanged(position, LikeImage(0))
                    likeIV.isSelected = false
                    listener.onItemUserUnlike(data)
                }
                holder.itemView.setSafeOnClickListener {
                    listener.onItemUserClick(data)
                }
                ratingTV.setSafeOnClickListener {
                    listener.onItemRatingClick(data)
                }
                totalReviewTV.setSafeOnClickListener {
                    listener.onItemRatingClick(data)
                }
            }
        }
    }



    override fun getItemCount() = userList.size



    interface UserListener{
        fun onItemUserClick(userResponse: FavouriteUserResponse.Data)
        fun onItemUserUnlike(userResponse: FavouriteUserResponse.Data)
        fun onItemRatingClick(userResponse: FavouriteUserResponse.Data)
    }

}
