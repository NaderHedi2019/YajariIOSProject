package com.app.yajari.ui.other_profile

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.HomeData
import com.app.yajari.databinding.AdapterFinalizedBinding
import com.app.yajari.utils.animateLikeImage
import com.app.yajari.utils.setSafeOnClickListener

class OtherProfileAdapter (
    private var homeList: MutableList<HomeData>,
    var mContext: Context,
    var listener: OtherProfileListener
) : RecyclerView.Adapter<OtherProfileAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val params: ViewGroup.MarginLayoutParams =
                finalizedCL.layoutParams as ViewGroup.MarginLayoutParams

            if (position == 0 || position==1) {
                params.topMargin = 20
            }
            finalizedCL.layoutParams = params
            val homeData=homeList[position]

            tagTV.text=homeData.tag
            likeIV.isSelected = homeData.isLike

            likeIV.setSafeOnClickListener {
                homeData.isLike = !homeData.isLike
                if(homeData.isLike)
                {
                    animateLikeImage(mContext,likeIV,false)
                }
                else{
                    animateLikeImage(mContext,likeIV,true)
                }
                notifyItemChanged(position)
            }
            holder.itemView.setSafeOnClickListener {
                listener.onItemOtherProfileClick()
            }
        }
    }

    override fun getItemCount() = homeList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterFinalizedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterFinalizedBinding) : RecyclerView.ViewHolder(binding.root)

    interface OtherProfileListener{
        fun onItemOtherProfileClick()
    }

}