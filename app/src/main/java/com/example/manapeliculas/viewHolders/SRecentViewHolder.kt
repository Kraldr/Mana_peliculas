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

        binding.textViewServer.text = titulo

        Glide.with(applicationContext)
            .load(hrefIMG)
            .into(binding.imageViewServer)

        binding.btnServer.setOnClickListener {
            val intent = Intent(applicationContext, Movie::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("href", href)
            applicationContext.startActivity(intent)
        }


    }
}