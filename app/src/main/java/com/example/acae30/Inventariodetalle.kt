package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.InventarioController
import com.example.acae30.databinding.ActivityInventariodetalleBinding
import com.example.acae30.listas.InventarioDetalleAdapter
import com.example.acae30.modelos.InventarioPrecios
import kotlinx.coroutines.launch

class Inventariodetalle : AppCompatActivity() {

    private lateinit var binding: ActivityInventariodetalleBinding
    private var idinventario = 0

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    private var inventarioController = InventarioController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventariodetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idinventario = preferences.getInt("idProducto", 0)

        binding.imageView7.setOnClickListener {
            AlertaPrecio(this@Inventariodetalle)  //muestra la alerta
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {

            val editor = preferences.edit()
            editor.remove("idProducto")
            editor.apply()

            val intento = Intent(this, com.example.acae30.Inventario::class.java)
            startActivity(intento)
            finish()
        }//BOTON ATRAS

        cargarInformacionProducto()
        cargarEscalas()
    }

    private fun cargarInformacionProducto(){
        this@Inventariodetalle.lifecycleScope.launch {
            try {
                val producto = inventarioController.obtenerInformacionProductoPorId(this@Inventariodetalle ,idinventario, false)
                with(binding){
                    txtcodigo.text = producto!!.Codigo
                    txtdescripcion.text = producto.descripcion
                    txtprecio.text = "$" + String.format("%.4f", producto.Precio_iva)
                    txtexistencia.text = producto.Existencia!!.toInt().toString() + " UNI"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventariodetalle, "ERROR AL CARGAR EL DETALLE DEL PRODUCTO", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun cargarEscalas(){
        this@Inventariodetalle.lifecycleScope.launch {
            try{
                val lista = inventarioController.obtenerEscalaPrecios(this@Inventariodetalle, idinventario, false)
                if(lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this@Inventariodetalle, "ERROR AL CARGAR LAS ESCALAS -> ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun ArmarLista(lista: ArrayList<InventarioPrecios>) {

        val mLayoutManager = LinearLayoutManager(
            this@Inventariodetalle,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaprecios.layoutManager = mLayoutManager
        val adapter = InventarioDetalleAdapter(lista, this@Inventariodetalle)
        binding.listaprecios.adapter = adapter

    }
    private fun AlertaPrecio(contexto: com.example.acae30.Inventariodetalle) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_costo)
        val costoProducto = dialogo.findViewById<TextView>(R.id.txtcosto)

        val producto = inventarioController.obtenerInformacionProductoPorId(this@Inventariodetalle ,idinventario, false)

        costoProducto!!.text = String.format("%.4f", producto!!.costo_iva)

        dialogo.show()

    } //muestra la alerta para MOSTRAR EL COSTO

}