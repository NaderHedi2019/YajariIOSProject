package com.app.yajari.ui.our_partner
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.PartnerResponse
import com.app.yajari.databinding.AdapterPartnerBinding
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setMarginTop

class OurPartnerAdapter(private var partnerList: MutableList<PartnerResponse.Data>) : RecyclerView.Adapter<OurPartnerAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val partnerData=partnerList[position]
            if (position == 0) {
                partnerCL.setMarginTop(30)
            }
            partnerIV.loadImage(partnerData.image, R.drawable.ic_placeholder_yajari)
            partnerNameTV.text=partnerData.title
        }
    }

    override fun getItemCount() = partnerList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterPartnerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterPartnerBinding) :
        RecyclerView.ViewHolder(binding.root)

}