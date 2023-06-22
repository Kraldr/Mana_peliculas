package com.example.manapeliculas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.SDestacada
import com.example.manapeliculas.viewHolders.SDestacadasViewHolder

class SDestacadasAdapter(val data: List<SDestacada>, val contexto: FragmentActivity): RecyclerView.Adapter<SDestacadasViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SDestacadasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SDestacadasViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SDestacadasViewHolder, position: Int) {
        val item: SDestacada = data[position];
        holder.bin(contexto, item.titulo, item.year, item.src, item.href)
    }

}