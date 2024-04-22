package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.PrecioPersonalizado

class PreciosAutorizadosController {

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var funciones = Funciones()


    //OBTENIEDO LOS PRECIOS AUTORIZADOS POR FECHA
    fun obtenerPrecioAutorizadoPorFecha(context: Context): ArrayList<PrecioPersonalizado>{
        val data = funciones.getDataBase(context).readableDatabase
        val fechanow = funciones.obtenerFecha()
        val list = ArrayList<PrecioPersonalizado>()

        try {
            val cursor = data.rawQuery("SELECT T.Id, T.cod_producto, I.Descripcion, E.nombre_empleado, T.precio_asig FROM preciosAutorizados T " +
                    "INNER JOIN inventario I ON I.Codigo = T.cod_producto " +
                    "INNER JOIN empleado E ON E.id_empleado = T.Id_vendedor " +
                    "WHERE fecha_registrado='$fechanow'", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val dataToken = PrecioPersonalizado(
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


}