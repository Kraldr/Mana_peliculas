package com.example.manapeliculas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.manapeliculas.adapters.ListEpisodesAdapter
import com.example.manapeliculas.adapters.RecomendedAdapter
import com.example.manapeliculas.data.MutableX
import com.example.manapeliculas.data.User
import com.example.manapeliculas.data.UserData
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.data.dataMovie
import com.example.manapeliculas.databinding.ActivityMovieBinding
import com.example.manapeliculas.ui.gallery.GalleryFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.random.Random

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
    private lateinit var dialog: AlertDialog
    private var numPage = ""
    private var like = false
    private var boockmark = false
    private var mDatabase: DatabaseReference? = null
    private var objectsArray = mutableListOf<LastP>()
    private var myListArray = mutableListOf<LastP>()
    private var objetoLastP: LastP? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDatabase = FirebaseDatabase.getInstance().reference;

        val userData = getUserDataFromSharedPreferences()
        val source = intent.getStringExtra("href")

        val fadeIn = ObjectAnimator.ofFloat(binding.imageView, "alpha", 0f, 1f)
        fadeIn.duration = 1000

        val scaleUpX = ObjectAnimator.ofFloat(binding.imageView, "scaleX", 0f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(binding.imageView, "scaleY", 0f, 1f)
        scaleUpX.duration = 1000
        scaleUpY.duration = 1000

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleUpX, scaleUpY)
        animatorSet.start()

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

        val decoration = GalleryFragment.SpacesItemDecoration(16)
        if (!isDecorationAlreadyApplied(binding.reomendedRecycler, decoration)) {
            binding.reomendedRecycler.addItemDecoration(decoration)
        }

        loadData(userData.userId, source)

        if (source != null) {
            if (source.contains("movies")) {
                scrapeCuevana2(source)
                screapePage()
            } else {
                scrapeCuevanaSeries2(source)
            }
        }

        binding.lottieAnimationAdd.setOnClickListener {
            if (!boockmark) {
                objetoLastP?.let { myListArray.add(it) }
                boockmark = likeAnimation(
                    binding.lottieAnimationAdd,
                    R.raw.animation_lm2nk2kg,
                    boockmark,
                    R.drawable.baseline_bookmark_border_24
                )
                saveData(userData.userId, "myList", myListArray)
            } else {
                boockmark = likeAnimation(
                    binding.lottieAnimationAdd,
                    R.raw.animation_lm2nk2kg,
                    boockmark,
                    R.drawable.baseline_bookmark_border_24
                )
                val iterator = myListArray.iterator()
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    if (data.href == objetoLastP?.href ?: "") {
                        iterator.remove()
                    }
                }

                deleteData(userData.userId, "myList", myListArray)
            }
        }

        binding.lottieAnimationView.setOnClickListener {
            if (!like) {
                objetoLastP?.let { objectsArray.add(it) }
                like = likeAnimation(binding.lottieAnimationView, R.raw.animation_lm2o3qv0, like, R.drawable.twitter_like)
                saveData(userData.userId, "like", objectsArray)
            } else {
                val iterator = objectsArray.iterator()
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    if (data.href == objetoLastP?.href ?: "") {
                        iterator.remove()
                    }
                }
                like = likeAnimation(binding.lottieAnimationView, R.raw.animation_lm2o3qv0, like, R.drawable.twitter_like)
                deleteData(userData.userId, "like", objectsArray)
            }
        }
    }

    private fun loadData(userId: String, source: String?) {
        mDatabase?.child("users")?.child(userId)?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val likeList = user.like
                    myListArray = user.myList
                    objectsArray = likeList
                    val contieneHrefBuscado = likeList.any { obj ->
                        obj.href == source
                    }

                    val contieneHrefBuscadomylist = myListArray.any { obj ->
                        obj.href == source
                    }

                    if (contieneHrefBuscadomylist) {
                        boockmark = likeAnimation(
                            binding.lottieAnimationAdd,
                            R.raw.animation_lm2nk2kg,
                            boockmark,
                            R.drawable.baseline_bookmark_border_24
                        )
                    }

                    if (contieneHrefBuscado) {
                        like = likeAnimation(
                            binding.lottieAnimationView,
                            R.raw.animation_lm2o3qv0,
                            like,
                            R.drawable.twitter_like
                        )
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
            }
        })
    }

    private fun saveData(userId: String, path: String, objectsArray: MutableList<LastP>) {
        mDatabase?.child("users")?.child(userId)?.child(path)?.setValue(objectsArray)
    }

    private fun deleteData(userId: String, path: String, objectsArray: MutableList<LastP>) {
        mDatabase?.child("users")?.child(userId)?.child(path)?.setValue(objectsArray)
    }

    private fun likeAnimation(
        imageView: LottieAnimationView,
        animation: Int,
        like: Boolean,
        image: Int
    ): Boolean {

        if (!like) {
            imageView.setAnimation(animation)
            imageView.playAnimation()

        } else {
            imageView.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animator: Animator) {
                        imageView.setImageResource(image)
                        imageView.alpha = 1f
                    }

                })

        }

        return !like
    }

    private fun screapePage() {
        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/archives/movies/top/week/page/100")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val numPageTemp =
                doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.pt-3.container > div > ul > li:nth-child(7) > a")
                    ?.text()
                    ?.toInt()

            withContext(Dispatchers.Main) {
                if (numPageTemp != null) {
                    scrapeRecomed(numPageTemp, "Películas recomendadas")
                }
            }
        }
    }

    private fun scrapeRecomed(page: Int, type: String) {
        val randomNumber = Random.nextInt(page) + 1
        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/archives/movies/top/week/page/$randomNumber")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val pDestacadas =
                doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(1) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

            val dataEpisode = mutableListOf<PDestacada>()
            for (pDestacada in pDestacadas) {
                dataEpisode.add(
                    PDestacada(
                        "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a")
                            ?.attr("href").toString(),
                        "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a > img")
                            ?.attr("src").toString(),
                        pDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                        pDestacada.selectFirst("article > div > span")?.text().toString()
                    )
                )
            }

            val dataEpisodeRandom = mutableListOf<PDestacada>()
            val randomNumbersSet = mutableSetOf<Int>()

            while (randomNumbersSet.size < 12) {
                val randomNumber = Random.nextInt(dataEpisode.size)
                randomNumbersSet.add(randomNumber)
            }

            for (index in randomNumbersSet) {
                dataEpisodeRandom.add(dataEpisode[index])
            }


            withContext(Dispatchers.Main) {

                binding.txtTyperecomeded.text = type

                binding.reomendedRecycler.apply {
                    layoutManager = GridLayoutManager(applicationContext, 3)
                    adapter = RecomendedAdapter(dataEpisodeRandom, applicationContext)
                }
            }
        }
    }

    private fun scrapeCuevana2(url: String) {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val dataMovies =
                doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div.mt-2.row > table > tbody > tr")

            val data = mutableListOf<dataMovie>()

            val episodes = mutableListOf<MutableX>()

            val duracion = dataMovies[3].selectFirst("td:nth-child(2)")?.text().toString()
            val hrefIMG =
                "https://www.cuevana2espanol.icu" + doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_image__LJrqk.col > div > img")
                    ?.attr("src")
            val originalTitle = dataMovies[1].selectFirst("td:nth-child(2)")?.text().toString()
            val rate = dataMovies[2].selectFirst("td:nth-child(2)")?.text().toString()
            val sinopsis =
                doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_data__HL5zl.pt-3.col > div:nth-child(2)")
                    ?.text().toString()
            val tags = dataMovies[6].selectFirst("td:nth-child(2)")?.text().toString()
            val titulo = dataMovies[0].selectFirst("td:nth-child(2)")?.text().toString()
            val year = dataMovies[4].selectFirst("td:nth-child(2)")?.text().toString()


            withContext(Dispatchers.Main) {
                binding.txtTitle.text = titulo
                binding.descriptionTextView.text = sinopsis
                binding.txtGen.text = tags
                binding.ratingTextView.text = rate

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .into(binding.imgFont)

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .transform(BlurTransformation(25, 3))
                    .into(binding.imageView)

                stopAnimation()
                objetoLastP = LastP(url, hrefIMG, titulo, year)
            }

        }
    }

    private fun scrapeCuevanaSeries2(url: String) {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val dataMovies =
                doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div.mt-2.row > table > tbody > tr")

            val data = mutableListOf<dataMovie>()

            val episodes = mutableListOf<MutableX>()

            val duracion = dataMovies[3].selectFirst("td:nth-child(2)")?.text().toString()
            val hrefIMG =
                "https://www.cuevana2espanol.icu" + doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_image__5Tx0e.col > div > img")
                    ?.attr("src")
            val originalTitle = dataMovies[1].selectFirst("td:nth-child(2)")?.text().toString()
            val rate = dataMovies[2].selectFirst("td:nth-child(2)")?.text().toString()
            val sinopsis =
                doc.selectFirst("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_data__SuMej.pt-3.col > div:nth-child(2)")
                    ?.text().toString()
            val tags = dataMovies[6].selectFirst("td:nth-child(2)")?.text().toString()
            val titulo = dataMovies[0].selectFirst("td:nth-child(2)")?.text().toString()
            val year = dataMovies[4].selectFirst("td:nth-child(2)")?.text().toString()

            val dataEpisodes =
                doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(2) > div.row.row-cols-xl-4.row-cols-lg-3.row-cols-2 > div")

            for (dataEpisode in dataEpisodes) {
                episodes.add(
                    MutableX(
                        dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > span")
                            ?.text().toString(),
                        dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a > h3")
                            ?.text().toString(),
                        "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a")
                            ?.attr("href").toString(),
                        "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_poster__AwaLr > a > img")
                            ?.attr("src").toString()

                    )
                )
                dataEpisode
            }


            withContext(Dispatchers.Main) {
                binding.txtTitle.text = titulo
                binding.descriptionTextView.text = sinopsis
                binding.txtGen.text = tags
                binding.ratingTextView.text = rate

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .into(binding.imgFont)

                Glide.with(applicationContext)
                    .load(hrefIMG)
                    .transform(BlurTransformation(25, 3))
                    .into(binding.imageView)


                initRecyclerViewRecent(episodes)
                stopAnimation()

                objetoLastP = LastP(url, hrefIMG, titulo, year)
            }

        }
    }

    private fun stopAnimation() {
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
        binding.shimmerTitle.visibility = View.GONE
        binding.shimmerTitle.stopShimmer()
        binding.shimmerTags.visibility = View.GONE
        binding.shimmerTags.stopShimmer()
        binding.shimmerRate.visibility = View.GONE
        binding.shimmerRate.stopShimmer()
        binding.shimmerSinop.visibility = View.GONE
        binding.shimmerSinop.stopShimmer()
    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.apply {
                left = space
                right = space
                bottom = space
                if (parent.getChildLayoutPosition(view) < 3) top = space
            }
        }
    }

    private fun isDecorationAlreadyApplied(
        recyclerView: RecyclerView,
        decoration: RecyclerView.ItemDecoration
    ): Boolean {
        return (0 until recyclerView.itemDecorationCount)
            .map { recyclerView.getItemDecorationAt(it) }
            .any { it === decoration }
    }

    private fun initRecyclerViewRecent(mutable: List<MutableX>) {
        val refresh = Handler(Looper.getMainLooper())
        refresh.post(kotlinx.coroutines.Runnable {
            binding.genresTextView.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                val adapter = ListEpisodesAdapter(mutable, applicationContext)
                this.adapter = adapter
            }
        })

    }

    private fun getUserDataFromSharedPreferences(): UserData {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserData", Context.MODE_PRIVATE)

        val userId = sharedPreferences.getString("userID", "") ?: ""
        val tags = sharedPreferences.getString("tags", "") ?: ""
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        return UserData(userId, tags, isLoggedIn)
    }
}