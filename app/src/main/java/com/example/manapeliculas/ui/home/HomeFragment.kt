package com.example.manapeliculas.ui.home

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.adapters.CarouselAdapter
import com.example.manapeliculas.adapters.LastPAdapter
import com.example.manapeliculas.adapters.LastSAdapter
import com.example.manapeliculas.adapters.PDestacadasAdapter
import com.example.manapeliculas.adapters.SDestacadasAdapter
import com.example.manapeliculas.data.cuevana2.Carousel
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.data.cuevana2.LastS
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.data.cuevana2.SDestacada
import com.example.manapeliculas.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var timer: Timer
    private lateinit var scrollTask: TimerTask
    private lateinit var smoothScroller: LinearSmoothScroller
    private var mDatabase: DatabaseReference? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    data class SliderItem(
        val titles: Title,
        val images: Image,
        val TMDbId: String,
        val slug: Slug
    )

    data class topDayMovies(
        val titles: Title,
        val images: Images,
        val releaseDate: String,
        val TMDbId: String,
        val slug: Slug
    )

    data class topDaySeries(
        val titles: Title,
        val images: Images,
        val releaseDate: String,
        val TMDbId: String,
        val slug: Slug
    )

    data class lastMovies(
        val titles: Title,
        val images: Images,
        val releaseDate: String,
        val TMDbId: String,
        val slug: Slug
    )

    data class lastSeries(
        val titles: Title,
        val images: Images,
        val releaseDate: String,
        val TMDbId: String,
        val slug: Slug
    )

    data class Title(
        val name: String
    )

    data class Image(
        val backdrop: String
    )

    data class Images(
        val poster: String
    )

    data class Slug(
        val name: String
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

            mDatabase = FirebaseDatabase.getInstance().reference

            val pDestacada = MutableList(4) { PDestacada("", "", "", "") }
            val sDestacada = MutableList(4) { SDestacada("", "", "", "") }
            val lastP = MutableList(4) { LastP("", "", "", "") }
            val lastS = MutableList(4) { LastS("", "", "", "") }
            val carousel = MutableList(3) { Carousel("", "", "") }

            setupRecyclerView(pDestacada, sDestacada, lastP, lastS, carousel)

            loadData()
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar el error de inicialización de vista
        }

        return binding.root
    }

    private fun setupRecyclerView(
        pDestacada: List<PDestacada>,
        sDestacada: List<SDestacada>,
        lastP: List<LastP>,
        lastS: List<LastS>,
        carousel: List<Carousel>
    ) {
        setupHorizontalRecyclerView(binding.recyViewPDestadas, PDestacadasAdapter(pDestacada, requireActivity()))
        setupHorizontalRecyclerView(binding.recyViewSDestadas, SDestacadasAdapter(sDestacada, requireActivity()))
        setupHorizontalRecyclerView(binding.recyViewLastP, LastPAdapter(lastP, requireActivity()))
        setupHorizontalRecyclerView(binding.recyViewLastS, LastSAdapter(lastS, requireActivity()))

        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyView.layoutManager = layoutManager
        binding.recyView.adapter = CarouselAdapter(carousel, requireActivity())

        val scrollSpeed = 6000L // Velocidad de desplazamiento en milisegundos
        var currentPosition = layoutManager.findFirstVisibleItemPosition()

        scrollTask = object : TimerTask() {
            override fun run() {
                currentPosition++
                if (currentPosition >= layoutManager.itemCount) {
                    currentPosition = 0
                }
                try {
                    requireActivity().runOnUiThread {
                        if (isAdded) {
                            scrollToPosition(currentPosition)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        timer = Timer()
        try {
            timer.schedule(scrollTask, scrollSpeed, scrollSpeed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupHorizontalRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            this.adapter = adapter
            scrollToPosition(3)
        }
    }

    private fun loadData() {
        mDatabase?.child("TokenID")?.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si hay datos antes de mostrar el Toast
                if (snapshot.exists()) {
                    val data = snapshot.value
                    initRecyclerViewData(data.toString())
                } else {
                    // Manejar el caso donde no hay datos
                    Toast.makeText(requireActivity(), "No hay datos disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
                Log.e("loadData", "Error al cargar datos: ${error.message}")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRecyclerViewData(data: String) {
        val url = "https://www.cuevana2espanol.net/_next/data/${data}/es.json"

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = getHttpResponse(url)

                // Convierte la respuesta JSON a un objeto Kotlin usando Gson
                val jsonObject = Gson().fromJson(response, JsonObject::class.java)

                // Accede a sliderItems -> data
                val sliderItemsObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("sliderItems")
                val sliderItemsData = sliderItemsObject?.getAsJsonArray("data")

                val dataCarousel = mutableListOf<Carousel>()

                if (sliderItemsData != null) {
                    // Convierte la lista de elementos en objetos Kotlin usando Gson
                    val sliderItemsList = Gson().fromJson(sliderItemsData, Array<SliderItem>::class.java)

                    // Ahora puedes trabajar con la lista de sliderItems
                    for (item in sliderItemsList) {

                        dataCarousel.add(
                            Carousel(
                                "https://www.cuevana2espanol.net/movies/" + item.slug.name,
                                item.images.backdrop,
                                item.titles.name
                            )
                        )
                    }
                } else {
                    println("No data found in sliderItems.")
                }

                val topDayMoviesObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("topDayMovies")
                val topDayMoviesData = topDayMoviesObject?.getAsJsonArray("data")

                var dataEpisodePDestacada = mutableListOf<PDestacada>()

                if (topDayMoviesData != null) {
                    val topDayMoviesList = Gson().fromJson(topDayMoviesData, Array<topDayMovies>::class.java)

                    for (item in topDayMoviesList) {
                        val year = obtainyear(item.releaseDate)
                        dataEpisodePDestacada.add(
                            PDestacada(
                                "https://www.cuevana2espanol.net/movies/" + item.slug.name,
                                item.images.poster,
                                item.titles.name,
                                year
                            )
                        )
                    }

                    dataEpisodePDestacada = dataEpisodePDestacada.reversed().toMutableList()
                } else {
                    println("No data found in topDayMovies.")
                }

                val topDaySeriesObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("topDaySeries")
                val topDaySeriesData = topDaySeriesObject?.getAsJsonArray("data")

                var dataEpisodeSDestacada = mutableListOf<SDestacada>()

                if (topDaySeriesData != null) {
                    val topDaySeriesList = Gson().fromJson(topDaySeriesData, Array<topDaySeries>::class.java)

                    for (item in topDaySeriesList) {
                        val year = obtainyear(item.releaseDate)
                        dataEpisodeSDestacada.add(
                            SDestacada(
                                "https://www.cuevana2espanol.net/series/" + item.slug.name,
                                item.images.poster,
                                item.titles.name,
                                year
                            )
                        )
                    }

                    dataEpisodeSDestacada = dataEpisodeSDestacada.reversed().toMutableList()
                } else {
                    println("No data found in topDayMovies.")
                }

                val lastMoviesObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("lastMovies")
                val lastMoviesData = lastMoviesObject?.getAsJsonArray("data")

                var dataEpisodelastP= mutableListOf<LastP>()

                if (topDaySeriesData != null) {
                    val lastMoviesList = Gson().fromJson(lastMoviesData, Array<lastMovies>::class.java)

                    for (item in lastMoviesList) {
                        val year = obtainyear(item.releaseDate)
                        dataEpisodelastP.add(
                            LastP(
                                "https://www.cuevana2espanol.net/movies/" + item.slug.name,
                                item.images.poster,
                                item.titles.name,
                                year
                            )
                        )
                    }

                    dataEpisodelastP = dataEpisodelastP.reversed().toMutableList()
                } else {
                    println("No data found in lastMovies.")
                }

                val lastSeriesObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("lastSeries")
                val lastSeriesData = lastSeriesObject?.getAsJsonArray("data")

                var dataEpisodeLastS= mutableListOf<LastS>()

                if (lastSeriesData != null) {
                    val lastSeriesList = Gson().fromJson(lastSeriesData, Array<lastSeries>::class.java)

                    for (item in lastSeriesList) {
                        val year = obtainyear(item.releaseDate)
                        dataEpisodeLastS.add(
                            LastS(
                                "https://www.cuevana2espanol.net/series/" + item.slug.name,
                                item.images.poster,
                                item.titles.name,
                                year
                            )
                        )
                    }

                    dataEpisodeLastS = dataEpisodeLastS.reversed().toMutableList()
                } else {
                    println("No data found in lastSeries.")
                }

                withContext(Dispatchers.Main) {
                    updateRecyclerView(binding.recyView, CarouselAdapter(dataCarousel, requireActivity()))
                    updateRecyclerView(binding.recyViewPDestadas, PDestacadasAdapter(dataEpisodePDestacada, requireActivity()))
                    updateRecyclerView(binding.recyViewSDestadas, SDestacadasAdapter(dataEpisodeSDestacada, requireActivity()))
                    updateRecyclerView2(binding.recyViewLastP, LastPAdapter(dataEpisodelastP, requireActivity()))
                    updateRecyclerView(binding.recyViewLastS, LastSAdapter(dataEpisodeLastS, requireActivity()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
    private fun updateRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            this.adapter = adapter
            scrollToPosition(3)
        }
    }

    private fun updateRecyclerView2(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            this.adapter = adapter
            scrollToPosition(7)
        }
    }

    private fun scrollToPosition(position: Int) {
        requireActivity().runOnUiThread {
            if (isAdded) {
                if (!::smoothScroller.isInitialized) {
                    smoothScroller = object : LinearSmoothScroller(requireContext()) {
                        override fun getHorizontalSnapPreference(): Int {
                            return SNAP_TO_START
                        }

                        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                            return 100f / displayMetrics.densityDpi
                        }
                    }
                }
                smoothScroller.targetPosition = position
                binding.recyView.layoutManager?.startSmoothScroll(smoothScroller)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::timer.isInitialized) {
            timer.cancel()
        }
        if (::scrollTask.isInitialized) {
            scrollTask.cancel()
        }
    }
}