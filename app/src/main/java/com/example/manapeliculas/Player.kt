package com.example.manapeliculas

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class Player : AppCompatActivity() {

    companion object {
        private var sRuntime: GeckoRuntime? = null

        fun getGeckoRuntime(context: Context): GeckoRuntime {
            if (sRuntime == null) {
                // Configurar el GeckoRuntime con la extensión
                val extensionPath = context.filesDir.absolutePath + "/ublock_origin.xpi"
                val settingsBundle = Bundle().apply {
                    putString("extensions", mapOf("uBlockOrigin@uBlockOrigin" to extensionPath).toString())
                }
                val settings = GeckoRuntimeSettings.Builder()
                    .extras(settingsBundle)
                    .build()
                sRuntime = GeckoRuntime.create(context, settings)
            }
            return sRuntime!!
        }
    }

    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar el modo oscuro en GeckoView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        setContentView(R.layout.activity_player)

        val intent = intent

        if (intent.hasExtra("result")) {
            val result = intent.getStringExtra("result")

            geckoView = findViewById(R.id.geckoview)
            geckoSession = GeckoSession()

            // Workaround for Bug 1758212
            geckoSession?.contentDelegate = object : GeckoSession.ContentDelegate {}

            val runtime = getGeckoRuntime(this)
            geckoSession?.open(runtime)
            geckoView?.setSession(geckoSession!!)

            if (result != null) {
                geckoSession?.loadUri(result)
            }
        }
    }

    override fun onDestroy() {
        // Cerrar la sesión y liberar recursos al destruir la actividad
        geckoSession?.close()
        super.onDestroy()
    }
}