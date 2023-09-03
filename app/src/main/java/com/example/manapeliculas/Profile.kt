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

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "Inicio de sesión exitoso", Snackbar.LENGTH_LONG).show()

        userData = getUserDataFromSharedPreferences()
        mDatabase = FirebaseDatabase.getInstance().reference

        loadData(userData!!.userId)

        binding.dropdownMenu.setOnClickListener {
            val bottomSheetDialogFragment = MyBottomSheetDialogFragment()
            bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
            // Abre la galería para seleccionar una imagen
            //val intent = Intent(Intent.ACTION_GET_CONTENT)
            //intent.type = "image/*"
            //startActivityForResult(intent, 1)
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java).apply {
        })
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            // Obtiene la Uri de la imagen seleccionada
            val imageUri: Uri = data.data!!

            // Genera un nombre único para la imagen
            val uniqueFileName = "${System.currentTimeMillis()}_${imageUri.lastPathSegment}"

            // Crea una referencia al archivo en Firebase Storage
            val storageRef = storage.reference.child("images/$uniqueFileName")

            // Sube la imagen a Firebase Storage
            storageRef.putFile(imageUri)
                .addOnSuccessListener { uploadTask ->
                    // La carga se completó con éxito
                    // Puedes obtener la URL de descarga si es necesario
                    uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()

                        // Guarda la URL de la imagen en la base de datos en tiempo real
                        // Supongamos que tienes una clave única de la persona, como "personKey"
                        val personKey =
                            userData?.userId // Reemplaza con la clave real de la persona

                        val database = Firebase.database
                        val personRef =
                            personKey?.let { database.reference.child("users")?.child(it) }

                        personRef?.child("userImage")?.setValue(downloadUrl)?.addOnSuccessListener {
                            // La URL de la imagen se guardó con éxito en la base de datos
                            // Puedes realizar acciones adicionales aquí si es necesario
                            Toast.makeText(this, "Imagen cargada y URL guardada con éxito", Toast.LENGTH_SHORT).show()

                            loadData(personKey)
                            borrarImagenEnFirebaseStorage(deleteImage)
                            val mainActivity = MainActivity.instance
                            mainActivity.loadData(personKey)
                        }?.addOnFailureListener { exception ->
                            // Maneja errores en la escritura de datos
                            // Por ejemplo, muestra un mensaje de error
                            Toast.makeText(this, "Error al guardar la URL de la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Maneja errores en la carga
                    // Por ejemplo, muestra un mensaje de error
                    Toast.makeText(this, "Error al cargar la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun borrarImagenEnFirebaseStorage(downloadUrl: String) {
        // Crea una referencia al archivo en Firebase Storage utilizando la URL
        val storageRef = Firebase.storage.getReferenceFromUrl(downloadUrl)

        // Borra el archivo
        storageRef.delete()
            .addOnSuccessListener {
                // El archivo se borró con éxito
                // Puedes realizar acciones adicionales aquí si es necesario
                Log.d("BORRAR_IMAGEN", "Imagen borrada con éxito")
            }
            .addOnFailureListener { exception ->
                // Maneja errores en la eliminación del archivo
                // Por ejemplo, muestra un mensaje de error
                Log.e("BORRAR_IMAGEN", "Error al borrar la imagen: ${exception.message}")
            }
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