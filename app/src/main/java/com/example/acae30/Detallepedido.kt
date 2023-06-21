package com.example.acae30

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.modelos.*
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_inicio.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import com.example.acae30.R as R1


class Detallepedido : AppCompatActivity() {

    //AGREGANDO EL SPINNER DE SUCURSALES
    private var spSucursal: Spinner? = null
    private var sinSucursal: TextView? = null
    private var idSucursal: Int? = null
    private var codigoSucursal: Int? = null
    private var sucursalName: String = ""
    private var nombreSucursalPedido: String? = ""
    private var pedidoEnviado: Boolean = false
    private var getSucursalPosition: Int? = null
    private lateinit var exportar : Button
    private lateinit var swcaes: Switch
    private lateinit var swruta: Switch
    private var tipoEnvio: Int? = null
    private lateinit var swFactura: Switch
    private lateinit var  swCredito: Switch
    private var tipoDocumento: String = "FC"


    private var btbuscarProducto: ImageButton? = null
    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var txtcliente: TextView? = null
    private var lienzo: ConstraintLayout? = null
    private var idpedido = 0
    private var visita_enviada: Boolean? = null
    private var from: String? = ""
    private var db: Database? = null
    private var funciones: Funciones? = null
    private var recicler: RecyclerView? = null
    private var txtfecha_creacion: TextView? = null
    private var txttotal: TextView? = null
    private var btnatras: ImageButton? = null
    private var btneliminar: Button? = null
    private var cabezera: Pedidos? = null
    private var btnenviar: Button? = null
    private var btnguardar: Button? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var idvendedor = 0
    private var idvisita = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null
    private var codigo = ""
    private var idapi = 0

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
    private val tituloText = "DETALLE DEL PEDIDO"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R1.layout.activity_detallepedido)
        supportActionBar?.hide()
        btnatras = findViewById(R1.id.imbtnatras)
        val intento = intent
        alerta = AlertDialogo(this)
        idcliente = intento.getIntExtra("id", 0)
        nombre = intento.getStringExtra("nombrecliente")
        idpedido = intento.getIntExtra("idpedido", 0)
        idvisita = intento.getIntExtra("visitaid", 0)
        codigo = intento.getStringExtra("codigo").toString()
        idapi = intento.getIntExtra("idapi", 0)
        from = intento.getStringExtra("from").toString()
        txtfecha_creacion = findViewById(R1.id.fecha_creacion)
        txtcliente = findViewById(R1.id.txtCliente)
        btbuscarProducto = findViewById(R1.id.imgbtnadd)
        lienzo = findViewById(R1.id.lienzo)
        funciones = Funciones()
        db = Database(this)
        txttotal = findViewById(R1.id.txttotal)
        btneliminar = findViewById(R1.id.btncancelar)
        btnenviar = findViewById(R1.id.btnenviar)
        btnguardar = findViewById(R1.id.btnguardar)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idvendedor = preferencias!!.getInt("Idvendedor", 0)
        vendedor = preferencias!!.getString("Vendedor", "").toString()
        ip = preferencias!!.getString("ip", "").toString()
        puerto = preferencias!!.getInt("puerto", 0)
        visita_enviada = false

        exportar = findViewById(R1.id.btnexportar)

        //FUNCION PARA OBTENER LA INFORMACION DEL PEDIDO
        getTipoEnvio(idpedido)

        //FUNCIONES AGRAGADAS PARA LOS CONTROLES DE ENVIO Y DOCUMENTOS
        swcaes = findViewById(R1.id.swcaes)
        swruta = findViewById(R1.id.swruta)
        swFactura = findViewById(R1.id.swFactura)
        swCredito = findViewById(R1.id.swCredito)

        //ACTIVADO EL TIPO DE ENVIO CORRESPONDIENTE
        if(tipoEnvio==0){
            swruta.isChecked = true
        }else{
            swcaes.isChecked = true
        }

        //ACTIVANDO EL TIPO DE DOCUMENTO
        if(tipoDocumento == "CF"){
            swCredito.isChecked = true
        } else {
            swFactura.isChecked = true
        }

        //LOGICA DE TIPO DE ENVIO
        swcaes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                swruta.isChecked = false
                updateTipoPedido(1, idpedido)
            } else {
                swruta.isChecked = true
            }
        }

        swruta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                swcaes.isChecked = false
                updateTipoPedido(0, idpedido)
            } else {
                swcaes.isChecked = true
            }
        }

        //LOGICA DE TIPO DE DOCUMENTO
        swFactura.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                swCredito.isChecked = false
                updateTipoDocumento("FC", idpedido)
            }else{
                swCredito.isChecked = true
            }
        }

        swCredito.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                swFactura.isChecked = false
                updateTipoDocumento("CF", idpedido)
            }else{
                swFactura.isChecked = true
            }
        }

        recicler = findViewById(R1.id.reciclerdetalle)
        sinSucursal = findViewById(R1.id.sinSucursal) //TEXTVIEW PARA LOS CLIENTES SIN SUCURSALES
        sinSucursal!!.visibility = View.GONE //DESHABILITAMOS LA VISIBILIDAD DEL TEXVIEW

        //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
        cargarSucursales()


        //CARTURANDO LA SUCURSAL SELECCIONADA
         getSucursalPosition = intento.getIntExtra("sucursalPosition", 0)
        //println("Sucursal desde Agregar Producto: $getSucursalPosition")
        if(getSucursalPosition != 0){
            spSucursal!!.setSelection(getSucursalPosition!!, true)
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
                    txtfecha_creacion!!.text = cursor.getString(5)
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

        // Si no hay visita, que no se pueda enviar pedido
        if (!visita_enviada!!) {
            btnenviar!!.isEnabled = false
            btnenviar!!.setBackgroundResource(R1.drawable.border_btndisable)
        }

        btnatras!!.setOnClickListener {
            val intento = Intent(this, Pedido::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        btbuscarProducto!!.setOnClickListener {
            if (idcliente >= 0) {
                val intento = Intent(this, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", txtcliente!!.text.toString())
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
        btneliminar!!.setOnClickListener {
            AlertaEliminar()
        }

        validarDatos()

        btnenviar!!.setOnClickListener {
            if (idpedido > 0) {
                if (funciones!!.isNetworkConneted(this)) {
                    GlobalScope.launch(Dispatchers.IO) {
                        //this@Detallepedido.lifecycleScope.launch {
                        try {
                            runOnUiThread {
                                alerta!!.pedidoEnviado()
                            }
                            Timer().schedule(2300){
                                var pedido = getPedidoSend(idpedido) //retorna el pedido
                                pedido!!.Idvendedor = idvendedor
                                pedido.Vendedor = vendedor
                                //agregamos los datos del vendedor
                                SendPedido(pedido, idpedido)
                                //envia el pedido y actualiza el estado del pedido en el cel
                                runOnUiThread {
                                    alerta!!.dismisss()
                                    var visitaAbierta = getEstadoVisita()

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
                            runOnUiThread {
                                alerta!!.dismisss()
                                val alert: Snackbar = Snackbar.make(
                                    lienzo!!,
                                    e.message.toString(),
                                    Snackbar.LENGTH_LONG
                                )
                                alert.view.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@Detallepedido,
                                        R1.color.moderado
                                    )
                                )
                                alert.show()
                            }
                        }
                    }
                } else {
                    val alert: Snackbar =
                        Snackbar.make(lienzo!!, "Enciende tu wifi", Snackbar.LENGTH_LONG)
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R1.color.moderado))
                    alert.show()
                } //valida conexion a internet
            }
        }//boton de enviar pedido

        btnguardar!!.setOnClickListener {
            if (idpedido > 0) {

                if (ConfirmarDetallePedido() > 0.toInt()) {
                    val bd = db!!.writableDatabase
                    try {
                        bd!!.execSQL("UPDATE pedidos set Cerrado=1 WHERE Id=$idpedido")
                    } catch (e: Exception) {
                        throw Exception(e.message)
                    } finally {
                        bd!!.close()
                    }

                    try {
                        runOnUiThread {
                            alerta!!.pedidoGuardado()
                            alerta!!.changeText("Guardando Pedido")
                        }
                        Timer().schedule(2300){
                            runOnUiThread {
                                alerta!!.dismisss()
                                var visitaAbierta = getEstadoVisita()

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
                        runOnUiThread {
                            alerta!!.dismisss()
                            val alert: Snackbar =
                                Snackbar.make(lienzo!!, e.message.toString(), Snackbar.LENGTH_LONG)
                            alert.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@Detallepedido,
                                    R1.color.moderado
                                )
                            )
                            alert.show()
                        }
                    }
                } else {
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        "Error: PRIMERO No hay productos agregados al pedido.",
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R1.color.moderado))
                    alert.show()
                }
            }
        }

        //BOTON DE EXPORTAR A PDF EL PEDIDO
        exportar.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            verificarPermisos(it)
        }

        //IMPLEMENTANDO LOGICA DE SUCURSAL SELECCIONADA EN SPINNER
        spSucursal!!.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sucursalName = parent?.getItemAtPosition(position).toString()

                if (sucursalName == "-- SELECCIONE UNA SUCURSAL --") {
                    btnguardar!!.isEnabled = false
                    btnenviar!!.isEnabled = false
                    btnenviar!!.setBackgroundResource(R1.drawable.border_btndisable)
                    btnguardar!!.setBackgroundResource(R1.drawable.border_btndisable)
                }else{
                    btnguardar!!.isEnabled = true
                    btnenviar!!.isEnabled = true
                    btnenviar!!.setBackgroundResource(R1.drawable.border_btnactualizar)
                    btnguardar!!.setBackgroundResource(R1.drawable.border_btnenviar)

                    getSucursalPosition = spSucursal!!.selectedItemPosition
                    //println("Sucursal Seleccioada: $getSucursalPosition")

                    updatePedidoSucursal(idcliente, sucursalName, idpedido)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //NADA IMPLEMENTADO
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
                codigoSucursal = data.codigoSucursal.toInt()
            }
            db!!.execSQL("UPDATE pedidos set id_sucursal=$idSucursal, codigo_sucursal=$codigoSucursal, nombre_sucursal='$nombreSucursal' WHERE id=$idpedidos")
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
        spSucursal = findViewById(R1.id.spSucursal)
        val listSucursal = nombreSucursal().toMutableList()

        val adaptador = ArrayAdapter(this@Detallepedido, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R1.layout.support_simple_spinner_dropdown_item)
        spSucursal!!.adapter = adaptador
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
                spSucursal!!.visibility = View.GONE
                sinSucursal!!.visibility = View.VISIBLE
            }
            dataSucursal.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaSucursales
    }


    //SE MODIFICO PARA AGREGAR FUNCIONABILIDAD DE LAS SUCURSALES
    //Y DE LOS TIPOS DE ENVIO
    @OptIn(DelicateCoroutinesApi::class)
    fun validarDatos() {
        if (idpedido > 0) {
           // btbuscar!!.visibility = View.GONE
            txtcliente!!.isEnabled = false
            txtcliente!!.text = nombre
            GlobalScope.launch(Dispatchers.IO) {
                //this@Detallepedido.lifecycleScope.launch {

                try {
                    val lista = getPedido(idpedido)
                    if (lista != null && lista.size > 0) {
                        if (cabezera != null) {
                            if (cabezera!!.Cerrado == 1) {
                              //  btbuscar!!.visibility = View.GONE
                                txtcliente!!.isEnabled = false
                                btbuscarProducto!!.visibility = View.GONE
                                btnenviar!!.visibility = View.GONE

                                exportar.visibility = View.GONE //DESHABILITANDO EL BOTON EXPORTAR - FERRETERIA EL REY

                                val visitaAbierta = getEstadoVisita()

                                if (visitaAbierta == 0 && !cabezera!!.Enviado) {
                                    //RUTINA PARA SOLO ENVIAR EL PEDIDO
                                    btnenviar!!.visibility = View.VISIBLE
                                    exportar.visibility = View.GONE
                                }

                                btnguardar!!.visibility = View.GONE
                                btneliminar!!.visibility = View.GONE

                                //MOSTRANDO EL NOMBRE DE LA SUCURSAL
                                if(nombreSucursalPedido != ""){
                                    sinSucursal!!.text = nombreSucursalPedido
                                }
                                else{
                                    sinSucursal!!.text = "NO TIENE SUCURSAL REGISTRADA"
                                }
                                //DESHABILITANDO EL SPINNER DE SUCURSALES
                                spSucursal!!.visibility = View.GONE

                                //DESHABILITANDO LOS TIPOS DE ENVIO
                                if(pedidoEnviado == true){
                                    swcaes.isEnabled = false
                                    swruta.isEnabled = false
                                    swFactura.isEnabled = false
                                    swCredito.isEnabled = false
                                }else{
                                    swcaes.isEnabled = true
                                    swruta.isEnabled = true
                                }

                            } else {

                              //  btbuscar!!.visibility = View.VISIBLE

                               // btbuscar!!.visibility = View.GONE
                              //  btbuscar!!.visibility = View.GONE
                                txtcliente!!.isEnabled = false
                                btbuscarProducto!!.visibility = View.VISIBLE
                                btnenviar!!.visibility = View.VISIBLE
                                btnguardar!!.visibility = View.VISIBLE
                                btneliminar!!.visibility = View.VISIBLE
                                exportar.visibility = View.GONE
                            }
                        }
                        ArmarLista(lista)
                    } else if (from == "visita") {

                        //RUTINA PARA AGREGAR NUEVO PEDIDO

                      //  btbuscar!!.visibility = View.VISIBLE

                      //  btbuscar!!.visibility = View.GONE
                      //  btbuscar!!.visibility = View.GONE
                        txtcliente!!.isEnabled = false
                        btbuscarProducto!!.visibility = View.VISIBLE
                        btnenviar!!.visibility = View.VISIBLE
                        btnguardar!!.visibility = View.VISIBLE
                        btneliminar!!.visibility = View.VISIBLE
                        exportar.visibility = View.GONE
                    } else {
                        throw Exception("Error al encontrar el pedido")
                    }

                    if (cabezera!!.Cerrado == 1) {
                        btnatras!!.visibility = View.VISIBLE
                    } else {
                        btnatras!!.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R1.color.moderado))
                    alert.show()
                }
            }

        } else {
            if (idcliente > 0) {
                txtcliente!!.text = nombre
              //  btbuscar!!.visibility = View.GONE
            } else {
              //  btbuscar!!.visibility = View.VISIBLE
            }
        }
    } //VALIA QUE YA EXISTA EL CLIENTE DATOS

    private fun getPedido(id: Int): ArrayList<DetallePedido> {
        val base = db!!.writableDatabase
        try {
            val pedido = base.rawQuery("SELECT * FROM pedidos where Id=$id", null)
            if (pedido.count > 0) {
                pedido.moveToFirst()
                cabezera = Pedidos(
                    pedido.getInt(0),
                    pedido.getInt(1),
                    pedido.getString(2),
                    pedido.getFloat(3),
                    pedido.getFloat(4),
                    pedido.getInt(5) == 1,
                    pedido.getString(6),
                    pedido.getInt(7),
                    pedido.getString(8),
                    pedido.getInt(9),
                    pedido.getInt(10),
                    pedido.getString(11)
                )
                pedido.close()
            }

            val cursor = base.rawQuery("SELECT *  FROM detalle_producto where Id_pedido=$id", null)
            val lista = ArrayList<DetallePedido>()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val detalle = DetallePedido(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getFloat(5),
                        cursor.getFloat(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getString(15),
                        cursor.getString(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getString(19),
                        cursor.getFloat(20),
                        cursor.getInt(21),
                        cursor.getFloat(22),
                        cursor.getString(23),
                        cursor.getInt(24),
                        cursor.getInt(25)
                    )
                    lista.add(detalle)
                } while (cursor.moveToNext())
                cursor.close()
            }
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }

    } //obtiene el detalle del pedido


    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun ArmarLista(lista: ArrayList<DetallePedido>) {
        var total = 0.toFloat()
        val mLayoutManager = LinearLayoutManager(
            this@Detallepedido,
            LinearLayoutManager.VERTICAL,
            false
        )
        recicler!!.layoutManager = mLayoutManager
        val adapter = PedidoDetalleAdapter(lista, this@Detallepedido) { i ->
            if (cabezera != null) {
                if (!cabezera!!.Enviado) {
                    val data = lista.get(i)
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
        }
        for (i in lista) {
            total = total + i.Subtotal!!
        }
        txttotal!!.text = "$" + "${String.format("%.4f", total)}"
        recicler!!.adapter = adapter

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
                        lienzo!!,
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

    private fun SendPedido(pedido: CabezeraPedidoSend, idpedido: Int) {
        try {
            val objecto = convertToJson(pedido, idpedido) //convertimos a json el objecto pedido
            val ruta: String = "http://$ip:$puerto/pedido" //ruta para enviar el pedido
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
                                                throw Exception(datosservidor.getString("response"))
                                            }
                                        }
                                        400 -> {
                                            throw Exception(datosservidor.getString("response"))
                                        }
                                        500 -> {
                                            throw Exception(datosservidor.getString("response"))
                                        }
                                        else -> {
                                            throw Exception("Ocurrio algo Intenta Nuevamente")
                                        }
                                    }
                                } else {
                                    throw Exception("No se recibio ninguna respuesta del servidor")
                                }
                            } else {
                                throw Exception("No se recibio ninguna respuesta del servidor")
                            }
                        } catch (e: Exception) {
                            throw Exception(e.message)
                        }
                    } //se obtiene la respuesta del servidor

                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: SEGUNDO No hay productos agregados al pedido.")
          //  print(e.message)
        }
    } //funcion que envia el pedido a la bd

    private fun ConfirmarPedido(idpedido: Int, idservidor: Int) {
        val bd = db!!.writableDatabase
        try {
            bd!!.execSQL("UPDATE pedidos set Id_pedido_sistema=$idservidor,Enviado=1,Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    } //actualiza el pedido y confirma que se envio

    //FUNCION PARA ACTUALIZAR EL TIPO DE ENVIO SELECCIONADO
    private fun updateTipoPedido(tipoPedido:Int, idpedido:Int){
        val data = db!!.writableDatabase
        try {
            data!!.execSQL("UPDATE pedidos set tipo_envio=$tipoPedido WHERE id=$idpedido")
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
    }

    //FUNCION PARA ACTUALIZAR EL TIPO DE DOCUMENTO SELECCIONADO
    private fun updateTipoDocumento(tipoDocumento:String, idpedido: Int){
        val data = db!!.writableDatabase
        try {
            data.execSQL("UPDATE pedidos SET tipo_documento='$tipoDocumento' WHERE id=$idpedido")
        }catch (e: Exception){
            throw Exception(e.message)
        }finally {
            data.close()
        }
    }

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

            val documento = Document(PageSize.LETTER, 2.5f, 2.5f, 3.5f, 3.5f)
            PdfWriter.getInstance(documento, fos)

            documento.open()

            //ESPACIOS
            val espaciosDocumento = Paragraph(
                "\n\n\n"
            )
            documento.add(espaciosDocumento)

            //DATOS DE LA EMPRESA
            val tablaEncabezado = PdfPTable(2)
            tablaEncabezado.widthPercentage = 80f
            val cellInforEmpresa = PdfPCell(Paragraph("FERRETERIA EL REY S.A DE C.V\n" +
                    "DESVIOS LOS MANGOS, LA UNION\n" +
                    "TEL. 2668-9999\n\n\n",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            ))
            cellInforEmpresa.horizontalAlignment = Element.ALIGN_LEFT
            cellInforEmpresa.border = 0
            tablaEncabezado.addCell(cellInforEmpresa)

            val cellLogo = PdfPCell(Paragraph("[LOGO]"))
            cellLogo.horizontalAlignment = Element.ALIGN_CENTER
            cellLogo.border = 0
            tablaEncabezado.addCell(cellLogo)
            documento.add(tablaEncabezado)

            //DATOS DEL CLIENTE
            val tablaCliente = PdfPTable(1)
            tablaCliente.widthPercentage = 80f
            val cellInforCliente = PdfPCell(Paragraph("$nombreCliente\n" +
                    "$nombreSucursalPedido\n\n\n",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            ))
            cellInforCliente.horizontalAlignment = Element.ALIGN_LEFT
            cellInforCliente.border = 0
            tablaCliente.addCell(cellInforCliente)
            documento.add(tablaCliente)

            //AGREGANDO TITULO PEDIDO
            val fechaDocumento = Paragraph(
                "$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //DATOS DEL PEDIDO
            val tablaPedido = PdfPTable(4)
            tablaPedido.widthPercentage = 80f

            val cellReferencia = PdfPCell(Paragraph("REFERENCIA",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellReferencia.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellReferencia)

            val cellDescripcion = PdfPCell(Paragraph("DESCRIPCION",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellDescripcion.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellDescripcion)

            val cellCantidad = PdfPCell(Paragraph("CANTIDAD",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellCantidad.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellCantidad)

            val cellTotal = PdfPCell(Paragraph("TOTAL",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            ))
            cellTotal.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellTotal)

            //AGREGANDO EL CONTENIDO DEL PEDIDO
            val lista = getPedido(idpedido)
            var total = 0f

            for(data in lista){

                val cellReferenciaP = PdfPCell(Paragraph(""+data.Codigo,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellReferenciaP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellReferenciaP)

                val cellDescripcionP = PdfPCell(Paragraph(""+data.Descripcion,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellDescripcionP.horizontalAlignment = Element.ALIGN_LEFT
                tablaPedido.addCell(cellDescripcionP)

                val cellCantidadP = PdfPCell(Paragraph(""+data.Cantidad,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellCantidadP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellCantidadP)

                val cellTotalP = PdfPCell(Paragraph("$ "+data.Total,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                ))
                cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
                tablaPedido.addCell(cellTotalP)

                total += data.Total!!
            }

            val cellReferenciaP = PdfPCell(Paragraph(""))
            cellReferenciaP.border = 0
            tablaPedido.addCell(cellReferenciaP)

            val cellDescripcionP = PdfPCell(Paragraph(""))
            cellDescripcionP.border = 0
            tablaPedido.addCell(cellDescripcionP)

            val cellCantidadP = PdfPCell(Paragraph("TOTAL",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            ))
            cellCantidadP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellCantidadP)

            val cellTotalP = PdfPCell(Paragraph("$ "+ total,
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            ))
            cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellTotalP)
            documento.add(tablaPedido)


            //DATOS DEL VENDEDOR
            val tablaVendedor = PdfPTable(1)
            tablaVendedor.widthPercentage = 80f
            val cellInforVendedor = PdfPCell(Paragraph("\n\nVENDEDOR: $vendedor\n" +
                    "FECHA DEL PEDIDO: $fecha\n",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            ))
            cellInforVendedor.horizontalAlignment = Element.ALIGN_LEFT
            cellInforVendedor.border = 0
            tablaVendedor.addCell(cellInforVendedor)
            documento.add(tablaVendedor)

            documento.close()

            Toast.makeText(this, "PEDIDO EXPORTADO CORRECTAMENTE", Toast.LENGTH_LONG).show()

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }

}