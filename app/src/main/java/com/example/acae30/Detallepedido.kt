package com.example.acae30

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.Library.PdfDocumentAdapter
import com.example.acae30.R
import com.example.acae30.controllers.PedidosController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityDetallepedidoBinding
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.Pedidos
import com.example.acae30.modelos.Sucursales
import com.example.acae30.modelos.dataPedidos
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Timer
import kotlin.concurrent.schedule
import com.example.acae30.R as R1


class Detallepedido : AppCompatActivity() {

    //AGREGANDO EL SPINNER DE SUCURSALES
    private var idSucursal: Int? = null
    private var codigoSucursal: String? = null
    private var nombreSucursalPedido: String? = ""
    private var pedidoEnviado: Boolean = false
    private var getSucursalPosition: Int? = null
    private var tipoEnvio: Int? = null

    private var tipoDocumento: String = ""

    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var idpedido = 0
    private var visita_enviada: Boolean? = null
    private var from: String? = ""
    private var db: Database? = null
    private var cabezera: Pedidos? = null
    private var idvendedor = 0
    private var idvisita = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null
    private var codigo = ""
    private var idapi = 0

    private var envioSelec : String = ""
    private var documentoSelec : String = ""
    private var sucursalName: String = ""

    private var funciones = Funciones()
    private var pedidosController = PedidosController()
    private lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"


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

    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    var fechaDoc = ""
    private val tituloText = "DETALLE FACTURADO"

    private lateinit var binding: ActivityDetallepedidoBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityDetallepedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val intento = intent
        alerta = AlertDialogo(this)
        idcliente = intento.getIntExtra("idcliente", 0)
        nombre = intento.getStringExtra("nombrecliente")
        idpedido = intento.getIntExtra("idpedido", 0)
        idvisita = intento.getIntExtra("visitaid", 0)
        codigo = intento.getStringExtra("codigo").toString()
        idapi = intento.getIntExtra("idapi", 0)
        from = intento.getStringExtra("from").toString()

        db = Database(this)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()
        ip = preferencias.getString("ip", "").toString()
        puerto = preferencias.getInt("puerto", 0)
        visita_enviada = false

        //DESHABILITAMOS LOS TEXTVIEWS DE INFORMACION ENVIADA
        binding.tvDocumentoSeleccionado.visibility = View.GONE
        binding.tvTipoenvio.visibility = View.GONE
        binding.sinSucursal.visibility = View.GONE

        //FUNCION PARA OBTENER LA INFORMACION DEL PEDIDO
        getTipoEnvio(idpedido)

        //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
        cargarSucursales()

        //FUNCION PARA DESHABILITAR FUNCIONES SEGUN PROCESO
        validarProcesoPedidos()

        //COMPLETANDO SPINNER TIPO ENVIO
        val listaTipoAdaptador = ArrayAdapter<String>(this@Detallepedido, android.R.layout.simple_spinner_dropdown_item)
        listaTipoAdaptador.addAll(listOf("RUTA", "ENCOMIENDA"))
        binding.spTipoEnvio.adapter = listaTipoAdaptador

        //COMPLETANDO TVTIPOENVIO
        when(tipoEnvio){
            0 -> {
                binding.tvTipoenvio.text = getString(R.string.ruta)
                binding.spTipoEnvio.setSelection(0, true)
            }
            1 -> {
                binding.tvTipoenvio.text = getString(R.string.encomienda)
                binding.spTipoEnvio.setSelection(1, true)
            }
        }

        //COMPLETANDO SPINNER DOCUMENTO
        val tipoDocumentoAdaptador = ArrayAdapter<String>(this@Detallepedido, android.R.layout.simple_spinner_dropdown_item)
        tipoDocumentoAdaptador.addAll(listOf("FACTURA", "CREDITO FISCAL", "FACTURA EXPORTACION"))
        binding.spDocumento.adapter = tipoDocumentoAdaptador

