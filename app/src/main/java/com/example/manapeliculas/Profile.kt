package com.example.manapeliculas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manapeliculas.adapters.ProfileListAdapter
import com.example.manapeliculas.data.User
import com.example.manapeliculas.data.UserData
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.databinding.ActivityProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var mDatabase: DatabaseReference? = null
    private var objectsArray = mutableListOf<LastP>()
    private var myListArray = mutableListOf<LastP>()
    private val storage = Firebase.storage
    private var deleteImage = ""
    private var userData: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorStatus)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatus)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "Inicio de sesión exitoso", Snackbar.LENGTH_LONG).show()

        userData = getUserDataFromSharedPreferences()
        mDatabase = FirebaseDatabase.getInstance().reference

        loadData(userData!!.userId)

        binding.dropdownMenu.setOnClickListener {
            val bottomSheetDialogFragment = MyBottomSheetDialogFragment()
            bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java).apply {
        })
        finish()
    }


    private fun loadData(userId: String) {
        mDatabase?.child("users")?.child(userId)?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val likeList = user.like
                    objectsArray = likeList
                    myListArray = user.myList
                    deleteImage = user.userImage
                    Glide.with(applicationContext).load(user.userImage).into(binding.imageProfile)

                    binding.nameProfile.text = user.name

                    initRecyclerView(binding.recyList, user.myList)
                    initRecyclerView(binding.recyLike, likeList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
            }
        })
    }

    private fun initRecyclerView(recyclerView: RecyclerView, objectsArray: MutableList<LastP>) {
        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = ProfileListAdapter(objectsArray, applicationContext)
        }
    }

    private fun getUserDataFromSharedPreferences(): UserData {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        val userId = sharedPreferences.getString("userID", "") ?: ""
        val tags = sharedPreferences.getString("tags", "") ?: ""
        val name = sharedPreferences.getString("name", "") ?: ""
        val userImage = sharedPreferences.getString("userImage", "") ?: ""
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return UserData(userId, tags, isLoggedIn, name, userImage)
    }
}