package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones

class InventarioController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"






    //FUNCION PARA OBTENER LA FECHA DEL INVENTARIO
    fun obtenerFechaInventario(context: Context){
        var fechaInventario: String = ""
        val base = funciones.getDataBase(context).readableDatabase

        try {
            val consulta = base.rawQuery("SELECT TOP(1) Fecha_inventario FROM inventario", null)

            fechaInventario = if(consulta.count > 0){
                consulta.moveToFirst()
                consulta.getString(0)
            }else ({
                fechaInventario = ""
            }).toString()

            consulta.close()
        }catch (e:Exception){
            print("ERROR: ${e.message}")
        }finally {
            base.close()
        }
    }


}