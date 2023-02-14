package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var ip: TextView? = null
    private var puerto: TextView? = null
    private var vista: View? = null
    private var alerta: AlertDialogo? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var funciones: Funciones? = null
    private var reconfig = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        funciones = Funciones()
        reconfig = intent.getBooleanExtra("reconfig", false)
        alerta = AlertDialogo(this)
        ip = findViewById(R.id.txtip)
        puerto = findViewById(R.id.txtpuerto)
        vista = findViewById(R.id.alerta)
        //amarramos el widgets a las variables
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val btn: Button = findViewById(R.id.btnguardar)
        btn.setOnClickListener {
            validar()
        } //accion que se ejecuta cuando damos click a un boton
        validateServer()
    }

    private fun validateServer() {
        if (preferencias!!.contains("puerto") && preferencias!!.contains("ip")) {
            if (reconfig) {
                ip!!.text = preferencias!!.getString("ip", "")
                puerto!!.text = preferencias!!.getInt("puerto", 0).toString()
            } else {
                if (preferencias!!.contains("sesion")) {
                    var sesionactiva = preferencias!!.getBoolean("sesion", false)
                    if (sesionactiva) {
                        val intento = Intent(this, Inicio::class.java)
                        startActivity(intento)
                        finish()
                    } else {
                        preferencias!!.edit().remove("Idvendedor").commit()
                        preferencias!!.edit().remove("Vendedor").commit()
                        val intento = Intent(this, Login::class.java)
                        startActivity(intento)
                        finish()
                    }
                } else {
                    preferencias!!.edit().remove("Idvendedor").commit()
                    preferencias!!.edit().remove("Vendedor").commit()
                    val intento = Intent(this, Login::class.java)
                    startActivity(intento)
                    finish()
                }
            } //valida si es una reconfiguracion

        }
    } //valida que ya se tenga la conexion al servidor guardada

    fun validar() {
        if (ip!!.text.length > 0 && puerto!!.text.length > 0) {
            alerta!!.Cargando()
            val v = vista
            GlobalScope.launch(Dispatchers.IO) {
                val ip = ip!!.text.toString()
                val p = puerto!!.text.toString()
                if (funciones!!.isNetworkConneted(this@MainActivity)) {
                    ComproBarConexion(ip, p)
                } else {
                    alerta!!.dismisss()
                    val alerta: Snackbar =
                        Snackbar.make(v!!, "Enciendo el WIFI para Continuar", Snackbar.LENGTH_LONG)
                    alerta.view.setBackgroundColor(resources.getColor(R.color.moderado))
                    alerta.show()
                }

            }

        } else {
            val alerta: Snackbar =
                Snackbar.make(this.vista!!, "Debes llenar los campos", Snackbar.LENGTH_LONG)
            alerta.view.setBackgroundColor(resources.getColor(R.color.moderado))
            alerta.show()

        }
    } //funcion que valida que haya internet,revisa si se han llenado las cajas y llama la peticio


    fun ComproBarConexion(ip: String, puerto: String) {
        try {
            val ruta: String = "http://$ip:$puerto/conexion" //ruta de la api
            val url = URL(ruta)
            val ctx = this.vista
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"  // optional default is GET
                    val i: Int? = responseCode
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use {
                            val response = StringBuffer()
                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            } //obtenemo la respuesta del servidor
                            it.close()
                            val respuesta =
                                JSONArray(response.toString()) //se convierte en un json array
                            if (respuesta.length() > 0) {
                                val error = respuesta.getJSONObject(0)
                                if (error.getInt("error") == 200) {
                                    val editor = preferencias!!.edit()
                                    editor!!.putInt("puerto", puerto.toInt())
                                    editor.putString("ip", ip)
                                    editor.commit()
                                    //se guarda la direccion del servidor y se envia al login
                                    alerta!!.dismisss()
                                    val intet: Intent = Intent(ctx!!.context, Login::class.java)
                                    startActivity(intet)
                                    //redirige hacia el login
                                    this@MainActivity.finish() //termina la actividad
                                } else {
                                    throw Exception(error.getString("response"))
                                }
                            } else {
                                throw  Exception("No se ha Recibido Respuesta del Servidor")
                            }
                        }
                    } else {
                        throw  Exception("No se encontro el Servidor")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }

        } catch (e: Exception) {
            alerta!!.dismisss()
            if (e.message.toString() == "Host unreachable") {
                val alert: Snackbar =
                    Snackbar.make(this.vista!!, "Servidor no Encontrado", Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            } else {
                val alert: Snackbar =
                    Snackbar.make(this.vista!!, e.message.toString(), Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            }

        }
    } //comprueba la comunicacion

    override fun onStop() {
        if (ip!!.text.length > 0) {
            preferencias!!.edit().putString("lbl1", ip!!.text.toString()).commit()
        }
        if (puerto!!.text.length > 0) {
            preferencias!!.edit().putInt("lbl2", puerto!!.text.toString().toInt()).commit()
        }

        //guardamos si se ha escrito algo el usuario en las cajas de texto.
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        var cajaip = ""
        var cajapuerto = ""
        if (preferencias!!.contains("lbl1")) {
            cajaip = preferencias!!.getString("lbl1", "").toString()
            preferencias!!.edit().remove("lbl1").commit()
        }
        if (preferencias!!.contains("lbl2")) {
            cajapuerto = preferencias!!.getInt("lbl2", 0).toString()
            preferencias!!.edit().remove("lbl2").commit()
        }
        //obtenemos los datos si hay
        ip!!.text = cajaip
        puerto!!.text = cajapuerto
        //se asignan a las cajas


        //los removemos de las preferencias
    }

    override fun onDestroy() {
        preferencias!!.edit().remove("lbl1").commit()
        preferencias!!.edit().remove("lbl2").commit()
        //los removemos de las preferencias
        super.onDestroy()
    } //se llama cuando se destruya la actividad

    private var doubleBackToExitPressedOnce = false
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presiona Nuevamente Para Salir", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)

    }//anula el boton atras

}