package com.example.manapeliculas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.detodito.ViewHolders.SRecentViewHolder
import com.example.manapeliculas.R
import com.example.manapeliculas.data.searchDataItem

class ListRecentAdapter(
    private val data: MutableList<searchDataItem>,
    private val applicationContext: Context
) : RecyclerView.Adapter<SRecentViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SRecentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_search, parent, false)
        return SRecentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SRecentViewHolder, position: Int) {
        val item: searchDataItem = data[position];
        holder.bin(item.titulo,item.href, item.hrefIMG, item.year, applicationContext)
    }

}