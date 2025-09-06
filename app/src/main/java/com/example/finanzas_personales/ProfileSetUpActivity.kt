package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.ktx.auth
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

        // Cargar datos previamente guardados y rellenar UI
        loadProfile()

        // Acciones
        b.btnSave.setOnClickListener { saveProfile() }
        b.btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun loadProfile() {
        val uid = Firebase.auth.uid ?: return
        val db = Firebase.firestore

        b.btnSave.isEnabled = false
        b.btnSave.text = "Cargando…"

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val first = doc.getString("firstName").orEmpty()
                    val last  = doc.getString("lastName").orEmpty()
                    val curr  = doc.getString("currency").orEmpty()

                    b.etFirstName.setText(first)
                    b.etLastName.setText(last)
                    selectSpinner(b.spCurrency, curr)
                } else {
                    Log.d("PROFILE", "No hay documento previo para $uid")
                }
            }
            .addOnFailureListener { e ->
                toast("No pude cargar el perfil: ${e.message}")
                Log.e("PROFILE", "loadProfile error", e)
            }
            .addOnCompleteListener {
                b.btnSave.isEnabled = true
                b.btnSave.text = "Guardar"
            }
    }

    private fun saveProfile() {
        val first = b.etFirstName.text.toString().trim()
        val last  = b.etLastName.text.toString().trim()
        val currency = b.spCurrency.selectedItem?.toString() ?: "ARS"

        if (first.isEmpty()) { toast("Ingresá tu nombre"); return }
        if (last.isEmpty())  { toast("Ingresá tu apellido"); return }

        val uid = Firebase.auth.uid ?: run { toast("Sesión inválida"); return }
        val db = Firebase.firestore
        val data = mapOf(
            "firstName" to first,
            "lastName"  to last,
            "currency"  to currency,
            "email"     to (Firebase.auth.currentUser?.email ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        b.btnSave.isEnabled = false
        b.btnSave.text = "Guardando…"

        db.collection("users").document(uid)
            .set(data) // merge no imprescindible si siempre escribís todo
            .addOnSuccessListener {
                toast("Perfil guardado")
                startActivity(Intent(this, MenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener { toast(it.message ?: "No se pudo guardar el perfil") }
            .addOnCompleteListener {
                b.btnSave.isEnabled = true
                b.btnSave.text = "Guardar"
            }
    }

    private fun selectSpinner(sp: Spinner, value: String) {
        if (value.isBlank()) return
        val adapter = sp.adapter as? ArrayAdapter<*> ?: return
        val pos = (0 until adapter.count).firstOrNull { adapter.getItem(it)?.toString() == value }
        if (pos != null) sp.setSelection(pos)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
