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
import com.example.manapeliculas.data.searchData
import com.example.manapeliculas.databinding.ActivitySearchBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class Search : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

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
        val url = "https://scrape-app-7fd846d66850.herokuapp.com/searchcue/${url}"
        val request = Request.Builder().url(url).get().build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val gson = Gson()
                val data = gson.fromJson(responseData, searchData::class.java)

                val refresh = Handler(Looper.getMainLooper())
                refresh.post {
                    initRecyclerViewRecent(data)
                }
            }
        })
    }

    private fun initRecyclerViewRecent(
        data: searchData
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