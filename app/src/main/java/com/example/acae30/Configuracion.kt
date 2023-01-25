package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class Configuracion : AppCompatActivity() {
    private var atras: ImageButton? = null
    private var ip: TextView? = null
    private var puerto: TextView? = null
    private var btnGuardar: Button? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var vista: View? = null
    private var txtvendedor: TextView? = null
    private var funciones: Funciones? = null
    private var alerta: AlertDialogo? = null
    private var sqLite: TextView? = null
    private var versionCode: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)
        supportActionBar?.hide()
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones = Funciones()
        vista = findViewById(R.id.vistaalerta)
        ip = findViewById(R.id.txtip)
        txtvendedor = findViewById(R.id.txtvendedor)
        puerto = findViewById(R.id.txtpuerto)
        btnGuardar = findViewById(R.id.btnupdate)
        atras = findViewById(R.id.imgbtnatras)
        alerta = AlertDialogo(this)
        sqLite = findViewById(R.id.txtSqlite)
        versionCode = findViewById(R.id.txtKotlin)

        var nombre_vendedor = preferencias!!.getString("Vendedor", "")
        txtvendedor!!.text = nombre_vendedor

        //OBTENIENDO LA VERSION DE SQLITE UTILIZADA
        val cursor =
            SQLiteDatabase.create(null).rawQuery("select sqlite_version() AS sqlite_version", null)
        var sqliteVersion = ""
        while (cursor.moveToNext()) {
            sqliteVersion += cursor.getString(0)
        }
        sqLite!!.text = sqliteVersion


        //OPTENIENDO LA VERSION DE KOTLIN UTLIZADA
        var versionC = BuildConfig.VERSION_NAME.toString()
        versionCode!!.text = versionC

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
            GlobalScope.launch(Dispatchers.IO) {
                ValidateConnection(ip!!.text.toString(), puerto!!.text.toString(), contexto)
            }
        }//guarda los datos del servidor
    }

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
                                        val alert: Snackbar = Snackbar.make(
                                            vista!!,
                                            res.getString("response"),
                                            Snackbar.LENGTH_LONG
                                        )
                                        alert.view.setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.moderado
                                            )
                                        )
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
}