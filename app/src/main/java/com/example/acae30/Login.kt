package com.example.acae30

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.acae30.database.Database
import com.example.acae30.modelos.JSONmodels.Login
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
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

class Login : AppCompatActivity() {
    private var btnlogin: Button? = null
    private var bd: Database? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var funciones: Funciones? = null
    private var txtusuario: TextView? = null
    private var txtclave: TextView? = null
    private var lienzo: ConstraintLayout? = null
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        alerta = AlertDialogo(this)
        bd = Database(this)
        lienzo = findViewById(R.id.lienzo)
        btnlogin = findViewById(R.id.btnlogin)
        funciones = Funciones()
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        ValidarDatos(preferencias!!)
        //limpia los datos del usuario o vendedor
        ip = preferencias!!.getString("ip", "").toString()
        puerto = preferencias!!.getInt("puerto", 0)
        txtclave = findViewById(R.id.txtclave)
        txtusuario = findViewById(R.id.txtusuario)

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
        val context = this

        btnlogin!!.setOnClickListener {
            var identidad = ""
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                identidad =
                    Build.DEVICE + " " + Build.MODEL + " " + Build.HARDWARE + " " + Build.USER
                println("El numero de identificacion: " + identidad)
            }
            Login(identidad, context)
        }  //funciones!!.VendedorVerific(this)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }//anula el boton atras

    private fun Login(identidad: String, thiscontext: com.example.acae30.Login) {
        var usuario: String = txtusuario!!.text.toString()
        var clave = txtclave!!.text.toString()
        var contexto = this
        if (usuario.length > 0 && clave.length > 0) {
            if (funciones!!.isNetworkConneted(this)) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        runOnUiThread {
                            alerta!!.Cargando() //muestra la alerta
                        } //se ejecuta en el hilo principal de la app
                        var respuesta_val = ValidarCredenciales(usuario, clave, identidad)

                        if (respuesta_val == "Valido") {
                            // Correcto y enviar datos para registrar
                            IniciarSesion(usuario, clave, identidad)
                        } else if (respuesta_val == "Invalido") {
                            // Incorrecto y mostrar mensaje de error

                            val alert: Snackbar =
                                Snackbar.make(
                                    lienzo!!,
                                    "Usario o contraseña no son correctos.",
                                    Snackbar.LENGTH_LONG
                                )
                            alert.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    thiscontext,
                                    R.color.moderado
                                )
                            )
                            alert.show()

                        } else if (respuesta_val == "Cerrar") {
                            // Correcto y sugerir cerrar sesion en otros dispositivos para iniciar en el actual
                            runOnUiThread {
                                alerta!!.dismisss()
                                AlertaCerrar(usuario, clave, identidad)
                            }
                        } else if (respuesta_val == "Max sesiones") {
                            // No se puede iniciar sesion porque ya se ha superado el maximo de conexiones permitidas
                            runOnUiThread {
                                alerta!!.dismisss()
                                AlertaMaxSesiones()
                            }
                        }
                        runOnUiThread {
                            alerta!!.dismisss() //muestra la alerta
                        } //se ejecuta en el hilo principal de la app
                    } catch (e: Exception) {
                        if (alerta != null) {
                            runOnUiThread {
                                alerta!!.dismisss() //cierra la alerta
                            }
                        }
                        runOnUiThread {
                            val alert: Snackbar =
                                Snackbar.make(lienzo!!, e.message.toString(), Snackbar.LENGTH_LONG)
                            alert.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    contexto,
                                    R.color.moderado
                                )
                            )
                            alert.show()
                        }

                    }//muestra alerta en caso de falla
                }//hilo aparte del principal
            } else {
                val alert: Snackbar =
                    Snackbar.make(lienzo!!, "Enciende tu wifi", Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                alert.show()
            }
            //val inte= Intent(this,Inicio::class.java)
            //startActivity(inte)
        } else {
            val alert: Snackbar =
                Snackbar.make(lienzo!!, "Ingresa tus Credenciales", Snackbar.LENGTH_LONG)
            alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
            alert.show()
        }
    } //valida la utenticacion del usuario

    private fun ValidarCredenciales(usuario: String, clave: String, identidad: String): String {
        var respuestaVal = ""
        try {
            val credenciales = Login(
                usuario.uppercase(),
                clave,
                identidad
            ) //se crea el modelo con los datos    que se enviaran
            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login/estado"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
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
                                            "Se ha llegado al maximo de sesiones activas" -> respuestaVal =
                                                "Max sesiones" // Validar si sobrepasa el numero de sesiones
                                        }
                                        println("respuestaVal: " + respuestaVal)
                                        println("estado: " + estado)
                                        println("identidad: " + identidad)
                                        println("identidad_param: " + identidad_param)

                                        if (respuestaVal == "Valido") {
                                            if (estado == "ACTIVO" && identidad != identidad_param && identidad_param != "") {
                                                respuestaVal =
                                                    "Cerrar" //Estado para sugerir cerrar sesion en otros dispositivos e iniciar en el actual
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
                            AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            respuestaVal = "Error"
            throw Exception(e.message)
        }
        return respuestaVal
    }//conecta con la api y valida las credenciales del usuario

    private fun IniciarSesion(usuario: String, clave: String, identidad: String) {
        try {
            val credenciales = Login(
                usuario.uppercase(),
                clave,
                identidad
            ) //se crea el modelo con los datos    que se enviaran
            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
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
                                        val nombreEmpleado : String = res.getString("nombreEmpleado")
                                        val identidad_param: String = res.getString("identidad")
                                        val estado: String = res.getString("estado")
                                        val generaToken : Int = res.getInt("generaToken")

                                        if (coderror > 0) {
                                            val editor = preferencias!!.edit()
                                            editor.putInt("Idvendedor", coderror)
                                            editor.putString("Vendedor", nombreEmpleado.uppercase()) //MODIFICACION OBTENIENDO EL NOMBRE DEL EMPLEADO
                                            editor.putString("Usuario", usuario.uppercase())
                                            editor.putString("Identidad", identidad)
                                            editor.putInt("generaToken", generaToken)//VALIDACION PARA INGRESO EN MODULO DE GENERAR TOKEN 1-> SI  0->NO
                                            editor.putBoolean("sesion", true)
                                            editor.commit()
                                            val inte = Intent(this@Login, Inicio::class.java)
                                            startActivity(inte)
                                            finish()
                                        } else {
                                            throw Exception(response)
                                        } //valida que el servidor confirmo y valido las credenciales
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
                            AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }//conecta con la api y valida las credenciales del usuario

    private fun ValidarDatos(preferencias: SharedPreferences) {
        if (preferencias.contains("ip") && preferencias.contains("puerto")) {
            if (preferencias.contains("sesion")) {
                var sesionactiva = preferencias.getBoolean("sesion", false)
                if (sesionactiva) {
                    val intento = Intent(this, Inicio::class.java)
                    startActivity(intento)
                    finish()
                } else {
                    preferencias.edit().remove("Idvendedor").commit()
                    preferencias.edit().remove("Vendedor").commit()
                }
            }
        } else {
            val intento = Intent(this, MainActivity::class.java)
            startActivity(intento)
            finish()
        }

    }//valida si ya hay una cesion  abierta y si se ha hecho login

    private fun AlertaEliminar() {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alert_eliminar)
        dialogo.findViewById<Button>(R.id.btneliminar).text = "Aceptar"
        dialogo.findViewById<TextView>(R.id.txttitulo).text = "No hay conexion con el Servidor"
        dialogo.findViewById<TextView>(R.id.txtsubtitulo).text = "Deseas reconfigurar la conexion?"
        dialogo.findViewById<Button>(R.id.btneliminar).setOnClickListener {
            val intento = Intent(this, MainActivity::class.java)
            intento.putExtra("reconfig", true)
            startActivity(intento)
            finish()

        }//boton eliminar

        dialogo.findViewById<Button>(R.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para eliminar

    private fun AlertaCerrar(usuario: String, clave: String, identidad: String) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alert_cerrar_sesion_dispositivos)
        dialogo.findViewById<Button>(R.id.btncerrar).setOnClickListener {
            try {
                GlobalScope.launch(Dispatchers.Main) {
                    IniciarSesion(usuario, clave, identidad)
                }
            } catch (e: java.lang.Exception) {
                println("Error cerrar: " + e.message)
            }

            dialogo.dismiss()
        }//boton cerrar

        dialogo.findViewById<Button>(R.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }//boton cancelar

        dialogo.show()

    } //muestra la alerta para sugerir cerrar sesion en otro dispositivo

    private fun AlertaMaxSesiones() {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_max_sesiones)
        dialogo.findViewById<Button>(R.id.btnok).setOnClickListener {
            dialogo.dismiss()
        }//boton ok

        dialogo.show()

    } //muestra la alerta para sugerir cerrar sesion en otro dispositivo
}


