package com.example.finanzas_personales

import android.content.Intent
import android.os.Bundle
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

        // Guardas de seguridad: exigir sesión + email verificado
        val u = Firebase.auth.currentUser
        if (u == null || !u.isEmailVerified) {
            goToLogin()
            return
        }

        // Clicks de menú (por ahora solo Toast; reemplazá por startActivity)
        b.btnIngresos.setOnClickListener { toast("Abrir Ingresos") /* startActivity(Intent(this, IngresosActivity::class.java)) */ }
        b.btnGastos.setOnClickListener { toast("Abrir Gastos") /* startActivity(Intent(this, GastosActivity::class.java)) */ }
        b.btnMovimientos.setOnClickListener { toast("Abrir Movimientos") /* startActivity(Intent(this, MovimientosActivity::class.java)) */ }
        b.btnReportes.setOnClickListener { toast("Abrir Reportes") /* startActivity(Intent(this, ReportesActivity::class.java)) */ }
        b.btnConfiguracion.setOnClickListener { toast("Abrir Configuración") /* startActivity(Intent(this, SettingsActivity::class.java)) */ }

        b.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            goToLogin(clearStack = true)
        }
    }

    private fun goToLogin(clearStack: Boolean = false) {
        val i = Intent(this, LoginActivity::class.java)
        if (clearStack) {
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(i)
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
