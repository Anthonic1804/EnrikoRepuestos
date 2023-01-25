package com.example.acae30

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.CuentaAdapter
import com.example.acae30.modelos.Cuenta
import kotlinx.coroutines.launch

class CuentasDetalle : AppCompatActivity() {
    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var txtnombre: TextView? = null
    private var txttotal: TextView? = null
    private var lienzo: ConstraintLayout? = null
    private var btnatras: ImageButton? = null
    private var lista: RecyclerView? = null
    private var totalcuenta = 0.toFloat()
    private var idcliente = 0
    private var nombrecliente = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuentas_detalle)
        supportActionBar?.hide()
        bd = Database(this)
        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente").toString()
        funciones = Funciones()
        txtnombre = findViewById(R.id.txtcliente)
        txttotal = findViewById(R.id.txttotal)
        lienzo = findViewById(R.id.lienzo)
        btnatras = findViewById(R.id.imgatras)
        lista = findViewById(R.id.lista)
    }

    override fun onStart() {
        super.onStart()
        txtnombre!!.text = nombrecliente
        btnatras!!.setOnClickListener {
            Regresar()
            finish()
        }
        //GlobalScope.launch(Dispatchers.IO) {
        this@CuentasDetalle.lifecycleScope.launch {
            try {
                if (idcliente > 0) {
                    val data = getCuentas(idcliente)
                    if (data.size > 0) {
                        list(data)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CuentasDetalle, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Regresar()
    }

    private fun Regresar() {
        val intento = Intent(this, Cuentas_list::class.java)
        startActivity(intento)
        finish()
    }

    private fun getCuentas(idcliente: Int): ArrayList<Cuenta> {
        val base = bd!!.writableDatabase
        try {
            var lista = ArrayList<Cuenta>()
            val cursor = base!!.rawQuery("SELECT * FROM cuentas where Id_cliente=$idcliente AND status LIKE '%PENDIENTE%'", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val cuenta = Cuenta(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getFloat(5),
                        cursor.getFloat(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getString(9),
                        cursor.getFloat(10),
                        cursor.getString(11),
                        cursor.getFloat(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getString(15)
                    )
                    if (cuenta != null) {
                        lista.add(cuenta)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            } //evalua si hayo datos
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }

    }

    private fun list(list: ArrayList<Cuenta>) {
        try {

            if (list.size > 0) {
                var mLayoutManager =
                    LinearLayoutManager(this@CuentasDetalle, LinearLayoutManager.VERTICAL, false)
                lista!!.layoutManager = mLayoutManager
                var t = 0.toFloat()
                for (i in 0 until list.size) {
                    var data = list.get(i)
                    t = t + data.Saldo_actual!!
                }
                txttotal!!.text = "$ " + String.format("%.4f", t)
                val adapter = CuentaAdapter(list, this@CuentasDetalle)
                lista!!.adapter = adapter

            }
        } catch (e: Exception) {

            throw Exception(e.message)
        }
    }
}