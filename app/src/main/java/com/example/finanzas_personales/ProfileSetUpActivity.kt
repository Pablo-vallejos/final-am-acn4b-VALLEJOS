package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si no hay sesión, ir al Login
        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish(); return
        }

        b = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Spinner de monedas
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.currencies, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        b.spCurrency.adapter = adapter

        // Acciones
        b.btnSave.setOnClickListener { saveProfile() }
        b.btnBack.setOnClickListener {
            // Volver al Login limpiando el back stack
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun saveProfile() {
        val first = b.etFirstName.text.toString().trim()
        val last  = b.etLastName.text.toString().trim()
        val currency = b.spCurrency.selectedItem?.toString() ?: "ARS"

        if (first.isEmpty()) { toast("Ingresá tu nombre"); return }
        if (last.isEmpty())  { toast("Ingresá tu apellido"); return }

        val uid = Firebase.auth.uid ?: run { toast("Sesión inválida"); return }
        val data = mapOf(
            "firstName" to first,
            "lastName" to last,
            "currency" to currency,
            "email" to (Firebase.auth.currentUser?.email ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        Firebase.firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Enviar verificación y volver al Login
                Firebase.auth.currentUser?.sendEmailVerification()
                Firebase.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java).putExtra(
                    "flash",
                    "Perfil guardado. Te enviamos un email para verificar tu cuenta. Luego iniciá sesión."
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener { toast(it.message ?: "No se pudo guardar el perfil") }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
