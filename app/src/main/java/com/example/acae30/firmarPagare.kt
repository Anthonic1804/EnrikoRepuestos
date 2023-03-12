package com.example.acae30

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.TextPaint
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_firmar_pagare.*
import org.w3c.dom.Text
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class firmarPagare : AppCompatActivity() {

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var btnCancelar : Button
    private lateinit var btnLimpiar : Button
    private lateinit var btnFirmar : Button
    private var idcliente : Int = 0
    private var nombreCliente : String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isAceptado ->
        if(isAceptado){
            Toast.makeText(this, "PERMISOS CONCEDIDOS", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "PERMISOS DENEGADOS", Toast.LENGTH_LONG).show()
        }
    }

    val tituloText = "PAGARÉ SIN PROTESTO"
    val textoPagare = "Por $400.00; PAGARÉ  en forma incondicional a la ordel del señor: ARMANDO ANTONIO" +
            " LOPEZ VIERA: con Documento Único de Identidad número: 01664366-2, propietario de " +
            "AGROFERRETERIA EL REY Y FORJADOS E INSERTOS EL SALVADOR, en cualquiera de sus " +
            "sucursales, en la ciudad de La Unión, la cantidad de $400 DÓLARES DE LOS " +
            "ESTADOS UNIDOS DE AMÉRICA, más el interés convencional de 5 por ciento mensual, " +
            "teniendo como fecha de vencimiento para el pago de la deuda, el día 15 de abril del " +
            " 2023; calculados a partir de la fecha de suscripción del presente documento y en " +
            "caso que no fueren cubiertos el capital más los interés a su vencimiento, pagaré además a partir de " +
            "esta última fecha, el interés moratorio del NO SE QUE VA AQUI. El tipo de interés " +
            "quedara sujeto a aumento o disminución de acuerdo a las fluctuaciones del mercado. Para los " +
            "efectos legales de esta obligación mercantil, tomamos como domicilio especial la Ciudad de La " +
            "Unión, y en caso de acción judicial renuncio al derecho de apelar del decreto de embargo, sentencia " +
            "de remate y de toda providencia apelable que se dictare en el Juicio Mercantil Ejecutivo o sus " +
            "incidentes, siendo a mi cargo cualquier gasto que hiciere el cobro de este pagaré, inclusive los " +
            "llamados personales y aun por regla general no hubiere condenación por costas procesales y " +
            "faculto a mi acreedor para que designe la persona depositaria de los bienes que se me embarguen " +
            "a quien relevo de la obligación de rendir fianza y cuenta de administración."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmar_pagare)
        btnCancelar = findViewById(R.id.btnCancelarFirma)
        btnLimpiar = findViewById(R.id.clear)
        btnFirmar = findViewById(R.id.save)
        idcliente = intent.getIntExtra("idcliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente")

        btnCancelar.setOnClickListener {
            mensajeCancelar()
        }

        btnLimpiar.setOnClickListener {
            signatureView.signatureClear()
        }

        btnFirmar.setOnClickListener {

            verificarPermisos(it)

        }

    }

    private fun verificarPermisos(view: View) {
        when{
            ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "PERMISOS CONCEDIDOS", Toast.LENGTH_LONG).show()
                generarPDF()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                WRITE_EXTERNAL_STORAGE
            ) -> {
                Snackbar.make(view, "ESTE PERMISO ES NECESARIO PARA CREAR EL ARCHIVO", Snackbar.LENGTH_INDEFINITE).setAction("Ok"){
                    requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
                }.show()
            }

            else -> {
                requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    fun generarPDF() {
        try {
            val carpeta = "/archivospdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, "Pagare.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document()
            PdfWriter.getInstance(documento, fos)

            documento.open()

            //AGREGANDO EL TITULO AL PAGARE
            val titulo = Paragraph(
                "$tituloText\n\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            documento.add(titulo)

            //AGREGANDO EL CONTENIDO AL PAGARE
            val descripcion = Paragraph(
                textoPagare,
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            documento.add(descripcion)

            //AGREGANDO EL PIE AL PAGARE + LA FIRMA DEL CLIENTE

            documento.close()

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }

    fun atras(){
        val intent = Intent(this, verPagare::class.java)
        intent.putExtra("idcliente", idcliente)
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
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }
}