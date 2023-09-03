package com.example.manapeliculas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.core.content.ContextCompat
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


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val source = intent.getStringExtra("href")
        val window = window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorStatus)
        }

        val fadeIn = ObjectAnimator.ofFloat(binding.imageView, "alpha", 0f, 1f).apply {
            duration = 1000
        }

        val scaleUpX = ObjectAnimator.ofFloat(binding.imageView, "scaleX", 0f, 1f).apply {
            duration = 1000
        }

        val scaleUpY = ObjectAnimator.ofFloat(binding.imageView, "scaleY", 0f, 1f).apply {
            duration = 1000
        }

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

        val userData = getUserDataFromSharedPreferences()

        if (source != null) {
            if (source.contains("movies")) {
                scrapeCuevanaPeliculas(source)
                scrapePage()
            } else {
                scrapeCuevanaSeries(source)
            }
        }

        binding.lottieAnimationAdd.setOnClickListener {
            when (boockmark) {
                false -> {
                    objetoLastP?.let { myListArray.add(it) }
                    boockmark = likeAnimation(
                        binding.lottieAnimationAdd,
                        R.raw.animation_lm2nk2kg,
                        boockmark,
                        R.drawable.baseline_bookmark_border_24
                    )
                    saveData(userData.userId, "myList", myListArray)
                }
                true -> {
                    boockmark = likeAnimation(
                        binding.lottieAnimationAdd,
                        R.raw.animation_lm2nk2kg,
                        boockmark,
                        R.drawable.baseline_bookmark_border_24
                    )
                    myListArray.removeIf { data -> data.href == objetoLastP?.href ?: "" }
                    deleteData(userData.userId, "myList", myListArray)
                }
            }
        }

        binding.lottieAnimationView.setOnClickListener {
            when (like) {
                false -> {
                    objetoLastP?.let { objectsArray.add(it) }
                    like = likeAnimation(
                        binding.lottieAnimationView,
                        R.raw.animation_lm2o3qv0,
                        like,
                        R.drawable.twitter_like
                    )
                    saveData(userData.userId, "like", objectsArray)
                }
                true -> {
                    like = likeAnimation(
                        binding.lottieAnimationView,
                        R.raw.animation_lm2o3qv0,
                        like,
                        R.drawable.twitter_like
                    )
                    objectsArray.removeIf { data -> data.href == objetoLastP?.href ?: "" }
                    deleteData(userData.userId, "like", objectsArray)
                }
            }
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        loadData(userData.userId, source)
    }

    private fun loadData(userId: String, source: String?) {
        mDatabase?.child("users")?.child(userId)?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    myListArray = user.myList
                    objectsArray = user.like

                    if (containsHref(user.myList, source)) {
                        boockmark = likeAnimation(
                            binding.lottieAnimationAdd,
                            R.raw.animation_lm2nk2kg,
                            boockmark,
                            R.drawable.baseline_bookmark_border_24
                        )
                    }

                    if (containsHref(user.like, source)) {
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

    private fun containsHref(list: List<LastP>, source: String?): Boolean {
        return list.any { obj ->
            obj.href == source
        }
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

    private fun scrapePage() {
        val database = FirebaseDatabase.getInstance()
        val reference =
            database.reference.child("numPage")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Este método se llama cuando los datos cambian en el nodo

                // Verifica si el nodo existe y contiene un valor
                if (dataSnapshot.exists() && dataSnapshot.value != null) {
                    // Accede al valor del número
                    val numeroRecuperado =
                        dataSnapshot.value as Long

                    scrapeRecomend(numeroRecuperado.toInt(), "Películas recomendadas")
                } else {
                    println("El nodo no existe o no contiene un valor")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Este método se llama si ocurre un error en la operación
                println("Error al acceder a la base de datos: ${databaseError.message}")
            }
        })
    }

    private fun scrapeRecomend(page: Int, type: String) {
        val randomNumber = Random.nextInt(page) + 1
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://www.cuevana2espanol.net/archives/movies/top/week/page/$randomNumber")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    println("Error en la solicitud HTTP: ${response.code}")
                    return@launch
                }

                val body = response.body?.string()
                if (body.isNullOrEmpty()) {
                    println("El cuerpo de la respuesta está vacío.")
                    return@launch
                }

                val doc = Jsoup.parse(body)

                val pDestacadas =
                    doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(1) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

                val dataEpisode = pDestacadas.mapNotNull { pDestacada ->
                    val href = pDestacada.selectFirst("article > div > a")?.attr("href")?.let {
                        "https://www.cuevana2espanol.icu$it"
                    }
                    val src = pDestacada.selectFirst("article > div > a > img")?.attr("src")?.let {
                        "https://www.cuevana2espanol.icu$it"
                    }
                    val title = pDestacada.selectFirst("article > div > a > h3")?.text()
                    val spanText = pDestacada.selectFirst("article > div > span")?.text()

                    if (href != null && src != null && title != null && spanText != null) {
                        PDestacada(href, src, title, spanText)
                    } else {
                        null
                    }
                }

                val randomNumbersSet = mutableSetOf<Int>()

                while (randomNumbersSet.size < 12 && randomNumbersSet.size < dataEpisode.size) {
                    val randomNumber = Random.nextInt(dataEpisode.size)
                    randomNumbersSet.add(randomNumber)
                }

                val dataEpisodeRandom = randomNumbersSet.mapNotNull { index ->
                    dataEpisode.getOrNull(index)
                }

                withContext(Dispatchers.Main) {
                    binding.txtTyperecomeded.text = type
                    binding.reomendedRecycler.apply {
                        layoutManager = GridLayoutManager(applicationContext, 3)
                        adapter = RecomendedAdapter(dataEpisodeRandom, applicationContext)
                    }
                }
            } catch (e: Exception) {
                println("Error al realizar el scraping: ${e.message}")
                // Aquí puedes manejar el error de la forma que desees
            }
        }
    }

    private fun scrapeCuevana(url: String, isSeries: Boolean = false) {
        coroutineScope.launch(Dispatchers.IO) {
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
            val hrefIMG = "https://www.cuevana2espanol.icu" + doc.selectFirst(if (isSeries) "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_image__5Tx0e.col > div > img" else "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_image__LJrqk.col > div > img")?.attr("src")
            val originalTitle = dataMovies[1].selectFirst("td:nth-child(2)")?.text().toString()
            val rate = dataMovies[2].selectFirst("td:nth-child(2)")?.text().toString()
            val sinopsis = doc.selectFirst(if (isSeries) "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_data__SuMej.pt-3.col > div:nth-child(2)" else "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_data__HL5zl.pt-3.col > div:nth-child(2)")?.text().toString()
            val tags = dataMovies[6].selectFirst("td:nth-child(2)")?.text().toString()
            val titulo = dataMovies[0].selectFirst("td:nth-child(2)")?.text().toString()
            val year = dataMovies[4].selectFirst("td:nth-child(2)")?.text().toString()

            if (isSeries) {
                val dataEpisodes = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(2) > div.row.row-cols-xl-4.row-cols-lg-3.row-cols-2 > div")

                for (dataEpisode in dataEpisodes) {
                    episodes.add(
                        MutableX(
                            dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > span")?.text().toString(),
                            dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a > h3")?.text().toString(),
                            "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a")?.attr("href").toString(),
                            "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_poster__AwaLr > a > img")?.attr("src").toString()
                        )
                    )
                }
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

                if (isSeries) {
                    initRecyclerViewRecent(episodes)
                }

                stopAnimation()

                objetoLastP = LastP(url, hrefIMG, titulo, year)
            }
        }
    }

    private fun scrapeCuevanaPeliculas(url: String) {
        scrapeCuevana(url, isSeries = false)
    }

    private fun scrapeCuevanaSeries(url: String) {
        scrapeCuevana(url, isSeries = true)
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
        val name = sharedPreferences.getString("name", "") ?: ""
        val userImage = sharedPreferences.getString("userImage", "") ?: ""
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return UserData(userId, tags, isLoggedIn, name, userImage)
    }
}