package com.app.yajari.ui.alert_settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.CategoryResponse
import com.app.yajari.databinding.AdapterAlertSelectedCategoryBinding

class AlertSelectedCategory(
    private val listener:OnItemCategoryListener
) : RecyclerView.Adapter<AlertSelectedCategory.ViewHolder>() {

    var selectedCategoryList = ArrayList<CategoryResponse.Category>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterAlertSelectedCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = selectedCategoryList[position]
        holder.bind(category)

    }

    override fun getItemCount(): Int {
        return selectedCategoryList.size
    }

    inner class ViewHolder(val binding: AdapterAlertSelectedCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryResponse.Category) {
            binding.itemName.text = category.title

            binding.removeItemIV.setOnClickListener {
                listener.onItemRemoveCategory(position = adapterPosition,id=category.uniqueId!!)
            }
        }
    }

    interface OnItemCategoryListener{
        fun onItemRemoveCategory(position: Int,id:String)
    }
}
