package com.example.manapeliculas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.manapeliculas.data.User
import com.example.manapeliculas.data.UserData
import com.example.manapeliculas.data.cuevana2.LastP
import com.example.manapeliculas.databinding.ActivityEditProfileBinding
import com.example.manapeliculas.databinding.ActivityProfileBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var mDatabase: DatabaseReference? = null
    private var objectsArray = mutableListOf<LastP>()
    private var myListArray = mutableListOf<LastP>()
    private val storage = Firebase.storage
    private var deleteImage = ""
    private var userData: UserData? = null
    private var loadingDialog: AlertDialog? = null

    private val itemsList = mutableListOf(
        "Acción",
        "Animación",
        "Crimen",
        "Familiar",
        "Misterio",
        "Suspenso",
        "Aventura",
        "Ciencia Ficción",
        "Drama",
        "Fantasía",
        "Romance",
        "Terror")

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
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

        userData = getUserDataFromSharedPreferences()
        mDatabase = FirebaseDatabase.getInstance().reference

        loadData(userData!!.userId)

        val autoCompleteTextView = binding.autoCompleteTextView
        val chipGroup = binding.chipGroup

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsList)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String

            // Agregar el elemento seleccionado como un chip
            addChip(selectedItem, chipGroup)

            // Remover el elemento seleccionado de la lista
            itemsList.remove(selectedItem)
            adapter.notifyDataSetChanged()
        }

        binding.editImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.editImageText.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.updateButton.setOnClickListener {
            showLoadingDialog()
            updateUserData()
        }

        binding.changePassButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            val passwordCurrentInputEdit = binding.passwordCurrentInputEdit
            val passwordInputEdit = binding.passwordInputEdit
            val passwordConfirmInputEdit = binding.passwordConfirmInputEdit

            if (passwordInputEdit.text != null) {
                val passwordCurrent = passwordCurrentInputEdit.text
                val password = passwordInputEdit.text
                val confirmPassword = passwordConfirmInputEdit.text

                if (password.toString() == confirmPassword.toString()) {

                    if (user != null) {
                        showLoadingDialog()

                        // Crear credenciales con el email del usuario y su contraseña actual
                        val credential = EmailAuthProvider.getCredential(user.email!!,
                            passwordCurrent.toString()
                        )

                        // Reautenticar al usuario
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                user?.updatePassword(password.toString())
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // La contraseña se actualizó con éxito
                                            hideLoadingDialog()
                                            passwordCurrentInputEdit.setText("")
                                            passwordInputEdit.setText("")
                                            passwordConfirmInputEdit.setText("")
                                            Log.d("ChangePassword", "Contraseña actualizada con éxito")
                                            Toast.makeText(this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Si hay un error al actualizar la contraseña, muestra un mensaje de error
                                            val errorMessage = task.exception?.message
                                            hideLoadingDialog()
                                            Log.e("ChangePassword", "Error al actualizar la contraseña: $errorMessage")
                                            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                            .addOnFailureListener { exception ->
                                // Manejar el fallo al reautenticar al usuario
                                hideLoadingDialog()
                                Log.e("Reauthentication", "Error al reautenticar al usuario: ${exception.message}")
                                Toast.makeText(this, "Error al reautenticar al usuario", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // El usuario es nulo, manejar según sea necesario
                        Log.e("Reauthentication", "El usuario es nulo")
                        Toast.makeText(this, "Usuario nulo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("ChangePassword", "Las contraseñas no coinciden")
                    Toast.makeText(this, "Las contraseñas deben ser iguales", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.w("ChangePassword", "La contraseña es nula o vacía")
                Toast.makeText(this, "La contraseña es vacía", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun addChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(chipGroup.context)
        chip.text = text
        chip.isCloseIconVisible = true

        // Manejar el evento de cierre del chip
        chip.setOnCloseIconClickListener {
            // Eliminar el chip seleccionado
            chipGroup.removeView(chip)

            // Agregar el elemento nuevamente a la lista
            (binding.autoCompleteTextView.adapter as? ArrayAdapter<String>)?.add(text)
            Toast.makeText(this, "Chip eliminado: $text", Toast.LENGTH_SHORT).show()
        }

        // Agregar el chip al grupo
        chipGroup.addView(chip)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            showLoadingDialog()
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
                            Toast.makeText(this, "Imagen cargada y URL guardada con éxito", Toast.LENGTH_SHORT).show()

                            loadData(personKey)
                            borrarImagenEnFirebaseStorage(deleteImage)
                            val mainActivity = MainActivity.instance
                            mainActivity.loadData(personKey)
                            hideLoadingDialog()
                        }?.addOnFailureListener { exception ->
                            println(exception.message)
                            hideLoadingDialog()
                            Toast.makeText(this, "Error al guardar la URL de la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    hideLoadingDialog()
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

    private fun updateUserData() {
        val userId = userData?.userId
        val newName = binding.apodoEditText.text.toString()
        val newTags = getChipsString(binding.chipGroup)

        if (userId != null) {
            val database = Firebase.database
            val userRef = database.reference.child("users").child(userId)

            // Actualiza el nombre y las tags en la base de datos
            userRef.child("name").setValue(newName)
            userRef.child("tags").setValue(newTags)
                .addOnSuccessListener {
                    // La actualización fue exitosa
                    hideLoadingDialog()
                    Toast.makeText(this, "Datos actualizados con éxito", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Maneja errores en la actualización
                    hideLoadingDialog()
                    Toast.makeText(this, "Error al actualizar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
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

                    binding.apodoEditText.setText(user.name)
                    binding.emailEditText.setText(user.email)

                    // Procesa las tags y agrega chips
                    processTags(user.tags)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
            }
        })
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

    private fun processTags(tags: String) {
        // Elimina los datos del array
        val tagsArray = tags.split("|").toMutableList()
        itemsList.removeAll(tagsArray)

        // Agrega chips por cada tag eliminada
        val chipGroup = binding.chipGroup
        for (tag in tagsArray) {
            addChip(tag, chipGroup)
        }

        // Actualiza el adapter del autoCompleteTextView
        val autoCompleteTextView = binding.autoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsList)
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun getChipsString(chipGroup: ChipGroup): String {
        val chips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chips.add(chip.text.toString())
        }
        return chips.joinToString(separator = "|")
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