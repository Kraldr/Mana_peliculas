package com.example.manapeliculas.ui.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var timer: Timer
    private lateinit var scrollTask: TimerTask
    private lateinit var smoothScroller: LinearSmoothScroller

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

            val pDestacada = MutableList(4) { PDestacada("", "", "", "") }
            val sDestacada = MutableList(4) { SDestacada("", "", "", "") }
            val lastP = MutableList(4) { LastP("", "", "", "") }
            val lastS = MutableList(4) { LastS("", "", "", "") }
            val carousel = MutableList(3) { Carousel("", "", "") }

            setupRecyclerView(pDestacada, sDestacada, lastP, lastS, carousel)

            initRecyclerViewData()
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

    private fun initRecyclerViewData() {
        val url = "https://www.cuevana2espanol.icu/"

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = getHttpResponse(url)
                val doc = Jsoup.parse(response)

                val pDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(4) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")
                val sDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(5) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")
                val lastPMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(6) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")
                val lastSMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(7) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")
                val popularMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(3) > div > div > div.carousel-inner > div")

                val pDestacadaList = parsePDestacadas(pDestacadas)
                val sDestacadaList = parseSDestacadas(sDestacadas)
                val lastPList = parseLastP(lastPMovies)
                val lastSList = parseLastS(lastSMovies)
                val carouselList = parseCarousel(popularMovies)

                // Actualizar la interfaz de usuario en el hilo principal
                withContext(Dispatchers.Main) {
                    updateRecyclerView(binding.recyViewPDestadas, PDestacadasAdapter(pDestacadaList, requireActivity()))
                    updateRecyclerView(binding.recyViewSDestadas, SDestacadasAdapter(sDestacadaList, requireActivity()))
                    updateRecyclerView(binding.recyViewLastP, LastPAdapter(lastPList, requireActivity()))
                    updateRecyclerView(binding.recyViewLastS, LastSAdapter(lastSList, requireActivity()))
                    updateRecyclerView(binding.recyView, CarouselAdapter(carouselList, requireActivity()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun parsePDestacadas(elements: Elements): List<PDestacada> {
        val dataEpisode = mutableListOf<PDestacada>()
        for (pDestacada in elements) {
            dataEpisode.add(
                PDestacada(
                    "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a > img")?.attr("src").toString(),
                    pDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                    pDestacada.selectFirst("article > div > span")?.text().toString()
                )
            )
        }
        dataEpisode.reverse()
        return dataEpisode
    }

    private fun parseSDestacadas(elements: Elements): List<SDestacada> {
        val dataEpisode = mutableListOf<SDestacada>()
        for (sDestacada in elements) {
            dataEpisode.add(
                SDestacada(
                    "https://www.cuevana2espanol.icu" + sDestacada.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + sDestacada.selectFirst("article > div > a > img")?.attr("src").toString(),
                    sDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                    sDestacada.selectFirst("article > div > span")?.text().toString()
                )
            )
        }
        dataEpisode.reverse()
        return dataEpisode
    }

    private fun parseLastP(elements: Elements): List<LastP> {
        val dataEpisode = mutableListOf<LastP>()
        for (lastMovie in elements) {
            dataEpisode.add(
                LastP(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a > img")?.attr("src").toString(),
                    lastMovie.selectFirst("article > div > a > h3")?.text().toString(),
                    lastMovie.selectFirst("article > div > span")?.text().toString()
                )
            )
        }
        dataEpisode.reverse()
        return dataEpisode
    }

    private fun parseLastS(elements: Elements): List<LastS> {
        val dataEpisode = mutableListOf<LastS>()
        for (lastMovie in elements) {
            dataEpisode.add(
                LastS(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a > img")?.attr("src").toString(),
                    lastMovie.selectFirst("article > div > a > h3")?.text().toString(),
                    lastMovie.selectFirst("article > div > span")?.text().toString()
                )
            )
        }
        dataEpisode.reverse()
        return dataEpisode
    }

    private fun parseCarousel(elements: Elements): List<Carousel> {
        val data = mutableListOf<Carousel>()
        for (lastMovie in elements) {
            data.add(
                Carousel(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("div > div:nth-child(2) > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("img")?.attr("src").toString(),
                    lastMovie.selectFirst("div > div:nth-child(1) > h3")?.text().toString()
                )
            )
        }
        data.reverse()
        return data
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

    private fun scrollToPosition(position: Int) {
        requireActivity().runOnUiThread {
            if (isAdded) { // Verificar si el fragmento está adjunto a la actividad
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