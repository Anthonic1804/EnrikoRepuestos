package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.JSONmodels.VisitaJSON
import com.example.acae30.modelos.Visitas
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class VisitaController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA INICIAR UNA VISITAR DONDE EL CLIENTE
    fun registrarVisita(
        Id_app_visita: Int,
        Fecha_hora_checkin: String,
        Latitud_checkin: String,
        Longitud_checkin: String,
        Id_cliente: Int,
        Cliente: String,
        Id_vendedor: Int,
        Fecha_hora_checkout: String,
        Latitud_checkout: String,
        Longitud_checkout: String,
        Comentarios: String,
        idVisitaGloval: Int,
        context: Context
    ) : Int{

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val server = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        var idvisitaApi = 0

        try {
            val datos = VisitaJSON(
                Id_app_visita,
                Fecha_hora_checkin,
                Latitud_checkin,
                Longitud_checkin,
                Id_cliente,
                Cliente,
                Id_vendedor,
                Fecha_hora_checkout,
                Latitud_checkout,
                Longitud_checkout,
                Comentarios
            )

            val objecto = Gson().toJson(datos)
            val ruta: String = server + "visitas/iniciar_visita"
            val url = URL(ruta)

            with(url.openConnection() as HttpURLConnection){
                try {
                    connectTimeout = 2000
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

                                val res: JSONObject = JSONObject(respuesta.toString())
                                if (res.length() > 0) {
                                    if (!res.isNull("error") && !res.isNull("response")) {
                                        val idser = res.getInt("error")
                                        idvisitaApi = idser
                                        updateCheckIn(idser, idVisitaGloval, context)
                                    } else {
                                        //throw Exception("Error en la respuesta del servidor")
                                        println("ERROR NO HAY RESPUESTA DEL SERVIDOR 1")
                                    }
                                } else {
                                    //throw Exception("Error al recibir respuesta del servidor")
                                    println("ERROR NO HAY RESPUESTA DEL SERVIDOR 2")
                                }

                            }catch (e:Exception){
                                //throw Exception("ERROR DE RESPUESTA -> " + e.message)
                                println("ERROR DE RESPUESTA -> ${e.message}")
                            }
                        }

                    }else {
                        //throw Exception("ERROR $responseCode : -> PARAMETROS INCORRECTOS")
                        println("ERROR $responseCode : -> PARAMETROS INCORRECTOS")
                    }

                }catch (e:Exception){
                    //throw Exception("ERROR DE COMUNICACION CON EL SERVER -> " + e.message)
                    println("ERROR DE COMUNICACION CON EL SERVER -> " + e.message)
                }
            }

        }catch (e:Exception){
            //throw Exception("ERROR AL REGISTRAR LA VISITA -> " + e.message)
            println("ERROR AL REGISTRAR LA VISITA -> " + e.message)
        }

        return idvisitaApi
    }

    //FUNCION PARA ACTUALIZAR EL CKECKIN EN SQLITE
    private fun updateCheckIn(idvisitaServer: Int, idvisita: Int, context: Context) {

        val base = funciones.getDataBase(context).writableDatabase
        try {
            val data = ContentValues()
            data.put("Idvisita", idvisitaServer)
            data.put("Enviado", true)
            base.update("visitas", data, "Id=?", arrayOf(idvisita.toString()))

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    }

    //FUNCION PARA OBTENER LA VISITA POR ID DEL CLIENTE
    fun obtenerVisita(idcliente: Int, context: Context): Visitas? {
        val bd = funciones.getDataBase(context)
        val base = bd.readableDatabase
        try {
            var visita: Visitas? = null
            val cursor = base.rawQuery("SELECT * FROM visitas where Id_cliente=$idcliente", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                visita = Visitas(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11) == 1,
                    cursor.getInt(12) == 1,
                    cursor.getInt(13) == 1
                )
                cursor.close()
            }
            return visita
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }
    }


}