package com.app.yajari.ui.chat_detail

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.ChatDetailResponse
import com.app.yajari.databinding.AdapterMsgTimeBinding
import com.app.yajari.databinding.AdapterReceiverMsgBinding
import com.app.yajari.databinding.AdapterSendMsgBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.getUtc2LocalDateTime
import kotlin.properties.Delegates

class ChatDetailsAdapter(private val mContext: Activity, private var loginUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    companion object {
        const val MyChat = 1
        const val ReceiverChat = 2
        const val dateTime = 3
        private const val loaderLayout = 0
    }

    inner class MyChatViewHolder(val binding: AdapterSendMsgBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceiverChatViewHolder(val binding: AdapterReceiverMsgBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class DateViewHolder(val binding: AdapterMsgTimeBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding: LayoutDialogMoreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (chatMessageList[position].senderId) {
            0 -> {
                loaderLayout
            }

            loginUserId.toInt() -> {
                MyChat
            }

            else -> {
                ReceiverChat
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MyChat -> MyChatViewHolder(
                AdapterSendMsgBinding.inflate(
                    LayoutInflater.from(mContext),
                    parent,
                    false
                )
            )

            ReceiverChat -> ReceiverChatViewHolder(
                AdapterReceiverMsgBinding.inflate(
                    LayoutInflater.from(
                        mContext
                    ), parent, false
                )
            )
            // dateTime -> DateViewHolder(AdapterMsgTimeBinding.inflate(LayoutInflater.from(mContext),parent,false))
            else -> ProgressViewHolder(
                LayoutDialogMoreBinding.inflate(
                    LayoutInflater.from(mContext),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = chatMessageList[position]
        when (holder) {
            is MyChatViewHolder -> {
                holder.binding.apply {
                    sendMsgTV.text = data.message
                    if(data.announcementType.isNotEmpty())
                    {
                        when(data.announcementType)
                        {
                            Constant.AVAILABLE ->
                            {
                                sendMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                sendMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_send_msg_cancelled)
                            }
                            Constant.RESERVED -> {
                                sendMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                sendMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_send_msg_reserved)
                            }
                            Constant.FINALIZED -> {
                                sendMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                sendMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_send_msg_finalize)
                            }

                        }
                    }
                    else{
                        sendMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                        sendMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_send_msg)
                    }
                    if (data.createdAt != mContext.getString(R.string.sending)) {
                        sendTimeTV.text = getUtc2LocalDateTime(
                            data.createdAt,
                            Constant.backEndUTCFormat,
                            Constant.chatDisplayFormat
                        )
                    } else {
                        sendTimeTV.text = data.createdAt
                    }


                }
            }

            is ReceiverChatViewHolder -> {
                holder.binding.apply {
                    receiverMsgTV.text = data.message
                    if(data.announcementType.isNotEmpty())
                    {
                        when(data.announcementType)
                        {
                            Constant.AVAILABLE ->
                            {
                                receiverMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                receiverMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_receiver_msg_cancelled)
                            }
                            Constant.RESERVED -> {
                                receiverMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                receiverMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_receiver_msg_reserved)
                            }
                            Constant.FINALIZED -> {
                                receiverMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.white))
                                receiverMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_receiver_msg_finalize)
                            }

                        }
                    }
                    else{
                        receiverMsgTV.setTextColor(ContextCompat.getColor(mContext,R.color.color_151515))
                        receiverMsgTV.background=ContextCompat.getDrawable(mContext,R.drawable.shape_receiver_msg)
                    }

                    if (data.createdAt != mContext.getString(R.string.sending)) {
                        receiverTimeTV.text = getUtc2LocalDateTime(
                            data.createdAt,
                            Constant.backEndUTCFormat,
                            Constant.chatDisplayFormat
                        )
                    } else {
                        receiverTimeTV.text = data.createdAt
                    }

                }
            }

        }
    }

    override fun getItemCount() = chatMessageList.size


    /* Add Loading progress at bottom */
    internal fun addLoading() {
        chatMessageList.add(ChatDetailResponse.Data())
        notifyItemInserted(chatMessageList.size - 1)
    }

    /* Remove loading progress from bottom after loading more items */
    internal fun removeLoading() {
        if (chatMessageList.size != 0) {
            val position = chatMessageList.size - 1
            if (chatMessageList[position].id == 0) {
                chatMessageList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /* Add API data to list */
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        chats: MutableList<ChatDetailResponse.Data>?,
        isSwipeRefresh:Boolean
    ) {
        if (chats == null) return

        if (chats.size == 1) {
            chatMessageList.addAll(chats)
            notifyItemInserted(itemCount - 1)
            notifyItemRangeChanged(itemCount - 1, chatMessageList.size - 1)
        } else {
//            chatMessageList.addAll(chats)
//            notifyItemRangeInserted(itemCount, chatMessageList.size - 1)
//            notifyItemRangeChanged(itemCount, chatMessageList.size - 1)
            if(isSwipeRefresh) {
                chatMessageList.clear()
            }
            val newItems = chatMessageList
            newItems.addAll(chats)
            this.chatMessageList = newItems.toSet().toMutableList()
        }

    }

    internal fun updateMessage(index: Int, chatMessage: ChatDetailResponse.Data?) {
        if(chatMessageList.size==index || chatMessageList.size==0)
        {
            chatMessageList.add(chatMessage!!)
            notifyItemInserted(0)
            notifyItemRangeChanged(0, itemCount - 1)

        }
        else {
            chatMessageList[index] = chatMessage!!
            notifyItemChanged(index)
        }
    }

    internal fun addMessageToFirst(chatMessage: MutableList<ChatDetailResponse.Data>) {
        chatMessageList.addAll(0, chatMessage)
        notifyItemInserted(0)
        notifyItemRangeChanged(0, itemCount - 1)
    }



    override fun getItemId(position: Int): Long {
        val chatData: ChatDetailResponse.Data = chatMessageList[position]
        return chatData.id!!.toLong()
    }

    private var chatMessageList: MutableList<ChatDetailResponse.Data> by Delegates.observable(
        mutableListOf()
    ) { _, old, new ->
        autoNotify(
            old,
            new
        ) { o, n -> o == n }
    }

}
