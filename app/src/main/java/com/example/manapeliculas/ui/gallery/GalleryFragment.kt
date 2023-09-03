package com.example.manapeliculas.ui.gallery

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.adapters.PDestacadasAdapter
import com.example.manapeliculas.data.cuevana2.PDestacada
import com.example.manapeliculas.databinding.FragmentGalleryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val decoration = SpacesItemDecoration(16)
        if (!isDecorationAlreadyApplied(binding.recyView, decoration)) {
            binding.recyView.addItemDecoration(decoration)
        }

        initRecyclerView(1)

        return root
    }


    private fun initRecyclerView(page: Int) {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/archives/movies/page/$page")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val pDestacadas = doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div:nth-child(1) > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div")

            val dataEpisode = mutableListOf<PDestacada>()
            for (pDestacada in pDestacadas) {
                dataEpisode.add(
                    PDestacada(
                    "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a")?.attr("href").toString(),
                    "https://www.cuevana2espanol.icu" + pDestacada.selectFirst("article > div > a > img")?.attr("src").toString(),
                    pDestacada.selectFirst("article > div > a > h3")?.text().toString(),
                    pDestacada.selectFirst("article > div > span")?.text().toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}