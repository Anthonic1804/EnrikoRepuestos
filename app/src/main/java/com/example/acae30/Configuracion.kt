package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dcastalia.localappupdate.DownloadApk
import com.example.acae30.database.Database
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class Configuracion : AppCompatActivity() {

    private var swlista: Switch? = null
    private var swminiatura: Switch? = null
    private var swSinExistencia: Switch? = null
    private var dataBase: Database? = null
    private lateinit var btnBuscarUpdate : Button
    private var url: String? = null
    private var versionAppServer : String? = null
    private var urlAppServer : String? = null
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private var versionActual : Float = 0f
    private lateinit var tvVersionActual : TextView

    private var atras: ImageButton? = null
    private var ip: TextView? = null
    private var puerto: TextView? = null
    private var btnGuardar: Button? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var vista: View? = null
    private var funciones: Funciones? = null
    private var alerta: AlertDialogo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)
        supportActionBar?.hide()
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones = Funciones()
        vista = findViewById(R.id.vistaalerta)
        ip = findViewById(R.id.txtip)
        puerto = findViewById(R.id.txtpuerto)
        btnGuardar = findViewById(R.id.btnupdate)
        atras = findViewById(R.id.imgbtnatras)
        alerta = AlertDialogo(this)
        dataBase = Database(this)

        //FUNCIONES AGRAGADAS PARA LOS CONTROLES DE VISTA DE INVENTARIO
        swlista = findViewById(R.id.swlista)
        swminiatura = findViewById(R.id.swminiatura)
        swSinExistencia = findViewById(R.id.swSinExistencias)
        swSinExistencia!!.isEnabled = false

        tvVersionActual = findViewById(R.id.tvVersionActualApp)

        btnBuscarUpdate = findViewById(R.id.btnBuscarUpdate)

        //OBTENIENDO LA URL DEL SERVIDOR
        getApiUrl()

        //ACTUALIZAR CONFIG PARA PEDIDOS SIN EXISTENCIAS
        swSinExistencia!!.isChecked = preferencias!!.getString("pedidos_sin_existencia", "") == "S"

        versionActual = preferencias!!.getFloat("versionActualApp", 1f)

        // 2 -> LISTADO
        // 1 -> VISTA MINIATURA
        swlista!!.isChecked = preferencias!!.getInt("vistaInventario", 0) == 2
        swminiatura!!.isChecked = preferencias!!.getInt("vistaInventario", 0) == 1

        tvVersionActual.setText("ACAE APP Ver. $versionActual")

        swlista!!.setOnCheckedChangeListener { _, isChecked ->
            val editor = preferencias!!.edit()
            editor.remove("vistaInventario")
            if (isChecked) {
                swminiatura!!.isChecked = false
                editor.putInt("vistaInventario", 2)
            } else {
                swminiatura!!.isChecked = true
                editor.putInt("vistaInventario", 1)
            }
            editor.apply()
        }

        swminiatura!!.setOnCheckedChangeListener { _, isChecked ->
            val editor = preferencias!!.edit()
            editor.remove("vistaInventario")
            if (isChecked) {
                swlista!!.isChecked = false
                editor.putInt("vistaInventario", 1)
            } else {
                swlista!!.isChecked = true
                editor.putInt("vistaInventario", 2)
            }
            editor.apply()
        }

        btnBuscarUpdate.setOnClickListener {
            //getVersionUpdate()
            if (url != null) {
                if (funciones!!.isNetworkConneted(this)) {

                    CoroutineScope(Dispatchers.IO).launch {
                        getAppVersion()
                    }//COURUTINA CARGAR DATOS DE ACTUALIZACION

                } else {
                    ShowAlert("ERROR: NO TIENES CONEXION A INTERNET")
                }
            } else {
                ShowAlert("ERROR: NO SE ENCONTRO LA CONFIGURACION DEL SERVIDOR")
            }
        }

    } //funcion que inicializa las variables

    override fun onStart() {
        super.onStart()
        GetServerData()
        atras!!.setOnClickListener {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()
        }//boton atras
        btnGuardar!!.setOnClickListener {
            val contexto = this
            alerta!!.Cargando()

            CoroutineScope(Dispatchers.IO).launch {
                ValidateConnection(ip!!.text.toString(), puerto!!.text.toString(), contexto)
            }

        }//guarda los datos del servidor
    }

    //FUNCION PARA OBTENER LA IP DEL SERVIDOR Y EL PUERTO DE CONEXION
    private fun GetServerData() {
        val e = preferencias!!
        ip!!.text = e.getString("ip", "")
        puerto!!.text = e.getInt("puerto", 0).toString()
    } //obtiene la ip y el puerto del servidor

    private fun ValidateConnection(ip: String, puerto: String, context: Context) {
        if (ip.length > 0 && puerto.length > 0) {
            if (funciones!!.isNetworkConneted(this)) {
                try {
                    val ruta: String = "http://$ip:$puerto/conexion"
                    val url = URL(ruta)
                    with(url.openConnection() as HttpURLConnection) {
                        connectTimeout = 30000
                        requestMethod = "GET"
                        if (responseCode == 200) {
                            inputStream.bufferedReader().use {
                                val response = StringBuffer()
                                var inputline = it.readLine()
                                while (inputline != null) {
                                    response.append(inputline)
                                    inputline = it.readLine()
                                }
                                it.close() //cerramos el buffer
                                val respuesta = JSONArray(response.toString())
                                if (respuesta.length() > 0) {
                                    val res = respuesta.getJSONObject(0) //obtenemos los datos


                                    if (res.getInt("error") > 0) {
                                        val editor = preferencias!!.edit()
                                        editor!!.putInt("puerto", puerto.toInt())
                                        editor.putString("ip", ip)
                                        editor.commit()

                                        alerta!!.dismisss()
                                        val alert: Snackbar = Snackbar.make(vista!!, res.getString("response"), Snackbar.LENGTH_LONG)
                                        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.btnVerde))
                                        alert.show()

                                    } else {
                                        throw  Exception(res.getString("response"))
                                    } //valida que la respuesta sea  la correcta
                                } else {
                                    throw  Exception("Se Conecto con el Servidor, No hubo Respuesta")
                                }//valida que se haya obtenido datos del JSON
                            } //obtenmos los datos que nos envia el servidor
                        } else {
                            throw  Exception("Error de Comunicacion, Codigo:$responseCode")
                        } //valida que el codigo de respuesta del servidor sea ok 200
                    }
                } catch (e: Exception) {
                    alerta!!.dismisss()
                    val alert: Snackbar =
                        Snackbar.make(this.vista!!, e.message.toString(), Snackbar.LENGTH_LONG)
                    alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
                    alert.show()
                } //valida se si presenta algun error de conexion u otro
            } else {
                alerta!!.dismisss()
                val alert: Snackbar = Snackbar.make(
                    this.vista!!,
                    "Enciende los Datos o el Wifi",
                    Snackbar.LENGTH_LONG
                )
                alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
                alert.show()
            } //valida que este encendido los datos o el wifi
        } else {
            alerta!!.dismisss()
            val alert: Snackbar =
                Snackbar.make(this.vista!!, "Debes llenar los campos", Snackbar.LENGTH_LONG)
            alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
            alert.show()
        } //valida los campos no sean vacios
    }//valida que haya comunicacion con el servidor


    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    //FUNCION PARA VERIFICAR LA VERSION DE LA APP INSTALADA
    private suspend fun getAppVersion() {
        try {
            val direccion = url!! + "updateapp"
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    runOnUiThread {
                        alerta!!.Cargando()
                    }
                    delay(5000)
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                            }
                            data.close()
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                for (i in 0 until respuesta.length()) {
                                    val dato = respuesta.getJSONObject(i)

                                    versionAppServer = funciones!!.validateJsonIsnullString(dato, "version")
                                    urlAppServer = funciones!!.validateJsonIsnullString(dato, "url")

                                    runOnUiThread {
                                        if(versionActual >= versionAppServer!!.toFloat()){
                                            alerta!!.dismisss()
                                            Toast.makeText(applicationContext, "NO ES NECESARIO ACTUALIZAR", Toast.LENGTH_SHORT).show()
                                        }else{
                                            alerta!!.dismisss()
                                            mensajeUpdate(versionAppServer.toString(), urlAppServer.toString())
                                        }
                                    }

                                } //termina el for
                            } else {
                                alerta!!.dismisss()
                                ShowAlert("NO SE ENCONTRARON DATOS DE ACTUALIZACIOIN")
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        alerta!!.dismisss()
                        throw Exception("SERVIDOR: NO SE ENCONTRARON DATOS DE ACTUALIZACION")
                    }
                } catch (e: Exception) {
                    alerta!!.dismisss()
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alerta!!.dismisss()
            ShowAlert("ERROR AL CONECTARSE CON EL SERVIDOR")
        }
    }

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(vista!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@Configuracion, R.color.moderado))
        alert.show()
    }

    //FUNCION PARA OBTERNER LA URL DEL SERVER
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.length > 0 && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    } //obtiene la url de la api

    //FUNCION PARA CREAR EL DIALOG DE ACTUALIZAR APP
    fun mensajeUpdate(versionServer: String, urlServer: String){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_update)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            //updateDialog.dismiss()
            //Toast.makeText(applicationContext, "FUNCION EN DESARROLLO", Toast.LENGTH_SHORT).show()
            updateDialog.dismiss()
            updateVersionApp(versionServer)
            Descargar(urlServer, "UpdateApp_$versionServer")
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    //FUNCION PARA ACTUALIZAR LA VERSION ACTUAL DE LA APP
    private fun updateVersionApp(versionApp:String){
        val editor = preferencias!!.edit()
        editor.remove("versionActualApp")
        editor.putFloat("versionActualApp", versionApp.toFloat())
        editor.apply()
    }

    //FUNCION PARA DESCARGAR Y EJECUTAR LA INSTALACION DE LA ACTUALIZACION
    fun Descargar(url: String, filename: String){
        val downloadApk = DownloadApk(this@Configuracion)
        downloadApk.startDownloadingApk(url, filename);
    }

}