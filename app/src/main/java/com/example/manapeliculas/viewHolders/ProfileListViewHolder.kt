package com.example.manapeliculas.viewHolders

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemMovieBinding


class ProfileListViewHolder (view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemMovieBinding.bind(view)

    fun bin (contexto: Context, titulo: String, year: String, src: String, href: String) {
        binding.txtTitle.text = titulo
        binding.txtYear.text = year

        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.54"

        val glideUrl = GlideUrl(
            src,
            LazyHeaders.Builder()
                .addHeader("User-Agent", userAgent)
                .build()
        )


        Glide.with(contexto)
            .load(glideUrl)
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