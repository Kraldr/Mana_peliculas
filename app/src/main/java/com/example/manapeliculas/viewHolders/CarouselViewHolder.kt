package com.example.manapeliculas.viewHolders

import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manapeliculas.Movie
import com.example.manapeliculas.databinding.ItemCarouselBinding
import com.bumptech.glide.request.target.Target;
import com.example.manapeliculas.data.servers.servers


class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCarouselBinding.bind(view)

    fun bin(context: FragmentActivity, href: String, src: String, titulo: String) {
        binding.txtTitleCarousel.text = titulo

        if (src.isNotEmpty()) {
            // Cargar la imagen con Glide
            Glide.with(context)
                .load(src)
                .into(binding.imgCarousel)

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

        binding.txtTitleCarousel.setOnClickListener {
            val intent = Intent(context, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            context.startActivity(intent)
        }
    }
}