package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.ConfigController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityCargaDatosBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class carga_datos : AppCompatActivity() {

    private lateinit var url: String
    private var idVendedor = 0
    private var alert: AlertDialogo? = null
    private var database: Database? = null

    private var configController = ConfigController()
    private var inventarioController = InventarioController()
    private var clietnesController = ClientesController()
    private var pedidosController = PedidosController()
    private var funciones = Funciones()

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var binding : ActivityCargaDatosBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCargaDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = this@carga_datos.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        alert = AlertDialogo(this@carga_datos)
        database = Database(this@carga_datos)

        url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        idVendedor = preferences.getInt("Idvendedor", 0)

    }

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            val intento = Intent(this@carga_datos, Inicio::class.java)
            startActivity(intento)
        }

        binding.cvclientes.setOnClickListener {
            if (funciones.isInternetAvailable(this@carga_datos)) {
                alert!!.Cargando()
                CoroutineScope(Dispatchers.IO).launch {
                    getClients()
                }
            } else {
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvinventario.setOnClickListener {
            val hojaCarga = preferences.getBoolean("Hoja_carga_inventario_app", false)
            if (funciones.isInternetAvailable(this@carga_datos)) {
                alert!!.Cargando()

                if(!hojaCarga){
                    /*
                    * Si la Hoja de Carga está desactivada en SQL Server
                    * Carga el inventario Completo
                    * */
                    CoroutineScope(Dispatchers.IO).launch {
                        getInventario()
                    }
                }else{
                    /*
                    * Si está Activa solamente cargar el inventario de dicha hoja
                    * */
                    ingresarHojaCarga()
                    alert!!.dismisss()
                }

            } else {
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvcuentas.setOnClickListener {
            if (funciones.isInternetAvailable(this@carga_datos)) {
                alert!!.Cargando()
                CoroutineScope(Dispatchers.IO).launch {
                    getCuentas()
                }
            } else {
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvpedidos.setOnClickListener {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    pedidosController.eliminarPedidosAntiguos(this@carga_datos)
                }
                funciones.mostrarMensaje("PEDIDOS ELIMINADOS", this@carga_datos, binding.vistaalerta)
            } catch (e: Exception) {
                funciones.mostrarAlerta("ERROR AL ELIMINAR LOS PEDIDOS -> ${e.message}", this@carga_datos, binding.vistaalerta)
            }

        }
    }

    //FUNCION PARA MOSTRAR EL DIALOG DE INGRESO DE HOJA DE CARGA
    private fun ingresarHojaCarga() {
        val hojaCargaDialog = Dialog(this, R.style.Theme_Dialog)
        hojaCargaDialog.setCancelable(false)

        hojaCargaDialog.setContentView(R.layout.hoja_carga)

        val btnAceptar = hojaCargaDialog.findViewById<TextView>(R.id.tvCargar)
        val btnCancelar = hojaCargaDialog.findViewById<TextView>(R.id.tvCancelar)

        btnAceptar.setOnClickListener {
            val numero = hojaCargaDialog.findViewById<TextInputEditText>(R.id.tietNumeroCarga).text.toString()
            if (numero.isEmpty() || numero.toInt() == 0) {
                funciones.mostrarAlerta("ERROR EN LA HOJA DE CARGA", this@carga_datos, binding.vistaalerta)
            } else {

                val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
                if(hojaCargaActiva == numero.toInt()){
                    mensajeRecargarHoja()
                }else{
                    CoroutineScope(Dispatchers.IO).launch {
                        //OBTENIENDO INVENTARIO DESDE HOJA DE CARGA
                        inventarioController.obtenerInventarioHojaCarga(0, numero.toInt(), idVendedor, this@carga_datos, binding.vistaalerta)
                    }
                }

                hojaCargaDialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener {
            hojaCargaDialog.dismiss()
        }

        hojaCargaDialog.show()

    }

    //FUNCION PARA MOSTRAR EL DIALOG DE RECARGA DE HOJA
    private fun mensajeRecargarHoja() {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        val idHojaCarga = preferences.getInt("idHojaCarga", 0)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "¿DESEA REALIZAR LA RECARGA DE SU HOJA?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            //Toast.makeText(this@carga_datos, "OPCION EN VERIFICACION", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                inventarioController.obtenerHojaRecargas(this@carga_datos,idHojaCarga,binding.vistaalerta)
            }
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    //OBTENIENDO SUCURSALES DESDE WEBSERVIS
    //09-03-2023
    private suspend fun getClients() {
        try {
            //val direccion = url!! + "clientes"
            val id_vendedor = preferences.getInt("Idvendedor", 0)
            val direccion = url + "clientes/vendedor/"+id_vendedor
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
                                saveClienteDataBase(respuesta)
                            } else {
                                throw Exception("Servidor no Devolvio datos")
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    alert!!.dismisss()
                    funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
                }
            } //ABRIMOS LA CONEXION
        } catch (e: Exception) {
            alert!!.dismisss()
            funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
        }

        //IMPORTANDO DATOS DE TABLA SUCURSALES CLIENTE

        try {
            val direccion = url + "sucursales"
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
                                saveSucursalesDatabase(respuesta) //guarda los datos en la bd
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Cliente Almacenados Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } else {
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Cliente Almacenados Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("SERVIDOR: NO SE ENCONTRARON SUCURSALES REGISTRADAS")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alert!!.dismisss()
            funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
        }
        finally {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    configController.obtenerConfigPagareObligatorio(this@carga_datos)
                }
            }catch (e:Exception){
                funciones.mostrarAlerta("ERROR AL CARGAR LA CONFIG -> ${e.message}", this@carga_datos, binding.vistaalerta)
            }finally {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        clietnesController.obtenerPreciosPersonalizados(this@carga_datos)
                    }
                }catch (e:Exception){
                    funciones.mostrarAlerta("ERROR AL CARGAR LOS PRECIOS PERSONALIZADOS -> ${e.message}", this@carga_datos, binding.vistaalerta)
                }
            }

        }
    } //obtiene los clientes del servidor

    //GUARDANDO SUCURSALES EN SQLITE
    //28-01-2023
    private fun saveSucursalesDatabase(json: JSONArray) {
        val bd = database!!.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("codigo_sucursal", funciones.validateJsonIsnullString(dato, "codigo_sucursal"))
                valor.put("nombre_sucursal", funciones.validateJsonIsnullString(dato, "nombre_sucursal"))
                valor.put("direccion_sucursal", funciones.validateJsonIsnullString(dato, "dteDireccion"))//DATO DTE
                valor.put("municipio_sucursal", funciones.validateJsonIsnullString(dato, "municipio"))//DATO DTE
                valor.put("depto_sucursal", funciones.validateJsonIsnullString(dato, "departamento"))//DATO DTE
                valor.put("telefono_1", funciones.validateJsonIsnullString(dato, "dteTelefono"))//DATO DTE
                valor.put("telefono_2", funciones.validateJsonIsnullString(dato, "telefono2"))
                valor.put("correo_sucursal", funciones.validateJsonIsnullString(dato, "correo"))
                valor.put("contacto_sucursal", funciones.validateJsonIsnullString(dato, "contacto"))

                //ARGEGANDO DATOS PENDIENTE Y DTE DE LA SUCURSAL
                valor.put("Id_ruta", dato.getInt("id_ruta"))
                valor.put("Ruta", funciones.validateJsonIsnullString(dato, "ruta"))
                valor.put("DTECodDepto", funciones.validateJsonIsnullString(dato, "dteCodDepto"))
                valor.put("DTECodMunicipio", funciones.validateJsonIsnullString(dato, "dteCodMunicipio"))
                valor.put("DTECodPais", funciones.validateJsonIsnullString(dato, "dteCodPais"))
                valor.put("DTEPais", funciones.validateJsonIsnullString(dato, "dtePais"))

                bd.insert("cliente_sucursal", null, valor)
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
    } //INSERTANDO DATOS EN LA TABLA SUCURSALES EN SQLITE

    private suspend fun getInventario() {
        // TABLA INVENTARIO
        try {
            val direccioncantidad = url + "inventario/cantidad"
            val urlcantidad = URL(direccioncantidad)
            var cantidadRegistros = 0.toInt()
            with(withContext(Dispatchers.IO) {
                urlcantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()

                            cantidadRegistros = readline.toInt()
                        }

                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception("Error #1 Linea 207:$responseCode")
                }
            }

            val BLOQUE = 1000.toInt()

            //Log.d("Cantidad: ", cantidadRegistros!!.toString())
            var inicio = 0.toInt()
            var longitud = 0.toInt()
            var registrosCargados = 0.toInt()

            if (cantidadRegistros < BLOQUE) {
                longitud = cantidadRegistros
                registrosCargados = cantidadRegistros
            } else {
                longitud = BLOQUE
                registrosCargados = BLOQUE
            }

            val bd = database!!.writableDatabase
            try {
                bd!!.beginTransaction() //inicio la transaccion
                //bd!!.execSQL("DELETE FROM inventario") //limpiamos los registros viejos par obtener los nuevos
                bd.delete("inventario", null, null)

                val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME =  'inventario'"
                bd.execSQL(sql2)

                bd.setTransactionSuccessful()
            } catch (e: Exception) {
                throw Exception("Error #2 Linea 237")
            } finally {
                bd!!.endTransaction()
                bd.close()
            }

            //var porcentaje = (100 * registrosCargados) / cantidadRegistros
            var porcentaje = 2

            do {

                if (porcentaje <= 99.toInt()) {
                    messageAsync("Cargando " + porcentaje.toString() + "%")
                }else{
                    messageAsync("Cargando 100%")
                }

                porcentaje += 13

                val direccion =
                    url + "inventario/" + inicio.toString() + "/" + longitud.toString()
                val url = URL(direccion)
                with(withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpURLConnection) {
                    try {
                        connectTimeout = 30000
                        requestMethod = "GET"
                        if (responseCode == 200) {

                            inputStream.bufferedReader().use { data ->
                                val response = StringBuffer()
                                var inputLine = data.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = data.readLine()
                                }
                                //messageAsync("Cargando 45%")
                                data.close()
//                                messageAsync("Cargando 50%")
                                val respuesta = JSONArray(response.toString())
                                if (respuesta.length() > 0) {
                                    inventarioController.saveInventarioDatabase(respuesta, this@carga_datos, binding.vistaalerta,0)
                                    //println("DATOS ALMACENADOS CORRECTAMEMENTE")
                                } else {
                                    throw Exception("Servidor no Devolvio datos")
                                } //caso que la respuesta venga vacia
                            }
                        } else {
                            throw Exception("Error de Comunicacion con el servidor:$responseCode")
                        }
                    } catch (e: Exception) {
                        throw Exception("Error #3 Linea 285:$responseCode")
                    }
                }//termina de obtener los datos

                inicio += BLOQUE
                registrosCargados += longitud

                if (registrosCargados > cantidadRegistros && inicio < cantidadRegistros) {
                    longitud = cantidadRegistros
                }

            } while (inicio < cantidadRegistros)

        } catch (e: Exception) {
            alert!!.dismisss()
            //ShowAlert("Error #4 Linea 301")
        }

        // TABLA INVENTARIO PRECIOS
        try {

            val direccionprecioscantidad = url + "inventario/precios/cantidad"
            val urlprecioscantidad = URL(direccionprecioscantidad)
            var cantidadPreciosRegistros = 0.toInt()
            with(withContext(Dispatchers.IO) {
                urlprecioscantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()

                            cantidadPreciosRegistros = readline.toInt()
                        }

                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception("Error #5 Linea 325:$responseCode")
                }
            }

            val BLOQUE_PRECIOS = 1000.toInt()

            //Log.d("Cantidad: ", cantidadRegistros!!.toString())
            var inicioPrecios = 0.toInt()
            var longitudPrecios = 0.toInt()
            var registrosPreciosCargados = 0.toInt()

            if (cantidadPreciosRegistros < BLOQUE_PRECIOS) {
                longitudPrecios = cantidadPreciosRegistros
                registrosPreciosCargados = cantidadPreciosRegistros
            } else {
                longitudPrecios = BLOQUE_PRECIOS
                registrosPreciosCargados = BLOQUE_PRECIOS
            }

            val bd = database!!.writableDatabase
            try {
                bd!!.beginTransaction() //inicio la transaccion
                bd.delete("inventario_precios", null, null)

                val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME =  'inventario_precios'"
                bd.execSQL(sql2)

                bd.setTransactionSuccessful()
            } catch (e: Exception) {
                throw Exception("Error #6 Linea 354")
            } finally {
                bd!!.endTransaction()
                bd.close()
            }

            do {
                var porcentaje = (100 * registrosPreciosCargados) / cantidadPreciosRegistros

                if (porcentaje > 100.toInt()) {
                    porcentaje = 100.toInt()
                }

                if (porcentaje > 50.toInt()) {
                    messageAsync("Cargando " + porcentaje.toString() + "%")
                }

                val direccion =
                    url!! + "inventario/precios/" + inicioPrecios.toString() + "/" + longitudPrecios.toString()
                val url = URL(direccion)
                with(withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpURLConnection) {
                    try {
                        connectTimeout = 30000
                        requestMethod = "GET"
                        if (responseCode == 200) {

                            inputStream.bufferedReader().use { data ->
                                val response = StringBuffer()
                                var inputLine = data.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = data.readLine()
                                }
                                //messageAsync("Cargando 45%")
                                data.close()
//                                messageAsync("Cargando 50%")
                                val respuesta = JSONArray(response.toString())
                                if (respuesta.length() > 0) {
                                    inventarioController.saveInventarioPreciosDatabase(respuesta, this@carga_datos)
                                } else {
                                    throw Exception("Servidor no Devolvio datos")
                                } //caso que la respuesta venga vacia
                            }
                        } else {
                            throw Exception("Error de Comunicacion con el servidor:$responseCode")
                        }
                    } catch (e: Exception) {
                        throw Exception("Error #7 Linea 401:$responseCode")
                    }
                }//termina de obtener los datos

                inicioPrecios += BLOQUE_PRECIOS
                registrosPreciosCargados += longitudPrecios

                if (registrosPreciosCargados > cantidadPreciosRegistros && inicioPrecios < cantidadPreciosRegistros) {
                    longitudPrecios = cantidadPreciosRegistros
                }

            } while (inicioPrecios < cantidadPreciosRegistros)

            messageAsync("Cargando 100%")
            delay(1000)
            messageAsync("Inventario Almacenado Exitosamente")
            delay(1500)
            alert!!.dismisss()

        } catch (e: Exception) {
            //alert!!.dismisss()
            //ShowAlert("NO SE ENCONTRARON ESCALAS REGISTRADAS")
           // ShowAlert("INVENTARIO GARGADO CORRECTAMENTE")
            messageAsync("Inventario Almacenado Exitosamente")
            delay(1500)
            alert!!.dismisss()
        }finally {
            inventarioController.obtenerFechaInventario(this@carga_datos)
        }
    }

    private suspend fun getCuentas() {
        try {
            val direccion = url + "cuentas"
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
                                saveCuentaDatabase(respuesta) //guarda los datos en la bd
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Cuentas Almacenadas Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } else {
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Cuentas Almacenados Exitosamente")
                                delay(1500)
                                alert!!.dismisss()
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alert!!.dismisss()
            funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
        }
    }

    fun messageAsync(mensaje: String) {
        if (alert != null) {
            runOnUiThread {
                alert!!.changeText(mensaje)
            }
        }
    }//muestra la carga del mensaje de forma asincrona

    private fun saveClienteDataBase(json: JSONArray) {

        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //inicio la transaccion

            bd.execSQL("DELETE FROM clientes") //limpiamos los registros viejos par obtener los nuevos
            bd.execSQL("DELETE FROM cliente_precios")
            bd.execSQL("DELETE FROM cliente_sucursal") //LIMPIANDO TABLA SUCURSALES

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones.validate(dato.getString("codigo")))
                data.put("Cliente", funciones.validate(dato.getString("cliente")))
                data.put("Dui", funciones.validate(dato.getString("dui")))
                data.put("Nit", funciones.validate(dato.getString("nit")))
                data.put("Nrc", funciones.validate(dato.getString("nrc")))
                data.put("Giro", funciones.validate(dato.getString("giro")))
                data.put(
                    "Categoria_cliente",
                    funciones.validate(dato.getString("categoria_cliente"))
                )
                data.put(
                    "Terminos_cliente",
                    funciones.validate(dato.getString("terminos_cliente"))
                )
                data.put("Plazo_credito", funciones.validate(dato.getInt("plazo_credito")))
                data.put("Limite_credito",
                    funciones.validate(dato.getString("limite_credito").toFloat())
                )
                data.put("Balance", funciones.validate(dato.getString("balance").toFloat()))
                data.put("Estado_credito", funciones.validate(dato.getString("estado_credito")))
                data.put("Direccion", funciones.validate(dato.getString("direccion")))
                data.put("Municipio", funciones.validate(dato.getString("municipio")))
                data.put("Departamento", funciones.validate(dato.getString("departamento")))
                data.put("Telefono_1", funciones.validate(dato.getString("telefono1")))
                data.put("Telefono_2", funciones.validate(dato.getString("telefono2")))
                data.put("Correo", funciones.validate(dato.getString("correo")))
                data.put("Contacto", funciones.validate((dato.getString("contacto"))))
                data.put("Id_ruta", funciones.validateJsonIsNullInt(dato, "id_ruta"))
                data.put("Id_vendedor", funciones.validateJsonIsNullInt(dato, "id_vendedor"))
                data.put("Vendedor", funciones.validate(dato.getString("vendedor")))
                data.put("Status", funciones.validate(dato.getString("status")))
                data.put("Ultima_venta", funciones.validate(dato.getString("fecha_ult_venta")))
                data.put(
                    "Aporte_mensual",
                    funciones.validate(dato.getString("aporte_mensual").toFloat())
                )

                //AGREGADO EL CAMPO PARA VERIFICACION DEL PAGARE
                val pagareFirmado = if(dato.getBoolean("pagare_Firmado_app")) 1 else 0
                data.put("Firmar_pagare_app", pagareFirmado)

                //AGREGANDO EL CAMPO PARA VERIFICACION DE PERSONA JURIDICA
                data.put("Persona_juridica", funciones.validate(dato.getString("persona_juridica")))

                //VALIDANDO EL DTEGIRO ALMACENADO EN EL SERVIDOR
                data.put("dteGiro", funciones.validate(dato.getString("dteGiro")))
                data.put("Ruta", funciones.validate(dato.getString("ruta")))

                data.put("Ruta", funciones.validate(dato.getString("ruta")))
                data.put("DTECodDepto", funciones.validate(dato.getString("dteCodDepto")))
                data.put("DTECodMunicipio", funciones.validate(dato.getString("dteCodMunicipio")))
                data.put("DTECodPais", funciones.validate(dato.getString("dteCodPais")))
                data.put("DTEDireccion", funciones.validate(dato.getString("dteDireccion")))
                data.put("DTEPais", funciones.validate(dato.getString("dtePais")))
                data.put("DTETelefono", funciones.validate(dato.getString("dteTelefono")))
                data.put("DTECorreo", funciones.validate(dato.getString("dteCorreo")))

                bd.insert("clientes", null, data)
                contador += talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //recorre el json array
            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }//guarda los datos en la bd

    private fun saveCuentaDatabase(json: JSONArray) {
        val bd = database!!.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd!!.beginTransaction() //inicia la transaccion
            bd.execSQL("DELETE FROM cuentas") //eliminamos la cuentas

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'cuentas'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put(
                    "Codigo_cliente",
                    funciones.validateJsonIsnullString(dato, "codigo_cliente")
                )
                valor.put("Documento", funciones.validateJsonIsnullString(dato, "documento"))
                valor.put("Fecha", funciones.validateJsonIsnullString(dato, "fecha"))
                valor.put("Valor", funciones.validateJsonIsNullFloat(dato, "valor"))
                valor.put(
                    "Abono_inicial",
                    funciones.validateJsonIsNullFloat(dato, "abono_inicial")
                )
                valor.put(
                    "Saldo_inicial",
                    funciones.validateJsonIsNullFloat(dato, "saldo_inicial")
                )
                valor.put("Plazo", funciones.validateJsonIsNullFloat(dato, "plazo"))
                valor.put(
                    "Fecha_vencimiento",
                    funciones.validateJsonIsnullString(dato, "fecha_vencimiento")
                )
                valor.put("Saldo_actual", funciones.validateJsonIsNullFloat(dato, "saldo_actual"))
                valor.put(
                    "Fecha_ult_pago",
                    funciones.validateJsonIsnullString(dato, "fecha_ult_pago")
                )
                valor.put("Valor_pago", funciones.validateJsonIsNullFloat(dato, "valor_pago"))
                valor.put("Relacionado", funciones.validateJsonIsnullString(dato, "relacionado"))
                valor.put("Status", funciones.validateJsonIsnullString(dato, "status"))
                valor.put(
                    "Fecha_cancelado",
                    funciones.validateJsonIsnullString(dato, "fecha_cancelado")
                )
                valor.put("dias_tardios", funciones.validateJsonIsNullInt(dato, "dias_tardios"))

                bd.insert("cuentas", null, valor)
                contador = contador + talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //termina el for
            bd.setTransactionSuccessful() //transaccion exitosa
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    } //inserta las cxc en la tabla

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
      //  super.onBackPressed()

    //   finish()
    }//anula el boton atras

}