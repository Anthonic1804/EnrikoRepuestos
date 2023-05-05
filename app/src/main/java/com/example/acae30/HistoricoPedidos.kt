package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.modelos.JSONmodels.BusquedaPedidoJSON
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_historico_pedidos.imgBuscarCliente
import kotlinx.android.synthetic.main.activity_historico_pedidos.imgRegresar
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_pedidos)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        edtCliente = findViewById(R.id.edtCliente)
        edtDesde = findViewById(R.id.etFechaDesde)
        edtHasta = findViewById(R.id.etFechaHasta)
        btnBuscarPedidos = findViewById(R.id.btnProcesarBusqueda)

        nombreVendedor = preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)
        idCliente = intent.getIntExtra("idCliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente")

         if(nombreCliente != ""){
            edtCliente.setText(nombreCliente)
        }

        getApiUrl()

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
            obtenerPedidos(idCliente, edtDesde.text.toString(), edtHasta.text.toString(), idVendedor, nombreVendedor)
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
            println("DIRECCION DEL SERVIDOR: $ruta")
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
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
                                    val res: JSONArray = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        cargarPedidos(res)
                                    } else {
                                        mensajeError("NO_ENCONTRADO")
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {mensajeError("ERROR_CARGAR")}
                        404 -> {mensajeError("NO_ENCONTRADO")}
                        else -> {throw Exception("Error de comunicacion con el servidor")}
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
        println("DATOS RECOLECTADOS DEL JSON_ARRAY: \n $json")
        Toast.makeText(this@HistoricoPedidos, "PEDIDOS CARGADOS CORRECTAMENTE", Toast.LENGTH_LONG)
            .show()
    }
    private fun mensajeError(mensaje: String){
        when(mensaje){
            "ERROR_CARGAR" -> {
                Toast.makeText(this@HistoricoPedidos, "ERROR AL CARGAR LOS PEDIDOS", Toast.LENGTH_LONG)
                    .show()
            }
            "NO_ENCONTRADO" -> {
                Toast.makeText(this@HistoricoPedidos, "NO SE ENCONTRARON PEDIDOS REGISTRADOS", Toast.LENGTH_LONG)
                    .show()
            }
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
    //FUNCIONES DE INTERFAZ
}