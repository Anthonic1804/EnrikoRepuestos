package com.example.acae30

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.acae30.database.Database
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

@Suppress("DEPRECATION")
class Inicio : AppCompatActivity() {

    private var load: CardView? = null
    private var cvconf: CardView? = null
    private var cvcliente: CardView? = null
    private var cvinventario: CardView? = null
    private var cvpedido: CardView? = null
    private var cvcuenta: CardView? = null
    private var funciones: Funciones? = null
    private var btnsalir: Button? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var database: Database? = null
    private var fechaUpdate: TextView? = null

    private var ip = ""
    private var puerto = 0
   // private val INTERVALO: Int = 2000 //2 segundos para salir
   // private var tiempoPrimerClick: Long = 0

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
       /* if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(this, "Vuelve a presionar para salir", Toast.LENGTH_SHORT).show()
        }
        tiempoPrimerClick = System.currentTimeMillis()*/
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        //supportActionBar?.title = "ACAE APP INICIO"
        funciones = Funciones()
        load = findViewById(R.id.cvData)
        cvconf = findViewById(R.id.cvconfig)
        cvcliente = findViewById(R.id.cvcliente)
        cvinventario = findViewById(R.id.cvinventario)
        cvpedido = findViewById(R.id.cvpedido)
        cvcuenta = findViewById(R.id.cvcuentas)
        btnsalir = findViewById(R.id.btnsalir)
        fechaUpdate = findViewById(R.id.lblupdate)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones!!.VendedorVerific(this) //valida que haya sesion y que haya configuracion
        database = Database(this)

