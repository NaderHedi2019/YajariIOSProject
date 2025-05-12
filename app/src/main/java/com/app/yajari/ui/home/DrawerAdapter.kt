package com.app.yajari.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.yajari.data.DrawerModel
import com.app.yajari.databinding.AdapterSideMenuBinding
import com.app.yajari.utils.setSafeOnClickListener

class DrawerAdapter (private val context : Context, private var models : List<DrawerModel>) : RecyclerView.Adapter<DrawerAdapter.DrawerHolder>() {

    inner class DrawerHolder(val binding : AdapterSideMenuBinding) : RecyclerView.ViewHolder(binding.root)

    lateinit var menuClick : SetOnMenuClick

    fun onMenuClick(click : SetOnMenuClick){
        menuClick = click
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerHolder {
        return DrawerHolder(AdapterSideMenuBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: DrawerHolder, position: Int) {
        holder.binding.apply {
            models[position].let { data->
                menuIV.setImageResource(data.menuIcon)
                menuTV.text = data.menuName
            }
        }
        holder.itemView.setSafeOnClickListener {
            menuClick.onMenuClick(position)
        }
    }

    override fun getItemCount(): Int = models.size

    interface SetOnMenuClick{
        fun onMenuClick(position : Int)
    }
}