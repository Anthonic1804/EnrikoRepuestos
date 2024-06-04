package com.example.acae30

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.R
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityDetallepedidoBinding
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.Sucursales
import com.example.acae30.modelos.dataPedidos
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
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
    private var terminosPedidos: String? = null

    private var tipoDocumento: String = ""

    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var idpedido = 0
    private var visita_enviada: Boolean? = null
    private var from: String? = ""
    private var db: Database? = null
    private var idvendedor = 0
    private var idvisita = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null
    private var codigo = ""
    private var idapi = 0
    var total = 0f

    private var envioSelec : String = ""
    private var documentoSelec : String = ""
    private var sucursalName: String = ""
    private var categoriaCliente: String = ""

    private var funciones = Funciones()
    private var pedidosController = PedidosController()
    private var inventarioController = InventarioController()
    private var clientesController = ClientesController()
    private var visitaController = VisitaController()
    private lateinit var preferencias: SharedPreferences

    private val instancia = "CONFIG_SERVIDOR"

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView
    private var enviandoPedido = false
    private var guardandoPedido = false


    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

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

        //OBTENIENDO LA CATEGORIA DEL CLIENTE
        categoriaCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)?.Categoria_cliente.toString()

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

        //COMPLETANDO SPINNER TERMINOS ENVIO
        val listaTipoAdaptador = ArrayAdapter<String>(this@Detallepedido, android.R.layout.simple_spinner_dropdown_item)
        when(terminosPedidos){
            "Contado" -> {
                listaTipoAdaptador.addAll(listOf("CONTADO"))
                binding.spTipoEnvio.adapter = listaTipoAdaptador
                binding.tvTipoenvio.text = "CONTADO"
            }
            else -> {
                listaTipoAdaptador.addAll(listOf("CONTADO", "CREDITO"))
                binding.spTipoEnvio.adapter = listaTipoAdaptador
                binding.spTipoEnvio.setSelection(1, true)

                binding.tvTipoenvio.text = "CREDITO"

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

                actualizarTotales()
            }
            "CF" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.credito_fiscal)
                binding.spDocumento.setSelection(1, true)

                actualizarTotales()
            }
            "FE" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.factura_exportacion)
                binding.spDocumento.setSelection(2, true)

                actualizarTotales()
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
            if (ConfirmarDetallePedido() > 0) {
                val pedidoInfo = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
                enviandoPedido = true

                if(pedidoInfo?.Cerrado == 0 && pedidoInfo.Enviado == 0){
                    alertaPago(total)
                }else{
                    verificarConexionEnvio()
                }
            } else {
                funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
            }

        }

        //EVENTO CLIC DEL BOTON GUARDAR.
        binding.btnguardar.setOnClickListener {
            if (ConfirmarDetallePedido() > 0) {
                guardandoPedido = true
                alertaPago(total)
            } else {
                funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
            }

        }

        //BOTON DE EXPORTAR A PDF EL PEDIDO
        binding.btnexportar.setOnClickListener {
            imprimirRecibo()
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
                    "CONTADO" -> {
                        pedidosController.actualizarTerminosEnvio("Contado", idpedido,this@Detallepedido)
                    }
                    "CREDITO" -> {
                        pedidosController.actualizarTerminosEnvio("Credito", idpedido, this@Detallepedido)
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
                        tipoDocumento = "FC"
                        actualizarTotales()
                    }
                    "CREDITO FISCAL" -> {
                        pedidosController.updateTipoDocumento("CF", idpedido, this@Detallepedido)
                        tipoDocumento = "CF"
                        actualizarTotales()
                    }
                    "FACTURA EXPORTACION" -> {
                        pedidosController.updateTipoDocumento("FE", idpedido, this@Detallepedido)
                        tipoDocumento = "FE"
                        actualizarTotales()
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    //FUNCION PARA GUARDAR EL PEDIDO EN EL DISPOSITIVO
    private fun guardarPedido(){
        try {
            //DESCARGANDO INVENTARIO
            descargarInventario()

            pedidosController.actualizarEstadoAlGuardar(idpedido, this@Detallepedido, binding.lienzo) //FUNCION PARA ACTUALIZAR EL ESTADO
            alerta!!.pedidoGuardado()
            alerta!!.changeText("Guardando Pedido")

            Timer().schedule(2300){
                runOnUiThread {
                    alerta!!.dismisss()
                }

                pedidoEnviado()
            }
        } catch (e: Exception) {
            alerta!!.dismisss()
            funciones.mostrarAlerta("ERROR: ${e.message}", this@Detallepedido, binding.lienzo)
        }
    }

    //FUNCION PARA ACTUALIZAR TOTALES CUANDO ES CREDITO FISCAL
    private fun actualizarTotales(){
        if(total > 0){
            when(tipoDocumento) {
                "CF" -> {
                    if(categoriaCliente == "Gran contribuyente"){
                        binding.txtSumas.text = "${String.format("%.4f", (total/1.13)/1.01)}"
                        binding.txtIva.text = "${String.format("%.4f", ((total/1.13)*0.13))}"
                        binding.txtIvaPerci.text = "${String.format("%.4f", ((total/1.13)*0.01))}"
                    }else{
                        binding.txtSumas.text = "${String.format("%.4f", (total/1.13))}"
                        binding.txtIva.text = "${String.format("%.4f", ((total/1.13)*0.13))}"
                        binding.txtIvaPerci.text = "${String.format("%.4f", 0f)}"
                    }
                }
                else -> {
                    binding.txtSumas.text = "${String.format("%.4f", total)}"
                    binding.txtIva.text = "${String.format("%.4f", 0f)}"
                    binding.txtIvaPerci.text = "${String.format("%.4f", 0f)}"

                }
            }
        }

        val sumas = binding.txtSumas.text.toString().toFloat()
        val iva = binding.txtIva.text.toString().toFloat()
        val ivaperci = binding.txtIvaPerci.text.toString().toFloat()

        pedidosController.actualizarTotalesFiscales(this@Detallepedido, idpedido,
            sumas, iva, ivaperci)

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

                val enviado = SendPedido(pedido, idpedido)//envia el pedido y actualiza el estado del pedido en el cel
                alerta!!.dismisss()

                if(enviado){
                    //SI EL PEDIDO YA HA FUE CERRADO NO REALIZA LA DESCARGA NUEVAMENTE
                    if(pedido.Cerrado!! == 0){
                        //DESCARGANDO INVENTARIO
                        descargarInventario()
                    }

                    pedidoEnviado()

                }else{
                    runOnUiThread {
                        Toast.makeText(this@Detallepedido,"DESEA ALMACENAR EL PEDIDO PARA LUEGO ENVIARLO", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                alerta!!.dismisss()

                funciones.mostrarAlerta("ERROR AL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }
    }

    //FUNCION PARA FINALIZAR EL ENVIO DEL PEDIDO
    private fun pedidoEnviado(){
        val visita = visitaController.obtenerVisitaPorID(idvisita, this@Detallepedido)
        if(visita!!.Abierta){
            val intento = Intent(this@Detallepedido, Visita::class.java)
            intento.putExtra("id", idcliente)
            intento.putExtra("nombrecliente", nombre)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisita)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idapi", idapi)
            startActivity(intento)
            finish()
        }else{
            val intento = Intent(this@Detallepedido, Pedido::class.java)
            startActivity(intento)
            finish()
        }

    }

    //METODO PARA VERIFICAR SI EL PEDIDO YA FUE CERRADO PARA PODER REDIRECCIONAR
    //CORRECTAMENTE
    override fun onResume() {
        super.onResume()
        if(enviandoPedido){
            envioAlerta()
        }

        if(guardandoPedido){
            guardarPedido()
        }
    }

    private fun envioAlerta(){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "¿DESEA ENVIAR EL PEDIDO?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            verificarConexionEnvio()
        }

        tvCancel.setOnClickListener {
            enviandoPedido = false
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun verificarConexionEnvio() {
        if(funciones.isInternetAvailable(this)){
            alerta!!.pedidoEnviado()

            CoroutineScope(Dispatchers.IO).launch {
                enviarPedidoaServidor()
            }
        }else{
            funciones.mostrarAlerta("ERROR: NO TIENES CONEXION A INTERNET", this@Detallepedido, binding.lienzo)
        }
    }

    //FUNCION PARA DESCARGAR EL PRODUCTO DE INVENTARIO APP
    private fun descargarInventario(){
        //REALIZANDO LA DESCAR DE INVENTARIO DE LA APP
        //SOLO SI SE USA HOJA DE CARGA DE ESCARRSA
        val hojaCarga = preferencias.getBoolean("Hoja_carga_inventario_app", false)
        if(hojaCarga){
            CoroutineScope(Dispatchers.IO).launch {
                inventarioController.descargarProductosInventario(idpedido, this@Detallepedido)
            }
        }
    }

    //OPTENIENDO INFORMACION DEL PEDIDO
    private fun getTipoEnvio(ipPedido: Int){
        val dataBase = db!!.readableDatabase
        try {
            val getTipo = dataBase.rawQuery("SELECT Enviado, nombre_sucursal, tipo_envio, tipo_documento, terminos FROM pedidos WHERE id=$ipPedido", null)
            val getPedidoData = ArrayList<dataPedidos>()
            if(getTipo.count > 0){
                getTipo.moveToFirst()
                do {
                    val data = dataPedidos(
                        getTipo.getInt(0) == 1,
                        getTipo.getString(1),
                        getTipo.getInt(2),
                        getTipo.getString(3),
                        getTipo.getString(4)
                    )
                    getPedidoData.add(data)
                }while (getTipo.moveToNext())
            }

            for(data in getPedidoData){
                pedidoEnviado = data.envioPedido!!
                nombreSucursalPedido = data.nombreSucursalPedido!!.toString()
                tipoEnvio = data.tipoPedido!!.toInt()
                tipoDocumento = data.tipoDocumento!!.toString()
                terminosPedidos = data.terminosPedido!!.toString()
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

                if(pedido!!.Enviado == 1){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.GONE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.btnexportar.visibility = View.VISIBLE
                    binding.spDocumento.visibility = View.GONE
                    binding.spTipoEnvio.visibility = View.GONE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                    binding.tvTipoenvio.visibility = View.VISIBLE

                }else if(pedido.Enviado == 0 && pedido.Cerrado == 1){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.VISIBLE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.btnexportar.visibility = View.GONE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
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
        //var total = 0.toFloat()
        val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val mLayoutManager = LinearLayoutManager(
            this@Detallepedido,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.reciclerdetalle.layoutManager = mLayoutManager
        val adapter = PedidoDetalleAdapter(lista, this@Detallepedido) { i ->
            if(pedido!!.Enviado != 1 && from == "visita"){
                val data = lista[i]
                val intento = Intent(this@Detallepedido, Producto_agregar::class.java)

                intento.putExtra("idpedidodetalle", data.Id)
                intento.putExtra("idpedido", data.Id_pedido)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombre)
                intento.putExtra("idproducto", data.Id_producto)
                intento.putExtra("proviene", "editar")
                intento.putExtra("total_param", data.Total_iva)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                startActivity(intento)
                finish()
            }
        }
        for (i in lista) {
            total += i.Total_iva!!
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
            cursor.close()
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
                    pedido.getFloat(11), //POR EL MOMENTO TIENE EL DATO DEL TOTAL
                    pedido.getFloat(5),
                    pedido.getFloat(11),
                    pedido.getInt(12),
                    pedido.getInt(16),
                    pedido.getInt(19),
                    pedido.getString(20),
                    pedido.getString(21),
                    pedido.getInt(23),
                    pedido.getString(22),
                    0,
                    "",
                    pedido.getString(18),
                    pedido.getString(24),
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
                            cdetalle.getFloat(15),
                            cdetalle.getString(16),
                            cdetalle.getInt(17),
                            cdetalle.getFloat(18),
                            cdetalle.getString(19),
                            cdetalle.getInt(20)
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

    private fun SendPedido(pedido: CabezeraPedidoSend, idpedido: Int) : Boolean  {
        var enviado = false
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
                    connectTimeout = 2000
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
                                                enviado = true
                                                ConfirmarPedido(idpedido, idpedidoS)
                                            } else {
                                                enviado = false
                                                funciones.mostrarAlerta("ERROR: AL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
                                            }
                                        }
                                        400 -> {
                                            enviado = false
                                            funciones.mostrarAlerta("ERROR: RESPUESTA NO ENCONTRADA", this@Detallepedido, binding.lienzo)
                                        }
                                        500 -> {
                                            enviado = false
                                            funciones.mostrarAlerta("ERROR INTERNO DEL SERVIDOR", this@Detallepedido, binding.lienzo)
                                        }
                                    }
                                } else {
                                    enviado = false
                                    funciones.mostrarAlerta("ERROR: NO HEY RESPUESTA DEL SERVIDOR 1", this@Detallepedido, binding.lienzo)
                                }
                            } else {
                                enviado = false
                                funciones.mostrarAlerta("ERROR: NO HAY RESPUESTA DEL SERVIDOR 2", this@Detallepedido, binding.lienzo)
                            }
                        } catch (e: Exception) {
                            enviado = false
                            funciones.mostrarAlerta("ERROR: AL LEER LA RESPUESTA DEL SERVER", this@Detallepedido, binding.lienzo)
                        }
                    } //se obtiene la respuesta del servidor
                } catch (e: Exception) {
                    enviado = false
                    //funciones.mostrarAlerta("ERROR: AL ENVIAR EL JSON DEL PEDIDO", this@Detallepedido, binding.lienzo)
                    println("ERROR AL ENVIAR EL PEDIDO -> ${e.message}")
                }

            }
        } catch (e: Exception) {
            enviado = false
            funciones.mostrarAlerta("ERROR: ENVIO DE PARAMETRO EQUIVOCADOS", this@Detallepedido, binding.lienzo)
        }
        return enviado
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
            d.addProperty("Total", data.Total)
            d.addProperty("Total_iva", data.Total_iva)
            d.addProperty("Unidad", data.Unidad)
            d.addProperty("Bonificado", data.Bonificado)
            d.addProperty("Descuento", data.Descuento)
            d.addProperty("Precio_editado", data.Precio_editado)
            d.addProperty("Idunidad", data.Idunidad)
            d.addProperty("FechaCreado", pedido.fechaCreado) /*ENVIANDO LA MISMA FECHA DEL PEDIDO DESDE EL CEL*/
            detalle.add(d)
        }
        json.add("detalle", detalle)
        return json

    }
    //convierte el pedido a json



    //FUNCION PARA IMPRIMIR EL RECIBO
    private fun imprimirRecibo(){
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = getString(R.string.app_name) + " Document"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val builder = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(builder, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val os = FileOutputStream(destination?.fileDescriptor)
                    val pdfDocument = PdfDocument()

                    // Create a page
                    val pageInfo = PdfDocument.PageInfo.Builder(280, calculateTicketHeight().toInt(), 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    // Draw the ticket content on the canvas
                    val canvas = page.canvas
                    crearTicket(canvas)

                    pdfDocument.finishPage(page)
                    pdfDocument.writeTo(os)
                    pdfDocument.close()

                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    Log.e("ERROR IMPRESION", "ERROR AL IMPRIMIR EL TICKET", e)
                    callback?.onWriteFailed(e.message)
                }
            }
        }, null)
    }

    //FUNCION PARA DIBUJAR EL TIKET
    private fun crearTicket(canvas: Canvas) {

        val inforPedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)

        // Tamaños de letra específicos para cada columna
        val textSizeCantidad = 9f
        val textSizeCodigo = 10f
        val textSizeTotal = 9f

        // Espacio entre las columnas
        val columnSpacing = 10f

        // Paint para el texto
        val paint = TextPaint().apply {
            textSize = 12f // Tamaño predeterminado para el título y la división
        }

        // Draw title
        paint.isFakeBoldText = true
        canvas.drawText("ESCARRSA, DE C.V", 50f, 50f, paint)
        canvas.drawText("FINAL AV. PERALTA Y 38A AV. NORTE, BO. LOURDES", 50f, 70f, paint)
        canvas.drawText("SAN SALVADOR, SAN SALVADOR", 50f, 90f, paint)
        canvas.drawText("N.R.C : 133843-2", 50f, 110f, paint)
        canvas.drawText("N.I.T : 0614-300801-101-7", 50f, 130f, paint)
        canvas.drawText("GIRO: VENTA AL POR MAYOR DE HIELO", 50f, 150f, paint)

        // Draw divider line
        paint.isFakeBoldText = false
        canvas.drawLine(50f, 160f, canvas.width - 50f, 160f, paint)

        // Draw column headers
        val columnWidths = floatArrayOf(20f, 100f, 60f) // Ancho fijo para cada columna
        val startY = 165f
        var y = startY
        val columnX = floatArrayOf(
            50f,
            50f + columnWidths[0] + columnSpacing,
            50f + columnWidths[0] + columnWidths[1] + columnSpacing
        )

        y += 20f
        val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
        var total = 0f

        for (data in lista) {
            // Draw text in each column with specific text sizes
            paint.textSize = textSizeCantidad
            val Cantidad = data.Cantidad?.plus(data.Bonificado!!)
            canvas.drawText("$Cantidad", columnX[0] + 5f, y + 15f, paint)

            paint.textSize = textSizeCodigo
            val descripcionLayout = StaticLayout(
                data.Descripcion, paint, columnWidths[1].toInt(),
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
            )
            canvas.save()
            canvas.translate(columnX[1] + 5f, y)
            descripcionLayout.draw(canvas)
            canvas.restore()

            paint.textSize = textSizeTotal
            val totalWidth = paint.measureText("$ ${data.Total_iva}")
            canvas.drawText("$ ${data.Total_iva}", columnX[2] + columnWidths[2] - totalWidth, y + 15f, paint)

            y += descripcionLayout.height.toFloat() + 20f
            total += data.Total_iva!!
        }

        // Draw divider line
        y+=20
        paint.isFakeBoldText = false
        canvas.drawLine(50f, y, canvas.width - 50f, y, paint)

        // Draw total
        y+= 20f
        paint.textSize = 12f
        canvas.drawText("SUBTOTAL:", 50f, y, paint)
        val subTotalText = paint.measureText("${inforPedido!!.Suma}")
        canvas.drawText("${inforPedido.Suma}", canvas.width - subTotalText - 50f, y, paint)

        y+= 20f
        paint.textSize = 12f
        canvas.drawText("IVA:", 50f, y, paint)
        val ivaText = paint.measureText("${inforPedido.Iva}")
        canvas.drawText("${inforPedido.Iva}", canvas.width - ivaText - 50f, y, paint)

        y+= 20f
        paint.textSize = 12f
        canvas.drawText("IVA/PER:", 50f, y, paint)
        val perciText = paint.measureText("{${inforPedido.Iva_Percibido}}")
        canvas.drawText("${inforPedido.Iva_Percibido}", canvas.width - perciText - 50f, y, paint)

        y += 20f
        paint.textSize = 12f // Restaurar el tamaño de letra predeterminado
        canvas.drawText("TOTAL:", 50f, y, paint)
        val totalTextWidth = paint.measureText("$total")
        canvas.drawText("$total", canvas.width - totalTextWidth - 50f, y, paint)

        // Draw divider line after the table
        canvas.drawLine(50f, y + 20f, canvas.width - 50f, y + 20f, paint)
        canvas.drawText("VENDIDO POR: $vendedor", 50f, y + 40f, paint)
        canvas.drawText("FECHA: $fecha", 50f,  y + 60f, paint)
    }

    //FUNCION PARA CALCULAR EL LARGO DEL TICKET
    private fun calculateTicketHeight(): Float {

        val paint = TextPaint().apply {
            textSize = 12f
        }

        var ticketHeight = 160f // Altura del título y la división inicialmente

        // Altura de cada fila de datos
        val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
        for (data in lista) {
            val descripcionLayout = StaticLayout(
                data.Descripcion, paint, 100,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
            )
            ticketHeight += descripcionLayout.height.toFloat() + 20f
        }

        // Altura del total
        ticketHeight += 160f

        return ticketHeight
    }

    //FUNCION PARA MOSTRAR VENTANA DE PAGO
    private fun alertaPago(total: Float){
        val dialogo = Dialog(this@Detallepedido)
        dialogo.show()
        dialogo.setContentView(R1.layout.vista_cobro)
        dialogo.setCancelable(false)

        val etTotal = dialogo.findViewById<TextInputEditText>(R1.id.txtTotalPago)
        etTotal.setText("$" + "${String.format("%.2f", total)}")

        val etCambio = dialogo.findViewById<TextInputEditText>(R1.id.txtCambioPago)
        var cambio = 0f

        val etPago = dialogo.findViewById<TextInputEditText>(R1.id.txtEfectivoPago)
        var pagoCliente = 0f

        etPago.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //NADA QUE HACER
            }

            override fun onTextChanged(pago: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(pago.isNullOrEmpty()){
                    pagoCliente = 0f
                    cambio = pagoCliente - total
                    etCambio.setText("${String.format("%.2f", cambio)}")
                }else{
                    pagoCliente = pago.toString().toFloat()
                    cambio = pagoCliente - total
                    etCambio.setText("${String.format("%.2f", cambio)}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //NADA QUE HACER
            }

        })

        //PROCESO DEL BOTON ACEPTAR
        dialogo.findViewById<Button>(R1.id.btnaceptar).setOnClickListener {
            if(terminosPedidos == "Contado" && etPago.text.toString().isEmpty()){
                Toast.makeText(this@Detallepedido, "DEBE DE INGRESAR EL PAGO DEL CLIENTE", Toast.LENGTH_SHORT)
                    .show()
            }else{
                CoroutineScope(Dispatchers.IO).launch {
                    pedidosController.actualizarPagoCambioPedido(this@Detallepedido, idpedido,
                        pagoCliente, cambio
                    )
                }
                dialogo.dismiss()
                imprimirRecibo()
            }
        }

        //PROCESO DEL BOTON CANCELAR
        dialogo.findViewById<Button>(R1.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }

    }

}