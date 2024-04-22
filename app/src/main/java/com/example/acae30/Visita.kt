package com.example.acae30

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityVisitaBinding
import com.example.acae30.modelos.Visitas
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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


class Visita : AppCompatActivity() {
    private var idcliente = 0
    private var nombre = ""
    private var codigo = ""
    private var latitud = "0"
    private var longitud = "0"
    private var idpedido = 0
    private var alerta: AlertDialogo? = null
    private var idvisitaGLOBAL: Int? = 0
    private var idvisitaApi = 0 //id de la base de datos de la tabla app_visitas

    private var funciones = Funciones()
    private var clientesController = ClientesController()
    private var visitaController = VisitaController()
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var bd: Database? = null
    private lateinit var binding: ActivityVisitaBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001


    private val Id_app_visita: Int = 0
    private var Fecha_hora_checkin: String = ""
    private var Latitud_checkin: String = ""
    private var Longitud_checkin: String = ""
    private var Id_cliente: Int = 0
    private var Cliente: String = ""
    private var Id_vendedor: Int = 0
    private var Fecha_hora_checkout: String = ""
    private var Latitud_checkout: String = ""
    private var Longitud_checkout: String = ""
    private val Comentarios: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityVisitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombre = intent.getStringExtra("nombrecliente").toString()
        codigo = intent.getStringExtra("codigo").toString()
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisitaApi = intent.getIntExtra("idapi", 0)
        idvisitaGLOBAL = intent.getIntExtra("visitaid", 0)

        alerta = AlertDialogo(this)
        bd = Database(this)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (idvisitaGLOBAL!! > 0) {
            val base = bd!!.readableDatabase
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

                    binding.txtcodigo.text = codigo
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
            binding.txtcodigo.text = codigo
        }

        binding.txtnombre.text = nombre

