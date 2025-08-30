package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Mensaje “flash” opcional (cuando volvemos del registro/perfil)
        intent.getStringExtra("flash")?.let { toast(it) }

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass  = b.etPass.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                toast("Completá email y contraseña"); return@setOnClickListener
            }

            Firebase.auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    val u = Firebase.auth.currentUser
                    if (u?.isEmailVerified == true) {
                        goToMenu()
                    } else {
                        toast("Tu email no está verificado. Revisá tu correo y volvé a iniciar sesión.")
                        Firebase.auth.signOut()
                    }
                }
                .addOnFailureListener { toast(it.message ?: "Error al iniciar sesión") }
        }

        b.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Auto-skip: si ya hay sesión y está verificada, saltar Login
        val u = Firebase.auth.currentUser
        if (u?.isEmailVerified == true) goToMenu()
    }

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
