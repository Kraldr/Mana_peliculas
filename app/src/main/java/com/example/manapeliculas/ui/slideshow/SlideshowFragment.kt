package com.example.manapeliculas.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.adapters.PDestacadasAdapter
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.databinding.FragmentSlideshowBinding
import com.example.manapeliculas.ui.gallery.GalleryFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val decoration = GalleryFragment.SpacesItemDecoration(16)
        if (!isDecorationAlreadyApplied(binding.recyView, decoration)) {
            binding.recyView.addItemDecoration(decoration)
        }

        initRecyclerView(1)
        return root
    }

    private fun initRecyclerView(page: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/archives/series/page/$page")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val pDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(1) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

            val dataEpisode = pDestacadas.map {
                val article = it.selectFirst("article > div > a")
                val img = it.selectFirst("article > div > a > img")
                val title = it.selectFirst("article > div > a > h3")
                val span = it.selectFirst("article > div > span")

                PDestacada(
                    "https://www.cuevana2espanol.icu${article?.attr("href")}",
                    "https://www.cuevana2espanol.icu${img?.attr("src")}",
                    title?.text().toString(),
                    span?.text().toString()
                )
            }

            withContext(Dispatchers.Main) {
                binding.recyView.apply {
                    layoutManager = GridLayoutManager(requireActivity(), 3)
                    adapter = PDestacadasAdapter(dataEpisode, requireActivity())
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}