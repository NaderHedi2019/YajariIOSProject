package com.app.yajari.ui.request_details

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.ChatListResponse
import com.app.yajari.databinding.AdapterRequestedUserBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.TimeAgo
import com.app.yajari.utils.getClr
import com.app.yajari.utils.getMilliSeconds
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.visible
import kotlin.properties.Delegates

class RequestUserAdapter(
    private var mContext:Context,
    private var listener: RequestUserListener
) : RecyclerView.Adapter<RequestUserAdapter.MyViewHolder>(),AutoUpdatableAdapter {
    private var announcementBookUserId=""
    private var status=""
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            statusTV.gone()
            val userData=userList[position]
            profileIV.loadImage(userData.profileImage, R.drawable.ic_placeholder_yajari)
            requestNameTV.text=userData.name
            lastMsgTV.text=userData.messageLatest?.message
            if(announcementBookUserId.isNotEmpty() && userData.id.toString()==announcementBookUserId)
            {
                statusTV.visible()
                when (status) {
                    Constant.RESERVED -> {
                        statusTV.text=mContext.getStr(R.string.reserved)
                        statusTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reserved_announcement,0,0,0)
                        statusTV.setTextColor(mContext.getClr(R.color.color_reserved))
                    }
                    Constant.FINALIZED -> {
                        statusTV.text=mContext.getStr(R.string.finalized)
                        statusTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_announcement,0,0,0)
                        statusTV.setTextColor(mContext.getClr(R.color.color_finalize))
                    }
                }
            }
            if (!userData.messageLatest?.createdAt.isNullOrEmpty()) {
                agoTV.text = TimeAgo(context = mContext).timeAgo(
                    getMilliSeconds(
                        userData.messageLatest?.createdAt,
                        Constant.backEndUTCFormat,
                        true
                    )
                )
            }

            if(userData.unreadMessage==0)
            {
                unReadCountTV.gone()
            }
            else{
                unReadCountTV.visible()
                unReadCountTV.text=userData.unreadMessage.toString()
            }
            holder.itemView.setOnClickListener {
                listener.onItemRequestUserClick(position,userData)
            }
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        users: MutableList<ChatListResponse.FriendRequest>?,
        isSwipeRefresh: Boolean,
        objectId: String?,
        announcementStatus: String
    ) {
        if (users!!.isEmpty()) return
        announcementBookUserId= objectId.toString()
        status=announcementStatus
        if(isSwipeRefresh) {
            this.userList.clear()
        }
        val newItems = userList
        newItems.addAll(users)
        this.userList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }


    override fun getItemCount() = userList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterRequestedUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterRequestedUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface RequestUserListener{
        fun onItemRequestUserClick(position: Int, userData: ChatListResponse.FriendRequest)
    }


     var userList: MutableList<ChatListResponse.FriendRequest> by Delegates.observable(
        mutableListOf()
    ) { _, old, new ->
        autoNotify(
            old,
            new
        ) { o, n -> o == n }
    }


}
