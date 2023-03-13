package com.example.acae30

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import java.time.format.DateTimeFormatter

class firmarPagare : AppCompatActivity() {

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var btnCancelar : Button
    private lateinit var btnLimpiar : Button
    private lateinit var btnFirmar : Button
    private var idcliente : Int = 0
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""

    private lateinit var imagenBitmap: Bitmap
    private lateinit var imageFinal : ByteArray
    //private lateinit var drawableImageFinal : BitmapDrawable


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
    @RequiresApi(Build.VERSION_CODES.O)
    val fechaPagare = "La Unión, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))


    val textoPagare = "Por _______; PAGARÉ  en forma incondicional a la ordel del señor: ARMANDO ANTONIO" +
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
            "a quien relevo de la obligación de rendir fianza y cuenta de administración."


    @RequiresApi(Build.VERSION_CODES.O)
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


        btnCancelar.setOnClickListener {
            mensajeCancelar()
        }

        btnLimpiar.setOnClickListener {
            signatureView.signatureClear()
        }

        btnFirmar.setOnClickListener {
            imagenBitmap = signatureView.getSignatureBitmap()!!
            imageFinal = bitmapToByteArray(imagenBitmap)

            //CONVIRTIENDO EN BITMAP
            //val bitmapImagenFinal = BitmapFactory.decodeByteArray(imageFinal, 0, imageFinal.size)

            //CONVIRTIENDO A DRAWABLE
            //drawableImageFinal = BitmapDrawable(resources, bitmapImagenFinal)

            verificarPermisos(it)

        }

    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarPDF(nombreCliente : String, direccionCliente : String, duiCliente : String) {
        try {
            val carpeta = "/archivospdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, "$nombreCliente.pdf")
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

            mensajeYaFirmado()

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
    fun pagareYaFirmado(){
        val intent = Intent(this, ClientesDetalle::class.java)
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
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    fun mensajeYaFirmado(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_firmado)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)


        tvUpdate.setOnClickListener {
            pagareYaFirmado()
            updateDialog.dismiss()
        }

        updateDialog.show()

    }
}