package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.database.Database
import com.example.acae30.modelos.JSONmodels.BusquedaPedidoJSON
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_historico_pedidos.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class HistoricoPedidos : AppCompatActivity() {

    private var idCliente: Int = 0
    private var nombreCliente: String? = ""
    private lateinit var edtCliente : EditText
    private lateinit var edtHasta: EditText
    private lateinit var edtDesde: EditText
    private lateinit var btnBuscarPedidos: Button

    private var idVendedor = 0
    private var nombreVendedor: String = ""

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var url: String? = null
    private var alert: AlertDialogo? = null
    private var funciones: Funciones? = null
    private var database: Database? = null

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvMsj : TextView
    private lateinit var tvTitulo : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_pedidos)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        edtCliente = findViewById(R.id.edtCliente)
        edtDesde = findViewById(R.id.etFechaDesde)
        edtHasta = findViewById(R.id.etFechaHasta)
        btnBuscarPedidos = findViewById(R.id.btnProcesarBusqueda)
        alert = AlertDialogo(this@HistoricoPedidos)
        funciones = Funciones()
        database = Database(this@HistoricoPedidos)

        nombreVendedor = preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)
        idCliente = intent.getIntExtra("idCliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente")

         if(nombreCliente != ""){
            edtCliente.setText(nombreCliente)
        }

        getApiUrl()

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStart() {
        super.onStart()

        edtDesde.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                edtDesde.setText(dateStr)
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        edtHasta.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                edtHasta.setText(dateStr)
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        imgRegresar.setOnClickListener { regresarInicio() }

        imgBuscarCliente.setOnClickListener { buscarCliente() }

        btnBuscarPedidos.setOnClickListener {
            if (url != null) {
                if (funciones!!.isNetworkConneted(this)) {
                    alert!!.Cargando() //MUESTRA EL MENSAJE DE CARGA
                    GlobalScope.launch(Dispatchers.IO) {
                        obtenerPedidos(idCliente, edtDesde.text.toString(), edtHasta.text.toString(), idVendedor, nombreVendedor)
                    } //COURUTINA PARA OBTENER EL HISTORIAL DE PEDIDOS
                } else {
                    mensajeError("WIFI")
                }
            } else {
                mensajeError("SERVIDOR")
            }
        }
    }

    //FUNCION PARA REALIZAR LA BUSQUEDA DE PEDIDOS
    private fun obtenerPedidos(id_cliente: Int, desde: String, hasta: String, id_vendedor: Int, nombre_vendedor: String) {
        try {
            val datos = BusquedaPedidoJSON(
                id_cliente,
                desde,
                hasta,
                id_vendedor,
                nombre_vendedor
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "pedido/search"
            val url = URL(ruta)
            //println("DIRECCION DEL SERVIDOR: $ruta")
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    println("CODIGO DE RESPUESTA DEL SERVIDOR: $responseCode")
                    when(responseCode){
                        200 -> {
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        cargarPedidos(res)
                                    } else {
                                        runOnUiThread {
                                            mensajeError("NO_ENCONTRADO")
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {
                            runOnUiThread {
                                mensajeError("ERROR_CARGAR")
                            }
                        }
                        404 -> {
                            runOnUiThread {
                                mensajeError("NO_ENCONTRADO")
                            }
                        }
                        else -> {
                            runOnUiThread {
                                mensajeError("SERVIDOR")
                            }
                        }
                    }
                } catch (e: Exception) {
                    throw  Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

   private fun cargarPedidos(json: JSONArray){
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.delete("ventasTemp", null, null) //LIMPIANDO LA TABLA VENTASTEMP
            bd.delete("ventasDetalleTemp", null, null) //LIMPIANDO LA TABLA VENTASDETALLETEMP

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("fecha", funciones!!.validateJsonIsnullString(dato, "fecha"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("id_sucursal", dato.getInt("id_sucursal"))
                valor.put("id_vendedor", dato.getInt("id_vendedor"))
                valor.put("total", funciones!!.validate(dato.getString("total").toFloat()))
                valor.put("numero", dato.getInt("numero"))

                //ASIGNADO EL JSON INTERNO A UNA VARIABLE
                val detalleVen = dato.getJSONArray("detalleVentas")// EXTRAEMOS EL JSON INTERNO

                //RECORRIENDO EL JSON DETALLEVENTAS
                for(x in 0 until detalleVen.length()){
                    val detalle = detalleVen.getJSONObject(x)
                    val item = ContentValues()
                    item.put("id_venta", detalle.getInt("id_ventas"))
                    item.put("id_producto", detalle.getInt("id_producto"))
                    item.put("producto", funciones!!.validateJsonIsnullString(detalle,"producto"))
                    item.put("precio_u_iva", funciones!!.validate(detalle.getString("precio_u_iva").toFloat()))
                    item.put("cantidad", detalle.getInt("cantidad"))
                    item.put("total_iva", funciones!!.validate(detalle.getString("total_iva").toFloat()))

                    bd.insert("ventasDetalleTemp", null, item) //INSERTANDO EN VENTASDETALLETEMP
                }
                bd.insert("ventasTemp", null, valor) //INSERTANDO EN VENTASDETALLE
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
            alert!!.dismisss()
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA LA URL DEL SERVIDOR
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.isNotEmpty() && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }

    //FUNCIONES DE INTERFAZ
    private fun regresarInicio(){
        val intent = Intent(this@HistoricoPedidos, Inicio::class.java)
        startActivity(intent)
        finish()
    }
    private fun buscarCliente(){
        val intent = Intent(this@HistoricoPedidos, Clientes::class.java)
        intent.putExtra("Historico", true)
        startActivity(intent)
        finish()
    }
    private fun mensajeError(msj: String){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMsj = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        alert!!.dismisss()

        val mensajeDialogo: String = when(msj){
            "ERROR_CARGAR" -> { "Error al Cargar los Pedidos" }
            "NO_ENCONTRADO" -> { "No se Encontraron pedidos Registrados" }
            "SERVIDOR" -> { "Error al intentar conectarse con el Servidor" }
            "WIFI" -> { "ENCIENDE TUS WIFI/DATOS MÓVILES POR FAVOR" }
            else -> { "ERROR AL CONECTARSE CON EL SERVIDOR" }
        }

        tvMsj.text = mensajeDialogo
        tvTitulo.text = getString(R.string.titulo_msj_informacion)
        tvCancel.text = getString(R.string.btn_salir)
        tvUpdate.visibility = View.GONE

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }
    //FUNCIONES DE INTERFAZ
}