package com.example.manapeliculas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.viewHolders.ProfileListViewHolder

class ProfileListAdapter(val data: MutableList<LastP>, val contexto: Context): RecyclerView.Adapter<ProfileListViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProfileListViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProfileListViewHolder, position: Int) {
        val item: LastP? = data?.get(position) ?: null;
        if (item != null) {
            holder.bin(contexto, item.titulo, item.year, item.src, item.href)
        }
    }

}