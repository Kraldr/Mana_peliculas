package com.example.manapeliculas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.manapeliculas.data.User
import com.example.manapeliculas.databinding.ActivitySignupBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


private lateinit var binding: ActivitySignupBinding
private var mAuth: FirebaseAuth? = null
private var mDatabase: DatabaseReference? = null
private var loadingDialog: AlertDialog? = null

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().reference;

        binding.registerButton.setOnClickListener {
            showLoadingDialog()
            registerUser()
        }

        binding.tagsInputLayout.setEndIconOnClickListener {
            val inputText = binding.tagsEditText.text.toString()
            if (inputText.isNotEmpty()) {
                addChipToGroup(inputText, binding.chipGroup)
                binding.tagsEditText.text = null
            }
        }

    }

    private fun registerUser() {
        val email: String = binding.emailEditText.text.toString()
        val password: String = binding.passwordEditText.text.toString()
        val name = binding.firstNameEditText.text.toString()
        val tags = getChipsString(binding.chipGroup)

        if (password == binding.passwordConfirmEditText.text.toString()) {
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId: String = mAuth?.currentUser!!.uid

                        // Crear un objeto User con los datos del usuario
                        val user = User(
                            email = email,
                            like = mutableListOf(),
                            myList = mutableListOf(),
                            name = name,
                            tags = tags,
                            userID = userId,
                            userImage = "https://mir-s3-cdn-cf.behance.net/project_modules/disp/84c20033850498.56ba69ac290ea.png"
                        )

                        // Guardar el objeto User en Firebase
                        mDatabase?.child("users")?.child(userId)?.setValue(user)
                            ?.addOnCompleteListener { innerTask ->
                                val rootView = findViewById<View>(android.R.id.content)
                                if (innerTask.isSuccessful) {
                                    Toast.makeText(applicationContext, "Datos guardados correctamente", Toast.LENGTH_LONG).show()
                                    Log.d("SignupActivity", "Datos guardados en la base de datos")
                                    hideLoadingDialog()
                                    finish()
                                } else {
                                    val error = innerTask.exception
                                    hideLoadingDialog()
                                    Toast.makeText(applicationContext, "Error al guardar los datos: ${error?.message}", Toast.LENGTH_LONG).show()
                                    Log.e("SignupActivity", "Error al guardar los datos: ${error?.message}")
                                }
                            }
                    } else {
                        val error = task.exception
                        Log.e("SignupActivity", "Error en el registro: ${error?.message}")
                    }
                }
        }
    }

    private fun addChipToGroup(inputText: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = inputText
        chip.isCloseIconVisible = true

        // Definir qué hacer cuando se hace clic en el icono de cierre de una ficha:
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }

        chipGroup.addView(chip)
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

    private fun getChipsString(chipGroup: ChipGroup): String {
        val chips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chips.add(chip.text.toString())
        }
        return chips.joinToString(separator = "|")
    }
}