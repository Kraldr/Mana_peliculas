package com.example.detodito.ViewHolders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manapeliculas.adapters.ListEpisodesAdapter
import com.example.manapeliculas.databinding.CustomListEpisodeBinding


class EpisodeViewHolder(view: View, private val listener: ListEpisodesAdapter.OnItemClickListener?): RecyclerView.ViewHolder(view) {

    private val binding = CustomListEpisodeBinding.bind(view)

    fun bin(
        name: String,
        url: String,
        urlIMG: String,
        episode: String,
        applicationContext: Context
    ) {

        binding.txtName.text = name
        binding.txtEp.text = episode
        Glide.with(applicationContext)
            .load(urlIMG)
            .into(binding.imageEpisode)

        binding.btnEpisode.setOnClickListener{
            listener?.onItemClicked(url)
        }
    }
}