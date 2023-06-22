package com.example.manapeliculas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.R
import com.example.manapeliculas.data.cuevana2.Carousel
import com.example.manapeliculas.viewHolders.CarouselViewHolder

class CarouselAdapter(val data: List<Carousel>, val contexto: FragmentActivity): RecyclerView.Adapter<CarouselViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CarouselViewHolder(layoutInflater.inflate(R.layout.item_carousel, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item: Carousel = data[position];
        holder.bin(contexto, item.href, item.src, item.titulo)
    }

}