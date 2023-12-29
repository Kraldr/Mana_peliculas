package com.example.manapeliculas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.detodito.ViewHolders.EpisodeViewHolder
import com.example.manapeliculas.adapters.ListEpisodesAdapter
import com.example.manapeliculas.adapters.RecomendedAdapter
import com.example.manapeliculas.data.MutableX
import com.example.manapeliculas.data.Series.SeriesInfo
import com.example.manapeliculas.data.User
import com.example.manapeliculas.data.UserData
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.databinding.ActivityMovieBinding
import com.example.manapeliculas.ui.gallery.GalleryFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.random.Random

class Movie : AppCompatActivity(), ListEpisodesAdapter.OnItemClickListener {

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
    private lateinit var userData:UserData
    private var selectedLanguage: String = ""

    class CyberLockerDataAdapter(context: Context, resource: Int, private val servers: List<EpisodeViewHolder.CyberLockerData>) :
        ArrayAdapter<EpisodeViewHolder.CyberLockerData>(context, resource, servers) {

        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            val view = super.getView(position, convertView, parent)

            val cyberLockerData = servers[position]
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = cyberLockerData.cyberlocker.uppercase()

            return view
        }
    }

    data class MovieInfo(
        val TMDbId: String,
        val IMDbId: String,
        val titles: MovieTitles,
        val images: MovieImages,
        val overview: String,
        val runtime: Int,
        val genres: List<MovieGenre>,
        val cast: MovieCast,
        val rate: MovieRate,
        val slug: MovieSlug,
        val releaseDate: String,
        val players: MoviePlayers,
        val downloads: List<MovieDownload>
    )

    data class MovieTitles(
        val name: String,
        val original: MovieOriginalTitle
    )

    data class MovieOriginalTitle(
        val name: String
    )

    data class MovieImages(
        val poster: String,
        val backdrop: String
    )

    data class MovieGenre(
        val slug: String,
        val name: String
    )

    data class MovieCast(
        val acting: List<MoviePerson>,
        val directing: List<MoviePerson>,
        val production: List<MoviePerson>,
        val countries: List<MovieCountry>
    )

    data class MoviePerson(
        val name: String
    )

    data class MovieCountry(
        val name: String
    )

    data class MovieRate(
        val average: Double,
        val votes: Int
    )

    data class MovieSlug(
        val name: String
    )

    data class MoviePlayers(
        val latino: List<MoviePlayer>,
        val spanish: List<MoviePlayer>,
        val english: List<MoviePlayer>
    )

    data class MoviePlayer(
        val cyberlocker: String,
        val result: String,
        val quality: String
    )

    data class MovieDownload(
        val cyberlocker: String,
        val result: String,
        val quality: String,
        val language: String
    )


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userData = getUserDataFromSharedPreferences()

        val source = intent.getStringExtra("href")
        val window = window

        if (source != null) {
            if (source.contains("movies")) {
                binding.btnStart.isVisible = true;
                binding.spinnerDropdown.isVisible = false;
            } else {
                binding.btnStart.isVisible = false;
                binding.spinnerDropdown.isVisible = true;
            }
        }


        supportActionBar!!.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorStatus)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatus)
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

        binding.lottieAnimationAdd.setOnClickListener {
            when (boockmark) {
                false -> {
                    if (userData.isLoggedIn) {
                        objetoLastP?.let { myListArray.add(it) }
                        boockmark = likeAnimation(
                            binding.lottieAnimationAdd,
                            R.raw.animation_lm2nk2kg,
                            boockmark,
                            R.drawable.baseline_bookmark_border_24
                        )
                        saveData(userData.userId, "myList", myListArray)
                    }else {
                        startActivity(Intent(applicationContext, Login::class.java).apply {
                        })
                    }
                }
                true -> {
                    if (userData.isLoggedIn) {
                        boockmark = likeAnimation(
                            binding.lottieAnimationAdd,
                            R.raw.animation_lm2nk2kg,
                            boockmark,
                            R.drawable.baseline_bookmark_border_24
                        )
                        myListArray.removeIf { data -> data.href == objetoLastP?.href ?: "" }
                        deleteData(userData.userId, "myList", myListArray)
                    }else {
                        startActivity(Intent(applicationContext, Login::class.java).apply {
                        })
                    }
                }
            }
        }

        binding.lottieAnimationView.setOnClickListener {
            when (like) {
                false -> {
                    if (userData.isLoggedIn) {
                        objetoLastP?.let { objectsArray.add(it) }
                        like = likeAnimation(
                            binding.lottieAnimationView,
                            R.raw.animation_lm2o3qv0,
                            like,
                            R.drawable.twitter_like
                        )
                        saveData(userData.userId, "like", objectsArray)
                    }else {
                        startActivity(Intent(applicationContext, Login::class.java).apply {
                        })
                    }
                }
                true -> {
                    if (userData.isLoggedIn) {
                        like = likeAnimation(
                            binding.lottieAnimationView,
                            R.raw.animation_lm2o3qv0,
                            like,
                            R.drawable.twitter_like
                        )
                        objectsArray.removeIf { data -> data.href == objetoLastP?.href ?: "" }
                        deleteData(userData.userId, "like", objectsArray)
                    }else {
                        startActivity(Intent(applicationContext, Login::class.java).apply {
                        })
                    }
                }
            }
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        loadData(userData.userId, source)
        loadToken(source)
    }

    private fun loadData(userId: String, source: String?) {
        if (userData.isLoggedIn) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtainyear(fechaString: String?): String {
        // Verificar si la fecha es nula
        if (fechaString.isNullOrEmpty()) {
            return "Fecha desconocida"
        }

        // Crear un formateador para el formato ISO 8601
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        try {
            // Parsear la cadena de fecha
            val fecha = LocalDateTime.parse(fechaString, formatter)

            // Obtener el año
            val anio = fecha.year

            return anio.toString()
        } catch (e: DateTimeParseException) {
            // Manejar errores de parseo de fecha
            e.printStackTrace()
            return "Fecha inválida"
        }
    }

    private fun loadToken(source: String?) {
        mDatabase?.child("TokenID")?.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si hay datos antes de mostrar el Toast
                if (snapshot.exists()) {
                    val data = snapshot.value
                    if (source != null) {
                        if (source.contains("movies")) {
                            val lastSegment = source.substringAfterLast("/")
                            scrapeCuevanaPeliculas(lastSegment, data.toString())
                            scrapePage(data.toString())
                        } else {
                            val lastSegment = source.substringAfterLast("/")
                            scrapeCuevanaSeries(lastSegment, data.toString())
                        }
                    }
                } else {
                    // Manejar el caso donde no hay datos
                    Toast.makeText(applicationContext, "No hay datos disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
                Log.e("loadData", "Error al cargar datos: ${error.message}")
            }
        })
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

    private fun scrapePage(data: String) {
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

                    scrapeRecomend(numeroRecuperado.toInt(), "Películas recomendadas", data)
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

    private fun scrapeRecomend(page: Int, type: String, data: String) {
        val randomNumber = Random.nextInt(page) + 1
        val urlx = "https://www.cuevana2espanol.net/_next/data/${data}/es/archives/movies/top/day/page/1.json?slug=page&slug=1"
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scrapeCuevana(url: String, isSeries: Boolean = false, data: String) {
        coroutineScope.launch(Dispatchers.IO) {
            if (!isSeries) {
                val response = getHttpResponse("https://www.cuevana2espanol.net/_next/data/${data}/es/movies/$url.json")
                val jsonObject = Gson().fromJson(response, JsonObject::class.java)

                val itemsObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("post")

                val infoItemsObject = Gson().fromJson(itemsObject, MovieInfo::class.java)

                withContext(Dispatchers.Main) {
                    binding.txtTitle.text = infoItemsObject.titles.name
                    binding.descriptionTextView.text = infoItemsObject.overview
                    binding.ratingTextView.text = infoItemsObject.rate.average.toString()
                    binding.txtGen.text = infoItemsObject.genres.joinToString(", ") { it.name }

                    Glide.with(applicationContext)
                        .load(infoItemsObject.images.poster)
                        .into(binding.imgFont)

                    Glide.with(applicationContext)
                        .load(infoItemsObject.images.backdrop)
                        .transform(BlurTransformation(25, 3))
                        .into(binding.imageView)

                    stopAnimation()

                    objetoLastP = if (!isSeries) {
                        LastP("https://www.cuevana2espanol.net/movies/$url", infoItemsObject.images.poster, infoItemsObject.titles.name, obtainyear(infoItemsObject.releaseDate))
                    }else {
                        LastP("https://www.cuevana2espanol.net/series/$url", infoItemsObject.images.poster, infoItemsObject.titles.name, obtainyear(infoItemsObject.releaseDate))
                    }
                }
            }else {
                val response = getHttpResponse("https://www.cuevana2espanol.net/_next/data/${data}/es/series/$url.json")
                val jsonObject = Gson().fromJson(response, JsonObject::class.java)
                val itemsObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("post")
                val infoItemsObject = Gson().fromJson(itemsObject, SeriesInfo::class.java)


                withContext(Dispatchers.Main) {
                    binding.txtTitle.text = infoItemsObject.titles.name
                    binding.descriptionTextView.text = infoItemsObject.overview
                    binding.ratingTextView.text = infoItemsObject.rate.average.toString()
                    binding.txtGen.text = infoItemsObject.genres.joinToString(", ") { it.name }

                    Glide.with(applicationContext)
                        .load(infoItemsObject.images.poster)
                        .into(binding.imgFont)

                    Glide.with(applicationContext)
                        .load(infoItemsObject.images.backdrop)
                        .transform(BlurTransformation(25, 3))
                        .into(binding.imageView)

                    val seasonSize = mutableListOf<String>()

                    for (i in infoItemsObject.seasons.indices) {
                        seasonSize.add("Temporada ${infoItemsObject.seasons[i].number}")
                    }

                    val spinner = binding.spinnerDropdown
                    binding.spinnerDropdownLayout.visibility = View.VISIBLE
                    val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, seasonSize)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    val episodes = mutableListOf<MutableX>()


                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parentView: AdapterView<*>,
                            selectedItemView: View?,
                            position: Int,
                            id: Long
                        ) {
                            episodes.clear()
                            for (dataEpisode in infoItemsObject.seasons[position].episodes) {
                                episodes.add(
                                    MutableX(
                                        "Episodio " + dataEpisode.number.toString(),
                                        dataEpisode.title,
                                        "https://www.cuevana2espanol.net/_next/data/${data}/es/series/${url}/seasons/${dataEpisode.slug.season}/episodes/${dataEpisode.slug.episode}.json",
                                        dataEpisode.image
                                    )
                                )
                            }

                            initRecyclerViewRecent(episodes)
                        }

                        override fun onNothingSelected(parentView: AdapterView<*>) {
                            // Do nothing here
                        }
                    }

                    stopAnimation()

                    objetoLastP = if (!isSeries) {
                        LastP("https://www.cuevana2espanol.net/movies/$url", infoItemsObject.images.poster, infoItemsObject.titles.name, obtainyear(infoItemsObject.releaseDate))
                    }else {
                        LastP("https://www.cuevana2espanol.net/series/$url", infoItemsObject.images.poster, infoItemsObject.titles.name, obtainyear(infoItemsObject.releaseDate))
                    }
                }

            }

//            val response = client.newCall(request).execute()
//            val body = response.body?.string()
//
//            val doc = Jsoup.parse(body)
//
//            val dataMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div.mt-2.row > table > tbody > tr")
//
//            val data = mutableListOf<dataMovie>()
//            val episodes = mutableListOf<MutableX>()
//
//            val duracion = dataMovies[3].selectFirst("td:nth-child(2)")?.text().toString()
//            val hrefIMG = "https://www.cuevana2espanol.icu" + doc.selectFirst(if (isSeries) "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_image__5Tx0e.col > div > img" else "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_image__LJrqk.col > div > img")?.attr("src")
//            val originalTitle = dataMovies[1].selectFirst("td:nth-child(2)")?.text().toString()
//            val rate = dataMovies[2].selectFirst("td:nth-child(2)")?.text().toString()
//            val sinopsis = doc.selectFirst(if (isSeries) "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(1) > div.serieInfo_data__SuMej.pt-3.col > div:nth-child(2)" else "#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div.mb-3.pt-3.container > div:nth-child(1) > div.movieInfo_data__HL5zl.pt-3.col > div:nth-child(2)")?.text().toString()
//            val tags = dataMovies[6].selectFirst("td:nth-child(2)")?.text().toString()
//            val titulo = dataMovies[0].selectFirst("td:nth-child(2)")?.text().toString()
//            val year = dataMovies[4].selectFirst("td:nth-child(2)")?.text().toString()
//
//            if (isSeries) {
//                val dataEpisodes = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div:nth-child(2) > div.row.row-cols-xl-4.row-cols-lg-3.row-cols-2 > div")
//
//                for (dataEpisode in dataEpisodes) {
//                    episodes.add(
//                        MutableX(
//                            dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > span")?.text().toString(),
//                            dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a > h3")?.text().toString(),
//                            "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_data__jsvqZ > a")?.attr("href").toString(),
//                            "https://www.cuevana2espanol.icu" + dataEpisode.selectFirst("article > div.EpisodeItem_poster__AwaLr > a > img")?.attr("src").toString()
//                        )
//                    )
//                }
//            }
//
//            withContext(Dispatchers.Main) {
//                binding.txtTitle.text = titulo
//                binding.descriptionTextView.text = sinopsis
//                binding.txtGen.text = tags
//                binding.ratingTextView.text = rate
//
//                Glide.with(applicationContext)
//                    .load(hrefIMG)
//                    .into(binding.imgFont)
//
//                Glide.with(applicationContext)
//                    .load(hrefIMG)
//                    .transform(BlurTransformation(25, 3))
//                    .into(binding.imageView)
//
//                if (isSeries) {
//                    initRecyclerViewRecent(episodes)
//                }
//
//                stopAnimation()
//
//                objetoLastP = LastP(url, hrefIMG, titulo, year)
//            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scrapeCuevanaPeliculas(url: String, data: String) {
        scrapeCuevana(url, isSeries = false, data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scrapeCuevanaSeries(url: String, data: String) {
        scrapeCuevana(url, isSeries = true, data)
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
                adapter.setOnItemClickListener(this@Movie)
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



    override fun onItemClicked(url: String, infoItemsObject: EpisodeViewHolder.SerieData) {
        println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        println(infoItemsObject)
        Toast.makeText(applicationContext, "Mensaje", Toast.LENGTH_SHORT).show()
        val languages = arrayOf("Latino", "Español", "Inglés")

        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, -1) { _, which ->
                // Guarda el idioma seleccionado
                selectedLanguage = languages[which]
            }
            .setPositiveButton("Next") { _, _ ->
                // Llama a la función para mostrar las opciones adicionales según el idioma
                showAdditionalOptions(infoItemsObject)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Función para mostrar opciones adicionales según el idioma seleccionado
    private fun showAdditionalOptions(infoItemsObject: EpisodeViewHolder.SerieData) {
        val players = mutableListOf<EpisodeViewHolder.CyberLockerData>()

        when (selectedLanguage) {
            "Latino" -> {
                for (dataEpisode in infoItemsObject.latino) {
                    players.add(
                        EpisodeViewHolder.CyberLockerData(
                            dataEpisode.cyberlocker,
                            dataEpisode.result,
                            dataEpisode.quality
                        )
                    )
                }
                showServersDialog(players)
            }
            "Español" -> {
                for (dataEpisode in infoItemsObject.spanish) {
                    players.add(
                        EpisodeViewHolder.CyberLockerData(
                            dataEpisode.cyberlocker,
                            dataEpisode.result,
                            dataEpisode.quality
                        )
                    )
                }
                showServersDialog(players)
            }
            "Inglés" -> {
                for (dataEpisode in infoItemsObject.english) {
                    players.add(
                        EpisodeViewHolder.CyberLockerData(
                            dataEpisode.cyberlocker,
                            dataEpisode.result,
                            dataEpisode.quality
                        )
                    )
                }
                showServersDialog(players)
            }
            else -> {
                // Manejo por defecto o error
                Toast.makeText(applicationContext, "Error: Idioma no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para mostrar un diálogo con los servidores disponibles para el idioma seleccionado
    private fun showServersDialog(servers: MutableList<EpisodeViewHolder.CyberLockerData>) {
        val adapter = CyberLockerDataAdapter(this, android.R.layout.simple_list_item_single_choice, servers)

        var selectedServer: EpisodeViewHolder.CyberLockerData? = null // Cambio aquí: inicializa como null

        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Select Server")
            .setSingleChoiceItems(adapter, -1) { _, which ->
                // Aquí puedes realizar acciones con el servidor seleccionado, por ejemplo, almacenar la URL
                selectedServer = servers[which]
            }
            .setPositiveButton("Seleccionar") { dialog, _ ->
                if (selectedServer != null) {
                    val intent = Intent(this, Player::class.java)
                    intent.putExtra("result", selectedServer!!.result)
                    startActivity(intent)

                    dialog.dismiss()
                } else {
                    // Manejar el caso cuando no se ha seleccionado ningún servidor
                    // Puedes mostrar un mensaje o realizar alguna acción adicional
                }
            }
            .show()
    }
}