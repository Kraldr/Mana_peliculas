package com.example.manapeliculas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.manapeliculas.data.UserData
import com.example.manapeliculas.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var mDatabase: DatabaseReference? = null

    companion object {
        lateinit var instance: MainActivity
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val btnProfile = findViewById<CardView>(R.id.btnProfile)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorStatus)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatus)
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        val userData = getUserDataFromSharedPreferences()

        if (userData.isLoggedIn) {
            loadData(userData.userId)
        } else {
            Glide.with(applicationContext)
                .load("https://mir-s3-cdn-cf.behance.net/project_modules/disp/84c20033850498.56ba69ac290ea.png")
                .into(findViewById(R.id.profile))
        }

        val btnSearch = findViewById<ImageButton>(R.id.search)

        btnSearch.setOnClickListener {
            startActivity(Intent(applicationContext, Search::class.java))
        }

        btnProfile.setOnClickListener {
            val userData = getUserDataFromSharedPreferences()
            if (userData.isLoggedIn) {
                startActivity(Intent(applicationContext, Profile::class.java))
                finish()
            } else {
                startActivity(Intent(applicationContext, Login::class.java))
            }
        }

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_news
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun loadData(userId: String) {
        mDatabase?.child("users")?.child(userId)?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.value as Map<*, *>?
                val userImage = userData?.get("userImage") as? String

                Glide.with(applicationContext)
                    .load(userImage)
                    .into(findViewById(R.id.profile))
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}