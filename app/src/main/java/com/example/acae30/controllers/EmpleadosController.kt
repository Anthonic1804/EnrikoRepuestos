package com.example.acae30.controllers

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class EmpleadosController {

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    private val funciones = Funciones()

    //OBTENIENDO LOS EMPLEADOS DEL SERVIDOR
    suspend fun obtenerEmpleados(context: Context, view: View) {
        val alert = AlertDialogo(context as Activity)
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        //IMPORTANDO DATOS DE TABLA EMPLEADOS
        try {
            val direccion = url + "empleados"
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        funciones.messageAsync("Cargando 10%")
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                                if (talla <= 45) {
                                    funciones.messageAsync("Cargando $talla%")
                                }
                            }
                            funciones.messageAsync("Cargando 45%")
                            data.close()
                            funciones.messageAsync("Cargando 50%")
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveEmpleadosDatabase(respuesta, context) //guarda los datos en la bd
                                funciones.messageAsync("Cargando 100%")
                                delay(1000)
                                funciones.messageAsync("Datos del Vendedor Almacenados Exitosamente")
                                delay(1500)
                                alert.dismisss()
                            } else {
                                funciones.messageAsync("Cargando 100%")
                                delay(1000)
                                funciones.messageAsync("Datos del Vendedor Almacenados Exitosamente")
                                delay(1500)
                                alert.dismisss()
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        funciones.mostrarAlerta("SERVIDOR: NO SE ENCONTRARON VENDEDORES REGISTRADOS", context, view)
                    }
                } catch (e: Exception) {
                    funciones.mostrarAlerta("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR", context, view)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alert.dismisss()
            funciones.mostrarAlerta("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR", context, view)
        }
    }

    //ALMACENANDO LOS EMPLEADOS EN LA BD SQLITE
    private fun saveEmpleadosDatabase(json: JSONArray, context: Context) {
        val bd = funciones.getDataBase(context).writableDatabase
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
                valor.put("nombre_empleado", funciones.validateJsonIsnullString(dato, "empleado"))
                bd.insert("empleado", null, valor)
                contador += talla
                val mensaje = contador + 50.toFloat()
                funciones.messageAsync("Cargando ${mensaje.toInt()}%")
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

}