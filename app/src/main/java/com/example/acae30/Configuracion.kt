package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.acae30.database.Database
import com.example.acae30.modelos.Config
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_inicio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class Configuracion : AppCompatActivity() {

    private var swlista: Switch? = null
    private var swminiatura: Switch? = null
    private var swSinExistencia: Switch? = null
    private var dataBase: Database? = null

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

    //VARIABLES TABLA CONFIG DE LA APP
    private var vistaInventario: Int? = null //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var sinExistencias: Int? = null  // 1 -> Si    0 -> no

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
        dataBase = Database(this)


        var nombre_vendedor = preferencias!!.getString("Vendedor", "")
        txtvendedor!!.text = nombre_vendedor

        //FUNCIONES AGRAGADAS PARA LOS CONTROLES DE VISTA DE INVENTARIO
        swlista = findViewById(R.id.swlista)
        swminiatura = findViewById(R.id.swminiatura)
        swSinExistencia = findViewById(R.id.swSinExistencias)

        mostrarSeleccionInventario()

        // 1 -> LISTADO
        // 2 -> VISTA MINIATURA
        swlista!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                swminiatura!!.isChecked = false
                updateVistaInventario(2)
            } else {
                swminiatura!!.isChecked = true
                updateVistaInventario(1)
            }
        }

        swminiatura!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                swlista!!.isChecked = false
                updateVistaInventario(1)
            } else {
                swlista!!.isChecked = true
                updateVistaInventario(2)
            }
        }

        //ACTUALIZAR CONFIG PARA PEDIDOS SIN EXISTENCIAS
        //1 -> SI
        //0 -> NO
        swSinExistencia!!.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                updateConfigPedido(1)
            }else{
                updateConfigPedido(0)
            }
        }

    } //funcion que inicializa las variables

    //FUNCION PARA SELECCION LOS DATOS DE LA TABLA CONFIG
    private fun getConfigInventario(): ArrayList<Config> {
        val data = dataBase!!.writableDatabase
        val list = ArrayList<Config>()

        try {
            val cursor = data.rawQuery("SELECT * FROM config", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do{
                    val arreglo = Config(
                        cursor.getInt(0),
                        cursor.getInt(1)
                    )
                    list.add(arreglo)
                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
        return list
    }

    //FUNCION PARA EXTRAER EL CAMPO VISTA INVENTARIO
    private fun mostrarSeleccionInventario(){
        try {
            val list: ArrayList<com.example.acae30.modelos.Config> = getConfigInventario()
            if(list.isNotEmpty()){
                for(data in list){
                    vistaInventario = data.vistaInventario!!.toInt()
                    sinExistencias = data.sinExistencias!!.toInt()
                }

                if(vistaInventario == 1){
                    swminiatura!!.isChecked = true
                }else{
                    swlista!!.isChecked = true
                }

                if(sinExistencias == 1){
                    swSinExistencia!!.isChecked = true
                }else{
                    swSinExistencia!!.isChecked = false
                }
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR LA TABLA CONFIG")
        }
    }

    //FUNCION PARA ACTUALIZAR EL CAMPO VISTA INVENTARIO
    private fun updateVistaInventario(vistaInventario:Int){
        val data = dataBase!!.writableDatabase
        try {
            data!!.execSQL("UPDATE config SET vistaInventario=$vistaInventario")
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
    }

    //FUNCION PARA ACTUALIZAR EL CAMPO DE EXISTENCIAS 0 EN PEDIDOS
    private fun updateConfigPedido(sinExistencia: Int){
        val data = dataBase!!.writableDatabase
        try {
            data!!.execSQL("UPDATE config SET sinExistencias=$sinExistencia")
        }catch (e: Exception){
            throw Exception(e.message)
        }finally{
            data.close()
        }
    }

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