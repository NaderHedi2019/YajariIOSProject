package com.app.yajari.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.DispenseData
import com.app.yajari.databinding.AdapterDispenseBinding
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener

class DispenseAdapter(private var dispenseList:MutableList<DispenseData>,private var dispenseListener:DispenseListener) : RecyclerView.Adapter<DispenseAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val dispenseData=dispenseList[position]
            radioIV.isSelected=dispenseData.selected
            nameTV.isSelected=dispenseData.selected
            nameTV.text=dispenseData.name
            if(dispenseList.size-1==position)
            {
                view.gone()
            }
            holder.itemView.setSafeOnClickListener {
                dispenseList.map { it.selected=false }
                dispenseData.selected=!dispenseData.selected
                notifyDataSetChanged()
                dispenseListener.onItemDispenseClick(dispenseData)
            }
        }
    }

    override fun getItemCount() = dispenseList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterDispenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterDispenseBinding) : RecyclerView.ViewHolder(binding.root)

    interface DispenseListener{
        fun onItemDispenseClick(dispenseData: DispenseData)
    }

}