package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import com.example.acae30.Detallepedido
import com.example.acae30.Funciones
import com.example.acae30.Visita
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.JSONmodels.ActualizarPagareFirmadoCliente
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class ClientesControllers {

    private var funciones = Funciones()
    private var visitaController = VisitaController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    /*
    * FUNCIONES PARA OBTENER LA INFORMACION DEL CLIENTE
    * DESDE EL SERVIDOR SQL
    * */

    //FUNCION PARA OBTENER LA INFORMACION DE LOS CLIENTES
    suspend fun obtenerClientesServidor(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())


    }

    //FUNCION PARA OBTENER LOS PRECIOS PERSONALIZADOS
    suspend fun obtenerPreciosPersonalizados(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString())

        /*PRIMER TRY PARA OBTENER LA CANTIDAD DE REGISTROS
        * Y LUEGO CARGARLOS POR BLOQUES
        */
        try {
            val urlCantidadRegistros = url + "clientes/precios/cantidad"
            val urlCantidad = URL(urlCantidadRegistros)
            var cantRegistros = 0

            with(withContext(Dispatchers.IO) {
                urlCantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()

                            cantRegistros = readline.toInt()
                        }

                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception("Error #1 Linea 207:$responseCode")
                }
            }

            //CALCULANDO BLOQUE DE REGISTROS
            val bloque = 500
            var inicio = 0
            var longitud: Int
            var registrosCargados: Int

            if(cantRegistros < bloque){
                longitud = cantRegistros
                registrosCargados = cantRegistros
            }else{
                longitud = bloque
                registrosCargados = bloque
            }


            var porcentaje = 2
            do{

                if (porcentaje <= 99) {
                    funciones.messageAsync("Cargando $porcentaje%")
                }else{
                    funciones.messageAsync("Cargando 100%")
                }

                porcentaje += 13

                val urlPreciosPersonalizados = url + "clientes/precios/" + inicio.toString() + "/" + longitud.toString()
                val urlPrecios = URL(urlPreciosPersonalizados)

                with(withContext(Dispatchers.IO) {
                    urlPrecios.openConnection()
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
                                data.close()
                                val respuesta = JSONArray(response.toString())
                                if (respuesta.length() > 0) {
                                    almacenarPrecioPersonalizados(respuesta, context)
                                    println("PRECIOS PERSONALIZADOS ALMACENADOS CORRECTAMEMENTE")
                                } else {
                                    throw Exception("EL SERVIDOR NO DEVOLVIO DATOS")
                                }
                            }
                        } else {
                            throw Exception("ERROR DE COMUNICACIOIN CON EL SERVIDOR:$responseCode")
                        }
                    } catch (e: Exception) {
                        throw Exception("ERROR DE CONEXION: $responseCode")
                    }
                }

                inicio += bloque
                registrosCargados += longitud

                if (cantRegistros in (inicio + 1) until registrosCargados) {
                    longitud = cantRegistros
                }

            }while(inicio < cantRegistros)


        }catch (e:Exception){
            println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADOS -> ${e.message}")
        }
    }

    //FUNCION PARA LAMACENAR LOS PRECIOS PERSONALIZADOS EN SQLITE
    private fun almacenarPrecioPersonalizados(json: JSONArray, context: Context){
        val base = funciones.getDataBase(context).writableDatabase
        try {
            base.beginTransaction()
            base.execSQL("DELETE FROM cliente_precios")

            for (i in 0 until json.length()){
                val datos = json.getJSONObject(i)
                val valor = ContentValues()

                valor.put("id_cliente", datos.getInt("id_cliente"))
                valor.put("id_inventario", datos.getInt("id_inventario"))
                valor.put("precio_p", funciones.validateJsonIsNullFloat(datos, "precio_p"))
                valor.put("precio_p_iva", funciones.validateJsonIsNullFloat(datos, "precio_p_iva"))

                base.insert("cliente_precios", null, valor)
            }
            base.setTransactionSuccessful()
        }catch (e:Exception){
            println("ERROR AL INSERTAR LOS PRECIOS PERSONALIZADOS -> ${e.message}")
        }finally {
            base.endTransaction()
            base.close()
        }
    }

    /*
    * FIN DE LAS FUNCIONES DE OBTENER INFORMACION DESDE
    * EL SERVIDOR
    * */


    //FUNCION PARA OBTENER LOS DATOS DEL CLIENTE POR ID
    fun obtenerInformacionCliente(context: Context, idCliente: Int): Cliente?{

        val base = funciones.getDataBase(context).readableDatabase
        var datosCliente: Cliente? = null

        try {
            val cursor = base.rawQuery("SELECT * FROM clientes " +
                    "WHERE id=$idCliente", null)

            if (cursor.count > 0) {
                cursor.moveToFirst()
                datosCliente = Cliente(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getFloat(10),
                    cursor.getFloat(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14),
                    cursor.getString(15),
                    cursor.getString(16),
                    cursor.getString(17),
                    cursor.getString(18),
                    cursor.getString(19),
                    cursor.getInt(20),
                    cursor.getInt(21),
                    cursor.getString(22),
                    cursor.getString(23),
                    cursor.getString(24),
                    cursor.getFloat(25),
                    cursor.getInt(27),
                    cursor.getString(28)
                )
                cursor.close()
            }
            cursor.close()
        }catch (e: Exception){
            println("ERROR: NO SE ENCONTRO EL CLIENTE -> ${e.message}")
        }finally {
            base.close()
        }

        return datosCliente
    }

    //FUNCION PARA OBTENER TODOS LOS CLIENTES
    fun obtenerListaClientes(context: Context, busqueda: String): ArrayList<Cliente>{
        val base = funciones.getDataBase(context).readableDatabase
        val listaClientes = ArrayList<Cliente>()
        var consutaSql: String = ""

        consutaSql = if (busqueda != "") {
            "SELECT * FROM Clientes WHERE Cliente LIKE '%$busqueda%'"
        } else {
            "SELECT * FROM Clientes LIMIT 50"
        }

        try {
            val consulta = base.rawQuery(consutaSql, null)

            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {
                    val listado = Cliente(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getString(4),
                        consulta.getString(5),
                        consulta.getString(6),
                        consulta.getString(7),
                        consulta.getString(8),
                        consulta.getInt(9),
                        consulta.getFloat(10),
                        consulta.getFloat(11),
                        consulta.getString(12),
                        consulta.getString(13),
                        consulta.getString(14),
                        consulta.getString(15),
                        consulta.getString(16),
                        consulta.getString(17),
                        consulta.getString(18),
                        consulta.getString(19),
                        consulta.getInt(20),
                        consulta.getInt(21),
                        consulta.getString(22),
                        consulta.getString(23),
                        consulta.getString(24),
                        consulta.getFloat(25),
                        consulta.getInt(27),
                        consulta.getString(28)
                    )
                    listaClientes.add(listado)

                } while (consulta.moveToNext())
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

        return listaClientes
    }

    //FUNCION PARA ACTUALIZAR EL PAGARE EN SERVIDOR SQL SERVER
    fun actualizarPagareFirmadoSqlServer(context: Context, idCliente: Int, vista: View){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val datos = ActualizarPagareFirmadoCliente(
                idCliente
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url + "clientes/actualizarPagare"
            val url2 = URL(ruta)
            with(url2.openConnection() as HttpURLConnection) {
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
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }

                                when (respuesta.toString()) {
                                    "CLIENTE_ACTUALIZADO" -> {
                                        actualizarPagareFirmadoSqLite(context, idCliente)
                                    }
                                    "ERROR_CLIENTE_NO_ENCONTRADO" -> {
                                        funciones.mostrarAlerta("ERROR AL ACTUALIZAR LA TABLA CLIENTES", context, vista)
                                    }
                                }

                            } catch (e: Exception) {
                                println("ERROR 1: ${e.message}")
                            }
                        }
                    }else {
                        println("ERROR AL ACTUALIZAR LA TABLA CLIENTES")
                    }

                } catch (e: Exception) {
                    println("ERROR 2: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("ERROR 3: ${e.message}")
        }
    }

    //FUNCION PARA ACTUALIZAR EL PAGARE DE FORMA LOCAL
    private fun actualizarPagareFirmadoSqLite(context: Context, idCliente: Int){
        val base = funciones.getDataBase(context).writableDatabase
        base.beginTransaction()
        val data = ContentValues()
        data.put("Firmar_pagare_app", 1)

        base.update("clientes", data, "Id=${idCliente}", null)

        base.setTransactionSuccessful()
        base.endTransaction()
        base.close()
    }

    //FUNCION DE REDIRECCION CUDNO SE VERIFICAR SI EL PAGARE ES OBLIGATORIO O NO
    fun verificarPagareObligatorio(context: Context, idCliente: Int, nomCliente:String, codCliente:String, visita:Boolean){
        if (visita) {
            val datos_visitas = visitaController.obtenerVisita(idCliente, context)
            if (datos_visitas != null) {
                if (datos_visitas.Abierta) {
                    val intento = Intent(context, Visita::class.java)
                    intento.putExtra("idcliente", idCliente)
                    intento.putExtra("nombrecliente", nomCliente)
                    intento.putExtra("codigo", codCliente)
                    intento.putExtra("visitaid", datos_visitas.Id)
                    intento.putExtra("idapi", datos_visitas.Idvisita)
                    context.startActivity(intento)
                } else {
                    val intento = Intent(context, Visita::class.java)
                    intento.putExtra("idcliente", idCliente)
                    intento.putExtra("nombrecliente", nomCliente)
                    intento.putExtra("codigo", codCliente)
                    context.startActivity(intento)

                } //valida si la visita esta abierta

            } else {
                val intento = Intent(context, Visita::class.java)
                intento.putExtra("idcliente", idCliente)
                intento.putExtra("nombrecliente", nomCliente)
                intento.putExtra("codigo", codCliente)
                context.startActivity(intento)
            } //valida si existe visita

        } else {
            val intento = Intent(context, Detallepedido::class.java)
            intento.putExtra("id", idCliente)
            intento.putExtra("nombrecliente", nomCliente)
            context.startActivity(intento)
        }
    }

    //FUNCION PARA OBTENER EL PRECIO PERSONALIZADO POR ID CLIENTE E ID PRODUCTO
    fun obtenerPrecioPersoCliente(idCliente: Int, idProducto: Int, context: Context) : Float{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val base = funciones.getDataBase(context).readableDatabase
        var precioIva = 0f
        try {
            val cursor = base.rawQuery("SELECT precio_p_iva from cliente_precios " +
                    "WHERE id_cliente=$idCliente AND id_inventario=$idProducto", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                precioIva = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL BUSCAR EL PRECIO PERSONALIZADO -> ${e.message}")
        }finally {
            base.close()
        }
        return precioIva
    }

}