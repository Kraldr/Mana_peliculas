package com.example.manapeliculas.viewHolders

import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemCarouselBinding
import com.bumptech.glide.request.target.Target;


class CarouselViewHolder (view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemCarouselBinding.bind(view)

    fun bin (contexto: FragmentActivity, href: String, src: String, titulo: String) {
        binding.txtTitleCarousel.text = titulo

        Glide.with(contexto)
            .load(src)
            .into(binding.imgCarousel);

        binding.txtTitleCarousel.setOnClickListener {
            val intent = Intent(contexto, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            contexto.startActivity(intent)
        }
    }
}