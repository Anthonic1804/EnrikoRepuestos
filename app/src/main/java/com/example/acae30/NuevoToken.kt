package com.example.acae30

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

class NuevoToken : AppCompatActivity() {

    private lateinit var btnAtras : Button
    private lateinit var btnBuscarProducto : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var edtProducto : EditText
    private lateinit var edtPrecio : EditText
    private lateinit var edtReferencia : EditText
    var codigoProducto : String? = ""
    var nombreProducto : String? = ""
    var precioProducto : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_token)
        val intento = intent
        btnAtras = findViewById(R.id.btnatras_token)
        btnBuscarProducto = findViewById(R.id.btnProductoToken)
        edtProducto = findViewById(R.id.edtProducto)
        edtPrecio = findViewById(R.id.edtPrecio)
        edtReferencia = findViewById(R.id.edtReferencia)
        codigoProducto = intento.getStringExtra("codigo")
        nombreProducto = intento.getStringExtra("producto")
        precioProducto = intento.getFloatExtra("precio", 0f).toString()

        edtProducto.isEnabled = false
        edtReferencia.isEnabled = false

        if(codigoProducto != ""){
            edtProducto.setText(nombreProducto)
            edtReferencia.setText(codigoProducto)
            edtPrecio.setText(precioProducto)
        }

        btnBuscarProducto.setOnClickListener {
            val intent = Intent(this@NuevoToken, Inventario::class.java)
            intent.putExtra("tokenBusqueda", true)
            startActivity(intent)
            finish()
        }

        btnAtras.setOnClickListener {
            mensajeCancelar()
        }
    }

    private fun atras(){
        val intent = Intent(this@NuevoToken, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    fun mensajeCancelar(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            atras()
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }
}