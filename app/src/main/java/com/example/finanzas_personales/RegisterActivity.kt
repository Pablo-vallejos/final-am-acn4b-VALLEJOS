package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityRegisterBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnRegister.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val p1 = b.etPass.text.toString()
            val p2 = b.etPass2.text.toString()

            if (email.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
                toast("Completá todos los campos"); return@setOnClickListener
            }
            if (p1 != p2) {
                toast("Las contraseñas no coinciden"); return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, p1)
                .addOnSuccessListener {
                    // Pasar a completar datos del perfil
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finish()
                }
                .addOnFailureListener { toast(it.message ?: "No se pudo crear la cuenta") }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
