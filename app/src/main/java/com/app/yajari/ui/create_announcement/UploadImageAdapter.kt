package com.app.yajari.ui.create_announcement

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.MediaModel
import com.app.yajari.databinding.AdapterPhotoBinding
import com.app.yajari.utils.loadImage
import com.app.yajari.utils.setSafeOnClickListener

class UploadImageAdapter(private var imageList:MutableList<MediaModel>,private var listener:ImageUploadListener) : RecyclerView.Adapter<UploadImageAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val imageData=imageList[position]
            if(imageData.type=="add")
            {
                photoIV.loadImage(imageData.media!!.absolutePath, R.drawable.ic_yajari_logo)
            }
            else{
                photoIV.loadImage(imageData.imageUrl, R.drawable.ic_yajari_logo)
            }

            closeIV.setSafeOnClickListener {
                listener.onMediaRemoveClick(imageData, position)
            }
        }
    }

    override fun getItemCount() = imageList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    interface ImageUploadListener{
        fun onMediaRemoveClick(data: MediaModel,position: Int)

    }
}
