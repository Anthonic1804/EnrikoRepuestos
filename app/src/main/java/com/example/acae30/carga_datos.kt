package com.example.acae30

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.acae30.controllers.ConfigController
import com.example.acae30.database.Database
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_carga_datos.*
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class carga_datos : AppCompatActivity() {

    private var btnatras: ImageButton? = null
    private var url: String? = null
    private var preferencias: SharedPreferences? = null
    private var cvinventario: CardView? = null
    private var cvcliente: CardView? = null
    private var cvcuentas: CardView? = null
    private var cvpedidos: CardView? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var alert: AlertDialogo? = null
    private var vista: ConstraintLayout? = null
    private var funciones: Funciones? = null
    private var database: Database? = null

    private var configController = ConfigController()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carga_datos)
        supportActionBar?.hide()
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        btnatras = findViewById(R.id.imgbtnatras)
        cvcliente = findViewById(R.id.cvclientes)
        cvinventario = findViewById(R.id.cvinventario)
        cvcuentas = findViewById(R.id.cvcuentas)
        cvpedidos = findViewById(R.id.cvpedidos)
        alert = AlertDialogo(this)
        vista = findViewById(R.id.vistaalerta)
        funciones = Funciones()
        database = Database(this)
        getApiUrl() //obtiene la url del servidor

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

    }

    override fun onStart() {
        super.onStart()

        btnatras!!.setOnClickListener {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
        }//BOTON ATRAS

        cvcliente!!.setOnClickListener {
            if (url != null) {
                if (funciones!!.isNetworkConneted(this)) {
                    alert!!.Cargando() //muestra la alerta

                    CoroutineScope(Dispatchers.IO).launch {
                        getClients()
                    }//COURUTINA PARA OBTENER CLIENTES Y SUCURSALES

                } else {
                    ShowAlert("Enciende tus datos o el wifi")
                }
            } else {
                ShowAlert("No hay configuracion del Servidor")
            }
        }//cuando se hace click en la card de

        cvinventario!!.setOnClickListener {
            if (url != null) {
                if (funciones!!.isNetworkConneted(this)) {
                    alert!!.Cargando() //muestra la alerta

                    CoroutineScope(Dispatchers.IO).launch {
                        getInventario()
                    }//courrutina para obtener clientes

                } else {
                    ShowAlert("Enciende tus datos o el wifi")
                }
            } else {
                ShowAlert("No hay configuracion del Servidor")
            }
        }//cuando se carga los inventarios

        cvcuentas!!.setOnClickListener {
            if (url != null) {
                if (funciones!!.isNetworkConneted(this)) {
                    alert!!.Cargando() //muestra la alerta

                    CoroutineScope(Dispatchers.IO).launch {
                        getCuentas()
                    }//courrutina para obtener clientes

                } else {
                    ShowAlert("Enciende tus datos o el wifi")
                }
            } else {
                ShowAlert("No hay configuracion del Servidor")
            }
        }

        cvpedidos!!.setOnClickListener {
            try {
                deletePedido()
                ShowAlert("Pedidos Eliminados")
            } catch (e: Exception) {
                ShowAlert(e.message.toString())
            }

        }//elimina los pedidos que no sean de la fecha en el que se presiona
    }

    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.length > 0 && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    } //obtiene la url de la api

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(vista!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@carga_datos, R.color.moderado))
        alert.show()

    }//

    //OBTENIENDO SUCURSALES DESDE WEBSERVIS
    //09-03-2023
    private suspend fun getClients() {
        try {
            //val direccion = url!! + "clientes"
            val id_vendedor = preferencias!!.getInt("Idvendedor", 0)
            val direccion = url!! + "clientes/vendedor/"+id_vendedor
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
                    ShowAlert(e.message.toString())
                }
            } //ABRIMOS LA CONEXION
        } catch (e: Exception) {
            alert!!.dismisss()
            ShowAlert(e.message.toString())
        }

        //IMPORTANDO DATOS DE TABLA SUCURSALES CLIENTE

        try {
            val direccion = url!! + "sucursales"
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
            ShowAlert(e.message.toString())
        }
        finally {
            CoroutineScope(Dispatchers.IO).launch {
                configController.obtenerConfigPagareObligatorio(this@carga_datos)
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
            bd.execSQL("DELETE FROM cliente_sucursal") //LIMPIANDO TABLA SUCURSALES

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'cliente_sucursal'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("codigo_sucursal", funciones!!.validateJsonIsnullString(dato, "codigo_sucursal"))
                valor.put("nombre_sucursal", funciones!!.validateJsonIsnullString(dato, "nombre_sucursal"))
                valor.put("direccion_sucursal", funciones!!.validateJsonIsnullString(dato, "direccion"))
                valor.put("municipio_sucursal", funciones!!.validateJsonIsnullString(dato, "municipio"))
                valor.put("depto_sucursal", funciones!!.validateJsonIsnullString(dato, "departamento"))
                valor.put("telefono_1", funciones!!.validateJsonIsnullString(dato, "telefono1"))
                valor.put("telefono_2", funciones!!.validateJsonIsnullString(dato, "telefono2"))
                valor.put("correo_sucursal", funciones!!.validateJsonIsnullString(dato, "correo"))
                valor.put("contacto_sucursal", funciones!!.validateJsonIsnullString(dato, "contacto"))

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
            val direccioncantidad = url!! + "inventario/cantidad"
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
                    url!! + "inventario/" + inicio.toString() + "/" + longitud.toString()
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
                                    saveInventarioDatabase(respuesta) //guarda los datos en la bd
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

            val direccionprecioscantidad = url!! + "inventario/precios/cantidad"
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

            var BLOQUE_PRECIOS = 1000.toInt()

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
                                    saveInventarioPreciosDatabase(respuesta) //guarda los datos en la bd
                                    //println("PRECIOS ALMACENADOS CORRECTAMENTE")
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
        }
    }

    private suspend fun getCuentas() {
        try {
            val direccion = url!! + "cuentas"
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
            ShowAlert(e.message.toString())
        }
    }

    private fun messageAsync(mensaje: String) {
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

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'clientes'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones!!.validate(dato.getString("codigo")))
                data.put("Cliente", funciones!!.validate(dato.getString("cliente")))
                data.put("Dui", funciones!!.validate(dato.getString("dui")))
                data.put("Nit", funciones!!.validate(dato.getString("nit")))
                data.put("Nrc", funciones!!.validate(dato.getString("nrc")))
                data.put("Giro", funciones!!.validate(dato.getString("giro")))
                data.put(
                    "Categoria_cliente",
                    funciones!!.validate(dato.getString("categoria_cliente"))
                )
                data.put(
                    "Terminos_cliente",
                    funciones!!.validate(dato.getString("terminos_cliente"))
                )
                data.put("Plazo_credito", funciones!!.validate(dato.getInt("plazo_credito")))
                data.put("Limite_credito",
                    funciones!!.validate(dato.getString("limite_credito").toFloat())
                )
                data.put("Balance", funciones!!.validate(dato.getString("balance").toFloat()))
                data.put("Estado_credito", funciones!!.validate(dato.getString("estado_credito")))
                data.put("Direccion", funciones!!.validate(dato.getString("direccion")))
                data.put("Municipio", funciones!!.validate(dato.getString("municipio")))
                data.put("Departamento", funciones!!.validate(dato.getString("departamento")))
                data.put("Telefono_1", funciones!!.validate(dato.getString("telefono1")))
                data.put("Telefono_2", funciones!!.validate(dato.getString("telefono2")))
                data.put("Correo", funciones!!.validate(dato.getString("correo")))
                data.put("Contacto", funciones!!.validate((dato.getString("contacto"))))
                data.put("Id_ruta", funciones!!.validateJsonIsNullInt(dato, "id_ruta"))
                data.put("Id_vendedor", funciones!!.validateJsonIsNullInt(dato, "id_vendedor"))
                data.put("Vendedor", funciones!!.validate(dato.getString("vendedor")))
                data.put("Status", funciones!!.validate(dato.getString("status")))
                data.put("Ultima_venta", funciones!!.validate(dato.getString("fecha_ult_venta")))
                data.put(
                    "Aporte_mensual",
                    funciones!!.validate(dato.getString("aporte_mensual").toFloat())
                )

                //AGREGADO EL CAMPO PARA VERIFICACION DEL PAGARE
                val pagareFirmado = if(!funciones!!.validateBoolean(dato.getBoolean("pagare_Firmado_app"))) 0 else 1
                data.put("Firmar_pagare_app", pagareFirmado)

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

    private fun saveInventarioDatabase(json: JSONArray) {
        //var total=json.length()
        //var talla=(50.toFloat()/total.toFloat()).toFloat()
        //var contador:Float=0.toFloat()
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //inicio la transaccion
//            bd!!.execSQL("DELETE FROM inventario") //limpiamos los registros viejos par obtener los nuevos
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json

                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones!!.validateJsonIsnullString(dato, "codigo"))
                data.put("Tipo", funciones!!.validateJsonIsnullString(dato, "tipo"))
                data.put("Id_linea", funciones!!.validateJsonIsNullInt(dato, "id_linea"))
                data.put("Linea", funciones!!.validateJsonIsnullString(dato, "linea"))
                data.put("Descripcion", funciones!!.validateJsonIsnullString(dato, "descripcion"))
                data.put(
                    "Unidad_medida",
                    funciones!!.validateJsonIsnullString(dato, "unidad_medida")
                )
                data.put("Fraccion", funciones!!.validateJsonIsNullFloat(dato, "Fraccion"))
                data.put(
                    "Nombre_fraccion", funciones!!.validateJsonIsnullString(
                        dato,
                        "nombre_fraccion"
                    )
                )
                data.put("Existencia", funciones!!.validateJsonIsNullFloat(dato, "existencia"))
                data.put("Costo", funciones!!.validateJsonIsNullFloat(dato, "costo"))
                data.put("costo_iva", funciones!!.validateJsonIsNullFloat(dato, "costo_iva"))
                data.put(
                    "Precio_oferta",
                    funciones!!.validateJsonIsNullFloat(dato, "precio_oferta")
                )
                data.put("Precio_iva", funciones!!.validateJsonIsNullFloat(dato, "precio_iva"))
                data.put("Precio_u", funciones!!.validateJsonIsNullFloat(dato, "precio_u"))
                data.put("Precio_u_iva", funciones!!.validateJsonIsNullFloat(dato, "precio_u_iva"))
                data.put("Precio", funciones!!.validateJsonIsNullFloat(dato, "precio"))
                data.put("Status", funciones!!.validateJsonIsnullString(dato, "status"))
                data.put("Id_productor", funciones!!.validateJsonIsNullInt(dato, "id_productor"))
                data.put("Productor", funciones!!.validateJsonIsnullString(dato, "productor"))
                data.put("Id_proveedor", funciones!!.validateJsonIsNullInt(dato, "id_proveedor"))
                data.put("Proveedor", funciones!!.validateJsonIsnullString(dato, "proveedor"))
                data.put("Cesc", funciones!!.validateJsonIsnullString(dato, "proveedor"))
                data.put("Combustible", funciones!!.validateJsonIsnullString(dato, "combustible"))
                data.put("Imagen", "")
                data.put("Rubro", funciones!!.validateJsonIsnullString(dato, "rubro"))
                data.put("Marca", funciones!!.validateJsonIsnullString(dato, "marca"))
                data.put("Sublinea", funciones!!.validateJsonIsnullString(dato, "sublinea"))
                data.put("Bonificado", funciones!!.validateJsonIsNullFloat(dato, "bonificado"))
                data.put(
                    "Desc_automatico", funciones!!.validateJsonIsNullFloat(
                        dato,
                        "desc_automatico"
                    )
                )
                data.put("Id_sublinea", funciones!!.validateJsonIsNullInt(dato, "id_sublinea"))
                data.put("Id_rubro", funciones!!.validateJsonIsNullInt(dato, "id_rubro"))
                data.put("Existencia_u", funciones!!.validateJsonIsNullFloat(dato, "existencia_u"))
                bd.insert("inventario", null, data)
                //contador=contador+talla
                //var mensaje=contador+50.toFloat()
                //messageAsync("Cargando ${mensaje.toInt()}%")
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    } //guarda los datos de inventario

    private fun saveInventarioPreciosDatabase(json: JSONArray) {
//        var total=json.length()
//        var talla=(30.toFloat()/total.toFloat()).toFloat()
//        var contador:Float=0.toFloat()
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //inicio la transaccion
//            bd!!.execSQL("DELETE FROM inventario_precios") //limpiamos los registros viejos par obtener los nuevos
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put(
                    "id_inventario",
                    funciones!!.validateJsonIsnullString(dato, "id_inventario")
                )
                data.put(
                    "Codigo_producto",
                    funciones!!.validateJsonIsnullString(dato, "codigo_producto")
                )
                data.put(
                    "Id_inventario_unidad",
                    funciones!!.validateJsonIsNullInt(dato, "id_inventario_unidad")
                )
                data.put("Unidad", funciones!!.validateJsonIsnullString(dato, "unidad"))
                data.put("Nombre", funciones!!.validateJsonIsnullString(dato, "nombre"))
                data.put("Terminos", funciones!!.validateJsonIsnullString(dato, "terminos"))
                data.put("Plazo", funciones!!.validateJsonIsNullFloat(dato, "Plazo"))
                data.put("cantidad", funciones!!.validateJsonIsNullFloat(dato, "cantidad"))
                data.put("porcentaje", funciones!!.validateJsonIsNullFloat(dato, "porcentaje"))
                data.put("precio", funciones!!.validateJsonIsNullFloat(dato, "precio"))
                data.put("precio_iva", funciones!!.validateJsonIsNullFloat(dato, "precio_iva"))

                bd.insert("inventario_precios", null, data)
//                contador=contador+talla
//                var mensaje=contador+70.toFloat()
//                messageAsync("Cargando ${mensaje.toInt()}%")
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    } //guarda los datos de inventario precios

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
                    funciones!!.validateJsonIsnullString(dato, "codigo_cliente")
                )
                valor.put("Documento", funciones!!.validateJsonIsnullString(dato, "documento"))
                valor.put("Fecha", funciones!!.validateJsonIsnullString(dato, "fecha"))
                valor.put("Valor", funciones!!.validateJsonIsNullFloat(dato, "valor"))
                valor.put(
                    "Abono_inicial",
                    funciones!!.validateJsonIsNullFloat(dato, "abono_inicial")
                )
                valor.put(
                    "Saldo_inicial",
                    funciones!!.validateJsonIsNullFloat(dato, "saldo_inicial")
                )
                valor.put("Plazo", funciones!!.validateJsonIsNullFloat(dato, "plazo"))
                valor.put(
                    "Fecha_vencimiento",
                    funciones!!.validateJsonIsnullString(dato, "fecha_vencimiento")
                )
                valor.put("Saldo_actual", funciones!!.validateJsonIsNullFloat(dato, "saldo_actual"))
                valor.put(
                    "Fecha_ult_pago",
                    funciones!!.validateJsonIsnullString(dato, "fecha_ult_pago")
                )
                valor.put("Valor_pago", funciones!!.validateJsonIsNullFloat(dato, "valor_pago"))
                valor.put("Relacionado", funciones!!.validateJsonIsnullString(dato, "relacionado"))
                valor.put("Status", funciones!!.validateJsonIsnullString(dato, "status"))
                valor.put(
                    "Fecha_cancelado",
                    funciones!!.validateJsonIsnullString(dato, "fecha_cancelado")
                )
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

    //FUNCION PARA ELIMINAR PEDIDOS ANTIGUOS
    private fun deletePedido() {
        val fechanow = getDateTime()
        val bd = database!!.writableDatabase
        try {
            bd.beginTransaction()
            val cursor = bd.rawQuery("SELECT * FROM pedidos where Enviado=1 AND Fecha_creado != '$fechanow'", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val id = cursor.getInt(0)
                    bd.delete("detalle_pedidos", "Id_pedido=?", arrayOf(id.toString()))
                    bd.delete("pedidos", "Id=?", arrayOf(id.toString()))

                } while (cursor.moveToNext())
                cursor.close()
                bd.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
            bd.close()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
      //  super.onBackPressed()

    //   finish()
    }//anula el boton atras

    override fun onDestroy() {
        super.onDestroy()
       // GlobalScope.cancel()
    }

}