        ip = preferencias!!.getString("ip", "").toString()
        puerto = preferencias!!.getInt("puerto", 0)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_PHONE_STATE
                ), 101
            )
        }

        // COMPROBAR SI AUN ES VALIDO EL INICIO DE SESION
        if (isConnected()) {
            GlobalScope.launch(Dispatchers.IO) {
                comprobarSesion()
            }
        }


    } //relacianomos los widgets y inicializamos la variables

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        menu() //llama los botones de menu
        funciones!!.VendedorVerific(this) //valida que haya sesion
        mostrarFecha() //MOSTRANDOLA FECHA DEL INVENTARIO
        //MODIFICACION REALIZADA PARA VERIFICAR EL FUNCIONAMIENTO DE GIT
    }

    //FUNCION PARA OBTENER LA FECHA DE INVENTARIO
    private fun getFechaInventario(): ArrayList<com.example.acae30.modelos.Inventario> {
        val lista = ArrayList<com.example.acae30.modelos.Inventario>()
        val base = database!!.writableDatabase

        try {
            val cursor = base.rawQuery("SELECT * FROM inventario LIMIT 1", null)

            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val arreglo = com.example.acae30.modelos.Inventario(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getFloat(15),
                        cursor.getFloat(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getInt(19),
                        cursor.getString(20),
                        cursor.getInt(21),
                        cursor.getString(22),
                        cursor.getString(23),
                        cursor.getString(24),
                        cursor.getString(25),
                        cursor.getString(26),
                        cursor.getString(27),
                        cursor.getInt(28),
                        cursor.getString(29),
                        cursor.getFloat(30),
                        cursor.getDouble(31),
                        cursor.getInt(32),
                        cursor.getFloat(33)
                    )
                    lista.add(arreglo)
                } while (cursor.moveToNext())
                cursor.close()
            }
        } catch (e: Exception) {
            println("ERROR EN FUNCION DE SELECCIONAR FECHA DE INVENTARIO")
        } finally {
            base!!.close()
        }
        return lista
    }

    private fun mostrarFecha(){
        try {
            val list: ArrayList<com.example.acae30.modelos.Inventario> = getFechaInventario()
            if(list.isNotEmpty()){
                var fecha = ""
                for(data in list){
                    fecha = data.Fecha_inventario.toString()
                }
                fechaUpdate!!.text = fecha
            }else{
                fechaUpdate!!.visibility = View.GONE
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR FECHA DE INVENTARIO")
        }
    }

    private fun menu() {
        load!!.setOnClickListener {
            val intento = Intent(this, carga_datos::class.java)
            startActivity(intento)
            finish()
        }

        cvconf!!.setOnClickListener {
            val intento = Intent(this, Configuracion::class.java)
            startActivity(intento)
            finish()
        }
        cvcliente!!.setOnClickListener {
            val intento = Intent(this, Clientes::class.java)
            startActivity(intento)
            finish()
        }
        cvinventario!!.setOnClickListener {
            val intento = Intent(this, Inventario::class.java)
            startActivity(intento)
            finish()
        }
        cvpedido!!.setOnClickListener {
            val intento = Intent(this, Pedido::class.java)
            intento.putExtra("proviene", "inicio")
            startActivity(intento)
            finish()
        }
        cvcuenta!!.setOnClickListener {
            val intento = Intent(this, Cuentas_list::class.java)
            intento.putExtra("cuentas", true)
            startActivity(intento)
            finish()
        }
        btnsalir!!.setOnClickListener {

            salir()

        } //boton salir
    }//acciones de los botones del menu

    private fun salir(){
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alert_cerrar_sesion_usuario)
        dialogo.findViewById<Button>(R.id.btncerrar).setOnClickListener {
            updateSesionServer()
            cerrarSesion()
        }
        dialogo.findViewById<Button>(R.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.show()
    }

    private fun comprobarSesion() {
        var usuario = preferencias!!.getString("Usuario", "")
        var identidad = preferencias!!.getString("Identidad", "")

        var comprobarEstado = comprobarEstado(usuario!!, identidad!!)

        if (comprobarEstado == "Invalido") {
            // BORRAR TODOS LOS DATOS
            cerrarSesion()
        }

    } // COMPROBAR ESTADO DE LA SESION Y REALIZAR ACCIONES NECESARIAS

    private fun comprobarEstado(usuario: String, identidad: String): String {
        var respuestaVal = ""
        try {
            val credenciales = com.example.acae30.modelos.JSONmodels.LoginComprobar(
                usuario.toUpperCase()
            ) //se crea el modelo con los datos    que se enviaran

            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login/comprobar"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 5000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()
                                val res: JSONObject =
                                    JSONObject(respuesta.toString()) //obtenemos la respuesta del servidor y la pasamos a json
                                if (res.length() > 0) {
                                    if (!res.isNull("error") && !res.isNull("response")) {
                                        val coderror: Int = res.getInt("error")
                                        val response: String = res.getString("response")
                                        val identidad_param: String = res.getString("identidad")
                                        val estado: String = res.getString("estado").trim()

                                        when (response) {
                                            "Credenciales validas" -> respuestaVal =
                                                "Valido" //Estado para ingresar a la primera
                                            "Clave o Usuario Invalidos" -> respuestaVal =
                                                "Invalido" // Usuario y contraseña no validos
                                        }
                                        println("respuestaVal: " + respuestaVal)
                                        println("estado: " + estado)
                                        println("identidad: " + identidad)
                                        println("identidad_param: " + identidad_param)

                                        if (respuestaVal == "Valido") {
                                            if (estado == "INACTIVO") {
                                                respuestaVal = "Invalido"
                                            }

                                            if (estado == "ACTIVO") {
                                                if (identidad != identidad_param) {
                                                    respuestaVal = "Invalido"
                                                }
                                            }
                                        }
                                        println("respuestaVal: " + respuestaVal)
                                    } else {
                                        throw Exception("Error al procesar la solicitud")
                                    }//valido si el json contiene esas variables
                                } else {
                                    throw Exception("error de comunicacion")
                                }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }//buffer donde se obtiene la respuesta del servidor
                    } else if (responseCode == 204) {
                        throw Exception("No se han recibido parametros")
                    } else {
                        throw Exception("Error de comunicacion con el servidor")
                    } //valido que la respuesta sea la correcta

                } catch (e: Exception) {
                    if (e.message.toString() == "Host unreachable") {
                        runOnUiThread {
                            //AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            respuestaVal = "Error"
            //throw Exception(e.message)
        }
        return respuestaVal

    } // CONSULTAR ESTADO EN EL SERVIDOR DE LA SESION

    private fun cerrarSesion() {

        // BORRAR TODAS LAS TABLAS
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //inicio la transaccion

            /* borra todos los datos del usuario
            val tablas = arrayOf(
                "detalle_pedidos", "pedidos",
                "rubros", "lineas", "inventario_unidades", "inventario_precios",
                "inventario", "clientes", "cuentas", "visitas"
            )
             */

            val tablas = arrayOf(
                "detalle_pedidos", "pedidos", "visitas"
            )
            tablas.forEach { key ->
                val sql = "DELETE FROM $key"
                bd.execSQL(sql)

                val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME =  '${key}'"
                bd.execSQL(sql2)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }

        // BORRAR DATOS DE SESION Y SALIR A LA PANTALLA DE LOGIN
        val editor = preferencias!!.edit()
        editor.putInt("Idvendedor", 0)
        editor.putString("Vendedor", "")
        editor.putString("Usuario", "")
        editor.putString("Identidad", "")
        editor.putBoolean("sesion", false)
        editor.commit()
        val intento = Intent(this, Login::class.java)
        startActivity(intento)
        finish()
    } // ELIMINAR TODOS LOS DATOS Y CIERRA SESION

    private fun updateSesionServer() {
        var usuario = preferencias!!.getString("Usuario", "")
        var identidad = preferencias!!.getString("Identidad", "")
        try {
            val credenciales = com.example.acae30.modelos.JSONmodels.Logout(
                usuario!!.toUpperCase(),
                identidad!!
            ) //se crea el modelo con los datos    que se enviaran

            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login/logout"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 5000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()
                                val res: JSONObject =
                                    JSONObject(respuesta.toString()) //obtenemos la respuesta del servidor y la pasamos a json
                                if (res.length() > 0) {
                                    if (!res.isNull("error") && !res.isNull("response")) {
                                        val coderror: Int = res.getInt("error")
                                        val response: String = res.getString("response")
                                        println("Respuesta: " + response)
                                    } else {
                                        throw Exception("Error al procesar la solicitud")
                                    }//valido si el json contiene esas variables
                                } else {
                                    throw Exception("error de comunicacion")
                                }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }//buffer donde se obtiene la respuesta del servidor
                    } else if (responseCode == 204) {
                        throw Exception("No se han recibido parametros")
                    } else {
                        throw Exception("Error de comunicacion con el servidor")
                    } //valido que la respuesta sea la correcta

                } catch (e: Exception) {
                    if (e.message.toString() == "Host unreachable") {
                        runOnUiThread {
                            //AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    } // CAMBIAR ESTADO EN EL SERVIDOR PARA CERRAR SESION

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        var isConnected = true

        isConnected = networkInfo != null && networkInfo.isConnected
        return isConnected
    } // VERIFICA LA CONEXION A WIFI O REDES

}