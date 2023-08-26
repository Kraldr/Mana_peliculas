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


class PDestacadasViewHolder (view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemMovieBinding.bind(view)

    fun bin (contexto: FragmentActivity, titulo: String, year: String, src: String, href: String) {
        binding.txtTitle.text = titulo
        binding.txtYear.text = year


        Glide.with(contexto)
            .load(src)
            .apply(
                RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Guardar en caché tanto la imagen original como la redimensionada
                .override(250, 340) // Establecer dimensiones específicas para la imagen
                .centerCrop()) // Recortar la imagen para que se ajuste a las dimensiones especificadas
            .into(binding.imgMovie)

        binding.btnAnime.setOnClickListener {
            val intent = Intent(contexto, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            contexto.startActivity(intent)
        }
    }
}