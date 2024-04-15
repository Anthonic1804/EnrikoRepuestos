package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ConfigController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA OBTERNER LA INFORMACION DE LA TABLA CONFIG SQLSERVER
    suspend fun obtenerConfigPagareObligatorio(context:Context){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        var confirmar: Boolean
        var modificarPrecio: Boolean
        var sinExistencias: String = ""
        var network: Boolean
        var hojaCarga: Boolean
        try {
            val direccion = url + "config"
            val url2 = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url2.openConnection()
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

                                for (i in 0 until respuesta.length()){
                                    val dato = respuesta.getJSONObject(i)
                                    confirmar = dato.getBoolean("pagare_obligatorio_app")
                                    modificarPrecio = dato.getBoolean("modificar_precio_app")
                                    sinExistencias = dato.getString("pedidos_sin_existencia")
                                    network = dato.getBoolean("networkProvider_app")
                                    hojaCarga = dato.getBoolean("hoja_carga_inventario_app")

                                    confirmarPagareObligatorio(confirmar, modificarPrecio, sinExistencias, network, hojaCarga, context)
                                }
                            } else {
                                println("ERROR AL LEER EL JSON CONFIG")
                            }
                        }
                    } else {
                        println("ERROR DE COMUNICACION CON EL SERVIDOR: $responseCode")
                    }
                } catch (e: Exception) {
                    println("ERROR SEGUNDO TRY CATCH -> ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("ERROR PRIMER TRY CATCH -> ${e.message}")
        }
    }

    //FUNCION PARA SETEAR LA FORMA DEL PAGARE EN SHAREDPREFERENCES
    private fun confirmarPagareObligatorio(confirmar: Boolean, modificar:Boolean, sinExistencia:String, network:Boolean, hojaCarga:Boolean, context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove("PagareObligatorio")
        editor.putBoolean("PagareObligatorio", confirmar)
        editor.putBoolean("modificar_precio_app", modificar)
        editor.putString("pedidos_sin_existencia", sinExistencia)
        editor.putBoolean("NetworkProvider_app", network)
        editor.putBoolean("Hoja_carga_inventario_app", hojaCarga)
        editor.putInt("vistaInventario", 2)
        editor.putFloat("versionActualApp", 1.0f)
        editor.apply()
    }
}