package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.acae30.modelos.JSONmodels.BusquedaPedidoJSON
import com.example.acae30.modelos.JSONmodels.TokenDataClassJSON
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_historico_pedidos.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

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

        val fechaDesde = edtDesde.text.toString()
        val fechaHasta = edtHasta.text.toString()

        if(nombreCliente != ""){
            edtCliente.setText(nombreCliente)
        }

        getApiUrl()

        edtDesde.setOnClickListener { calendarDialogDesde() }

        edtHasta.setOnClickListener { calendarDialogHasta() }

        imgRegresar.setOnClickListener { regresarInicio() }

        imgBuscarCliente.setOnClickListener { buscarCliente() }

        btnBuscarPedidos.setOnClickListener { obtenerPedidos(idCliente, fechaDesde, fechaHasta, idVendedor, nombreVendedor) }

    }

    //FUNCION PARA REALIZAR LA BUSQUEDA DE PEDIDOS
    private fun obtenerPedidos(idCliente: Int, fechaDesde: String, fechaHasta: String, idVendedor: Int, nombreVendedor: String) {
        try {
            val datos = BusquedaPedidoJSON(
                idCliente,
                fechaDesde,
                fechaHasta,
                idVendedor,
                nombreVendedor
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "pedido/search"
            val url = URL(ruta)
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
                    if (responseCode == 200) {
                        cargarPedidos()
                    }else {
                        throw Exception("Error de comunicacion con el servidor")
                    }

                } catch (e: Exception) {
                    throw  Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private fun cargarPedidos(){

    }

    //FUNCION PARA LA URL DEL SERVIDOR
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.isNotEmpty() && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }


    //FUNCIONES PARA CALENDARIOS
    private fun calendarDialogDesde() {
        val datePicker = DatePickerFragment{day, month, year -> onDateSelectedDesde(day, month, year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    private fun onDateSelectedDesde(day: Int, month:Int, year:Int){
        val mes = month + 1
        val date = "$year/$mes/$day"
        edtDesde.setText(date)
    }

    private fun calendarDialogHasta() {
        val datePicker = DatePickerFragment{day, month, year -> onDateSelectedHasta(day, month, year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    private fun onDateSelectedHasta(day: Int, month:Int, year:Int){
        val mes = month + 1
        val date = "$year/$mes/$day"
        edtHasta.setText(date)
    }
    //FUNCIONES PARA CALENDARIOS

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
}