package com.example.detodito.ViewHolders

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manapeliculas.adapters.ListEpisodesAdapter
import com.example.manapeliculas.databinding.CustomListEpisodeBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import kotlin.math.log


class EpisodeViewHolder(
    view: View,
    private val listener: ListEpisodesAdapter.OnItemClickListener?,
    applicationContext: Context
): RecyclerView.ViewHolder(view) {

    private val binding = CustomListEpisodeBinding.bind(view)
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val client = OkHttpClient()

    data class SerieData(
        val latino: List<CyberLockerData>,
        val spanish: List<CyberLockerData>,
        val english: List<CyberLockerData>,
        val downloads: List<DownloadData>
    )

    data class CyberLockerData(
        val cyberlocker: String,
        val result: String,
        val quality: String
    )

    data class DownloadData(
        val cyberlocker: String,
        val result: String,
        val quality: String,
        val language: String
    )

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
            println("URL: $url")
            coroutineScope.launch(Dispatchers.IO) {
                val response = getHttpResponse(url)
                val jsonObject = Gson().fromJson(response, JsonObject::class.java)
                val itemsObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("episode").getAsJsonObject("players")
                val infoItemsObject = Gson().fromJson(itemsObject, SerieData::class.java)

                withContext(Dispatchers.Main) {
                    listener?.onItemClicked(url, infoItemsObject)
                }
            }
        }
    }

    private fun getHttpResponse(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        return response.body?.string() ?: ""
    }
}