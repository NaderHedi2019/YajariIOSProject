package com.app.yajari.ui.announcement_details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.AnnouncementDetailsResponse
import com.app.yajari.databinding.AdapterImgSliderBinding
import com.app.yajari.utils.loadImage

class ImageSliderAdapter(private var gallery: MutableList<AnnouncementDetailsResponse.Gallery>?) : RecyclerView.Adapter<ImageSliderAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            if(gallery!![position].file.toString().isNotEmpty()) {
                objectIV.loadImage(
                    gallery!![position].file.toString(),
                    R.drawable.ic_placeholder_yajari
                )
            }
            else
            {
                objectIV.setImageResource(R.drawable.ic_placeholder_yajari)
            }
        }
    }

    override fun getItemCount() = gallery!!.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterImgSliderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterImgSliderBinding) : RecyclerView.ViewHolder(binding.root)

}
