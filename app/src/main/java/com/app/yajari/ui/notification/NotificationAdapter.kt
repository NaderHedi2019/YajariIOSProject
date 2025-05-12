package com.app.yajari.ui.notification
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.NotificationResponse
import com.app.yajari.databinding.AdapterNotificationBinding
import com.app.yajari.databinding.LayoutDialogMoreBinding
import com.app.yajari.utils.AutoUpdatableAdapter
import com.app.yajari.utils.Constant
import com.app.yajari.utils.TimeAgo
import com.app.yajari.utils.getMilliSeconds
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import kotlin.properties.Delegates

class NotificationAdapter(private var listener:NotificationListener,private var mContext:Context) :RecyclerView.Adapter<RecyclerView.ViewHolder>(),AutoUpdatableAdapter {
    companion object {
        private const val LOAD_ITEM_VIEW_TYPE = 0
        private const val ITEM = 1
    }

    override fun getItemCount() =notificationList.size

    inner class MyViewHolder(val binding : AdapterNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ProgressViewHolder(val binding : LayoutDialogMoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> MyViewHolder(AdapterNotificationBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> ProgressViewHolder(LayoutDialogMoreBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (notificationList[position].id == 0) {
            LOAD_ITEM_VIEW_TYPE
        } else
            ITEM
    }

    //Add Loading Progress At Bottom
    internal fun addLoading() {
        notificationList.add(NotificationResponse.Data())
        notifyItemInserted(notificationList.size - 1)
    }

    //Remove Loading progress After Loading More Item
    internal fun removeLoading() {
        if (notificationList.size != 0) {
            val position = notificationList.size - 1
            if (notificationList[position].id == 0) {
                notificationList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    /*Add API data to list*/
    @SuppressLint("NotifyDataSetChanged")
    internal fun addData(
        donation: MutableList<NotificationResponse.Data>?
    ) {
        if (donation == null) return
        val newItems = notificationList
        newItems.addAll(donation)
        notificationList = newItems.toSet().toMutableList()
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            holder.binding.apply {
                val notificationData=notificationList[position]
                val params: ViewGroup.MarginLayoutParams =
                    notificationCL.layoutParams as ViewGroup.MarginLayoutParams

                if (position == 0) {
                    params.topMargin = 40

                } else if (position == notificationList.size-1) {
                    params.bottomMargin = 40
                }
                notificationCL.layoutParams = params
              //  titleTV.text=notificationData.pushTitle
                titleTV.text=mContext.getString(R.string.app_name)
                msgTV.text=notificationData.pushMessage
                if (!notificationData.createdAt.isNullOrEmpty()) {
                    agoTV.text = TimeAgo(context = mContext).timeAgo(
                        getMilliSeconds(
                            notificationData.createdAt,
                            Constant.backEndUTCFormat,
                            true
                        )
                    )
                }
                holder.itemView.setSafeOnClickListener {
                    listener.onItemClickNotification(notificationData)
                }
            }
        }

    }

    private var notificationList: MutableList<NotificationResponse.Data> by Delegates.observable(mutableListOf()) { _, old, new ->
        autoNotify(old, new) { o, n -> o == n }
    }

    interface NotificationListener
    {
        fun onItemClickNotification(notificationData: NotificationResponse.Data)
    }

}
