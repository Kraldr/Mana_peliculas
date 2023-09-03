package com.example.manapeliculas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.viewHolders.RecomendedViewHolder

class RecomendedAdapter(val data: List<PDestacada>, val contexto: Context): RecyclerView.Adapter<RecomendedViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomendedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecomendedViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecomendedViewHolder, position: Int) {
        val item: PDestacada = data[position];
        holder.bin(contexto, item.titulo, item.year, item.src, item.href)
    }

}