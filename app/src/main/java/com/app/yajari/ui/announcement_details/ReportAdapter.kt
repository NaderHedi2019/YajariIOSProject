package com.app.yajari.ui.announcement_details
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.R
import com.app.yajari.data.ReportData
import com.app.yajari.databinding.AdapterReportBinding
import com.app.yajari.utils.setSafeOnClickListener

class ReportAdapter (private var listener: ReportListener, private var reportList: MutableList<ReportData>) : RecyclerView.Adapter<ReportAdapter.MyViewHolder>(){
    override fun getItemCount() = reportList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        AdapterReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    class MyViewHolder(val binding: AdapterReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data =reportList[position]
        holder.binding.apply {
            reportNameTV.text=data.name
            holder.itemView.setSafeOnClickListener {
                reportList.map { it.selected=false }
                data.selected=!data.selected
                notifyDataSetChanged()
                listener.onItemReportClick(data)
            }

            if(data.selected)
            {
                reportNameTV.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_report_selected,0)
            }
            else{
                reportNameTV.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
            }
        }
    }
    interface ReportListener
    {
        fun onItemReportClick(data: ReportData)
    }
}