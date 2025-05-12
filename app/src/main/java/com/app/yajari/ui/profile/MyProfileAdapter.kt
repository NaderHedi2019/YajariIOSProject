package com.app.yajari.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.HomeData
import com.app.yajari.databinding.AdapterFinalizedBinding
import com.app.yajari.utils.setSafeOnClickListener

class MyProfileAdapter (
    private var homeList: MutableList<HomeData>,
    var mContext: Context,
    var listener: OtherProfileListener
) : RecyclerView.Adapter<MyProfileAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val homeData=homeList[position]
//            val params: ViewGroup.MarginLayoutParams =
//                finalizedCL.layoutParams as ViewGroup.MarginLayoutParams
//            if (position == 0 || position==1) {
//                params.topMargin = 20
//            }
//            finalizedCL.layoutParams = params
            tagTV.text=homeData.tag
            likeIV.setImageResource(R.drawable.ic_option_menu)
            likeIV.setSafeOnClickListener {
                listener.onItemOptionClick(likeIV)
            }
            holder.itemView.setSafeOnClickListener {
                listener.onItemMyProfileClick()
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
        fun onItemOptionClick(likeIV: AppCompatImageView)
        fun onItemMyProfileClick()
    }

}