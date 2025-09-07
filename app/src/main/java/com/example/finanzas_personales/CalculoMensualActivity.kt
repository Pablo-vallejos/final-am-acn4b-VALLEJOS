package com.example.finanzas_personales

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finanzas_personales.databinding.ActivityCalculoMensualBinding
import kotlin.math.max

class CalculoMensualActivity : AppCompatActivity() {

    private lateinit var b: ActivityCalculoMensualBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCalculoMensualBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnCalcular.setOnClickListener { calcular() }
        b.btnLimpiar.setOnClickListener {
            b.etPersonas.setText("")
            b.etPorcentaje.setText("")
            b.etGastos.setText("")
            b.etIngresos.setText("")
            b.tvTotal.text = "—"
        }
    }

    private fun calcular() {
        val personas = b.etPersonas.text.toString().toIntOrNull()
        val porcentaje = b.etPorcentaje.text.toString().replace(",", ".").toDoubleOrNull()
        val gastos = b.etGastos.text.toString().replace(",", ".").toDoubleOrNull()
        val ingresos = b.etIngresos.text.toString().replace(",", ".").toDoubleOrNull()

        if (personas == null || personas <= 0) { toast("Ingresá cantidad de personas (>0)"); return }
        if (porcentaje == null || porcentaje < 0) { toast("Ingresá el porcentaje a dividir"); return }
        if (gastos == null || gastos < 0) { toast("Ingresá los gastos"); return }
        if (ingresos == null || ingresos < 0) { toast("Ingresá los ingresos"); return }

        // Lógica:
        // 1) Neto a cubrir = max(gastos - ingresos, 0)
        // 2) Monto a dividir = neto * (porcentaje / 100)
        // 3) Total por persona = monto / personas
        val neto = max(gastos - ingresos, 0.0)
        val montoADividir = neto * (porcentaje / 100.0)
        val porPersona = if (personas > 0) montoADividir / personas else 0.0

        b.tvTotal.text = String.format("%.2f", porPersona)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
