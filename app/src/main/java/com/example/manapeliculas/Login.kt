package com.example.manapeliculas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
private var loadingDialog: AlertDialog? = null

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorStatus)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatus)
        }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        binding.register.setOnClickListener {
            startActivity(Intent(applicationContext, Signup::class.java))
        }

        binding.loginButton.setOnClickListener {
            showLoadingDialog()
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
                        mDatabase?.child("users")?.child(userId)
                            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val userData = snapshot.value as Map<*, *>?
                                    val userTags = userData?.get("tags") as? String
                                    val name = userData?.get("name") as? String
                                    val userImage = userData?.get("userImage") as? String

                                    val sharedPreferences: SharedPreferences =
                                        getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()

                                    editor.putString("userID", userId)
                                    editor.putString("tags", userTags)
                                    editor.putString("name", name)
                                    editor.putString("userImage", userImage)
                                    editor.putBoolean("isLoggedIn", true)

                                    editor.apply()

                                    // Iniciar la nueva actividad si es necesario
                                    val intent = Intent(applicationContext, Profile::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(applicationContext, "Error al obtener los datos: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                } else {
                    val error = task.exception?.message
                    Toast.makeText(applicationContext, "Error en el inicio de sesión: $error", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoadingDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.custom_progress_dialog, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)  // Evita que el usuario pueda cancelar el diálogo

        loadingDialog = dialogBuilder.create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }


}