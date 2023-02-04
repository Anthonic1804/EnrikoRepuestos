package com.example.acae30

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.acae30.database.Database
import com.example.acae30.modelos.Visitas
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*


class Visita : AppCompatActivity() {
    private var idcliente = 0
    private var nombre = ""
    private var codigo = ""

    private var txtcodigo: TextView? = null
    private var txtnombre: TextView? = null

    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var btnvisita: Button? = null
    private var btnfinvisita: Button? = null
    private var btnpedido: Button? = null
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var btnatras: ImageButton? = null
    private var lienzo: ConstraintLayout? = null
    private var latitud = "0"
    private var longitud = "0"
    private var gpspermiso = false
    private var idpedido = 0
    private var alerta: AlertDialogo? = null
    private var idvisitaGLOBAL: Int? = 0
    private var idvisitaApi = 0 //id de la base de datos de la tabla app_visitas

    // Location request is a config file for all settings related to FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    // Google's API for location service
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visita)
        supportActionBar?.title = "VISITA"
        supportActionBar?.hide()
        idcliente = intent.getIntExtra("idcliente", 0)
        nombre = intent.getStringExtra("nombrecliente").toString()
        codigo = intent.getStringExtra("codigo").toString()
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisitaApi = intent.getIntExtra("idapi", 0)
        idvisitaGLOBAL = intent.getIntExtra("visitaid", 0)
        funciones = Funciones()
        btnfinvisita = findViewById(R.id.btnfinvisita)
        btnpedido = findViewById(R.id.btnpedido)
        txtcodigo = findViewById(R.id.txtcodigo)
        txtnombre = findViewById(R.id.txtnombre)
        lienzo = findViewById(R.id.lienzo)
        btnatras = findViewById(R.id.imbtnatras)

        alerta = AlertDialogo(this)
        bd = Database(this)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        btnvisita = findViewById(R.id.btnvisita)

        if (idvisitaGLOBAL!! > 0) {
            val base = bd!!.writableDatabase
            try {
                val cursor = base!!.rawQuery(
                    "select c.codigo as codigo, v.Id as idvisita, c.id as idcliente, c.cliente as nombre from visitas v inner join clientes c on v.id_cliente = c.Id where v.id = ${idvisitaGLOBAL}",
                    null
                )
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    codigo = cursor.getString(0)
                    idvisitaGLOBAL = cursor.getInt(1)
                    idcliente = cursor.getInt(2)
                    nombre = cursor.getString(3)

                    txtcodigo!!.text = codigo
                    cursor.close()
                } else {
                    throw Exception("Error al obtener código de cliente")
                }
            } catch (e: Exception) {
                throw Exception(e.message)
            } finally {
                base.close()
            }
        } else {
            txtcodigo!!.text = codigo
        }

        txtnombre!!.text = nombre

        btnvisita!!.setOnClickListener {
            getGps(true)

        } //obtiene las coordenadas del gps
        //configuracion general del gps
        btnfinvisita!!.setOnClickListener {
            getGps(false)
        }

        RevisarVisita()

        btnpedido!!.setOnClickListener {
            CreatePedido()
            val intento = Intent(this, Detallepedido::class.java)
            intento.putExtra("id", idcliente)
            intento.putExtra("nombrecliente", nombre)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisitaGLOBAL!!)
            intento.putExtra("idapi", idvisitaApi)
            intento.putExtra("from", "visita")
            startActivity(intento)
            finish()
        }

        btnatras!!.setOnClickListener {
            if (idvisitaGLOBAL!! < 1) {
                val intento = Intent(this, Clientes::class.java)
                intento.putExtra("busqueda", true)
                intento.putExtra("visita", true)
                startActivity(intento)
            }
        }

        // GET UBICACIÓN

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // set all properties of LocationRequest
        locationRequest = LocationRequest()
        locationRequest.interval = 1000 * 30
        locationRequest.fastestInterval = 1000 * 5
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // event that is triggered whenver the update interval is met
        locationCallback = object : LocationCallback() {

        }
        solicitarPermisos()
        //Thread.sleep(1000 * 5)

        //updateGPS()
        startLocationUpdates()

        alerta!!.Cargando()
        alerta!!.changeText("Buscando tu ubicación")
        updateGPS()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun RevisarVisita() {
        if (idvisitaGLOBAL!! > 0) {
            btnvisita!!.visibility = View.GONE
            btnfinvisita!!.visibility = View.VISIBLE
            btnatras!!.visibility = View.GONE


            if (idpedido > 0) {
                btnpedido!!.text = "NUEVO PEDIDO"
            } else {
                btnpedido!!.text = "PEDIDO"
            }

            btnpedido!!.visibility = View.VISIBLE

        } else {
            btnfinvisita!!.visibility = View.GONE
            btnpedido!!.visibility = View.GONE
            btnatras!!.visibility = View.VISIBLE
        }
    }

    // CONOCER SI SE TIENE ACCESO A INTERNET
    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        var isConnected = true

        isConnected = (networkInfo != null) && networkInfo.isConnected
        return isConnected
    }

    // REGISTRA LA ENTRADA Y SALIDA DE LA VISITA DE MANERA LOCAL Y EN EL SERVIDOR EXTERNO
    private fun getGps(inicio: Boolean) {
        startLocationUpdates()

        this@Visita.lifecycleScope.launch {
            try {
                // GUARDA LA VISITA EN LA BD LOCAL
                updateGPS()

                var inicioVisita = JSONObject()
                var finVisita = JSONObject()
                var datos_enviar = JSONObject()
                var datosEnviados: Boolean = false

                runOnUiThread {
                    alerta!!.Cargando()
                    if (inicio) {
                        alerta!!.changeText("Iniciando Visita!!")
                    } else {
                        alerta!!.changeText("Finalizando Visita!!")
                    }
                }

                if (inicio) {
                    val datos = CheckIn("0,0") //guardamos la visita en la bd

                    idvisitaGLOBAL = datos.Id

                    inicioVisita.put("Id_app_visita", 0)
                    inicioVisita.put("Fecha_hora_checkin", datos.Fecha_inicial)
                    inicioVisita.put("Id_cliente", datos.Id_cliente)
                    inicioVisita.put("Cliente", datos.Nombre_cliente)
                    inicioVisita.put("Fecha_hora_checkout", datos.Fecha_inicial)
                    inicioVisita.put("comentarios", "")

                } else {
                    val datos = checkOut(idvisitaGLOBAL!!, "0,0")

                    val get_datos = getVisita(datos.Id)

                    datosEnviados = get_datos.Enviado

                    if (get_datos.Enviado) {

                        finVisita.put("idvisita", datos.Idvisita)
                        finVisita.put("fecha", datos.Fecha_final)
                        finVisita.put("comentarios", "")
                        finVisita.put("nombreimagen", "")
                        finVisita.put("imagen", "")

                    } else {

                        datos_enviar.put("Id_app_visita", 0)
                        datos_enviar.put("Fecha_hora_checkin", get_datos.Fecha_inicial)
                        datos_enviar.put("Id_cliente", get_datos.Id_cliente)
                        datos_enviar.put("Cliente", get_datos.Nombre_cliente)
                        datos_enviar.put("Fecha_hora_checkout", get_datos.Fecha_final)
                        datos_enviar.put("comentarios", "")

                    }
                }

                // EN CASO DE TENER INTERNET ENVIA LOS DATOS AL SERVIDOR EXTERNO
                if (isConnected()) {

                    try {
                        val id_vendedor = preferencias.getInt("Idvendedor", 0)
                        var latitud_p = latitud
                        var longitud_p = longitud
                        var ubicacion = "${latitud_p},${longitud_p}"

                        updateGpsVisita(idvisitaGLOBAL!!, ubicacion, inicio)

                        if (inicio) {
                            inicioVisita.put("Latitud_checkin", latitud_p)
                            inicioVisita.put("Longitud_checkin", longitud_p)
                            inicioVisita.put("Latitud_checkout", latitud_p)
                            inicioVisita.put("Longitud_checkout", longitud_p)
                            inicioVisita.put("Id_vendedor", id_vendedor)

                            SendInicio(inicioVisita, idvisitaGLOBAL!!)
                        } else {

                            if (datosEnviados) {

                                finVisita.put("latitud", latitud_p)
                                finVisita.put("longitud", longitud_p)
                                finVisita.put("Id_vendedor", id_vendedor)
                                Sendfinal(finVisita)

                            } else {

                                datos_enviar.put("Latitud_checkin", latitud_p)
                                datos_enviar.put("Longitud_checkin", longitud_p)
                                datos_enviar.put("Latitud_checkout", latitud_p)
                                datos_enviar.put("Longitud_checkout", longitud_p)
                                datos_enviar.put("Id_vendedor", id_vendedor)
                                enviarVisita(datos_enviar, idvisitaGLOBAL!!)

                            }
                        }
                    } catch (e: Exception) {
                        println("Error33: " + e.message.toString())
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    alerta!!.dismisss()
                    var mensaje = "No se pudo iniciar la visita"
                    if (!inicio) {
                        mensaje = "No se pudo finalizar la visita"
                    }
                    Toast.makeText(
                        this@Visita,
                        mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
                println("Error34" + e.message.toString())
            }

            runOnUiThread {
                var mensaje = "Visita Iniciada"
                if (inicio) {
                    btnfinvisita!!.visibility = View.VISIBLE
                    btnpedido!!.visibility = View.VISIBLE
                    btnvisita!!.visibility = View.GONE
                    btnatras!!.visibility = View.GONE
                } else {
                    mensaje = "Visita Finalizada"
                    val intento = Intent(this@Visita, Pedido::class.java)
                    startActivity(intento)
                    finish()
                }

                alerta!!.dismisss()
                Toast.makeText(this@Visita, mensaje, Toast.LENGTH_LONG).show()
            }
        }
    } //funcion que obtiene las coordenadas del gps


    override fun onStop() {
        val preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        var editor = preferencias.edit()
        editor.putString("nombrecliente", nombre)
        editor.putString("codigo", codigo)
        editor.putInt("idcliente", idcliente)
        editor.commit()
        super.onStop()
    }

    override fun onRestart() {
        val preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        nombre = preferencias.getString("nombrecliente", "").toString()
        codigo = preferencias.getString("codigo", "").toString()
        idcliente = preferencias.getInt("idcliente", 0)
        var editor = preferencias.edit()
        editor.remove("nombrecliente")
        editor.remove("codigo")
        editor.remove("idcliente")
        editor.commit()
        txtcodigo!!.text = codigo
        txtnombre!!.text = nombre
        super.onRestart()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed()
//        if (idvisitaGLOBAL!! < 1) {
//            val intento = Intent(this, Clientes::class.java)
//            intento.putExtra("busqueda", true)
//            intento.putExtra("visita", true)
//            startActivity(intento)
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpspermiso = true
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),  /* Este codigo es para identificar tu request */
                        1
                    )
                }
            }
        }
    } //CONCEDE PERMISOS PARA usar el gps

    private fun CheckIn(ubicacion: String): Visitas {
        val base = bd!!.writableDatabase
        try {
            var visita = ContentValues()
            val fechanow = getDateTime()
            visita.put("Id_cliente", idcliente)
            visita.put("Nombre_cliente", nombre)
            //visita.put("Gps_in", "$latitud,$longitud")
            visita.put("Gps_in", ubicacion)
            visita.put("Fecha_inicial", fechanow)
            //visita.put("Gps_out", "$latitud,$longitud")
            visita.put("Gps_out", ubicacion)
            visita.put("Fecha_final", fechanow)
            visita.put("Abierta", true)
            visita.put("Enviado", false)
            visita.put("Enviado_final", false)

            val id = base.insert("visitas", null, visita)
            idvisitaGLOBAL = id.toInt()

            val cursor = base!!.rawQuery("SELECT * FROM visitas where Id=${id.toInt()}", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val datos = Visitas(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11) == 1,
                    cursor.getInt(12) == 1,
                    cursor.getInt(13) == 1
                )
                cursor.close()
                return datos
            } else {
                throw Exception("Error al obtener los datos")
            }
            //return id.toInt()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    }//crea el checkin

    private fun getVisita(idvisitaparam: Int): Visitas {
        val base = bd!!.writableDatabase
        try {
            val cursor = base!!.rawQuery("SELECT * FROM visitas where Id=${idvisitaparam}", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val datos = Visitas(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11) == 1,
                    cursor.getInt(12) == 1,
                    cursor.getInt(13) == 1
                )
                cursor.close()
                return datos
            } else {
                throw Exception("Error al obtener los datos")
            }
            //return id.toInt()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    }//crea el checkin

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
                                    idvisitaApi = idser
                                    updateCheckIn(idser, idvisita)
                                    updateCheckOut(idvisitaGLOBAL!!)

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
    } //envia la data al servidor

    private fun SendInicio(data: JSONObject, idvisita: Int) {
        try {
            val strinjson = data.toString()
            val ip = preferencias.getString("ip", "")
            val puerto = preferencias.getInt("puerto", 0)
            val direccion = "http://$ip:$puerto/visitas/iniciar_visita"
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
                                    idvisitaApi = idser
                                    updateCheckIn(idser, idvisita)

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
    } //envia la data al servidor

    private fun updateCheckIn(idvisitaServer: Int, idvisita: Int) {
        val base = bd!!.writableDatabase
        try {
            val data = ContentValues()
            data.put("Idvisita", idvisitaServer)
            data.put("Enviado", true)
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

    private fun updateGpsVisita(idvisita: Int, gps_p: String, inicio: Boolean) {
        val base = bd!!.writableDatabase
        try {
            val data = ContentValues()
            if (inicio) {
                data.put("Gps_in", gps_p)
            } else {
                data.put("Gps_out", gps_p)
            }
            base.update("visitas", data, "Id=?", arrayOf(idvisita.toString()))
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    } //ACTUALIZA CON EL ID DEL PEDIDO DE LA BD

    // ACTUALIZACIÓN DE LA UBICACIÓN EN LA BD LOCAL DE CHECKOUT
    private fun checkOut(idvisita: Int, coordenadas: String): Visitas {
        val base = bd!!.writableDatabase
        try {
            val data = ContentValues()
            data.put("Gps_out", coordenadas)
            data.put("Fecha_final", getDateTime())
            data.put("Abierta", false)
            base.update("visitas", data, "Id=?", arrayOf(idvisita.toString()))

            val cursor = base!!.rawQuery("SELECT * FROM visitas where Id=${idvisita}", null)

            if (cursor.count > 0) {
                cursor.moveToFirst()
                val datos = Visitas(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11) == 1,
                    cursor.getInt(12) == 1,
                    cursor.getInt(13) == 1
                )

                cursor.close()

                return datos

            } else {
                throw Exception("Error al obtener los datos")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    } //ACTUALIZA CON EL ID DEL PEDIDO DE LA BD

    // ESTADO DE FINALIZACIÓN DE LA ACIVIDAD
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

    private fun parsearFecha(fecha: String): String {
        val DATE_INPUT_FORMAT = "dd-MMM-yy HH:mm:ss"
        val formato = SimpleDateFormat(DATE_INPUT_FORMAT)
        val fe = formato.parse(fecha)
        formato.applyPattern("yyyy-MM-ddTHH:mm:ss")
        return formato.format(fe).toString()
    }

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    } //retorna la fecha en formato

    private fun Sendfinal(data: JSONObject) {
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
                                    updateCheckOut(idvisitaGLOBAL!!)
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

    private fun CreatePedido() {
        val base = bd!!.writableDatabase
        val fechanow = getDateTime()
        try {
            base.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Id_cliente", idcliente)
            contenido.put("Nombre_cliente", nombre)
            contenido.put("Total", 0.toFloat())
            contenido.put("Descuento", 0.toFloat())
            contenido.put("Enviado", false)
            contenido.put("Idvisita", idvisitaGLOBAL)
            contenido.put("Fecha_creado", fechanow)
            val id = base.insert("pedidos", null, contenido)
            //inserta el encabezado del pedido
            idpedido = id.toInt()

            //base.execSQL("UPDATE visitas SET Pedido = 'TRUE', Idpedido = ${idpedido} WHERE Id=${idvisitaGLOBAL}")

            base.setTransactionSuccessful()
        } catch (e: Exception) {
            idpedido = 0
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()
        }
    } //crea el pedido en caso de que no exista

    // HACER PETICIÓN DE POSICIÓN ACTUAL DEL GPS
    private fun updateGPS() {
        // get permissions from the user to track GPS
        // get the current location from the fused client
        // update the UI - i.e. set all properties in their associated text view items.

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@Visita)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // user provided the permission
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // we got permissions. put the values of location. XXX into the UI Components.
                    if (location != null) {
                        latitud = location.latitude.toString()
                        longitud = location.longitude.toString()
                    }
                    alerta!!.dismisss()
                }
        } else {
            // permissions not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                solicitarPermisos()
                alerta!!.dismisss()
            }
        }
    }

    // PERMISOS PARA ACCESO AL GPS
    private fun solicitarPermisos() {
        // SOLICITAR
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  /* Este codigo es para identificar tu request */
            1
        )
    }
}