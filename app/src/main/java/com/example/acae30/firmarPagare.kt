package com.example.acae30

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_firmar_pagare.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class firmarPagare : AppCompatActivity() {

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvMsj : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var btnCancelar : Button
    private lateinit var btnLimpiar : Button
    private lateinit var btnFirmar : Button

    private var idcliente : Int = 0
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""
    private var limiteCredito : Float = 0f
    private var porcentaje : Float = 0f
    private var plazo : Long = 0
    private var textoPagare : String = ""

    private lateinit var imagenBitmap: Bitmap
    private lateinit var imageFinal : ByteArray
    private var imageFirmada : Boolean = false


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

    private val tituloText = "PAGARÉ SIN PROTESTO"
    val fechaPagare = "La Unión, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    var fechaDoc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmar_pagare)
        btnCancelar = findViewById(R.id.btnCancelarFirma)
        btnLimpiar = findViewById(R.id.clear)
        btnFirmar = findViewById(R.id.save)
        idcliente = intent.getIntExtra("idcliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente").toString()
        direccionCliente = intent.getStringExtra("direccionCliente").toString()
        duiCliente = intent.getStringExtra("duiCliente").toString()
        limiteCredito = intent.getFloatExtra("limiteCredito", 0f)
        plazo = intent.getLongExtra("plazoCredito", 0)

        btnCancelar.setOnClickListener {
            mensaje("Cancelar")
        }

        btnLimpiar.setOnClickListener {
            signatureView.clearCanvas()
        }

        //CALCULANDO LA FECHA DE VENCIMIENTO DE ACUERDO AL PLAZO DADO EN EL CREDITO.
        //val fechaVencimiento = LocalDate.now().plusDays(plazo).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

        //ASIGNADO PORCENTAJES DE INTERES SEGUN TABLA PROPORCIONADA POR EL CLIENTE
        porcentaje = when(limiteCredito){
            in 1.00..4380.00 -> 6.9f
            in 4381.00..8760.00 -> 4.5f
            in 8761.00..14965.00 -> 3.0f
            else -> 2.1f
        }

        textoPagare = "Por $ ${String.format("%.2f", limiteCredito)}; PAGARÉ  en forma incondicional a la ordel del señor: ARMANDO ANTONIO" +
                " LOPEZ VIERA: con Documento Único de Identidad número: 01664366-2, propietario de " +
                "AGROFERRETERIA EL REY Y FORJADOS E INSERTOS EL SALVADOR, en cualquiera de sus " +
                "sucursales, en la ciudad de La Unión, la cantidad de $ ${String.format("%.2f", limiteCredito)} DÓLARES DE LOS " +
                "ESTADOS UNIDOS DE AMÉRICA, más el interés convencional del $porcentaje por ciento mensual, " +
                "teniendo como fecha de vencimiento para el pago de la deuda, el día el día ____ de ______ del " +
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
                "a quien relevo de la obligación de rendir fianza y cuenta de administración."


        btnFirmar.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            imageFirmada = signatureView.isBitmapEmpty
            if(imageFirmada){
                Toast.makeText(this, "POR FAVOR INGRESE SU FIRMA", Toast.LENGTH_LONG).show()
            }else{
                imagenBitmap = signatureView.signatureBitmap
                imageFinal = bitmapToByteArray(imagenBitmap)
                verificarPermisos(it)
            }
        }

    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun verificarPermisos(view: View) {
        when{
            ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {

                //Toast.makeText(this, "PERMISOS CONCEDIDOS", Toast.LENGTH_LONG).show()

                generarPDF(nombreCliente, direccionCliente, duiCliente)
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

    private fun generarPDF(nombreCliente : String, direccionCliente : String, duiCliente : String) {
        try {
            val carpeta = "/archivospdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, nombreCliente + "_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document()
            PdfWriter.getInstance(documento, fos)

            documento.open()
            documento.pageSize = PageSize.LETTER

            //AGREGANDO EL TITULO AL PAGARE
            val titulo = Paragraph(
                "\n\n$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            titulo.alignment = Element.ALIGN_CENTER
            documento.add(titulo)

            //AGREGANDO LA FECHA DEL DOCUMENTO
            val fechaDocumento = Paragraph(
                "$fechaPagare\n\n",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //AGREGANDO EL CONTENIDO AL PAGARE
            val descripcion = Paragraph(
                textoPagare,
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            descripcion.alignment = Element.ALIGN_JUSTIFIED
            documento.add(descripcion)

            //AGREGANDO EL PIE AL PAGARE + LA FIRMA DEL CLIENTE
            val pieDocumento = Paragraph(
                "\n\n\n" +
                        "NOMBRE: $nombreCliente\n" +
                        "D.U.I: $duiCliente\n" +
                        "DIRECCION: $direccionCliente\n\n" +
                        "FIRMA: ",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            documento.add(pieDocumento)

            //CARGANDO LA FIRMA REALIZADA EN EL PDF
            val firmaDocumento = Image.getInstance(imageFinal, true)
            firmaDocumento.scaleToFit(70f, 70f)
            documento.add(firmaDocumento)

            documento.close()

            mensaje("Firmado")

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }

    fun atras(){
        val intent = Intent(this, ClientesDetalle::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
    }
    fun pagareYaFirmado(){
        val intent = Intent(this, ClientesDetalle::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
    }

    private fun mensaje(msj: String){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMsj = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        var tituloDialogo = ""
        var mensajeDialogo = ""
        var mensajeBotonAceptar = ""

        when(msj){
            "Cancelar" -> {
                tituloDialogo = "CANCELAR PROCESO"
                mensajeDialogo = "Está seguro de Cancelar el Proceso"
                mensajeBotonAceptar = "SALIR"

                tvUpdate.setOnClickListener {
                    atras()
                    updateDialog.dismiss()
                }

                tvMsj.text = mensajeDialogo
                tvTitulo.text = tituloDialogo
                tvUpdate.text = mensajeBotonAceptar

                tvCancel.setOnClickListener {
                    updateDialog.dismiss()
                }
            }
            "Firmado" -> {
                tvCancel.visibility = View.GONE

                tituloDialogo = "PROCESO COMPLETO"
                mensajeDialogo = "El Proceso fue Generado Correctamente, Regresando a Detalle del Cliente"
                mensajeBotonAceptar = "ACEPTAR"

                tvUpdate.setOnClickListener {
                    pagareYaFirmado()
                    updateDialog.dismiss()
                }

                tvMsj.text = mensajeDialogo
                tvTitulo.text = tituloDialogo
                tvUpdate.text = mensajeBotonAceptar
            }
        }

        updateDialog.show()

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}