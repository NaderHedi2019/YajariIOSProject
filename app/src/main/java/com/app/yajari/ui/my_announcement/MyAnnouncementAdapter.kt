package com.app.yajari.ui.my_announcement
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.databinding.AdapterHomeBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.TimeAgo
import com.app.yajari.utils.getClr
import com.app.yajari.utils.getMilliSeconds
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible
import kotlin.properties.Delegates

class MyAnnouncementAdapter (private var mContext: Context, private var listener:MyAnnouncementListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),AutoUpdatableAdapter
{
    private var objectType:String=""

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
        donationList.add(MyAnnouncementResponse.Data())
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
        donation: MutableList<MyAnnouncementResponse.Data>?
    ) {
        if (donation == null) return
        val newItems =  this.donationList
        newItems.addAll(donation)
        this.donationList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }

    internal fun setObjectType(oType:String)
    {
        this.objectType=oType
    }



    interface MyAnnouncementListener {
        fun onItemOptionClick(
            announcementData: MyAnnouncementResponse.Data,
            likeIV: AppCompatImageView
        )
        fun onItemClick(announcementData: MyAnnouncementResponse.Data)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) = if (payloads.isNotEmpty()) {
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
    private var donationList: MutableList<MyAnnouncementResponse.Data> by Delegates.observable(mutableListOf()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }

    data class LikeImage(val isLike: Int)

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val data = donationList[position]
            Log.e("Position:###","$position")
            holder.binding.apply {
                likeIV.setImageResource(R.drawable.ic_option_menu)
                if(data.type== Constant.FOOD || data.objectType==Constant.REQUEST)
                {
                    tagTV.gone()
                }
                else{
                    tagTV.visible()
                    tagTV.text=data.condition
                }

                productNameTV.text=data.title
                kmTV.text="${String.format("%.1f",data.distance?.toFloat())} km"
                timeTV.text= TimeAgo(context = mContext).timeAgo(
                    getMilliSeconds(
                        data.createdAt,
                        Constant.backEndUTCFormat,
                        true
                    )
                )
                when (data.status) {
                    Constant.AVAILABLE -> {
                        likeIV.visible()
                        announcementTV.text=mContext.getStr(R.string.available)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_available_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_available))
                    }
                    Constant.RESERVED -> {
                        likeIV.gone()
                        announcementTV.text=mContext.getStr(R.string.reserved)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reserved_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_reserved))
                    }
                    Constant.FINALIZED -> {
                        likeIV.gone()
                        announcementTV.text=mContext.getStr(R.string.finalized)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_finalize))
                    }
                    Constant.REJECTED -> {
                        likeIV.visible()
                        announcementTV.text=mContext.getStr(R.string.reject_by_admin)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_rejected))
                    }
                    Constant.PENDING -> {
                        likeIV.visible()
                        announcementTV.text=mContext.getStr(R.string.pending)
                        announcementTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pending_announcement,0,0,0)
                        announcementTV.setTextColor(mContext.getClr(R.color.color_pending))
                    }
                }

                productIV.loadImage(data.image, R.drawable.ic_placeholder_yajari)
                likeIV.setOnClickListener {
                    listener.onItemOptionClick(data,likeIV)
                }
                holder.itemView.setSafeOnClickListener {
                        listener.onItemClick(data)
                }
            }


        }
    }
}