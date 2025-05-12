package com.app.yajari.ui.home
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.AnnouncementResponse
import com.app.yajari.databinding.AdapterHomeBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.TimeAgo
import com.app.yajari.utils.animateLikeImage
import com.app.yajari.utils.getClr
import com.app.yajari.utils.getMilliSeconds
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible
import kotlin.properties.Delegates

class DonationAdapter (private var mContext:Context,private var listener:DonationListener,private var objectType:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),AutoUpdatableAdapter
{
    private var isHome=true
    private var userLoginId=0
    companion object {
    private const val LOAD_ITEM_VIEW_TYPE = 0
    private const val ITEM = 1
}

    override fun getItemCount() =donationList.size

    inner class MyViewHolder(val binding : AdapterHomeBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding : LayoutDialogMoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> MyViewHolder(AdapterHomeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> ProgressViewHolder(LayoutDialogMoreBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (donationList[position].id == 0) {
            LOAD_ITEM_VIEW_TYPE
        } else
            ITEM
    }

    //Add Loading Progress At Bottom
    internal fun addLoading() {
        donationList.add(AnnouncementResponse.Data())
        notifyItemInserted(donationList.size - 1)
    }

    //Remove Loading progress After Loading More Item
    internal fun removeLoading() {
        if (donationList.size != 0) {
            val position = donationList.size - 1
            if (donationList[position].id == 0) {
                donationList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /*Add API data to list*/
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        donation: MutableList<AnnouncementResponse.Data>?,
        isSwipeRefresh: Boolean
    ) {
        if (donation!!.isEmpty()) return
        if(isSwipeRefresh) {
            this.donationList.clear()
        }
        val newItems = donationList
        newItems.addAll(donation)
        this.donationList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }
    internal fun setUserId(userId: Int)

    {
        this.userLoginId=userId
    }

    internal fun setObjectType(oType:String,isComeHome:Boolean)
    {
        this.objectType=oType
        this.isHome=isComeHome
    }
    private var donationList: MutableList<AnnouncementResponse.Data> by Delegates.observable(mutableListOf()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }

    interface DonationListener {
        fun onItemLike(announcementData: AnnouncementResponse.Data)
        fun onItemClick(announcementData: AnnouncementResponse.Data)
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
                            holder.binding.likeIV.isSelected = donationList[position].isBookmark == 1
                        }
                    }

                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }

    }

    public data class LikeImage(val isLike: Int)

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val data = donationList[position]

            holder.binding.apply {
                if(objectType==Constant.FOOD)
                {
                    tagTV.gone()
                }
                else{
                    tagTV.visible()
                    tagTV.text=data.condition
                }

                likeIV.isSelected = data.isBookmark==1
                productNameTV.text=data.title
                kmTV.text="${String.format("%.1f",data.distance?.toFloat())} km"
                    timeTV.text = TimeAgo(context = mContext).timeAgo(
                        getMilliSeconds(
                            data.createdAt,
                            Constant.backEndUTCFormat,
                            true
                        )
                    )

                when (data.status) {
                    Constant.AVAILABLE -> {
                        announcementTV.text=mContext.getStr(R.string.available)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_available_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_available))
                    }
                    Constant.RESERVED -> {
                        announcementTV.text=mContext.getStr(R.string.reserved)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reserved_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_reserved))
                    }
                    Constant.FINALIZED -> {
                        announcementTV.text=mContext.getStr(R.string.finalized)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_finalize))
                    }
                }


                productIV.loadImage(data.image, R.drawable.ic_placeholder_yajari)
                likeIV.setOnClickListener {
                    if(userLoginId!=0) {
                        if (data.isBookmark == 1) {
                            data.isBookmark = 0
                        } else {
                            data.isBookmark = 1
                        }
                        if (data.isBookmark == 1) {
                            animateLikeImage(mContext, likeIV, false)
                        } else {
                            animateLikeImage(mContext, likeIV, true)
                        }
                        notifyItemChanged(position,LikeImage(data.isBookmark!!))
                    }
                    listener.onItemLike(data)
                }
            }
            holder.itemView.setSafeOnClickListener {
                listener.onItemClick(data)
            }

        }
    }
}
