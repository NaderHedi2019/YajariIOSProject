package com.app.yajari.ui.create_announcement

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.AskData
import com.app.yajari.databinding.AdapterAskPublishBinding
import com.app.yajari.utils.setSafeOnClickListener

class AskSliderAdapter(var askList:MutableList<AskData>,private var listener:AskListener) : RecyclerView.Adapter<AskSliderAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val askData=askList[position]
            option1IV.setImageResource(askData.icon1)
            option2IV.setImageResource(askData.icon2)
            optionName1TV.text=askData.optionName1
            optionName2TV.text=askData.optionName2
            askTitleTV.text=askData.title
            if(askData.selected1)
            {
                option1IV.isSelected=true
                optionCL1.isSelected=true
               arrow1IV.isSelected=true
            }
            else{
                option1IV.isSelected=false
                optionCL1.isSelected=false
                arrow1IV.isSelected=false
            }
            if(askData.selected2)
            {
                option2IV.isSelected=true
                optionCL2.isSelected=true
                arrow2IV.isSelected=true
            }
            else{
                option2IV.isSelected=false
                optionCL2.isSelected=false
                arrow2IV.isSelected=false
            }
            optionCL1.setSafeOnClickListener {
                askData.selected2=false
                askData.selected1=true
                notifyDataSetChanged()
                listener.onItemOptionClick(askData,askList)
            }
            optionCL2.setSafeOnClickListener {
                askData.selected1=false
                askData.selected2=true
                notifyDataSetChanged()
                listener.onItemOptionClick(askData,askList)
            }
        }
    }

    override fun getItemCount() = askList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterAskPublishBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterAskPublishBinding) : RecyclerView.ViewHolder(binding.root)

    interface AskListener{
        fun onItemOptionClick(askData: AskData,askList:MutableList<AskData>)
    }

}
