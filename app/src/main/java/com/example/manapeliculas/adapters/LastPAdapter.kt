package com.example.manapeliculas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.viewHolders.LastPViewHolder

class LastPAdapter(val data: List<LastP>, val contexto: FragmentActivity): RecyclerView.Adapter<LastPViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastPViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LastPViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: LastPViewHolder, position: Int) {
        val item: LastP = data[position];
        holder.bin(contexto, item.titulo, item.year, item.src, item.href)
    }

}