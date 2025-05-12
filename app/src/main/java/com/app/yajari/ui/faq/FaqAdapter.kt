package com.app.yajari.ui.faq
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.FaqResponse
import com.app.yajari.databinding.AdapterFaqBinding
import com.app.yajari.utils.gone
import com.app.yajari.utils.setMarginTop
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible

class FaqAdapter(private var faqList: MutableList<FaqResponse.Data>) : RecyclerView.Adapter<FaqAdapter.MyViewHolder>(){

    override fun getItemCount() = faqList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    class MyViewHolder(val binding: AdapterFaqBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = faqList[position]
        holder.binding.apply {
            if(position!=0)
            {
                faqCL.setMarginTop(0)
            }
            titleTV.text=data.title
            descTV.text=data.description
            faqCL.setSafeOnClickListener {
                data.isExpand = data.isExpand != true
                notifyItemChanged(position)
            }
            if(data.isExpand)
            {
                descTV.visible()
                titleTV.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_arrow_up_circle,0)
            }
            else{
                descTV.gone()
                titleTV.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_arrow_down_circle,0)
            }
        }
    }

}