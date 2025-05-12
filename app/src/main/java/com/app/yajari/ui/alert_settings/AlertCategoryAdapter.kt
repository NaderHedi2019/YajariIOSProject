package com.app.yajari.ui.alert_settings

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.CategoryResponse
import com.app.yajari.databinding.AdapterCategoryBinding
import com.app.yajari.utils.setSafeOnClickListener
import java.util.Locale

class AlertCategoryAdapter(
    var listener: CategoryListener,
    var categoryList: MutableList<CategoryResponse.Category>,
    val context: Context,
    private val onItemSelectChangeListener: (selectedCategories: List<CategoryResponse.Category>) -> Unit
) : RecyclerView.Adapter<AlertCategoryAdapter.MyViewHolder>(), Filterable {

    var categoryFilterList = ArrayList<CategoryResponse.Category>()

    init {
        categoryFilterList = categoryList as ArrayList<CategoryResponse.Category>
    }

    inner class MyViewHolder(val binding: AdapterCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(AdapterCategoryBinding.inflate(LayoutInflater.from(context), parent, false))

    }

    override fun getItemCount(): Int {
        return categoryFilterList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = categoryFilterList[position]
        holder.binding.apply {
            nameTV.text = data.title
            selectionIV.isSelected = data.isSelected
        }
        holder.itemView.setSafeOnClickListener {
            data.isSelected = !data.isSelected
            notifyDataSetChanged()
            listener.onItemClickCategory(data)
            val selectedItems = categoryFilterList.filter { it.isSelected }
            onItemSelectChangeListener.invoke(selectedItems)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch: String = constraint.toString()
                categoryFilterList = if (charSearch.isEmpty()) {
                    categoryList as ArrayList<CategoryResponse.Category>
                } else {
                    val resultList = ArrayList<CategoryResponse.Category>()
                    for (row in categoryList) {
                        if (row.title?.lowercase(Locale.getDefault())!!.contains(
                                constraint.toString()
                                    .lowercase(Locale.getDefault())
                            )
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = categoryFilterList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                categoryFilterList = results?.values as ArrayList<CategoryResponse.Category>
                notifyDataSetChanged()
            }
        }
    }



    interface CategoryListener{
        fun onItemClickCategory(categoryData:CategoryResponse.Category)
    }
}