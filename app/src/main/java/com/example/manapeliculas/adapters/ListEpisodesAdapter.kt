package com.example.manapeliculas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.detodito.ViewHolders.EpisodeViewHolder
import com.example.manapeliculas.R
import com.example.manapeliculas.data.MutableX

class ListEpisodesAdapter(
    private val data: List<MutableX>,
    private val applicationContext: Context
) : RecyclerView.Adapter<EpisodeViewHolder> () {

    interface OnItemClickListener {
        fun onItemClicked(url: String, infoItemsObject: EpisodeViewHolder.SerieData)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.custom_list_episode, parent, false)
        return EpisodeViewHolder(view, listener, applicationContext)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val item: MutableX = data[position];
        holder.bin(item.name,item.url, item.urlIMG, item.episode, applicationContext)
    }

}