        //COMPLETANDO TVTIPODOCUMENTO
        when(tipoDocumento){
            "FC" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.factura)
                binding.spDocumento.setSelection(0, true)
            }
            "CF" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.credito_fiscal)
                binding.spDocumento.setSelection(1, true)
            }
            "FE" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.factura_exportacion)
                binding.spDocumento.setSelection(2, true)
            }
        }


        //CARTURANDO LA SUCURSAL SELECCIONADA
         getSucursalPosition = intento.getIntExtra("sucursalPosition", 0)
        //println("Sucursal desde Agregar Producto: $nombreSucursalPedido")
        if(getSucursalPosition != 0){
            binding.spSucursal.setSelection(getSucursalPosition!!, true)
        }

        // Consultar datos de visita
        if (idpedido > 0) {
            val base = db!!.writableDatabase
            try {
                val cursor = base!!.rawQuery(
                    "select c.codigo as codigo, c.cliente as nombre, c.id as idcliente, v.id as idvisita, v.enviado as visita_enviada, strftime('%d/%m/%Y %H:%M', p.fecha_creado) as fecha_creado from pedidos p inner join clientes c on p.Id_cliente = c.Id inner join visitas v on p.idvisita = v.id where p.id = ${idpedido}",
                    null
                )
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    codigo = cursor.getString(0)
                    nombre = cursor.getString(1)
                    idcliente = cursor.getInt(2)
                    idvisita = cursor.getInt(3)
                    visita_enviada = cursor.getInt(4) == 1
                    binding.fechaCreacion.text = cursor.getString(5)
                    cursor.close()
                } else {
                    throw Exception("Error al obtener código de cliente")
                }
            } catch (e: Exception) {
                throw Exception(e.message)
            } finally {
                base.close()
            }
        }

        //MODIFICACION 04/12/2023
        // VERIFICAMOS SI TENEMOS CONEXION A INTERNET PARA PODER ENVIAR EL PEDIDO O ALMACENARLO
        if(!funciones.isInternetAvailable(this@Detallepedido)){
            binding.btnenviar.isEnabled = false
            binding.btnenviar.setBackgroundResource(R1.drawable.border_btndisable)
        }

        binding.imbtnatras.setOnClickListener {
            val intento = Intent(this, Pedido::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        binding.imgbtnadd.setOnClickListener {
            if (idcliente >= 0) {
                val intento = Intent(this, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", binding.txtCliente.text.toString())
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                startActivity(intento)
            }
        }
        //muestra el listado de los productos
        binding.btncancelar.setOnClickListener {
            AlertaEliminar()
        }

        //EVENTRO CLIC DEL BOTON ENVIAR
        binding.btnenviar.setOnClickListener {
            if(funciones.isInternetAvailable(this)){
                if(ConfirmarDetallePedido() > 0){
                    alerta!!.pedidoEnviado()

                    CoroutineScope(Dispatchers.IO).launch {
                        enviarPedidoaServidor()
                    }

                }else{
                    funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
                }
            }else{
                funciones.mostrarAlerta("ERROR: NO TIENES CONEXION A INTERNET", this@Detallepedido, binding.lienzo)
            }
        }

        //EVENTO CLIC DEL BOTON GUARDAR.
        binding.btnguardar.setOnClickListener {
            if (ConfirmarDetallePedido() > 0) {
                try {
                    pedidosController.actualizarEstadoAlGuardar(idpedido, this@Detallepedido, binding.lienzo) //FUNCION PARA ACTUALIZAR EL ESTADO
                    alerta!!.pedidoGuardado()
                    alerta!!.changeText("Guardando Pedido")

                    Timer().schedule(2300){
                        runOnUiThread {
                            alerta!!.dismisss()
                            val visitaAbierta = getEstadoVisita()

                            if (visitaAbierta == 1) {
                                val intento = Intent(this@Detallepedido, Visita::class.java)
                                intento.putExtra("id", idcliente)
                                intento.putExtra("nombrecliente", nombre)
                                intento.putExtra("idpedido", idpedido)
                                intento.putExtra("visitaid", idvisita)
                                intento.putExtra("codigo", codigo)
                                intento.putExtra("idapi", idapi)
                                startActivity(intento)
                                finish()
                            } else {
                                val intento = Intent(this@Detallepedido, Pedido::class.java)
                                startActivity(intento)
                                finish()
                            }
                        }
                    }
                } catch (e: Exception) {
                    alerta!!.dismisss()
                    funciones.mostrarAlerta("ERROR: ${e.message}", this@Detallepedido, binding.lienzo)
                }
            } else {
                funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }

        //BOTON DE EXPORTAR A PDF EL PEDIDO
        binding.btnexportar.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            verificarPermisos(it)
        }

        //IMPLEMENTANDO LOGICA DE SUCURSAL SELECCIONADA EN SPINNER
        binding.spSucursal.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sucursalName = parent?.getItemAtPosition(position).toString()

                validarSelecciones(sucursalName)

                if (sucursalName != "-- SELECCIONE UNA SUCURSAL --") {
                    getSucursalPosition = binding.spSucursal.selectedItemPosition
                    updatePedidoSucursal(idcliente, sucursalName, idpedido)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE TIPO ENVIO SELECCIONADA EN SPINNER
        binding.spTipoEnvio.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                envioSelec = parent?.getItemAtPosition(position).toString()

                when(envioSelec){
                    "ENCOMIENDA" -> {
                       pedidosController.updateTipoPedido(1, idpedido, this@Detallepedido)
                    }
                    "RUTA" -> {
                        pedidosController.updateTipoPedido(0, idpedido, this@Detallepedido)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE TIPO DOCUMENTO SELECCIONADA EN SPINNER
        binding.spDocumento.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                documentoSelec = parent?.getItemAtPosition(position).toString()

                when(documentoSelec){
                    "FACTURA" -> {
                        pedidosController.updateTipoDocumento("FC", idpedido, this@Detallepedido)
                    }
                    "CREDITO FISCAL" -> {
                        pedidosController.updateTipoDocumento("CF", idpedido, this@Detallepedido)
                    }
                    "FACTURA EXPORTACION" -> {
                        pedidosController.updateTipoDocumento("FE", idpedido, this@Detallepedido)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    //FUNCION PARA VALIDAR OPCIONES SELECCIONADAS
    private fun validarSelecciones(sucursalSelec:String){
        if(sucursalSelec != "-- SELECCIONE UNA SUCURSAL --"){
            binding.btnguardar.isEnabled = true
            binding.btnenviar.isEnabled = true
            binding.btnenviar.setBackgroundResource(R1.drawable.border_btnactualizar)
            binding.btnguardar.setBackgroundResource(R1.drawable.border_btnenviar)
        }else{
            binding.btnguardar.isEnabled = false
            binding.btnenviar.isEnabled = false
            binding.btnenviar.setBackgroundResource(R1.drawable.border_btndisable)
            binding.btnguardar.setBackgroundResource(R1.drawable.border_btndisable)
        }
    }

    //FUNCION PARA ENVIAR EL PEDIDO AL SERVIDOR
    private suspend fun enviarPedidoaServidor(){
        try {
            Timer().schedule(2300){
                val pedido = getPedidoSend(idpedido) //retorna el pedido

                pedido!!.Idvendedor = idvendedor // ASIGNAMOS EL ID DEL VENDEDOR AL PEDIDO

                pedido.Vendedor = vendedor//agregamos los datos del vendedor

                SendPedido(pedido, idpedido)//envia el pedido y actualiza el estado del pedido en el cel
                alerta!!.dismisss()

                val visitaAbierta = getEstadoVisita()

                if (visitaAbierta == 1) {
                    val intento = Intent(this@Detallepedido, Visita::class.java)
                    intento.putExtra("id", idcliente)
                    intento.putExtra("nombrecliente", nombre)
                    intento.putExtra("idpedido", idpedido)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    startActivity(intento)
                    finish()
                } else {
                    val intento = Intent(this@Detallepedido, Pedido::class.java)
                    startActivity(intento)
                    finish()
                }

            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                alerta!!.dismisss()

                funciones.mostrarAlerta("ERRORAL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }
    }

    //OPTENIENDO INFORMACION DEL PEDIDO
    private fun getTipoEnvio(ipPedido: Int){
        val dataBase = db!!.readableDatabase
        try {
            val getTipo = dataBase.rawQuery("SELECT * FROM pedidos WHERE id=$ipPedido", null)
            val getPedidoData = ArrayList<dataPedidos>()
            if(getTipo.count > 0){
                getTipo.moveToFirst()
                do {
                    val data = dataPedidos(
                        getTipo.getInt(5) == 1,
                        getTipo.getString(14),
                        getTipo.getInt(16),
                        getTipo.getString(15)
                    )
                    getPedidoData.add(data)
                }while (getTipo.moveToNext())
            }

            for(data in getPedidoData){
                pedidoEnviado = data.envioPedido!!
                nombreSucursalPedido = data.nombreSucursalPedido!!.toString()
                tipoEnvio = data.tipoPedido!!.toInt()
                tipoDocumento = data.tipoDocumento!!.toString()
            }
            getTipo.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            dataBase!!.close()
        }
    }

    //ACTUALIZANDO LA SUCURSAL DEL PEDIDO
    //CAMBIO EN EL TIPO DE DATO PARA EL CODIGO DE LA SUCURSAL, SE CAMBIO A STRING
    //09/10/2023
    private fun updatePedidoSucursal(idCliente:Int, nombreSucursal: String, idpedidos: Int){
        val db = db!!.writableDatabase
        try {
            val dataSucursal = db.rawQuery("SELECT * FROM cliente_sucursal WHERE id_cliente=$idCliente and nombre_sucursal like '%$nombreSucursal%'", null)
            val listaSucursales = ArrayList<Sucursales>()
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                do{
                    val data = Sucursales(
                        dataSucursal.getString(0),
                        dataSucursal.getString(2),
                        dataSucursal.getString(3)
                    )
                    listaSucursales.add(data)
                }while (dataSucursal.moveToNext())
            }

            for (data in listaSucursales) {
                idSucursal = data.idSucursa.toInt()
                codigoSucursal = data.codigoSucursal
            }
            db!!.execSQL("UPDATE pedidos set id_sucursal=$idSucursal, codigo_sucursal='$codigoSucursal', nombre_sucursal='$nombreSucursal' WHERE id=$idpedidos")
            dataSucursal.close()

        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
    }

    //CARGANDO EL NOMBRE DE LA SUCURSAL EN EL SPINNER
    private fun nombreSucursal(): ArrayList<String> {
        val nombreSucursal = arrayListOf<String>()
        nombreSucursal.add("-- SELECCIONE UNA SUCURSAL --")
        try {
            val list: ArrayList<com.example.acae30.modelos.Sucursales> = getSucursalesNombre(idcliente)
            if(list.isNotEmpty()){
                for(data in list){
                    nombreSucursal.add(data.nombreSucursal)
                }
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR LA TABLA CONFIG")
        }
        return nombreSucursal
    }

    //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
    private fun cargarSucursales() {
        val listSucursal = nombreSucursal().toMutableList()

        val adaptador = ArrayAdapter(this@Detallepedido, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R1.layout.support_simple_spinner_dropdown_item)
        binding.spSucursal.adapter = adaptador
    }

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE.
    //03-02-2023
    private fun getSucursalesNombre(idCliente:Int): ArrayList<Sucursales> {
        val db = db!!.readableDatabase
        val listaSucursales = ArrayList<Sucursales>()
        try {

            val dataSucursal = db.rawQuery("SELECT * FROM cliente_sucursal WHERE id_cliente='$idCliente'", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                do{
                    val data = Sucursales(
                        dataSucursal.getString(0),
                        dataSucursal.getString(2),
                        dataSucursal.getString(3)
                    )
                    listaSucursales.add(data)
                }while (dataSucursal.moveToNext())
            }else{
                binding.spSucursal.visibility = View.GONE
                binding.sinSucursal.visibility = View.VISIBLE
            }
            dataSucursal.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaSucursales
    }

    //FUNCION PARA DESHABILITAR OPCIONES SEGUN VISTA EN PEDIDOS
    private fun validarProcesoPedidos(){
        binding.txtCliente.setText(nombre)
        val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        this@Detallepedido.lifecycleScope.launch {
            try {
                val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
                if(lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                funciones.mostrarAlerta("NO SE PUEDO CARGAR EL DETALLE DEL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }
        when(from){
            "ver" -> {
                //MOSTRANDO EL NOMBRE DE LA SUCURSAL
                if (nombreSucursalPedido != "") {
                    binding.sinSucursal.text = nombreSucursalPedido
                } else {
                    binding.sinSucursal.text = getString(R.string.no_tiene_sucursal_registrada_)
                }

                if(pedido!!.Enviado){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.GONE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.btnexportar.visibility = View.VISIBLE
                    binding.spDocumento.visibility = View.GONE
                    binding.spTipoEnvio.visibility = View.GONE
                    binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                    binding.tvTipoenvio.visibility = View.VISIBLE

                }else if(!pedido.Enviado && pedido.Cerrado == 1){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.VISIBLE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.btnexportar.visibility = View.VISIBLE
                }

            }
            "visita" -> {
                //RUTINA PARA AGREGAR NUEVO PEDIDO

                binding.txtCliente.isEnabled = false
                binding.imgbtnadd.visibility = View.VISIBLE
                binding.btnenviar.visibility = View.VISIBLE
                binding.imbtnatras.visibility = View.VISIBLE
                binding.btncancelar.visibility = View.VISIBLE
                binding.btnexportar.visibility = View.GONE
                binding.imbtnatras.visibility = View.GONE
            }
        }
    }
    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun ArmarLista(lista: ArrayList<DetallePedido>) {
        var total = 0.toFloat()
        val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val mLayoutManager = LinearLayoutManager(
            this@Detallepedido,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.reciclerdetalle.layoutManager = mLayoutManager
        val adapter = PedidoDetalleAdapter(lista, this@Detallepedido) { i ->
            if(!pedido!!.Enviado && from == "visita"){
                val data = lista[i]
                val intento = Intent(this@Detallepedido, Producto_agregar::class.java)

                intento.putExtra("idpedidodetalle", data.Id)
                intento.putExtra("idpedido", data.Id_pedido)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombre)
                intento.putExtra("idproducto", data.Id_producto)
                intento.putExtra("proviene", "editar")
                intento.putExtra("total_param", data.Subtotal)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                startActivity(intento)
                finish()
            }
        }
        for (i in lista) {
            total = total + i.Subtotal!!
        }
        binding.txttotal.text = "$" + "${String.format("%.4f", total)}"
        binding.reciclerdetalle.adapter = adapter

    } //muestra el detalle del pedido

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun AlertaEliminar() {
        val dialogo = Dialog(this)
        dialogo.show()
        dialogo.setContentView(R1.layout.alert_eliminar)
        dialogo.findViewById<Button>(R1.id.btneliminar).setOnClickListener {
                try {
                    EliminarPedido(idpedido)

                    val intento = Intent(this@Detallepedido, Visita::class.java)
                    intento.putExtra("id", idcliente)
                    intento.putExtra("nombrecliente", nombre)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    startActivity(intento)
                    finish()
                    dialogo.dismiss()
                } catch (e: Exception) {
                    dialogo.dismiss()
                    val alert: Snackbar = Snackbar.make(
                        binding.lienzo,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R1.color.moderado))
                    alert.show()
                }
        }//boton eliminar
        dialogo.findViewById<Button>(R1.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar



    } //muestra la alerta para eliminar

    private fun EliminarPedido(idpedido: Int) {
        val bd = db!!.writableDatabase
        try {
            val cursor =
                bd!!.rawQuery("SELECT * FROM pedidos where Id=$idpedido and Enviado=1", null)
            if (cursor.count > 0) {
                throw Exception("Este pedido ya fue enviado no se puede eliminar")
            } else {
                bd.execSQL("DELETE FROM pedidos where Id=$idpedido")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    }

    //AGREGANDO CAMPOS DE SUCURSAL Y TIPO DE ENVIO A LA CABECERA DEL PEDIDO
    private fun getPedidoSend(idpedido: Int): CabezeraPedidoSend? {
        val base = db!!.readableDatabase
        try {
            var envio: CabezeraPedidoSend? = null
            val pedido = base!!.rawQuery("SELECT * FROM pedidos where Id=$idpedido", null)
            if (pedido.count > 0) {
                pedido.moveToFirst()
                envio = CabezeraPedidoSend(
                    pedido.getInt(1),//id del cliente
                    pedido.getString(2), //nombre del cliente
                    pedido.getFloat(3),
                    pedido.getFloat(4),
                    pedido.getFloat(3),
                    pedido.getInt(5) == 1,
                    pedido.getInt(9) == 1,
                    pedido.getInt(12),
                    pedido.getString(13),
                    pedido.getString(14),
                    pedido.getInt(16),
                    pedido.getString(15),
                    0,
                    "",
                    pedido.getString(11),
                    pedido.getString(17),
                    null

                )
                pedido.close()
                val cdetalle =
                    base.rawQuery("SELECT * FROM detalle_producto WHERE Id_pedido=$idpedido", null)
                if (cdetalle.count > 0) {
                    val list = ArrayList<DetallePedido>() //lista donde se guardara el pedido
                    cdetalle.moveToFirst()
                    do {
                        val detalle = DetallePedido(
                            cdetalle.getInt(0),
                            cdetalle.getInt(1),
                            cdetalle.getInt(2),
                            cdetalle.getString(3),
                            cdetalle.getString(4),
                            cdetalle.getFloat(5),
                            cdetalle.getFloat(6),
                            cdetalle.getFloat(7),
                            cdetalle.getFloat(8),
                            cdetalle.getFloat(9),
                            cdetalle.getFloat(10),
                            cdetalle.getFloat(11),
                            cdetalle.getFloat(12),
                            cdetalle.getFloat(13),
                            cdetalle.getFloat(14),
                            cdetalle.getString(15),
                            cdetalle.getString(16),
                            cdetalle.getString(17),
                            cdetalle.getString(18),
                            cdetalle.getString(19),
                            cdetalle.getFloat(20),
                            cdetalle.getInt(21),
                            cdetalle.getFloat(22),
                            cdetalle.getString(23),
                            cdetalle.getInt(24),
                            cdetalle.getInt(25)
                        )
                        list.add(detalle)
                    } while (cdetalle.moveToNext())
                    cdetalle.close()
                    envio.detalle = list //se agrega al objecto el detalle del pedido
                }
            }
            return envio
        } catch (e: Exception) {
            throw Exception(e)
        } finally {
            base.close()
        }
    }//obtiene el pedido
    //obtiene el pedido de la base de datos

    private fun SendPedido(pedido: CabezeraPedidoSend, idpedido: Int)  {
        try {
            val objecto = convertToJson(pedido, idpedido) //convertimos a json el objecto pedido
            val ruta: String = "http://$ip:$puerto/pedido" //ruta para enviar el pedido

            //println("JSON GENERADO: /n $objecto")
            //val ruta="http://192.168.0.103:53272/pedido"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    connectTimeout = 10000
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto.toString()) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                        try {
                            val respuesta = StringBuffer()
                            var inpuline = it.readLine()
                            while (inpuline != null) {
                                respuesta.append(inpuline)
                                inpuline = it.readLine()
                            }
                            it.close()
                            val data: String? = respuesta.toString()
                            if (data != null && data.length > 0) {
                                val datosservidor = JSONObject(data)
                                if (!datosservidor.isNull("error") && !datosservidor.isNull("response")) {
                                    when (responseCode) {
                                        201 -> {
                                            val idpedidoS = datosservidor.getString("error").toInt()
                                            if (idpedidoS > 0) {
                                                ConfirmarPedido(idpedido, idpedidoS)
                                            } else {
                                                funciones.mostrarAlerta("ERROR: AL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
                                            }
                                        }
                                        400 -> {
                                            funciones.mostrarAlerta("ERROR: RESPUESTA NO ENCONTRADA", this@Detallepedido, binding.lienzo)
                                        }
                                        500 -> {
                                            funciones.mostrarAlerta("ERROR INTERNO DEL SERVIDOR", this@Detallepedido, binding.lienzo)
                                        }
                                    }
                                } else {
                                    funciones.mostrarAlerta("ERROR: NO HEY RESPUESTA DEL SERVIDOR 1", this@Detallepedido, binding.lienzo)
                                }
                            } else {
                                funciones.mostrarAlerta("ERROR: NO HAY RESPUESTA DEL SERVIDOR 2", this@Detallepedido, binding.lienzo)
                            }
                        } catch (e: Exception) {
                            funciones.mostrarAlerta("ERROR: AL LEER LA RESPUESTA DEL SERVER", this@Detallepedido, binding.lienzo)
                        }
                    } //se obtiene la respuesta del servidor
                } catch (e: Exception) {
                    funciones.mostrarAlerta("ERROR: AL ENVIAR EL JSON DEL PEDIDO", this@Detallepedido, binding.lienzo)
                }

            }
        } catch (e: Exception) {
            funciones.mostrarAlerta("ERROR: ENVIO DE PARAMETRO EQUIVOCADOS", this@Detallepedido, binding.lienzo)
        }
    } //funcion que envia el pedido a la bd

    private fun ConfirmarPedido(idpedido: Int, idservidor: Int) {
        val bd = db!!.writableDatabase
        try {
            bd!!.execSQL("UPDATE pedidos set Id_pedido_sistema=$idservidor,Enviado=1,Cerrado=1 WHERE Id=$idpedido")
            //bd!!.execSQL("UPDATE pedidos set Enviado=1,Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    } //actualiza el pedido y confirma que se envio

    private fun ConfirmarDetallePedido(): Int {
        val bd = db!!.writableDatabase
        var cantidadDetallepedido = 0.toInt()
        try {
            val cursor = bd!!.rawQuery(
                "select count(id_pedido) as cantidad from detalle_pedidos where id_pedido = ${idpedido}",
                null
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                cantidadDetallepedido = cursor.getInt(0)
                cursor.close()
            } else {
                throw Exception("Error al buscar productos del pedidos.")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
        return cantidadDetallepedido
    } //actualiza el pedido y confirma que se envio

    private fun convertToJson(pedido: CabezeraPedidoSend, idpedido_param: Int): JsonObject {

        var idvisita_v = 0.toInt()

        var horaProceso = funciones?.getFechaHoraProceso()

        val base = db!!.writableDatabase
        try {
            val cursor = base!!.rawQuery(
                "select v.Idvisita from visitas v inner join pedidos p on v.id = p.idvisita where p.id = ${idpedido_param}",
                null
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                idvisita_v = cursor.getInt(0)
                cursor.close()
            } else {
                throw Exception("Error al obtener código de cliente")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

        val json = JsonObject()
        json.addProperty("Idcliente", pedido.Idcliente)
        json.addProperty("Cliente", pedido.Cliente)
        json.addProperty("Subtotal", pedido.Subtotal)
        json.addProperty("Descuento", pedido.Descuento)
        json.addProperty("Total", pedido.Total)
        json.addProperty("Envidado", false)
        json.addProperty("Cerrado", false)
        json.addProperty("IdSucursal", pedido.IdSucursal)
        json.addProperty("CodigoSucursal", pedido.CodigoSucursal)
        json.addProperty("NombreSucursal", pedido.NombreSucursal)
        json.addProperty("TipoEnvio", pedido.TipoEnvio)
        json.addProperty("Tipo_documento_app", pedido.TipoDocumento)
        json.addProperty("Idvendedor", pedido.Idvendedor)
        json.addProperty("Vendedor", pedido.Vendedor)
        json.addProperty("Terminos", pedido.Terminos)
        json.addProperty("fechaCreado", pedido.fechaCreado) /*ENVIANDO LA FECHA DESDE EL DISPOSITIVO MOVIL*/
        json.addProperty("HoraProceso", horaProceso)/*ENVIANDO EL TIMESTAMP DE CREACION DEL PEDIDO*/
        json.addProperty("Idapp", idvisita_v)
        //se ordena la cabezera
        val detalle = JsonArray()
        for (i in 0..(pedido.detalle!!.size - 1)) {
            val data = pedido.detalle!!.get(i)
            val d = JsonObject()
            d.addProperty("Id", data.Id)
            d.addProperty("Id_pedido", data.Id_pedido)
            d.addProperty("Id_producto", data.Id_producto)
            d.addProperty("Codigo", data.Codigo)
            d.addProperty("Descripcion", data.Descripcion)
            d.addProperty("Costo", data.Costo)
            d.addProperty("Costo_iva", data.Costo_iva)
            d.addProperty("Precio", data.Precio)
            d.addProperty("Precio_iva", data.Precio_iva)
            d.addProperty("Precio_u", data.Precio_u)
            d.addProperty("Precio_u_iva", data.Precio_u_iva)
            d.addProperty("Cantidad", data.Cantidad)
            d.addProperty("Precio_venta", data.Precio_venta)
            d.addProperty("Unidad", data.Unidad)
            d.addProperty("Subtotal", data.Subtotal)
            d.addProperty("Total", data.Total)
            d.addProperty("Bonificado", data.Bonificado)
            d.addProperty("Descuento", data.Descuento)
            d.addProperty("Precio_editado", data.Precio_editado)
            d.addProperty("Idunidad", data.Idunidad)
            d.addProperty("Idtalla", data.Id_talla)
            d.addProperty("FechaCreado", pedido.fechaCreado) /*ENVIANDO LA MISMA FECHA DEL PEDIDO DESDE EL CEL*/
            detalle.add(d)
        }
        json.add("detalle", detalle)
        return json

    }
    //convierte el pedido a json

    private fun getEstadoVisita(): Int {
        var visitaAbierta = 1

        val base = db!!.writableDatabase
        try {
            val cursor = base!!.rawQuery("select Abierta from visitas where id = ${idvisita}", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                visitaAbierta = cursor.getInt(0)
                cursor.close()
            } else {
                //throw Exception("Error al obtener código de cliente")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

        return visitaAbierta
    }


    //FUNCION PARA VERIFICAR PERMISOS DE CREACION DE DIRECTORIO Y DOCUMENTOS
    private fun verificarPermisos(view: View) {
        when{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                generarPDF(nombre!!, vendedor)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                Snackbar.make(view, "ESTE PERMISO ES NECESARIO PARA CREAR EL ARCHIVO", Snackbar.LENGTH_INDEFINITE).setAction("Ok"){
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }.show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    //FUNCION PARA GENERAR EL REPORTE EN PDF
    private fun generarPDF(nombreCliente : String, nombreVendedor: String) {
        try {
            val carpeta = "/pedidospdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, nombreCliente + "_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            //val documento = Document(PageSize.LETTER, 2.5f, 2.5f, 3.5f, 3.5f)
            val documento = Document(PageSize.LARGE_CROWN_OCTAVO, 0f, 0f, .5f, .5f)
            PdfWriter.getInstance(documento, fos)

            documento.open()

            //ESPACIOS
            val espaciosDocumento = Paragraph(
                "\n\n\n"
            )
            documento.add(espaciosDocumento)

            // --------- DATOS DE LA EMPRESA
            // LOGO
            val tablaLogo = PdfPTable(1)
            tablaLogo.widthPercentage = 80f
            val logoEmpresa = PdfPCell(Paragraph("[LOGO]"))
            logoEmpresa.horizontalAlignment = Element.ALIGN_CENTER
            logoEmpresa.border = 0
            tablaLogo.addCell(logoEmpresa)
            documento.add(tablaLogo)

            val tablaEncabezado = PdfPTable(1)
            tablaEncabezado.widthPercentage = 80f
            val cellInforEmpresa = PdfPCell(Paragraph("ESCARRSA, DE C.V\n" +
                    "FINAL AV. PERALTA Y 38A AV. NORTE, BO. LOURDES " +
                    "SAN SALVADOR, SAN SALVADOR\n",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellInforEmpresa.horizontalAlignment = Element.ALIGN_CENTER
            cellInforEmpresa.border = 0
            tablaEncabezado.addCell(cellInforEmpresa)
            documento.add(tablaEncabezado)

            //DATOS CREDITICIOS
            val tablaDatosCrediticios = PdfPTable(1)
            tablaDatosCrediticios.widthPercentage = 80f

            val cellDatosCrediticios = PdfPCell(Paragraph("N.R.C : 133843-2\n" +
                    "N.I.T : 0614-300801-101-7 \n" +
                    "GIRO: VENTA AL POR MAYOR DE HIELO\n\n",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)))
            cellDatosCrediticios.horizontalAlignment = Element.ALIGN_CENTER
            cellDatosCrediticios.border = 0
            tablaDatosCrediticios.addCell(cellDatosCrediticios)
            documento.add(tablaDatosCrediticios)

            //AGREGANDO TITULO PEDIDO
            val fechaDocumento = Paragraph(
                "$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //AGREGANDO SEPARADO
            val lineSeparator = LineSeparator()
            lineSeparator.percentage = 80f
            lineSeparator.lineWidth = 1f
            lineSeparator.alignment = Element.ALIGN_CENTER
            documento.add(lineSeparator)

            //DATOS DEL PEDIDO
            val tablaPedido = PdfPTable(4)
            tablaPedido.widthPercentage = 90f
            /*
            val cellCantidad = PdfPCell(Paragraph("CANT",
                FontFactory.getFont("arial", 11f, Font.BOLD, BaseColor.BLACK)
            ))
            cellCantidad.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellCantidad)

            val cellDescripcion = PdfPCell(Paragraph("PRODUCTO",
                FontFactory.getFont("arial", 11f, Font.BOLD, BaseColor.BLACK)
            ))
            cellDescripcion.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellDescripcion)

            val cellTotal = PdfPCell(Paragraph("TOTAL",
                FontFactory.getFont("arial", 11f, Font.BOLD, BaseColor.BLACK)
            ))
            cellTotal.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellTotal)*/

            //AGREGANDO EL CONTENIDO DEL PEDIDO
            val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
            var total = 0f

            for(data in lista){

                val cellCantidadP = PdfPCell(Paragraph(""+data.Cantidad,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellCantidadP.horizontalAlignment = Element.ALIGN_RIGHT
                cellCantidadP.border = 0
                tablaPedido.addCell(cellCantidadP)

                val cellDescripcionP = PdfPCell(Paragraph(""+data.Descripcion,
                    FontFactory.getFont("arial", 9f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellDescripcionP.horizontalAlignment = Element.ALIGN_LEFT
                cellDescripcionP.border = 0
                tablaPedido.addCell(cellDescripcionP)

                val cellPrecioU = PdfPCell(Paragraph("$ "+data.Precio_iva,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellPrecioU.horizontalAlignment = Element.ALIGN_CENTER
                cellPrecioU.border = 0
                tablaPedido.addCell(cellPrecioU)

                val cellTotalP = PdfPCell(Paragraph("$ "+data.Total,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellTotalP.horizontalAlignment = Element.ALIGN_LEFT
                cellTotalP.border = 0
                tablaPedido.addCell(cellTotalP)

                total += data.Total!!
            }

            val cellDescripcionP = PdfPCell(Paragraph(""))
            cellDescripcionP.border = 0
            tablaPedido.addCell(cellDescripcionP)

            val cellDescripcionP2 = PdfPCell(Paragraph(""))
            cellDescripcionP2.border = 0
            tablaPedido.addCell(cellDescripcionP2)

            val cellCantidadP = PdfPCell(Paragraph("TOTAL",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellCantidadP.horizontalAlignment = Element.ALIGN_CENTER
            cellCantidadP.border = 0
            tablaPedido.addCell(cellCantidadP)

            val cellTotalP = PdfPCell(Paragraph("$ "+ total,
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellTotalP.horizontalAlignment = Element.ALIGN_LEFT
            cellTotalP.border = 0
            tablaPedido.addCell(cellTotalP)
            documento.add(tablaPedido)

            //AGREGANDO SEPARADO
            val lineSeparator2 = LineSeparator()
            lineSeparator2.percentage = 80f
            lineSeparator2.lineWidth = 1f
            lineSeparator2.alignment = Element.ALIGN_CENTER
            documento.add(lineSeparator2)


            //DATOS DEL VENDEDOR
            val tablaVendedor = PdfPTable(1)
            tablaVendedor.widthPercentage = 80f
            val cellInforVendedor = PdfPCell(Paragraph("\n\n\n\nVENDEDOR: $vendedor\n\n" +
                    "FECHA: $fecha\n\n",
                FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
            ))
            cellInforVendedor.horizontalAlignment = Element.ALIGN_CENTER
            cellInforVendedor.border = 0
            tablaVendedor.addCell(cellInforVendedor)
            documento.add(tablaVendedor)

            //FINAL DOC
            val tablaFinal = PdfPTable(1)
            tablaFinal.widthPercentage = 80f
            val cellFinal = PdfPCell(Paragraph("ESTE COMPROBANTE NO ES UN " +
                    "DOCUMENTO LEGAL\n", FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)))
            cellFinal.horizontalAlignment = Element.ALIGN_CENTER
            cellFinal.border = 0
            tablaFinal.addCell(cellFinal)
            documento.add(tablaFinal)

            documento.close()

            printPDF(nombreCliente, fechaDoc)

            //funciones.mostrarMensaje("PEDIDO EXPORTADO CORRECTAMENTE", this@Detallepedido, binding.lienzo)

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }

    //FUNCION PARA IMPRIMIR EL PDF CON VISUALIZACION PREVIA
    private fun printPDF(nombreCliente: String, fechaDoc: String){
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        try {
            val printAdapter = PdfDocumentAdapter(this@Detallepedido, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/pedidospdf/" + nombreCliente + "_$fechaDoc.pdf")
            printManager.print("DOCUMENTO", printAdapter, PrintAttributes.Builder().build())
        }catch (e: Exception){
            e.message?.let { Log.e("MENSAJE ERROR", it) }
        }
    }


}