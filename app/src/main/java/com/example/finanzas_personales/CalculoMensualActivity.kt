package com.example.finanzas_personales

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finanzas_personales.databinding.ActivityCalculoMensualBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

class CalculoMensualActivity : AppCompatActivity() {

    private lateinit var b: ActivityCalculoMensualBinding
    private val percentEdits = mutableListOf<EditText>()
    private val EPS = 0.01

    private var currencyCode   = "ARS"
    private var currencySymbol = "$"
    private val moneyFmt by lazy {
        DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("es", "AR")).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCalculoMensualBinding.inflate(layoutInflater)
        setContentView(b.root)

        loadCurrency()

        b.btnGenerarPorcentajes.setOnClickListener { generarCampos() }
        b.btnCalcular.setOnClickListener { calcular() }
        b.btnLimpiar.setOnClickListener { limpiar() }
        b.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    // ---------------- Moneda ----------------
    private fun loadCurrency() {
        val uid = Firebase.auth.uid ?: run { applyCurrencyUI(); return }
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                currencyCode   = doc.getString("currency") ?: "ARS"
                currencySymbol = symbolFor(currencyCode)
                applyCurrencyUI()
            }
            .addOnFailureListener { applyCurrencyUI() }
    }

    private fun applyCurrencyUI() {
        runCatching { b.tvCurrency.text = "Moneda: $currencyCode ($currencySymbol)" }
        b.etGastos.hint   = "Gastos ($currencySymbol)"
        b.etIngresos.hint = "Ingresos ($currencySymbol)"
        runCatching { b.tvLabelTotal.text = "Total por persona ($currencySymbol)" }
    }

    private fun symbolFor(code: String): String = try {
        java.util.Currency.getInstance(code).getSymbol(Locale("es", "AR"))
    } catch (_: Exception) { code }

    // ------------- Porcentajes / Cálculo -------------
    private fun generarCampos() {
        val n = b.etPersonas.text.toString().toIntOrNull()
        if (n == null || n <= 0) { toast("Ingresá cantidad de personas mayor a 0"); return }
        if (n > 30) { toast("Máximo 30 personas"); return }

        percentEdits.clear()
        b.containerPorcentajes.removeAllViews()

        val base = 100.0 / n
        var acum = 0.0
        repeat(n) { idx ->
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { if (idx > 0) topMargin = 8 }
                hint = "Persona ${idx + 1} (%)"
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) = updateSumHint()
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            val valor = if (idx < n - 1) {
                val r = redondear2(base); acum += r; r
            } else {
                redondear2(100.0 - acum)
            }
            et.setText("%.2f".format(valor))

            percentEdits += et
            b.containerPorcentajes.addView(et)
        }
        updateSumHint()
    }

    private fun calcular() {
        val personas = b.etPersonas.text.toString().toIntOrNull()
        val gastos   = safeDouble(b.etGastos.text)
        val ingresos = safeDouble(b.etIngresos.text)

        if (personas == null || personas <= 0) { toast("Ingresá cantidad de personas mayor a 0"); return }
        if (gastos == null || gastos < 0)       { toast("Ingresá los gastos"); return }
        if (ingresos == null || ingresos < 0)   { toast("Ingresá los ingresos"); return }
        if (percentEdits.isEmpty())             { toast("Generá los porcentajes primero"); return }
        if (percentEdits.size != personas)      { toast("Volvé a generar los porcentajes"); return }

        val porcentajes = leerPorcentajes() ?: return
        val suma = porcentajes.sum()

        // Solo calcula si suma == 100
        if (abs(suma - 100.0) > EPS) {
            b.tvTotal.text = "—"
            b.tvHintSum.text = "La suma de porcentajes debe ser exactamente 100% • Actual: %.2f%%".format(suma)
            b.tvHintSum.setTextColor(color(android.R.color.holo_red_dark))
            // señalá un campo para que el usuario corrija
            percentEdits.firstOrNull()?.apply {
                error = if (suma > 100) "Te pasaste: reducí porcentajes" else "Falta porcentaje hasta 100"
                requestFocus()
            }
            return
        }

        val totalBase = max(ingresos - gastos, 0.0)
        val sb = StringBuilder()
        porcentajes.forEachIndexed { i, p ->
            val monto = totalBase * (p / 100.0)
            sb.append("Persona ${i + 1}: ")
                .append(currencySymbol).append(" ")
                .append(moneyFmt.format(monto))
            if (i != porcentajes.lastIndex) sb.append("\n")
        }
        b.tvTotal.text = sb.toString()
    }

    private fun leerPorcentajes(): List<Double>? {
        val list = mutableListOf<Double>()
        percentEdits.forEachIndexed { idx, et ->
            val v = safeDouble(et.text)
            if (v == null) {
                et.error = "Valor inválido"; et.requestFocus()
                toast("Porcentaje inválido en Persona ${idx + 1}")
                return null
            }
            if (v < 0.0) {
                et.error = "No puede ser negativo"; et.requestFocus()
                toast("El porcentaje de Persona ${idx + 1} debe ser ≥ 0")
                return null
            }
            list += v
        }
        return list
    }

    private fun updateSumHint() {
        val sum = percentEdits.sumOf { safeDouble(it.text) ?: 0.0 }
        val sumIs100 = abs(sum - 100.0) <= EPS

        // Calcular solo si suma == 100
        b.btnCalcular.isEnabled = sumIs100

        when {
            sum > 100.0 + EPS -> {
                b.tvTotal.text = "—"
                b.tvHintSum.text = "La suma de porcentajes debe ser exactamente 100% • Actual: %.2f%%".format(sum)
                b.tvHintSum.setTextColor(color(android.R.color.holo_red_dark))
            }
            sumIs100 -> {
                b.tvHintSum.text = "Suma actual: 100.00% (OK)"
                b.tvHintSum.setTextColor(color(android.R.color.holo_green_dark))
            }
            else -> {
                val falta = 100.0 - sum
                b.tvHintSum.text = "Suma actual: %.2f%% • Falta: %.2f%%".format(sum, falta)
                b.tvHintSum.setTextColor(color(android.R.color.holo_orange_dark))
            }
        }
    }

    private fun limpiar() {
        b.etPersonas.setText("")
        b.etGastos.setText("")
        b.etIngresos.setText("")
        b.tvTotal.text = "—"
        percentEdits.clear()
        b.containerPorcentajes.removeAllViews()
        b.tvHintSum.text = getString(R.string.hint_sumar_100)
        b.tvHintSum.setTextColor(color(android.R.color.secondary_text_dark))
    }

    // ---------------- Helpers ----------------
    private fun safeDouble(txt: CharSequence?): Double? {
        if (txt == null) return null
        val s = txt.toString().trim()
            .replace(" ", "")
            .replace(",", ".")
        if (s.isEmpty() || s == "." || s == "-" || s == "-.") return null
        return s.toDoubleOrNull()
    }

    private fun color(res: Int) = ContextCompat.getColor(this, res)
    private fun redondear2(x: Double) = round(x * 100.0) / 100.0
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
