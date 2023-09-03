package com.example.manapeliculas

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.manapeliculas.adapters.ListRecentAdapter
import com.example.manapeliculas.data.searchDataItem
import com.example.manapeliculas.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class Search : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }

        binding.search.setOnEditorActionListener { _, keyCode, event ->
            if (((event?.action ?: -1) == KeyEvent.ACTION_DOWN)
                || keyCode == EditorInfo.IME_ACTION_DONE
            ) {

                val replacedString = binding.search.text.toString().replace(" ", "%20")
                searchCuevana2(replacedString)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun searchCuevana2(url: String) {

        coroutineScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://www.cuevana2espanol.icu/search?q=$url")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()

            val doc = Jsoup.parse(body)

            val searchData =
                doc.select("#__next > div.pt-3.container > div > div.mainWithSidebar_content__FcoHh.col-md-9 > div > div.row.row-cols-xl-5.row-cols-lg-4.row-cols-3 > div:nth-child(1)")


            val data = mutableListOf<searchDataItem>()
            for (search in searchData) {
                data.add(
                    searchDataItem(
                        "https://www.cuevana2espanol.icu" + search.selectFirst("article > div > a")
                            ?.attr("href").toString(),
                        "https://www.cuevana2espanol.icu" + search.selectFirst("article > div > a > img")
                            ?.attr("src").toString(),
                        search.selectFirst("article > div > a > h3")?.text().toString(),
                        search.selectFirst("article > div > span")?.text().toString()
                    )
                )
            }

            data.reverse()

            withContext(Dispatchers.Main) {
                initRecyclerViewRecent(data)
            }
        }
    }

    private fun initRecyclerViewRecent(
        data: MutableList<searchDataItem>
    ) {
        val refresh = Handler(Looper.getMainLooper())
        refresh.post(kotlinx.coroutines.Runnable {
            binding.recyViewRecent.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                val adapter = ListRecentAdapter(data, applicationContext)
                this.adapter = adapter
            }
        })

    }
}