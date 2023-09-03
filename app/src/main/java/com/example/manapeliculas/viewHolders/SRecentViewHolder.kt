package com.example.detodito.ViewHolders

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemSearchBinding


class SRecentViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemSearchBinding.bind(view)

    fun bin(
        titulo: String,
        href: String,
        hrefIMG: String,
        year: String,
        applicationContext: Context
    ) {

        if (hrefIMG.isNotEmpty()) {
            Glide.with(applicationContext)
                .load(hrefIMG)
                .into(binding.imageViewServer)

            // Agregar una animación de desvanecimiento gradual al ShimmerLayout
            binding.shimmerLayout.animate()
                .alpha(0f)
                .setDuration(500) // Duración de la animación en milisegundos
                .withEndAction {
                    // Después de la animación, ocultar el ShimmerLayout y detener la animación del Shimmer
                    binding.shimmerLayout.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                }

            binding.shimmerTitle.animate()
                .alpha(0f)
                .setDuration(500) // Duración de la animación en milisegundos
                .withEndAction {
                    // Después de la animación, ocultar el ShimmerLayout y detener la animación del Shimmer
                    binding.shimmerTitle.visibility = View.GONE
                    binding.shimmerTitle.stopShimmer()
                }

            binding.shimmerSubtitle.animate()
                .alpha(0f)
                .setDuration(500) // Duración de la animación en milisegundos
                .withEndAction {
                    // Después de la animación, ocultar el ShimmerLayout y detener la animación del Shimmer
                    binding.shimmerSubtitle.visibility = View.GONE
                    binding.shimmerSubtitle.stopShimmer()
                }
        }

        binding.textViewServer.text = titulo
        binding.textViewType.text = year


        binding.btnServer.setOnClickListener {
            val intent = Intent(applicationContext, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            applicationContext.startActivity(intent)
        }

    }
}