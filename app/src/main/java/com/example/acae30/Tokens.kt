package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.TokenAdapter
import com.example.acae30.modelos.TokenData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Tokens : AppCompatActivity() {

    private lateinit var btnEmpleado : FloatingActionButton
    private lateinit var btnNuevo : FloatingActionButton
    private lateinit var btnAtras : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var rvTokenResgistrados : RecyclerView
    private lateinit var lblNoData : TextView

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
        rvTokenResgistrados = findViewById(R.id.rvTokenRegistrados)
        lblNoData = findViewById(R.id.lblNoData)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones = Funciones()
        database = Database(this)
        alert = AlertDialogo(this)

        lblNoData.visibility = View.GONE

        getApiUrl()
        validarDatos()

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

    //OBTENIENDO LA FECHA CON EL FORMATO CORRECTO
    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    //MOSTRANDOS LOS TOKEN REGISTRADO EN LA FECHA ACTUAL
    private fun getTokenByDate(): ArrayList<TokenData>{
        val data = database!!.readableDatabase
        val fechanow = getDateTime()
        val list = ArrayList<TokenData>()

        try {
            val cursor = data.rawQuery("SELECT T.Id, T.cod_producto, I.Descripcion, E.nombre_empleado, T.precio_asig FROM token T " +
                    "INNER JOIN inventario I ON I.Codigo = T.cod_producto " +
                    "INNER JOIN empleado E ON E.id_empleado = T.Id_vendedor " +
                    "WHERE fecha_registrado='$fechanow'", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val dataToken = TokenData(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getFloat(4)
                    )
                    list.add(dataToken)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
        return list
    }

    private fun validarDatos(){
        try{
            val lista = getTokenByDate()
            if(lista.size > 0){
                ArmarLista(lista)
            }else{
                lblNoData.visibility = View.VISIBLE
                lblNoData.text = "NO SE ENCONTRARON DATOS REGISTRADOS"
            }
        }catch (e: Exception){
                throw Exception(e.message)
        }
    }
    private fun ArmarLista(lista: java.util.ArrayList<TokenData>) {

        val mLayoutManager = LinearLayoutManager(
            this@Tokens,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvTokenResgistrados.layoutManager = mLayoutManager
        val adapter = TokenAdapter(lista, this@Tokens)
        rvTokenResgistrados.adapter = adapter

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
    private fun cargarEmpleados(){
        if (url != null) {
            if (funciones!!.isNetworkConneted(this)) {
                alert!!.Cargando() //muestra la alerta

                CoroutineScope(Dispatchers.IO).launch {
                    getEmpleados()
                }

            } else {
                mostrarAlerta("ERROR: NO TIENES CONEXION A INTERNET")
            }
        } else {
            mostrarAlerta("ERROR: NO SE ENCONTRO CONFIGURACION DEL SERVIDOR")
        }
    }

    //OBTENIENDO LOS EMPLEADOS DEL SERVIDOR
    private suspend fun getEmpleados() {
        //IMPORTANDO DATOS DE TABLA EMPLEADOS
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
                        mostrarAlerta("SERVIDOR: NO SE ENCONTRARON VENDEDORES REGISTRADOS")
                    }
                } catch (e: Exception) {
                    mostrarAlerta("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR")
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alert!!.dismisss()
            mostrarAlerta("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR")
        }
    }

    //ALMACENANDO LOS EMPLEADOS EN LA BD SQLITE
    private fun saveEmpleadosDatabase(json: JSONArray) {
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

    private fun mostrarAlerta(mensaje: String) {
        val alert: Snackbar = Snackbar.make(vista!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@Tokens, R.color.moderado))
        alert.show()
    }

    //FUNCIONES DE REDIRECCION
    private fun nuevoToken(){
        val intent = Intent(this@Tokens, NuevoToken::class.java)
        startActivity(intent)
        finish()
    }
    private fun atras(){
        val intent = Intent(this@Tokens, Inicio::class.java)
        startActivity(intent)
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }



}