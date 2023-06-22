package com.example.manapeliculas.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.manapeliculas.data.cuevana2.homeCuevana2
import com.example.manapeliculas.databinding.FragmentHomeBinding
import com.google.gson.Gson
import okhttp3.*
import okio.IOException
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val url = "https://scrape-app-7fd846d66850.herokuapp.com/scrapeHomec2"
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
        })

        return binding.root
    }

    private fun initRecyclerViewDestacadas(pDestacadas: List<PDestacada>) {
        binding.recyViewPDestadas.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            adapter = PDestacadasAdapter(pDestacadas, requireActivity())
            scrollToPosition(3)
        }
    }

    private fun initRecyclerViewSDestacadas(sDestacadas: List<SDestacada>) {
        binding.recyViewSDestadas.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            adapter = SDestacadasAdapter(sDestacadas, requireActivity())
            scrollToPosition(3)
        }
    }

    private fun initRecyclerViewLastP(lastP: List<LastP>) {
        binding.recyViewLastP.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            adapter = LastPAdapter(lastP, requireActivity())
            scrollToPosition(3)
        }
    }

    private fun initRecyclerViewLastS(lastS: List<LastS>) {
        binding.recyViewLastS.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, true
            )
            adapter = LastSAdapter(lastS, requireActivity())
            scrollToPosition(3)
        }
    }

    private fun initRecyclerViewPopular(data: List<Carousel>) {
        val layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recyView.layoutManager = layoutManager
        binding.recyView.adapter = CarouselAdapter(data, requireActivity())

        val scrollSpeed = 5000L // Velocidad de desplazamiento en milisegundos
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