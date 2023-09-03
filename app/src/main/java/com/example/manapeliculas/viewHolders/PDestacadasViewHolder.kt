package com.example.manapeliculas.viewHolders

import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemMovieBinding


class PDestacadasViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemMovieBinding.bind(view)

    fun bin(context: FragmentActivity, titulo: String, year: String, src: String, href: String) {
        binding.txtTitle.text = titulo
        binding.txtYear.text = year

        if (src.isNotEmpty()) {
            Glide.with(context)
                .load(src)
                .centerCrop()
                .into(binding.imgMovie)

            // Agregar una animación de desvanecimiento gradual al ShimmerLayout
            binding.shimmerLayout.animate()
                .alpha(0f)
                .setDuration(500) // Duración de la animación en milisegundos
                .withEndAction {
                    // Después de la animación, ocultar el ShimmerLayout y detener la animación del Shimmer
                    binding.shimmerLayout.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                }
        }

        binding.btnAnime.setOnClickListener {
            val intent = Intent(context, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            context.startActivity(intent)
        }
    }
}