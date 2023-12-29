package com.example.manapeliculas.ui.slideshow

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manapeliculas.adapters.SDestacadasAdapter
import com.example.manapeliculas.data.cuevana2.SDestacada
import com.example.manapeliculas.databinding.FragmentSlideshowBinding
import com.example.manapeliculas.ui.gallery.GalleryFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!
    private var mDatabase: DatabaseReference? = null
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    data class SerieData(
        val titles: SerieTitles,
        val TMDbId: String,
        val images: SerieImages,
        val releaseDate: String,
        val slug: SerieSlug
    )

    data class SerieTitles(
        val name: String
    )

    data class SerieImages(
        val poster: String
    )

    data class SerieSlug(
        val name: String
    )


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mDatabase = FirebaseDatabase.getInstance().reference
        val decoration = GalleryFragment.SpacesItemDecoration(16)
        if (!isDecorationAlreadyApplied(binding.recyView, decoration)) {
            binding.recyView.addItemDecoration(decoration)
        }
        loadData()
        return root
    }

    private fun loadData() {
        mDatabase?.child("TokenID")?.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si hay datos antes de mostrar el Toast
                if (snapshot.exists()) {
                    val data = snapshot.value
                    initRecyclerView(1, data.toString())
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
    private fun initRecyclerView(page: Int, data: String) {

        val url = "https://www.cuevana2espanol.net/_next/data/${data}/es/archives/series/page/${page}.json?slug=page&slug=${page}"

        coroutineScope.launch(Dispatchers.IO) {

            val response = getHttpResponse(url)

            val jsonObject = Gson().fromJson(response, JsonObject::class.java)
            val seriesItemsObject  = jsonObject.getAsJsonObject("pageProps").getAsJsonObject("results")
            val seriesItemsData = seriesItemsObject?.getAsJsonArray("data")

            if (seriesItemsData != null) {
                // Convierte la lista de elementos en objetos Kotlin usando Gson
                val seriesItemsList = Gson().fromJson(seriesItemsData, Array<SerieData>::class.java)

                val dataSeries = mutableListOf<SDestacada>()

                // Ahora puedes trabajar con la lista de sliderItems
                for (item in seriesItemsList) {
                    val year = obtainyear(item.releaseDate)
                    dataSeries.add(
                        SDestacada(
                            "https://www.cuevana2espanol.net/series/" + item.slug.name,
                            item.images.poster,
                            item.titles.name,
                            year
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.recyView.layoutManager = GridLayoutManager(requireActivity(), 3)
                    binding.recyView.adapter = SDestacadasAdapter(dataSeries, requireActivity())
                }

            } else {
                println("No data found in seriesItemsObject.")
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