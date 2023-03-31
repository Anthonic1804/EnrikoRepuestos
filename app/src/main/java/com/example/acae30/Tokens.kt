package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.acae30.database.Database
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class Tokens : AppCompatActivity() {

    private lateinit var btnEmpleado : FloatingActionButton
    private lateinit var btnNuevo : FloatingActionButton
    private lateinit var btnAtras : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView

    private var database: Database? = null
    private var funciones: Funciones? = null
    private var alert: AlertDialogo? = null
    private var url: String? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var vista: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tokens)

        btnEmpleado = findViewById(R.id.btn_floatEmpleados)
        btnNuevo = findViewById(R.id.btn_floatNuevo)
        btnAtras = findViewById(R.id.imgbtnatras)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones = Funciones()
        database = Database(this)
        alert = AlertDialogo(this)

        getApiUrl()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

    }

    override fun onStart() {
        super.onStart()

        btnAtras.setOnClickListener {
            atras()
        }

        btnNuevo.setOnClickListener{
            nuevoToken()
        }

        btnEmpleado.setOnClickListener {
            mensajeEmpleados()
        }
    }

    //MOSTRANDO DIALOGO DE CARGA DE INFORMACION
    private fun mensajeEmpleados(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cargar_empleados)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            cargarEmpleados()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    //OBTENIENDO LA URL DEL SERVIDOR
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.length > 0 && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }

    //VALIDANDO LA CARGA DE LOS EMPLEADOS
    @OptIn(DelicateCoroutinesApi::class)
    private fun cargarEmpleados(){
        if (url != null) {
            if (funciones!!.isNetworkConneted(this)) {
                alert!!.Cargando() //muestra la alerta
                GlobalScope.launch(Dispatchers.IO) {
                    getEmpleados()
                } //courrutina para obtener clientes
            } else {
                ShowAlert("Enciende tus datos o el wifi")
            }
        } else {
            ShowAlert("No hay configuracion del Servidor")
        }
    }

    //OBTENIENDO LOS EMPLEADOS DEL SERVIDOR
    suspend fun getEmpleados() {
        //IMPORTANDO DATOS DE TABLA SUCURSALES CLIENTE
        try {
            val direccion = url!! + "empleados"
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        messageAsync("Cargando 10%")
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                                if (talla <= 45) {
                                    messageAsync("Cargando $talla%")
                                }
                            }
                            messageAsync("Cargando 45%")
                            data.close()
                            messageAsync("Cargando 50%")
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveEmpleadosDatabase(respuesta) //guarda los datos en la bd
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Vendedor Almacenados Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } else {
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Vendedor Almacenados Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("SERVIDOR: NO SE ENCONTRARON VENDEDORES REGISTRADOS")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alert!!.dismisss()
            ShowAlert(e.message.toString())
        }
    }

    //ALMACENANDO LOS EMPLEADOS EN LA BD SQLITE
    fun saveEmpleadosDatabase(json: JSONArray) {
        val bd = database!!.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.execSQL("DELETE FROM empleado") //LIMPIANDO TABLA EMPLEADO

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'empleado'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id_empleado", dato.getInt("id"))
                valor.put("nombre_empleado", funciones!!.validateJsonIsnullString(dato, "empleado"))
                bd.insert("empleado", null, valor)
                contador += talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    fun messageAsync(mensaje: String) {
        if (alert != null) {
            runOnUiThread {
                alert!!.changeText(mensaje)
            }
        }
    }

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(vista!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
        alert.show()
    }

    //FUNCIONES DE REDIRECCION
    private fun nuevoToken(){
        val Intent = Intent(this@Tokens, NuevoToken::class.java)
        startActivity(Intent)
        finish()
    }

    private fun atras(){
        val Intent = Intent(this@Tokens, Inicio::class.java)
        startActivity(Intent)
        finish()
    }



}