        binding.btnvisita.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                getGps(true)
            }

        } //obtiene las coordenadas del gps
        //configuracion general del gps
        binding.btnfinvisita.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                getGps(false)
                updateSharedPreferencesFinalizarVisita()
            }
        }

        RevisarVisita()

        binding.btnpedido.setOnClickListener {
            CreatePedido()
            val intento = Intent(this, Detallepedido::class.java)
            intento.putExtra("idcliente", idcliente)
            intento.putExtra("nombrecliente", nombre)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisitaGLOBAL!!)
            intento.putExtra("idapi", idvisitaApi)
            intento.putExtra("from", "visita")
            startActivity(intento)
            finish()
        }

        binding.imbtnatras.setOnClickListener {
            if (idvisitaGLOBAL!! < 1) {
                val intento = Intent(this, Clientes::class.java)
                startActivity(intento)
            }
        }

        // GET UBICACIÓN
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no hay permiso, solicitarlo
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Si ya hay permiso, obtener la ubicación
            alerta!!.Cargando()
            alerta!!.changeText("Buscando tu ubicación")
            updateGPS()
        }

    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                updateGPS()
            } else {
                // Permiso denegado, mostrar un mensaje o realizar otra acción
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //FUNCION PARA ELIMINAR LAS SHARED PREFERENCES CREADAS
    //13/01/2024
    private fun updateSharedPreferencesFinalizarVisita(){
        val editor = preferencias.edit()
        editor.remove("visita")
        editor.remove("busqueda")
        editor.apply()
    }

    private fun RevisarVisita() {
        if (idvisitaGLOBAL!! > 0) {
            binding.btnvisita.visibility = View.GONE
            binding.btnfinvisita.visibility = View.VISIBLE
            binding.imbtnatras.visibility = View.GONE


            if (idpedido > 0) {
                binding.btnpedido.text = getString(R.string.nuevo_pedido)
            } else {
                binding.btnpedido.text = getString(R.string.pedido)
            }

            binding.btnpedido.visibility = View.VISIBLE

        } else {
            binding.btnfinvisita.visibility = View.GONE
            binding.btnpedido.visibility = View.GONE
            binding.imbtnatras.visibility = View.VISIBLE
        }
    }

    // REGISTRA LA ENTRADA Y SALIDA DE LA VISITA DE MANERA LOCAL Y EN EL SERVIDOR EXTERNO
    private fun getGps(inicio: Boolean) {

        this@Visita.lifecycleScope.launch {
            try {
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

                    Fecha_hora_checkin = datos.Fecha_inicial
                    Id_cliente = datos.Id_cliente
                    Cliente = datos.Nombre_cliente
                    Fecha_hora_checkout = datos.Fecha_inicial

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
                if (funciones.isInternetAvailable(this@Visita)) {

                    try {
                        val id_vendedor = preferencias.getInt("Idvendedor", 0)
                        val latitud_p = latitud
                        val longitud_p = longitud
                        val ubicacion = "${latitud_p},${longitud_p}"

                        updateGpsVisita(idvisitaGLOBAL!!, ubicacion, inicio)

                        if (inicio) {

                            try {
                                Latitud_checkin = latitud_p
                                Longitud_checkin = longitud_p
                                Latitud_checkout = latitud_p
                                Longitud_checkout = longitud_p
                                Id_vendedor = id_vendedor

                                CoroutineScope(Dispatchers.IO).launch {
                                    idvisitaApi = visitaController.registrarVisita(
                                        Id_app_visita,
                                        Fecha_hora_checkin,
                                        Latitud_checkin,
                                        Longitud_checkin,
                                        Id_cliente,
                                        Cliente,
                                        Id_vendedor,
                                        Fecha_hora_checkout,
                                        Latitud_checkout,
                                        Longitud_checkout,
                                        Comentarios,
                                        idvisitaGLOBAL!!,
                                        this@Visita
                                    )
                                }
                            }catch (e:Exception){
                                println("NO SE PUEDE CONECTAR CON EL SERVER -> ${e.message}")
                            }


                        } else {

                            if (datosEnviados) {

                                finVisita.put("latitud", latitud_p)
                                finVisita.put("longitud", longitud_p)
                                finVisita.put("Id_vendedor", id_vendedor)
                                CoroutineScope(Dispatchers.IO).launch {
                                    Sendfinal(finVisita)
                                }

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
                        println("ERROR DE CONEXION CON EL SERVIDOR: " + e.message.toString())
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
                    binding.btnfinvisita.visibility = View.VISIBLE
                    binding.btnpedido.visibility = View.VISIBLE
                    binding.btnvisita.visibility = View.GONE
                    binding.imbtnatras.visibility = View.GONE
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

        val editor = preferencias.edit()
        editor.putString("nombrecliente", nombre)
        editor.putString("codigo", codigo)
        editor.putInt("idcliente", idcliente)
        editor.apply()
        super.onStop()
    }

    override fun onRestart() {
        val preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        nombre = preferencias.getString("nombrecliente", "").toString()
        codigo = preferencias.getString("codigo", "").toString()
        idcliente = preferencias.getInt("idcliente", 0)

        val editor = preferencias.edit()
        editor.remove("nombrecliente")
        editor.remove("codigo")
        editor.remove("idcliente")
        editor.apply()
        binding.txtcodigo.text = codigo
        binding.txtnombre.text = nombre
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

    private fun CheckIn(ubicacion: String): Visitas {
        val base = bd!!.writableDatabase
        try {
            var visita = ContentValues()
            val fechanow = funciones.getFechaHoraProceso()
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
            data.put("Fecha_final", funciones.getFechaHoraProceso())
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
                                    //throw Exception("Error en la respuesta del servidor")
                                    println("ERROR DE RESPUESTA DEL SERVIDOR")
                                }
                            } else {
                                //throw Exception("Error al recibir respuesta del servidor")
                                println("ERROR AL RECIBIR RESPUESTA DEL SERVIDOR")
                            }
                        }
                    } //termina response 201

                    else -> {
                        //throw Exception("Error al recibir respuesta del servidor")
                        println("PARRAMETROS ERRONEOS")
                    }
                }
            }
        } catch (e: Exception) {
            //throw Exception(e.message)
            println("ERROR DE CONEXION CON EL SERVIDOR -> ${e.message}")
        }
    }//finaliza el checkout

    private fun CreatePedido() {
        val base = bd!!.writableDatabase
        val fechanow = funciones.getFechaHoraProceso()
        val terminos = clientesController.obtenerInformacionCliente(this@Visita, idcliente)
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
            contenido.put("Terminos", terminos!!.Terminos_cliente)
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
    @SuppressLint("MissingPermission")
    private fun updateGPS() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // OBTENIENDO LA UBICACION ACTUAL
                location?.let {

                    latitud = location.latitude.toString()
                    longitud = location.longitude.toString()

                    /* Toast.makeText(
                         this,
                         "Latitud: $latitud, Longitud: $longitud",
                         Toast.LENGTH_SHORT
                     ).show()*/

                    alerta!!.dismisss()

                } ?: run {
                    // ERROR AL NO OBTENER LA UBICACION
                    /*Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT)
                        .show()*/

                    latitud = 0.toString()
                    longitud = 0.toString()

                    alerta!!.dismisss()
                }
            }
            .addOnFailureListener { e ->
                // ERROR AL NO OBTENER LA UBICACION
                Toast.makeText(this, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()

                alerta!!.dismisss()
            }
    }
}