package com.example.manapeliculas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.LastS
import com.example.manapeliculas.viewHolders.LastSViewHolder

class LastSAdapter(val data: List<LastS>, val contexto: FragmentActivity): RecyclerView.Adapter<LastSViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastSViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LastSViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: LastSViewHolder, position: Int) {
        val item: LastS = data[position];
        holder.bin(contexto, item.titulo, item.year, item.src, item.href)
    }

}