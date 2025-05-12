package com.app.yajari.ui.my_package
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.PlanData
import com.app.yajari.databinding.AdapterSubPackageBinding
import com.app.yajari.utils.setSafeOnClickListener

class PackageAdapter (private var mContext:Context,private var packageList:MutableList<PlanData>, private var packageListener:PackageListener,private var packageDescListener: PackageDescAdapter.PackageDescListener) : RecyclerView.Adapter<PackageAdapter.MyViewHolder>() {
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {
            val packageData = packageList[position]
            priceTV.text = packageData.price
            titleTV.text = packageData.title
            planTypeTV.text=packageData.planDuration
            if(packageData.selected)
            {
                packageCV.strokeWidth=2
                packageCV.strokeColor= ContextCompat.getColor(mContext,R.color.colorPrimary)
            }
            else{
                packageCV.strokeWidth=0
            }
            packageDescRV.apply {
                adapter=PackageDescAdapter(packageData.planDesc,packageDescListener,holder.adapterPosition)
            }
            packageCV.setSafeOnClickListener {
                packageList.map { it.selected = false }
                packageData.selected = !packageData.selected
                notifyDataSetChanged()
                packageListener.onItemPackageClick()
            }
        }
    }
     @SuppressLint("NotifyDataSetChanged")
     fun updateData(position: Int)
    {
        packageList.map { it.selected = false }
        packageList[position].selected = !packageList[position].selected
        notifyDataSetChanged()
    }

    override fun getItemCount() = packageList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterSubPackageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class MyViewHolder(val binding: AdapterSubPackageBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface PackageListener {
        fun onItemPackageClick()
    }
}

