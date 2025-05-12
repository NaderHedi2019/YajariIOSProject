package com.app.yajari.ui.profile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.ProfileOptionData
import com.app.yajari.databinding.AdapterProfileOptionBinding
import com.app.yajari.utils.setSafeOnClickListener

class ProfileOptionAdapter(private var optionList:MutableList<ProfileOptionData>,private var listener:ProfileListener) : RecyclerView.Adapter<ProfileOptionAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val optionData = optionList[position]
           optionIV.setImageResource(optionData.menuIcon)
           optionNameTV.text=optionData.menuName
            holder.itemView.setSafeOnClickListener {
                listener.onItemOptionClick(position)
            }
        }
    }

    override fun getItemCount() = optionList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterProfileOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterProfileOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface ProfileListener{
        fun onItemOptionClick(position: Int)
    }
}