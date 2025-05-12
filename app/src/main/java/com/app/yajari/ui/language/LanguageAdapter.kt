package com.app.yajari.ui.language
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.LanguageData
import com.app.yajari.databinding.AdapterLanguageBinding
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible

class LanguageAdapter(private var listener:LanguageListener,private var languageList:MutableList<LanguageData>) : RecyclerView.Adapter<LanguageAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val languageData = languageList[position]
            if(languageData.isSelected)
            {
                selectedIV.visible()
                languageCV.strokeWidth=1
            }
            else{
                selectedIV.gone()
                languageCV.strokeWidth=0
            }
            flagIV.setImageResource(languageData.flag)
            languageNameTV.text=languageData.language
            holder.itemView.setSafeOnClickListener {
                languageList.map { it.isSelected=false }
                languageData.isSelected=!languageData.isSelected
                notifyDataSetChanged()
                listener.onItemLanguageClick(languageData)
            }
        }
    }

    override fun getItemCount() = languageList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface LanguageListener{
        fun onItemLanguageClick(data: LanguageData)
    }
}