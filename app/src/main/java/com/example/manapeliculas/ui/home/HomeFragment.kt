package com.example.manapeliculas.ui.home

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
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
import org.jsoup.Jsoup
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private val gson = Gson()
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var timer: Timer
    private lateinit var scrollTask: TimerTask
    private lateinit var smoothScroller: LinearSmoothScroller

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        initRecyclerViewPopular()
        initRecyclerViewDestacadas()
        initRecyclerViewSDestacadas()
        initRecyclerViewLastP()
        initRecyclerViewLastS()

        /*val url = "http://10.0.2.2:3000/scrapeHomec2"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val data = gson.fromJson(responseData, homeCuevana2::class.java)

                val refresh = Handler(Looper.getMainLooper())
                refresh.post {
                    initRecyclerViewPopular(data.carousel)
                    initRecyclerViewDestacadas(data.pDestacadas)
                    initRecyclerViewSDestacadas(data.sDestacadas)
                    initRecyclerViewLastP(data.lastP)
                    initRecyclerViewLastS(data.lastS)
                }
            }
        })*/

        return binding.root
    }

    private fun initRecyclerViewDestacadas() {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val pDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(4) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

            val dataEpisode = mutableListOf<PDestacada>()
            for (pDestacada in pDestacadas) {
                dataEpisode.add(PDestacada(
                    "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a")?.attr("href").toString(),
                     "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a > img")?.attr("src").toString(),
                         pDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                         pDestacada.selectFirst("article > div > span")?.text().toString())
                )
            }

            dataEpisode.reverse()

            withContext(Dispatchers.Main) {
                binding.recyViewPDestadas.apply {
                    layoutManager = LinearLayoutManager(
                        requireActivity(), LinearLayoutManager.HORIZONTAL, true
                    )
                    adapter = PDestacadasAdapter(dataEpisode, requireActivity())
                    scrollToPosition(3)
                }
            }
        }
    }

    private fun initRecyclerViewSDestacadas() {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val sDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(5) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

            val dataEpisode = mutableListOf<SDestacada>()
            for (sDestacada in sDestacadas) {
                dataEpisode.add(SDestacada(
                    "https://www.cuevana2espanol.icu" + sDestacada.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + sDestacada.selectFirst("article > div > a > img")?.attr("src").toString(),
                    sDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                    sDestacada.selectFirst("article > div > span")?.text().toString())
                )
            }

            dataEpisode.reverse()

            withContext(Dispatchers.Main) {
                binding.recyViewSDestadas.apply {
                    layoutManager = LinearLayoutManager(
                        requireActivity(), LinearLayoutManager.HORIZONTAL, true
                    )
                    adapter = SDestacadasAdapter(dataEpisode, requireActivity())
                    scrollToPosition(3)
                }
            }
        }
    }

    private fun initRecyclerViewLastP() {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val lastMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(6) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")


            val data = mutableListOf<LastP>()
            for (lastMovie in lastMovies) {
                data.add(LastP(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a > img")?.attr("src").toString(),
                    lastMovie.selectFirst("article > div > a > h3")?.text().toString(),
                    lastMovie.selectFirst("article > div > span")?.text().toString())
                )
            }

            data.reverse()

            withContext(Dispatchers.Main) {
                binding.recyViewLastP.apply {
                    layoutManager = LinearLayoutManager(
                        requireActivity(), LinearLayoutManager.HORIZONTAL, true
                    )
                    adapter = LastPAdapter(data, requireActivity())
                    scrollToPosition(3)
                }
            }
        }
    }

    private fun initRecyclerViewLastS() {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val lastMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(7) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")


            val data = mutableListOf<LastS>()
            for (lastMovie in lastMovies) {
                data.add(LastS(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("article > div > a > img")?.attr("src").toString(),
                    lastMovie.selectFirst("article > div > a > h3")?.text().toString(),
                    lastMovie.selectFirst("article > div > span")?.text().toString())
                )
            }

            data.reverse()

            withContext(Dispatchers.Main) {
                binding.recyViewLastS.apply {
                    layoutManager = LinearLayoutManager(
                        requireActivity(), LinearLayoutManager.HORIZONTAL, true
                    )
                    adapter = LastSAdapter(data, requireActivity())
                    scrollToPosition(3)
                }
            }
        }
    }

    private fun initRecyclerViewPopular() {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val lastMovies = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(3) > div > div > div.carousel-inner > div")


            val data = mutableListOf<Carousel>()
            for (lastMovie in lastMovies) {
                data.add(Carousel(
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("div > div:nth-child(2) > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + lastMovie.selectFirst("img")?.attr("src").toString(),
                    lastMovie.selectFirst("div > div:nth-child(1) > h3")?.text().toString())
                )
            }

            data.reverse()

            withContext(Dispatchers.Main) {
                val layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.recyView.layoutManager = layoutManager
                binding.recyView.adapter = CarouselAdapter(data, requireActivity())

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