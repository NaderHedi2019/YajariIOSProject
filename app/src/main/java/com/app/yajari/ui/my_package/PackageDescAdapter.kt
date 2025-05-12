package com.app.yajari.ui.my_package

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.databinding.AdapterSubDescBinding
import com.app.yajari.utils.setSafeOnClickListener

class PackageDescAdapter(
    private var descList: MutableList<String>,
    private var packageDescListener: PackageDescListener,
    private var adapterPosition: Int
) : RecyclerView.Adapter<PackageDescAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val descData = descList[position]
            descTV.text = descData
            holder.itemView.setSafeOnClickListener {
                packageDescListener.onItemDescClick(adapterPosition)
            }
        }
    }

    override fun getItemCount() = descList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterSubDescBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterSubDescBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface PackageDescListener{
        fun onItemDescClick(adapterPosition: Int)
    }
}