package com.example.manapeliculas.viewHolders

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemMovieBinding


class RecomendedViewHolder (view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemMovieBinding.bind(view)

    fun bin (contexto: Context, titulo: String, year: String, src: String, href: String) {
        binding.txtTitle.text = titulo
        binding.txtYear.text = year


        Glide.with(contexto)
            .load(src)
            .apply(
                RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(250, 340)
                .centerCrop())
            .into(binding.imgMovie)

        binding.shimmerLayout.animate()
            .alpha(0f)
            .setDuration(500) // Duración de la animación en milisegundos
            .withEndAction {
                // Después de la animación, ocultar el ShimmerLayout y detener la animación del Shimmer
                binding.shimmerLayout.visibility = View.GONE
                binding.shimmerLayout.stopShimmer()
            }

        binding.btnAnime.setOnClickListener {
            val intent = Intent(contexto, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            contexto.startActivity(intent)


        }
    }
}