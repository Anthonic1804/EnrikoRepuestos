package com.example.acae30

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class verPagare : AppCompatActivity() {

    private lateinit var textPagare : EditText
    private lateinit var tvFecha : TextView
    private lateinit var btnCancelar : Button
    private lateinit var btnFirmar : Button
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private var idcliente : Int = 0
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_pagare)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente").toString()
        direccionCliente = intent.getStringExtra("direccionCliente").toString()
        duiCliente = intent.getStringExtra("duiCliente").toString()

        textPagare = findViewById(R.id.textoPagare)
        tvFecha = findViewById(R.id.tvFecha)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnFirmar = findViewById(R.id.btnFirmar)

        tvFecha.text = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

        textPagare.isEnabled = false
        textPagare.setText("Por _______; PAGARÉ  en forma incondicional a la ordel del señor: ARMANDO ANTONIO" +
                " LOPEZ VIERA: con Documento Único de Identidad número: 01664366-2, propietario de " +
                "AGROFERRETERIA EL REY Y FORJADOS E INSERTOS EL SALVADOR, en cualquiera de sus " +
                "sucursales, en la ciudad de La Unión, la cantidad de _____ DÓLARES DE LOS " +
                "ESTADOS UNIDOS DE AMÉRICA, más el interés convencional de _____ por ciento mensual, " +
                "teniendo como fecha de vencimiento para el pago de la deuda, el día ____ de ______ del " +
                " _____; calculados a partir de la fecha de suscripción del presente documento y en " +
                "caso que no fueren cubiertos el capital más los interés a su vencimiento, pagaré además a partir de " +
                "esta última fecha, el interés moratorio del ________________. El tipo de interés " +
                "quedara sujeto a aumento o disminución de acuerdo a las fluctuaciones del mercado. Para los " +
                "efectos legales de esta obligación mercantil, tomamos como domicilio especial la Ciudad de La " +
                "Unión, y en caso de acción judicial renuncio al derecho de apelar del decreto de embargo, sentencia " +
                "de remate y de toda providencia apelable que se dictare en el Juicio Mercantil Ejecutivo o sus " +
                "incidentes, siendo a mi cargo cualquier gasto que hiciere el cobro de este pagaré, inclusive los " +
                "llamados personales y aun por regla general no hubiere condenación por costas procesales y " +
                "faculto a mi acreedor para que designe la persona depositaria de los bienes que se me embarguen " +
                "a quien relevo de la obligación de rendir fianza y cuenta de administración.")


        btnCancelar.setOnClickListener {
            mensajeCancelar()
        }

        btnFirmar.setOnClickListener {
            //Toast.makeText(this, "FUNCION EN DESARROLLO", Toast.LENGTH_LONG).show()
            firmarPagareVista()
        }

    }

    fun atras(){
        val intent = Intent(this, ClientesDetalle::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
    }

    fun firmarPagareVista(){
        val intent = Intent(this, firmarPagare::class.java)
        intent.putExtra("idcliente", idcliente)
        intent.putExtra("nombreCliente", nombreCliente)
        intent.putExtra("direccionCliente", direccionCliente)
        intent.putExtra("duiCliente", duiCliente)
        startActivity(intent)
        finish()
    }

    //FUNCION PARA CREAR EL DIALOG DE ACTUALIZAR APP
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