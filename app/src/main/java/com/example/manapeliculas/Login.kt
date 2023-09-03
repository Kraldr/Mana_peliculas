package com.example.manapeliculas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.manapeliculas.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding: ActivityLoginBinding
private var mAuth: FirebaseAuth? = null
private var mDatabase: DatabaseReference? = null

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        binding.register.setOnClickListener {
            startActivity(Intent(applicationContext, Signup::class.java))
        }

        binding.loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = binding.emailEdittext.text.toString()
        val password = binding.passwordEdittext.text.toString()

        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this) { task ->
                val rootView = findViewById<View>(android.R.id.content)
                if (task.isSuccessful) {
                    val user = mAuth?.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        mDatabase?.child("users")?.child(userId)?.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.value as Map<*, *>?
                                val userTags = userData?.get("tags") as? String

                                val sharedPreferences: SharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                val editor: SharedPreferences.Editor = sharedPreferences.edit()

                                editor.putString("userID", userId)
                                editor.putString("tags", userTags)
                                editor.putBoolean("isLoggedIn", true)

                                editor.apply()

                                // Iniciar la nueva actividad si es necesario
                                val intent = Intent(applicationContext, Profile::class.java)
                                startActivity(intent)
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Snackbar.make(rootView, "Error al obtener los datos: ${error.message}", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        })
                    }
                } else {
                    val error = task.exception?.message
                    Snackbar.make(rootView, "Error en el inicio de sesión: $error", Snackbar.LENGTH_LONG)
                        .show()
                }
            }
    }
}