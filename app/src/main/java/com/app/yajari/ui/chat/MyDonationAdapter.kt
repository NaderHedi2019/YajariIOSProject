package com.app.yajari.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.ChatListResponse
import com.app.yajari.databinding.AdapterMyDonationBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.getClr
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setMarginBottom
import com.app.yajari.utils.setMarginTop
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible
import kotlin.properties.Delegates

class MyDonationAdapter(private var mContext: Context, private var listener: MyDonationListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    companion object {
        private const val LOAD_ITEM_VIEW_TYPE = 0
        private const val ITEM = 1
    }

    override fun getItemCount() = chatList.size

    inner class MyViewHolder(val binding: AdapterMyDonationBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding: LayoutDialogMoreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> MyViewHolder(
                AdapterMyDonationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> ProgressViewHolder(
                LayoutDialogMoreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].id == 0) {
            LOAD_ITEM_VIEW_TYPE
        } else
            ITEM
    }

    //Add Loading Progress At Bottom
    internal fun addLoading() {
        chatList.add(ChatListResponse.Data())
        notifyItemInserted(chatList.size - 1)
    }

    //Remove Loading progress After Loading More Item
    internal fun removeLoading() {
        if (chatList.size != 0) {
            val position = chatList.size - 1
            if (chatList[position].id == 0) {
                chatList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /*Add API data to list*/
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        donation: MutableList<ChatListResponse.Data>?,
        isSwipeRefresh: Boolean,
    ) {
        if (donation!!.isEmpty()) return
        if (isSwipeRefresh) {
            this.chatList.clear()
        }
        val newItems = chatList
        newItems.addAll(donation)
        this.chatList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }

    private var chatList: MutableList<ChatListResponse.Data> by Delegates.observable(mutableListOf()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val data = chatList[position]
            holder.binding.apply {
                val params: ViewGroup.MarginLayoutParams =
                    myDonationCL.layoutParams as ViewGroup.MarginLayoutParams
                when (position) {
                    0 -> {
                        params.topMargin = 0

                    }

                    chatList.size - 1 -> {
                        params.bottomMargin = 40
                    }

                    else -> {
                        params.bottomMargin = 0
                    }
                }
                myDonationCL.layoutParams = params

                if (data.unreadMsgCount == 0) {
                    requestTV.gone()
                    requestCountTV.gone()
                } else {
                    requestTV.visible()
                    requestCountTV.visible()
                    requestCountTV.text = data.unreadMsgCount.toString()
                }
                profileIV.loadImage(data.thumbImage?.file, R.drawable.ic_placeholder_yajari)
                requestNameTV.text = data.title

                when (data.status) {
                    Constant.AVAILABLE -> {
                        requestTypeTV.text = mContext.getStr(R.string.available)
                        requestTypeTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_available_announcement,
                            0,
                            0,
                            0
                        )
                        requestTypeTV.setTextColor(mContext.getClr(R.color.color_available))
                    }

                    Constant.RESERVED -> {
                        requestTypeTV.text = mContext.getStr(R.string.reserved)
                        requestTypeTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_reserved_announcement,
                            0,
                            0,
                            0
                        )
                        requestTypeTV.setTextColor(mContext.getClr(R.color.color_reserved))
                    }

                    Constant.FINALIZED -> {
                        requestTypeTV.text = mContext.getStr(R.string.finalized)
                        requestTypeTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_announcement,
                            0,
                            0,
                            0
                        )
                        requestTypeTV.setTextColor(mContext.getClr(R.color.color_finalize))
                    }
                }
            }

            holder.itemView.setSafeOnClickListener {
                listener.onItemDonationClick(data)
            }
        }
    }

    interface MyDonationListener {
        fun onItemDonationClick(data: ChatListResponse.Data)
    }

}