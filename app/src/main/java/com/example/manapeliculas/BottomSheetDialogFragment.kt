package com.example.manapeliculas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import com.example.manapeliculas.data.UserData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_dialog, container, false)
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userData = getUserDataFromSharedPreferences()

        // Accede a los botones y agrega acciones a ellos
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btnEditProfile)
        val btnSettings = view.findViewById<LinearLayout>(R.id.btnSettings)
        val btnHelp = view.findViewById<LinearLayout>(R.id.btnHelp)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), EditProfile::class.java).apply {
            })
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(requireActivity(), Settings::class.java).apply {
            })
        }

        btnHelp.setOnClickListener {
            startActivity(Intent(requireActivity(), Help::class.java).apply {
            })
        }

        btnLogout.setOnClickListener {
            val actividad = activity as? Profile

            AlertDialog.Builder(requireActivity())
                .setTitle("¿Cerrar sesión de la aplicación?")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí") { _, _ ->
                    val sharedPreferences: SharedPreferences =
                        requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()

                    editor.putString("userID", "")
                    editor.putString("tags", "")
                    editor.putString("name", "")
                    editor.putString("userImage", "")
                    editor.putBoolean("isLoggedIn", false)
                    editor.apply()

                    startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                    })

                    actividad?.finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun getUserDataFromSharedPreferences(): UserData {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)

        val userId = sharedPreferences.getString("userID", "") ?: ""
        val tags = sharedPreferences.getString("tags", "") ?: ""
        val name = sharedPreferences.getString("name", "") ?: ""
        val userImage = sharedPreferences.getString("userImage", "") ?: ""
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return UserData(userId, tags, isLoggedIn, name, userImage)
    }

}