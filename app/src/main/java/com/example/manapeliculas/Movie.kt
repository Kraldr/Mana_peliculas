package com.example.manapeliculas

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Outline
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.manapeliculas.adapters.ListEpisodesAdapter
import com.example.manapeliculas.data.MutableX
import com.example.manapeliculas.data.dataMovie
import com.example.manapeliculas.data.servers.servers
import com.example.manapeliculas.databinding.ActivityMovieBinding
import com.google.gson.Gson
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class Movie : AppCompatActivity() {

    private lateinit var binding: ActivityMovieBinding

    private lateinit var animDrawable: AnimationDrawable
    private lateinit var animDrawable2: AnimationDrawable
    private lateinit var animDrawable3: AnimationDrawable
    private lateinit var animDrawable4: AnimationDrawable
    private lateinit var animDrawable5: AnimationDrawable

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val client = OkHttpClient()
    private lateinit var dialog:AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = ObjectAnimator.ofFloat(binding.imageView, "alpha", 0f, 1f)
        fadeIn.duration = 1000

        val scaleUpX = ObjectAnimator.ofFloat(binding.imageView, "scaleX", 0f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(binding.imageView, "scaleY", 0f, 1f)
        scaleUpX.duration = 1000
        scaleUpY.duration = 1000

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleUpX, scaleUpY)
        animatorSet.start()

        animDrawable = binding.rootLayout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()

        animDrawable2 = binding.rootLayoutText1.background as AnimationDrawable
        animDrawable2.setEnterFadeDuration(10)
        animDrawable2.setExitFadeDuration(5000)
        animDrawable2.start()

        animDrawable3 = binding.rootLayoutText2.background as AnimationDrawable
        animDrawable3.setEnterFadeDuration(10)
        animDrawable3.setExitFadeDuration(5000)
        animDrawable3.start()

        animDrawable4 = binding.rootLayoutText3.background as AnimationDrawable
        animDrawable4.setEnterFadeDuration(10)
        animDrawable4.setExitFadeDuration(5000)
        animDrawable4.start()

        animDrawable5 = binding.rootLayoutText4.background as AnimationDrawable
        animDrawable5.setEnterFadeDuration(10)
        animDrawable5.setExitFadeDuration(5000)
        animDrawable5.start()

        val cardView = binding.cardview

        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val radius = 16f
                outline.setRoundRect(0, 0, view.width, view.height + radius.toInt(), radius)
            }
        }
        cardView.outlineProvider = outlineProvider
        cardView.clipToOutline = true
        cardView.elevation = 30f

        val source = intent.getStringExtra("href")

        if (source != null) {
            if (source.contains("movies")) {
                scrapeCuevana2(source)
            }else {
                scrapeCuevanaSeries2 (source)
            }
        }
    }

    private fun scrapeCuevana2 (url: String) {
        val url = "http://10.0.2.2:3000/searchcue/${url}"
        val request = Request.Builder().url(url).get().build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val gson = Gson()
                val data = gson.fromJson(responseData, dataMovie::class.java)

                val refresh = Handler(Looper.getMainLooper())
                refresh.post {
                    binding.txtTitle.text = data.titulo
                    binding.descriptionTextView.text = data.sinopsis
                    binding.txtGen.text = data.tags
                    binding.ratingTextView.text = data.rate

                    binding.rootLayoutText1.isVisible = false
                    binding.rootLayoutText2.isVisible = false
                    binding.rootLayoutText3.isVisible = false
                    binding.rootLayoutText4.isVisible = false
                    binding.rootLayoutText1.isEnabled = false
                    binding.rootLayoutText2.isEnabled = false
                    binding.rootLayoutText3.isEnabled = false
                    binding.rootLayoutText4.isEnabled = false
                    animDrawable.stop()
                    animDrawable2.stop()
                    animDrawable3.stop()
                    animDrawable4.stop()
                    animDrawable5.stop()

                    Glide.with(applicationContext)
                        .load(data.hrefIMG)
                        .into(binding.imgFont)

                    Glide.with(applicationContext)
                        .load(data.hrefIMG)
                        .transform(BlurTransformation(25, 3))
                        .into(binding.imageView)

                    binding.rootLayout.isVisible = false
                    binding.rootLayout.isEnabled = false
                    animDrawable.stop()

                }
            }
        })
    }

    private fun scrapeCuevanaSeries2 (url: String) {

        val parts = url.split("/")
        val lastPart = parts.lastOrNull()

        /*coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val dataMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div.mt-2.row > table > tbody > tr")

            val data = mutableListOf<dataMovie>()

            val episodes = mutableListOf<MutableX>()

            val duracion = dataMovies[3].selectFirst("td:nth-child(2)")?.text().toString()
            val hrefIMG = "https://www.cuevana2espanol.icu" + doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_image__5Tx0e.col > div > img")?.attr("src")
            val originalTitle = dataMovies[1].selectFirst("td:nth-child(2)")?.text().toString()
            val rate = dataMovies[2].selectFirst("td:nth-child(2)")?.text().toString()
            val sinopsis = doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_data__SuMej.pt-3.col > div:nth-child(2)")?.text().toString()
            val tags = dataMovies[6].selectFirst("td:nth-child(2)")?.text().toString()
            val titulo = dataMovies[0].selectFirst("td:nth-child(2)")?.text().toString()
            val year = dataMovies[4].selectFirst("td:nth-child(2)")?.text().toString()

            val dataEpisodes = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(2) > div.row.row-cols-xl-4.row-cols-lg-3.row-cols-2 > div")

            for (dataEpisode in dataEpisodes) {
                episodes.add(
                    MutableX(
                        dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > span")?.text().toString(),
                        dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a > h2")?.text().toString(),
                        "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a")?.attr("href").toString(),
                        "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_poster__AwaLr > a > img")?.attr("src").toString()

                    )
                )
                dataEpisode
            }


            withContext(Dispatchers.Main) {
                binding.txtTitle.text = titulo
                binding.descriptionTextView.text = sinopsis
                binding.txtGen.text = tags
                binding.ratingTextView.text = rate

                binding.rootLayoutText1.isVisible = false
                binding.rootLayoutText2.isVisible = false
                binding.rootLayoutText3.isVisible = false
                binding.rootLayoutText4.isVisible = false
                binding.rootLayoutText1.isEnabled = false
                binding.rootLayoutText2.isEnabled = false
                binding.rootLayoutText3.isEnabled = false
                binding.rootLayoutText4.isEnabled = false
                animDrawable.stop()
                animDrawable2.stop()
                animDrawable3.stop()
                animDrawable4.stop()
                animDrawable5.stop()

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .into(binding.imgFont)

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .transform(BlurTransformation(25, 3))
                    .into(binding.imageView)

                binding.rootLayout.isVisible = false
                binding.rootLayout.isEnabled = false
                animDrawable.stop()

                initRecyclerViewRecent(episodes)
            }

        }*/

        val urls = "http://10.0.2.2:3000/scrapeDataSerie/${lastPart}"


        val request = Request.Builder().url(urls).get().build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val gson = Gson()
                println(responseData)
                val data = gson.fromJson(responseData, dataMovie::class.java)

                val refresh = Handler(Looper.getMainLooper())
                refresh.post {
                    binding.txtTitle.text = data.titulo
                    binding.descriptionTextView.text = data.sinopsis
                    binding.txtGen.text = data.tags
                    binding.ratingTextView.text = data.rate

                    binding.rootLayoutText1.isVisible = false
                    binding.rootLayoutText2.isVisible = false
                    binding.rootLayoutText3.isVisible = false
                    binding.rootLayoutText4.isVisible = false
                    binding.rootLayoutText1.isEnabled = false
                    binding.rootLayoutText2.isEnabled = false
                    binding.rootLayoutText3.isEnabled = false
                    binding.rootLayoutText4.isEnabled = false
                    animDrawable.stop()
                    animDrawable2.stop()
                    animDrawable3.stop()
                    animDrawable4.stop()
                    animDrawable5.stop()

                    Glide.with(applicationContext)
                        .load(data.hrefIMG)
                        .into(binding.imgFont)

                    Glide.with(applicationContext)
                        .load(data.hrefIMG)
                        .transform(BlurTransformation(25, 3))
                        .into(binding.imageView)

                    binding.rootLayout.isVisible = false
                    binding.rootLayout.isEnabled = false
                    animDrawable.stop()

                    //initRecyclerViewRecent(data.mutable)
                    dataEpisodes(data.mutable)
                }
            }
        })
    }

    private fun initRecyclerViewRecent(mutable: List<MutableX>) {
        val refresh = Handler(Looper.getMainLooper())
        refresh.post(kotlinx.coroutines.Runnable {
            binding.genresRecyclerView.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                val adapter = ListEpisodesAdapter(mutable, applicationContext)
                this.adapter = adapter
            }
        })

    }

    private fun dataEpisodes(mutable: List<MutableX>) {

        val adapter = ListEpisodesAdapter(
            mutable,
            applicationContext
        )

        adapter.setOnItemClickListener(object :
            ListEpisodesAdapter.OnItemClickListener {
            override fun onItemClicked(url: String) {
                coroutineScope.launch {
                    val text = withContext(Dispatchers.IO) {
                        try {
                            val parts = url.split('/')
                            val extractedPart = parts.drop(3).joinToString("/")

                            val urls = "http://10.0.2.2:3000/frameFenix/${extractedPart}"


                            val request = Request.Builder().url(urls).get().build()

                            val client = OkHttpClient()
                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    println(e)
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responseData = response.body?.string()
                                    val gson = Gson()
                                    val data = gson.fromJson(responseData, servers::class.java)

                                }
                            })
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }
            }
        })

        binding.genresRecyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            this.adapter = adapter
        }
    }
}