package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.database.Database
import com.example.acae30.modelos.Empleados
import com.example.acae30.modelos.JSONmodels.PrecioPersonalizadoJSON
import com.google.gson.Gson
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

class NuevoPrecioAutorizado : AppCompatActivity() {

    private lateinit var btnAtras : Button
    private lateinit var btnProcesar : Button
    private lateinit var btnBuscarProducto : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView
    private lateinit var edtProducto : EditText
    private lateinit var edtPrecio : EditText
    private lateinit var edtReferencia : EditText
    private lateinit var edtPrecioOld : EditText
    private lateinit var spEmpleado : Spinner
    private var codigoProducto : String? = ""
    private var nombreProducto : String? = ""
    private var precioProducto : String? = ""
    private var empleadoName: String = ""
    private var db: Database? = null
    private var empleadoId : Int = 0
    private var adminId : Int = 0

    private var url: String? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_token)
        val intento = intent
        btnAtras = findViewById(R.id.btnatras_token)
        btnProcesar = findViewById(R.id.btnProcesarToken)
        btnBuscarProducto = findViewById(R.id.btnProductoToken)
        edtProducto = findViewById(R.id.edtProducto)
        edtPrecio = findViewById(R.id.edtPrecio)
        edtPrecioOld = findViewById(R.id.edtPrecioOld)
        edtReferencia = findViewById(R.id.edtReferencia)
        codigoProducto = intento.getStringExtra("codigo")
        nombreProducto = intento.getStringExtra("producto")
        precioProducto = intento.getFloatExtra("precio", 0f).toString()
        db = Database(this)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        adminId = preferencias!!.getInt("Idvendedor", 0)

        getApiUrl()

        cargarEmpleado()

        if(codigoProducto != ""){
            edtProducto.setText(nombreProducto)
            edtReferencia.setText(codigoProducto)
            edtPrecioOld.setText("$ " + precioProducto)
        }

        btnBuscarProducto.setOnClickListener {
            val intent = Intent(this@NuevoPrecioAutorizado, Inventario::class.java)
            intent.putExtra("tokenBusqueda", true)
            startActivity(intent)
            finish()
        }

        btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        btnProcesar.setOnClickListener {
            if(validarFormulario()){
                val precioNuevo : Float = edtPrecio.text.toString().toFloat()
                if(precioNuevo <= 0f){
                    Toast.makeText(this@NuevoPrecioAutorizado, "EL NUEVO PRECIO INGRESADO ES INCORRECTO", Toast.LENGTH_LONG).show()
                }else{
                    validadToken(empleadoId, adminId, codigoProducto!!, precioNuevo)
                }
            }
            //Toast.makeText(this@NuevoToken, "FUNCION EN DESARROLLO", Toast.LENGTH_SHORT).show()
        }

        //IMPLEMENTANDO LOGICA DE EMPLEADO SELECCIONADA EN SPINNER
        spEmpleado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                empleadoName = parent?.getItemAtPosition(position).toString()

                if (empleadoName == "-- SELECCIONE UN EMPLEADO --") {
                    btnProcesar.isEnabled = false
                    btnProcesar.setBackgroundResource(R.drawable.border_btndisable)
                }else{
                    btnProcesar.isEnabled = true
                    btnProcesar.setBackgroundResource(R.drawable.border_btnactualizar)

                    getEmpleadoId(empleadoName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //NADA IMPLEMENTADO
            }

        }
    }

    private fun validarFormulario(): Boolean{
        val producto = edtProducto.text.toString()
        val nuevoPrecio = edtPrecio.text.toString()

        if(producto.isEmpty() || nuevoPrecio.isEmpty()){
            Toast.makeText(this@NuevoPrecioAutorizado, "LOS CAMPOS PRODUCTO Y NUEVO PRECIO SON REQUERIDOS", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.length > 0 && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }

    private fun getEmpleadoId(nombre : String){
        val db = db!!.readableDatabase
        val listaEmpleados = ArrayList<Empleados>()
        try {

            val dataEmpleado = db.rawQuery("SELECT * FROM empleado WHERE nombre_empleado='$nombre'", null)
            if(dataEmpleado.count > 0){
                dataEmpleado.moveToFirst()
                do{
                    val data = Empleados(
                        dataEmpleado.getString(0),
                        dataEmpleado.getString(1)
                    )
                    listaEmpleados.add(data)
                }while (dataEmpleado.moveToNext())

                for(data in listaEmpleados){
                    empleadoId = data.idEmpleado.toInt()
                }
            }else{
                Toast.makeText(this@NuevoPrecioAutorizado, "NO SE ENCONTRARON VENDEDORES REGISTRADOS", Toast.LENGTH_LONG).show()
            }
            dataEmpleado.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
    }

    private fun atras(){
        val intent = Intent(this@NuevoPrecioAutorizado, PreciosAutorizados::class.java)
        startActivity(intent)
        finish()
    }

    fun mensajeCancelar(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            atras()
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }


    //CARGANDO EL NOMBRE DEL EMPLEADO EN EL SPINNER
    private fun nombreEmpleado(): ArrayList<String> {
        val nombreEmpleado = arrayListOf<String>()
        nombreEmpleado.add("-- SELECCIONE UN EMPLEADO --")
        try {
            val list: ArrayList<com.example.acae30.modelos.Empleados> = getEmpleadoNombre()
            if(list.isNotEmpty()){
                for(data in list){
                    nombreEmpleado.add(data.nombreEmpleado)
                }
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR LA TABLA EMPLEADOS")
        }
        return nombreEmpleado
    }

    //FUNCION PARA CARGAR LOS EMPLEADOS AL SPINNER
    private fun cargarEmpleado() {
        spEmpleado = findViewById(R.id.spVendedor)
        val listSucursal = nombreEmpleado().toMutableList()

        val adaptador = ArrayAdapter(this@NuevoPrecioAutorizado, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spEmpleado.adapter = adaptador
    }

    //FUNCION PARA OBTENER LOS EMPLEADOS.
    private fun getEmpleadoNombre(): ArrayList<Empleados> {
        val db = db!!.readableDatabase
        val listaEmpleados = ArrayList<Empleados>()
        try {

            val dataEmpleado = db.rawQuery("SELECT * FROM empleado", null)
            if(dataEmpleado.count > 0){
                dataEmpleado.moveToFirst()
                do{
                    val data = Empleados(
                        dataEmpleado.getString(0),
                        dataEmpleado.getString(1)
                    )
                    listaEmpleados.add(data)
                }while (dataEmpleado.moveToNext())
            }else{
                Toast.makeText(this@NuevoPrecioAutorizado, "NO SE ENCONTRARON VENDEDORES REGISTRADOS", Toast.LENGTH_LONG).show()
            }
            dataEmpleado.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaEmpleados
    }

    private fun validadToken(id_empleado:Int, id_admin:Int, cod_producto:String, precio_asig:Float){
        try {
            val datos = PrecioPersonalizadoJSON(
                id_empleado,
                id_admin,
                cod_producto,
                precio_asig
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "token"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    if (responseCode == 201) {
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
                                    JSONObject(respuesta.toString())
                                if (res.length() > 0) {
                                    if (res.getInt("id_token") > 0 && !res.isNull("response")) {
                                        val idTokenServer: Int = res.getInt("id_token").toInt()
                                        when (res.getString("response")) {
                                                "TOKEN_OK" -> {
                                                    confirmarToken(id_empleado,
                                                        id_admin,
                                                        cod_producto,
                                                        precio_asig,
                                                        idTokenServer)
                                                }
                                                "ERROR_TOKEN" -> {
                                                    runOnUiThread {
                                                        Toast.makeText(this@NuevoPrecioAutorizado, "ERROR AL AUTORIZAR EL TOKEN", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        throw Exception("Error al procesar la solicitud")
                                    }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }
                    }else {
                        throw Exception("Error de comunicacion con el servidor")
                    }

                } catch (e: Exception) {
                    throw  Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private fun confirmarToken(id_empleado:Int, id_admin:Int, cod_producto:String, precio_asig:Float, idServer: Int) {
        val base = db!!.writableDatabase
        try {
            base.beginTransaction()
            val fechanow = getDateTime()
            val contenido = ContentValues()
            contenido.put("Id_vendedor", id_empleado)
            contenido.put("Id_admin", id_admin)
            contenido.put("cod_producto", cod_producto)
            contenido.put("precio_asig", precio_asig)
            contenido.put("fecha_registrado", fechanow)
            contenido.put("id_server", idServer)

            base.insert("preciosAutorizados", null, contenido)
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()

            mensajeCreado()
        }
    }

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    fun mensajeCreado(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "PROCESO REALIZADO CORRECTAMENTE"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            atras()
            updateDialog.dismiss()
        }

        tvCancel.visibility = View.GONE

        updateDialog.show()

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }

}
