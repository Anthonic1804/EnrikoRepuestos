package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import com.example.acae30.Detallepedido
import com.example.acae30.Funciones
import com.example.acae30.Visita
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.JSONmodels.ActualizarPagareFirmadoCliente
import com.example.acae30.modelos.JSONmodels.TokenDataClassJSON
import com.google.gson.Gson
import org.json.JSONObject
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


}