package com.example.acae30

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.controllers.PedidosController
import com.example.acae30.database.Database
import com.example.acae30.listas.PedidosAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.JSONmodels.DatosReporteJSON
import com.example.acae30.modelos.JSONmodels.PedidoDTE
import com.example.acae30.modelos.Pedidos
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
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

class Pedido : AppCompatActivity() {

    private var funciones: Funciones? = null
    private var bd: Database? = null
    private var reciclado: RecyclerView? = null
    private var lienzo: ConstraintLayout? = null
    private var btnsincronizar: Button? = null
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var idvendedor = 0
    private var btnatras: ImageButton? = null
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var proviene: String? = ""
    var fechaDoc = ""

    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    private val tituloText = "DETALLE DE PEDIDOS ENVIADOS"

    private lateinit var btnReporte: FloatingActionButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var lblMensaje: TextView
    private lateinit var lblTitulo: TextView

   /* private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            isAceptado ->
        if(isAceptado){
            Toast.makeText(this, "PERMISOS CONCEDIDOS", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "PERMISOS DENEGADOS", Toast.LENGTH_LONG).show()
        }
    }*/

    private var pedidosController = PedidosController()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)
        btnsincronizar = findViewById(R.id.btnsincronizar)
        preferencias = getSharedPreferences(this.instancia, MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()
        ip = preferencias.getString("ip", "").toString()
        puerto = preferencias.getInt("puerto", 0)
        proviene = intent.getStringExtra("proviene")

        btnatras = findViewById(R.id.imbtnatras)
        btnReporte = findViewById(R.id.btnReporte)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        funciones = Funciones()
        bd = Database(this)
        reciclado = findViewById(R.id.recicler)


        lienzo = findViewById(R.id.lienzo)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val editor = preferencias.edit()
            editor.putBoolean("busqueda", true)
            editor.putBoolean("visita", true)
            editor.apply()

            val intento = Intent(this, Clientes::class.java)
            startActivity(intento)
            finish()
        }

        //sincronizar los datos que no se han enviado
        btnsincronizar!!.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                val pedidoDTE = pedidosController.obtenerPedidosNoTransmitidos(this@Pedido)
                var idPedidoDTE = 0
                if(pedidoDTE != null)
                {
                    idPedidoDTE = pedidoDTE.Id_pedido_sistema!!
                }

                if (idPedidoDTE > 0) {
                    obtenerPedidosDTEServidor(idPedidoDTE)
                }
            }

            //Toast.makeText(this@Pedido, "FUNCION EN CONSTRUCCION", Toast.LENGTH_SHORT).show()
            /*btnsincronizar!!.isEnabled = false
            if (isConnected()) {

                var alert: Snackbar =
                    Snackbar.make(
                        lienzo!!,
                        "Sincronizando datos...",
                        Snackbar.LENGTH_LONG
                    )
                alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                alert.show()

                var error = false


                //ENVIAR VISITAS QUE NO SE HAN ENVIADO

                var list = ArrayList<Visitas>() //lista donde se guardara las visitas

                val base = bd!!.readableDatabase
                try {
                    var visitas = base!!.rawQuery("SELECT * FROM visitas WHERE enviado=0", null)

                    if (visitas.count > 0) {
                        visitas.moveToFirst()
                        do {
                            val datos = Visitas(
                                visitas.getInt(0),
                                visitas.getInt(1),
                                visitas.getString(2),
                                visitas.getString(3),
                                visitas.getString(4),
                                visitas.getString(5),
                                visitas.getString(6),
                                visitas.getInt(7),
                                visitas.getString(8),
                                visitas.getString(9),
                                visitas.getString(10),
                                visitas.getInt(11) == 1,
                                visitas.getInt(12) == 1,
                                visitas.getInt(13) == 1
                            )

                            list.add(datos)

                        } while (visitas.moveToNext())

                        visitas.close()
                    }
                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 11: " + e.message)
                } finally {
                    base.close()
                }

                try {

                    list.forEach { visita ->
                        val datos_enviar = JSONObject()

                        val gpsdata_in = visita.Gps_in.split(",")
                        val gpsdata_out = visita.Gps_out.split(",")
                        val id_vendedor = preferencias.getInt("Idvendedor", 0)

                        datos_enviar.put("Id_app_visita", 0)
                        datos_enviar.put("Fecha_hora_checkin", visita.Fecha_inicial)
                        datos_enviar.put("Latitud_checkin", gpsdata_in.get(0))
                        datos_enviar.put("Longitud_checkin", gpsdata_in.get(1))
                        datos_enviar.put("Id_cliente", visita.Id_cliente)
                        datos_enviar.put("Cliente", visita.Nombre_cliente)
                        datos_enviar.put("Fecha_hora_checkout", visita.Fecha_final)
                        datos_enviar.put("Latitud_checkout", gpsdata_out.get(0))
                        datos_enviar.put("Longitud_checkout", gpsdata_out.get(1))
                        datos_enviar.put("comentarios", "")
                        datos_enviar.put("Id_vendedor", id_vendedor)

                        enviarVisita(datos_enviar, visita.Id)
                    }

                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 12: " + e.message)
                }

                //ENVIAR VISITAS QUE NO SE HA ENVIADO LOS DATOS DEL FINAL DE LA VISITA

                var list_visita_final = ArrayList<Visitas>() //lista donde se guardara las visitas

                val base_v_final = bd!!.readableDatabase
                try {
                    var visitas =
                        base_v_final!!.rawQuery("SELECT * FROM visitas WHERE enviado_final=0", null)

                    if (visitas.count > 0) {
                        visitas.moveToFirst()
                        do {
                            val datos = Visitas(
                                visitas.getInt(0),
                                visitas.getInt(1),
                                visitas.getString(2),
                                visitas.getString(3),
                                visitas.getString(4),
                                visitas.getString(5),
                                visitas.getString(6),
                                visitas.getInt(7),
                                visitas.getString(8),
                                visitas.getString(9),
                                visitas.getString(10),
                                visitas.getInt(11) == 1,
                                visitas.getInt(12) == 1,
                                visitas.getInt(13) == 1
                            )

                            list_visita_final.add(datos)

                        } while (visitas.moveToNext())

                        visitas.close()
                    }
                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 13: " + e.message)
                } finally {
                    base_v_final.close()
                }

                try {

                    list_visita_final.forEach { visita ->
                        var finVisita = JSONObject()

                        val gpsdata_out = visita.Gps_out.split(",")

                        finVisita.put("idvisita", visita.Idvisita)
                        finVisita.put("fecha", visita.Fecha_final)
                        finVisita.put("comentarios", "")
                        finVisita.put("nombreimagen", "")
                        finVisita.put("imagen", "")
                        finVisita.put("latitud", gpsdata_out.get(0))
                        finVisita.put("longitud", gpsdata_out.get(1))

                        Sendfinal(finVisita, visita.Id)
                    }

                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 14: " + e.message)
                }

                //ENVIAR PEDIDOS QUE NO SE HAN ENVIADO

                var listPedidos = ArrayList<Pedidos>() //lista donde se guardara las visitas

                val base_p = bd!!.readableDatabase

                try {

                    var cursor =
                        base_p!!.rawQuery("SELECT * FROM pedidos WHERE enviado=0", null)

                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        do {
                            val datos = Pedidos(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getString(2),
                                cursor.getFloat(11),
                                cursor.getFloat(5),
                                cursor.getInt(12),
                                cursor.getString(13),
                                cursor.getInt(14),
                                cursor.getString(15),
                                cursor.getInt(16),
                                cursor.getInt(17),
                                cursor.getString(18)
                            )

                            listPedidos.add(datos)

                        } while (cursor.moveToNext())

                        cursor.close()
                    }
                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 15: " + e.message)
                } finally {
                    base_p.close()
                }

                try {

                    listPedidos.forEach { pedido_l ->

                        try {
                            var pedido = getPedidoSend(pedido_l.Id) //retorna el pedido
                            pedido!!.Idvendedor = idvendedor
                            pedido.Vendedor = vendedor
                            SendPedido(pedido, pedido_l.Id)

                        } catch (e: Exception) {
                            val alert: Snackbar =
                                Snackbar.make(
                                    lienzo!!,
                                    "Ha ocurrido un error al sincronizar.",
                                    Snackbar.LENGTH_LONG
                                )
                            alert.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.moderado
                                )
                            )
                            alert.show()

                            println("Mensaje de error: " + e.message)
                        }

                    }

                } catch (e: Exception) {
                    val alert: Snackbar =
                        Snackbar.make(
                            lienzo!!,
                            "Ha ocurrido un error al sincronizar.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()

                    error = true

                    println("Error 16: " + e.message)
                }

                try {
                    val lista = GetPedido()
                    if (lista.size > 0) {
                        ShowList(lista)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        val alert: Snackbar = Snackbar.make(
                            lienzo!!,
                            e.message.toString(),
                            Snackbar.LENGTH_LONG
                        )
                        alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                        alert.show()

                        error = true

                        println("Error 17: " + e.message.toString())
                    }

                }

                if (error) {
                    alert =
                        Snackbar.make(
                            lienzo!!,
                            "No se han podido sincronizar los datos.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()
                } else {
                    alert =
                        Snackbar.make(
                            lienzo!!,
                            "Datos sincronizados correctamente.",
                            Snackbar.LENGTH_LONG
                        )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()
                }

            } else {
                val alert: Snackbar =
                    Snackbar.make(lienzo!!, "Enciende tu wifi", Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                alert.show()
            } //valida conexion a internet
            btnsincronizar!!.isEnabled = true
        */}

        btnatras!!.setOnClickListener {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        // SOLICITAR PERMISOS DE GPS
        solicitarPermisos()

        // MOSTRAR MENSAJE DE GPS
        if (proviene == "inicio") {
            AlertaGPS(this@Pedido)
        }

    }

    override fun onStart() {
        super.onStart()
        //GlobalScope.launch(Dispatchers.IO) {
        this@Pedido.lifecycleScope.launch {
            try {
                val lista = GetPedido()
                if (lista.size > 0) {
                    ShowList(lista)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                    alert.show()
                }
            }
        }

        //BOTON PARA GENERAR EL REPORTE DE PEDIDOS EN PDF
        btnReporte.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            mensajeReporte(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

    private fun ShowList(list: ArrayList<Pedidos>) {
        var mLayoutManager = LinearLayoutManager(this@Pedido, LinearLayoutManager.VERTICAL, false)
        reciclado!!.layoutManager = mLayoutManager
        val adapter = PedidosAdapter(list, this@Pedido) { position ->

            //GlobalScope.launch(Dispatchers.Main) {
            this@Pedido.lifecycleScope.launch {

                val data = list.get(position)

                val intento = Intent(this@Pedido, Detallepedido::class.java)
                intento.putExtra("nombrecliente", data.Nombre_cliente)
                intento.putExtra("idcliente", data.Id_cliente!!)
                intent.putExtra("codigo", "")
                intento.putExtra("idpedido", data.Id)
                intento.putExtra("from", "ver")
                startActivity(intento)
                finish()

            }

        }
        reciclado!!.adapter = adapter

    }

    private fun GetPedido(): ArrayList<Pedidos> {
        val base = bd!!.writableDatabase
        try {
            val cursor = base!!.rawQuery(
                "SELECT Id," +
                        " Id_cliente," +
                        " Nombre_cliente," +
                        " Total," +
                        " Descuento," +
                        " Enviado," +
                        " Fecha_enviado," +
                        " Id_pedido_sistema," +
                        " Gps," +
                        " Cerrado," +
                        " Idvisita," +
                        " strftime('%d/%m/%Y %H:%M'," +
                        " fecha_creado) as fecha_creado," +
                        "Sumas," +
                        "Iva," +
                        "Iva_percibido, " +
                        "pedido_dte, " +
                        "pedido_dte_error FROM pedidos " +
                        "order by id desc",
                null
            )
            var lista = ArrayList<Pedidos>()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {

                    val pedido = Pedidos(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getFloat(3),
                        cursor.getFloat(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getString(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getInt(15),
                        cursor.getInt(16),
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                    lista.add(pedido)

                } while (cursor.moveToNext())
                cursor.close()
            }
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

    }//obtiene el listado de los pedidos

    private fun enviarVisita(data: JSONObject, idvisita: Int) {
        try {
            val strinjson = data.toString()
            val ip = preferencias.getString("ip", "")
            val puerto = preferencias.getInt("puerto", 0)
            val direccion = "http://$ip:$puerto/visitas/registrar_visita"
            val url = URL(direccion)
            with(url.openConnection() as HttpURLConnection) {
                connectTimeout = 5000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json;charset=utf-8")
                val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                or.write(strinjson) //escribimos el json
                or.flush() //se envia el json
                val codigoRespuesta = responseCode
                when (codigoRespuesta) {
                    201 -> {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            val respuesta = StringBuffer()
                            var inpuline = it.readLine()
                            while (inpuline != null) {
                                respuesta.append(inpuline)
                                inpuline = it.readLine()
                            } //obtenemos la respuesta completa
                            it.close()
                            var data: String? = respuesta.toString()
                            if (data != null) {
                                val res = JSONObject(data)
                                if (!res.isNull("error") && !res.isNull("response")) {
                                    val idser = res.getInt("error")
                                    updateCheckIn(idser, idvisita)
                                    updateCheckOut(idvisita)
                                } else {
                                    println("Error en la respuesta del servidor")
                                    throw Exception("Error en la respuesta del servidor")
                                }
                            } else {
                                println("Error al recibir respuesta del servidor")
                                throw Exception("Error al recibir respuesta del servidor")
                            }
                        }
                    } //termina response 201

                    else -> {
                        println("Error al recibir respuesta del servidor")
                        throw Exception("Error al recibir respuesta del servidor")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error 3: " + e.message)
            throw Exception(e.message)
        }
    } //envia la data al servidor

    private fun updateCheckIn(idvisitaServer: Int, idvisita: Int) {
        val base = bd!!.writableDatabase
        try {
            val data = ContentValues()
            data.put("Idvisita", idvisitaServer)
            data.put("Enviado", true)
            data.put("Enviado_final", true)
            base.update("visitas", data, "Id=?", arrayOf(idvisita.toString()))

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    } //ACTUALIZA CON EL ID DEL PEDIDO DE LA BD

    private fun updateCheckOut(idvisita: Int) {
        val base = bd!!.writableDatabase
        try {
            val data = ContentValues()
            data.put("Enviado_final", true)
            base.update("visitas", data, "Id=?", arrayOf(idvisita.toString()))

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    } //ACTUALIZA CON EL ID DEL PEDIDO DE LA BD

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
//        super.onBackPressed();

    }//anula el boton atras

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        var isConnected = true

        isConnected = networkInfo != null && networkInfo.isConnected
        return isConnected
    }

    private fun getPedidoSend(idpedido: Int): CabezeraPedidoSend? {
        val base = bd!!.readableDatabase
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
                    pedido.getFloat(25),
                    pedido.getFloat(26),
                    pedido.getFloat(27),
                    pedido.getFloat(28),
                    pedido.getString(39),
                    pedido.getString(29),
                    pedido.getString(30),
                    pedido.getString(31),
                    pedido.getString(32),
                    pedido.getString(33),
                    pedido.getString(34),
                    pedido.getString(35),
                    pedido.getString(36),
                    pedido.getString(37),
                    pedido.getString(38),
                    null

                )
                pedido.close()
                var cdetalle =
                    base.rawQuery("SELECT * FROM detalle_producto WHERE Id_pedido=$idpedido", null)
                if (cdetalle.count > 0) {
                    var list = ArrayList<DetallePedido>() //lista donde se guardara el pedido
                    cdetalle.moveToFirst()
                    do {
                        var detalle = DetallePedido(
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
                    connectTimeout = 5000
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
                            var data: String? = respuesta.toString()
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
            throw Exception("Error: No hay productos agregados al pedido.")
            print(e.message)
        }
    } //funcion que envia el pedido a la bd

    private fun ConfirmarPedido(idpedido: Int, idservidor: Int) {
        val bd = bd!!.writableDatabase
        try {
            bd!!.execSQL("UPDATE pedidos set Id_pedido_sistema=$idservidor,Enviado=1,Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    } //actualiza el pedido y confirma que se envio

    private fun convertToJson(pedido: CabezeraPedidoSend, idpedido_param: Int): JsonObject {
        // CONSULTAR EL ID DE LA VISITA EN EL SERVIDOR
        var idvisita_v = 0.toInt()

        val base = bd!!.writableDatabase
        try {
            var cursor = base!!.rawQuery(
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

        var json = JsonObject()
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
        json.addProperty("Idvendedor", pedido.Idvendedor)
        json.addProperty("Vendedor", pedido.Vendedor)
        json.addProperty("Idapp", idvisita_v)

        //se ordena la cabezera
        var detalle = JsonArray()
        for (i in 0..(pedido.detalle!!.size - 1)) {
            val data = pedido.detalle!!.get(i)
            var d = JsonObject()
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
            d.addProperty("FechaCreado", pedido.fechaCreado)
            detalle.add(d)
        }
        json.add("detalle", detalle)
        return json

    } //convierte el pedido a json

    private fun Sendfinal(data: JSONObject, idvisita: Int) {
        try {
            val strinjson = data.toString()
            val ip = preferencias.getString("ip", "")
            val puerto = preferencias.getInt("puerto", 0)
            val direccion = "http://$ip:$puerto/visitas/fin_visita"
            val url = URL(direccion)
            with(url.openConnection() as HttpURLConnection) {
                connectTimeout = 5000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json;charset=utf-8")
                val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                or.write(strinjson) //escribimos el json
                or.flush() //se envia el json
                val codigoRespuesta = responseCode
                when (codigoRespuesta) {
                    200 -> {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            val respuesta = StringBuffer()
                            var inpuline = it.readLine()
                            while (inpuline != null) {
                                respuesta.append(inpuline)
                                inpuline = it.readLine()
                            } //obtenemos la respuesta completa
                            it.close()
                            var data: String? = respuesta.toString()
                            if (data != null) {
                                val res = JSONObject(data)
                                if (!res.isNull("error") && !res.isNull("response")) {
                                    //respuesta correcta
                                    updateCheckOut(idvisita)
                                } else {
                                    throw Exception("Error en la respuesta del servidor")
                                }
                            } else {
                                throw Exception("Error al recibir respuesta del servidor")
                            }
                        }
                    } //termina response 201

                    else -> {
                        throw Exception("Error al recibir respuesta del servidor")
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }//finaliza el checkout

    private fun solicitarPermisos() {
        // SOLICITAR
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  /* Este codigo es para identificar tu request */
            1
        )
    }

    private fun AlertaGPS(contexto: com.example.acae30.Pedido) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_gps)

        // Acccion de click al boton OK
        dialogo.findViewById<Button>(R.id.btnok).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para agregar precio


    //FUNCIONES PARA REPORTE DE PEDIDOS ENVIADOS DIARIMENTE DESDE LA APP
    //MODIFICACION 21/06/2023
    //FUNCION PARA EL MENSAJE DE ADVERTENCIA DE REPORTE
    private fun mensajeReporte(view: View){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cargar_empleados)
        lblMensaje = updateDialog.findViewById(R.id.lblMensaje)
        lblTitulo = updateDialog.findViewById(R.id.lblTitulo)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)

        lblTitulo.text = "REPORTE DE PEDIDOS DIARIOS"
        lblMensaje.text = "¿Desea generar el Reporte de Pedidos?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            obtenerPedidos(idvendedor, fecha, view)
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }
    //FUNCION PARA OBTENER LOS PEDIDOS DESDE EL SERVIDOR
    private fun obtenerPedidos(Idvendedor: Int, Fecha: String, view: View) {
        try {
            val datos = BusquedaReporteJSON(
                Idvendedor,
                Fecha
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = "http://$ip:$puerto/pedido/reporte"
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
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
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
                                        cargarPedidos(res, view)
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS DE ESTE DIA", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }

                        400 -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "PARAMETROS ERRONEOS", Toast.LENGTH_LONG).show() }
                        }

                        404 -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS ENVIADOS", Toast.LENGTH_LONG).show() }
                        }

                        else -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "ERROR DE CONEXION CON EL SERVIDOR", Toast.LENGTH_LONG).show() }
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }
    //FUNCION PARA CARGAR LOS PEDIDOS ENVIADOS EN LA BD
    private fun cargarPedidos(json: JSONArray, view: View) {
        val bd = bd!!.writableDatabase
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.delete("reporteTemp", null, null) //LIMPIANDO LA TABLA VENTASTEMP

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Cliente", funciones!!.validateJsonIsnullString(dato, "cliente"))
                valor.put("Sucursal", funciones!!.validateJsonIsnullString(dato, "sucursal"))
                valor.put("Total", funciones!!.validate(dato.getString("total").toFloat()))

                bd.insert("reporteTemp", null, valor) //INSERTANDO EN VENTASDETALLE
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()

            generarPDF()
            //verificarPermisos(view)
        }
    }

    //FUNCION PARA VERIFICAR PERMISOS DE CREACION DE DIRECTORIO Y DOCUMENTOS
    /*private fun verificarPermisos(view: View) {
        when{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                generarPDF()
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
    }*/
    //FUNCION PARA GENERAR EL REPORTE EN PDF
    private fun generarPDF() {
        try {
            val carpeta = "/reportespdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, vendedor + "_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document(PageSize.LETTER, 2.5f, 2.5f, 3.5f, 3.5f)
            PdfWriter.getInstance(documento, fos)

            documento.open()

            //ESPACIOS
            val espaciosDocumento = Paragraph(
                "\n\n\n"
            )
            documento.add(espaciosDocumento)

            //AGREGANDO TITULO PEDIDO
            val fechaDocumento = Paragraph(
                "$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //DATOS DEL VENDEDOR
            val tablaCliente = PdfPTable(1)
            tablaCliente.widthPercentage = 80f
            val cellInforCliente = PdfPCell(
                Paragraph("VENDEDOR: $vendedor\n" +
                    "FECHA: $fecha\n\n\n",
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            )
            cellInforCliente.horizontalAlignment = Element.ALIGN_LEFT
            cellInforCliente.border = 0
            tablaCliente.addCell(cellInforCliente)
            documento.add(tablaCliente)

            //DATOS DEL PEDIDO
            val tablaPedido = PdfPTable(3)
            tablaPedido.widthPercentage = 80f

            val cellReferencia = PdfPCell(
                Paragraph("CLIENTE",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            )
            )
            cellReferencia.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellReferencia)

            val cellDescripcion = PdfPCell(
                Paragraph("SUCURSAL",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            )
            )
            cellDescripcion.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellDescripcion)

            val cellTotal = PdfPCell(
                Paragraph("TOTAL",
                FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            )
            )
            cellTotal.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellTotal)

            //AGREGANDO EL CONTENIDO DEL PEDIDO
            val lista = getReporte()
            var total = 0f

            for(data in lista){

                val cellReferenciaP = PdfPCell(
                    Paragraph(""+data.Cliente,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                )
                )
                cellReferenciaP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellReferenciaP)

                val cellDescripcionP = PdfPCell(
                    Paragraph(""+data.Sucursal,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                )
                )
                cellDescripcionP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellDescripcionP)

                val cellTotalP = PdfPCell(
                    Paragraph("$ "+data.Total,
                    FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                )
                )
                cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
                tablaPedido.addCell(cellTotalP)

                total += data.Total
            }

            val cellReferenciaP = PdfPCell(Paragraph(""))
            cellReferenciaP.border = 0
            tablaPedido.addCell(cellReferenciaP)

            val cellCantidadP = PdfPCell(
                Paragraph("TOTAL",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            )
            cellCantidadP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellCantidadP)

            val cellTotalP = PdfPCell(
                Paragraph("$ "+ total,
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            )
            cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellTotalP)
            documento.add(tablaPedido)

            documento.close()

            val alert: Snackbar = Snackbar.make(lienzo!!, "REPORTE GENERADO CORRECTAMENTE", Snackbar.LENGTH_LONG)
            alert.view.setBackgroundColor(ContextCompat.getColor(this@Pedido, R.color.btnVerde))
            alert.show()

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }
    //FUNCION PARA OBTENER LOS DATOS PARA EL REPORTE
    private fun getReporte(): ArrayList<DatosReporteJSON> {
        val base = bd!!.writableDatabase
        try {
            val cursor = base.rawQuery("SELECT *  FROM reporteTemp", null)
            val lista = ArrayList<DatosReporteJSON>()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val detalle = DatosReporteJSON(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getFloat(2)
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

    }

    //FUNCION PARA OBTENER LOS PEDIDOS DESDE EL SERVIDOR
    private fun obtenerPedidosDTEServidor(Id_pedido:Int) {
        try {
            val datos = PedidoDTE(
                Id_pedido
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = "http://$ip:$puerto/pedido/dte"
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
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
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
                                    val res = JSONObject(respuesta.toString())
                                    if (res.length() > 0) {
                                        //cargarPedidos(res, view)
                                        val res_pedido_dte: String = res.getString("pedido_dte")
                                        val res_pedido_dte_error: String = res.getString("pedido_dte_error")
                                        val dteAmbiente : String = res.getString("dteAmbiente")
                                        val dteCodigoGeneracion : String = res.getString("dteCodigoGeneracion")
                                        val dteSelloRecibido: String = res.getString("dteSelloRecibido")
                                        val dteNumeroControl: String = res.getString("dteNumeroControl")
                                        var pedido_dte = 0
                                        var pedido_dte_error = 0

                                        if(res_pedido_dte == "true"){
                                            pedido_dte = 1
                                        }

                                        if(res_pedido_dte_error == "true"){
                                            pedido_dte_error = 1
                                        }

                                        println("RESPUESTA PEDIDO_DETE: $res_pedido_dte ------- RESPUESTA PEDIDO_DTE_ERROR: $res_pedido_dte_error")

                                        pedidosController.actualizarEstadoTransmisionPedido(this@Pedido, Id_pedido,pedido_dte, pedido_dte_error, dteAmbiente, dteCodigoGeneracion,
                                            dteSelloRecibido, dteNumeroControl)
                                        runOnUiThread {
                                            actualizarVistaDTE()
                                        }

                                    } else {
                                        //runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS DE ESTE DIA", Toast.LENGTH_LONG).show() }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }

                        400 -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "PARAMETROS ERRONEOS", Toast.LENGTH_LONG).show() }
                        }

                        404 -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS ENVIADOS", Toast.LENGTH_LONG).show() }
                        }

                        else -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "ERROR DE CONEXION CON EL SERVIDOR", Toast.LENGTH_LONG).show() }
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    fun actualizarVistaDTE(){
        try {
            val lista = GetPedido()
            if (lista.size > 0) {
                ShowList(lista)
            }
        } catch (e: Exception) {
            runOnUiThread {
                val alert: Snackbar = Snackbar.make(
                    lienzo!!,
                    e.message.toString(),
                    Snackbar.LENGTH_LONG
                )
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            }
        }
    }

}