package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityMenuBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {

    private lateinit var b: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ Solo chequeo de sesión (sin isEmailVerified)
        if (Firebase.auth.currentUser == null) {
            goToLogin(); return
        }

        // Ocultos por ahora
        b.btnIngresos.visibility = View.GONE
        b.btnGastos.visibility = View.GONE
        b.btnReportes.visibility = View.GONE

        // Activos
        b.btnMovimientos.setOnClickListener {
            startActivity(Intent(this, CalculoMensualActivity::class.java))
        }
        b.btnConfiguracion.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
        b.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            goToLogin(clearStack = true)
        }
    }

    private fun goToLogin(clearStack: Boolean = false) {
        val i = Intent(this, LoginActivity::class.java)
        if (clearStack) i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
