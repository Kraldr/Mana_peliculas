package com.example.manapeliculas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.viewHolders.PDestacadasViewHolder
import org.jsoup.nodes.Element

class PDestacadasAdapter(val data: MutableList<PDestacada>, val contexto: FragmentActivity): RecyclerView.Adapter<PDestacadasViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDestacadasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PDestacadasViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PDestacadasViewHolder, position: Int) {
        val item: PDestacada = data[position];
        holder.bin(contexto, item.titulo, item.year, item.src, item.href)
    }

}