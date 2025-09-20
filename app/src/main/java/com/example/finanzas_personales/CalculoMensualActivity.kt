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
        val personas   = b.etPersonas.text.toString().toIntOrNull()
        val porcentaje = b.etPorcentaje.text.toString().replace(",", ".").toDoubleOrNull()
        val gastos     = b.etGastos.text.toString().replace(",", ".").toDoubleOrNull()
        val ingresos   = b.etIngresos.text.toString().replace(",", ".").toDoubleOrNull()

        // Validaciones
        if (personas == null || personas <= 0) { toast("Ingresá cantidad de personas (>0)"); return }
        if (porcentaje == null || porcentaje < 0) { toast("Ingresá el porcentaje"); return }
        if (gastos == null || gastos < 0)         { toast("Ingresá los gastos"); return }
        if (ingresos == null || ingresos < 0)     { toast("Ingresá los ingresos"); return }

        // Total base a repartir: neto (ingresos - gastos). Si queda negativo, 0.
        // Si querés usar SOLO ingresos, reemplazá la línea de abajo por: val totalBase = ingresos
        val totalBase = max(ingresos - gastos, 0.0)

        val p = porcentaje.coerceIn(0.0, 100.0)

        if (personas == 1) {
            b.tvTotal.text = "Persona 1: ${"%.2f".format(totalBase)}"
            return
        }

        // Persona 1 recibe p%; el resto (100-p)% se reparte entre (personas-1)
        val persona1 = totalBase * (p / 100.0)
        val resto = totalBase - persona1
        val otros = personas - 1
        val cadaUnoResto = if (otros > 0) resto / otros else 0.0

        val sb = StringBuilder()
        sb.append("Persona 1: ").append("%.2f".format(persona1))
        for (i in 2..personas) {
            sb.append("\nPersona ").append(i).append(": ").append("%.2f".format(cadaUnoResto))
        }
        b.tvTotal.text = sb.toString()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
