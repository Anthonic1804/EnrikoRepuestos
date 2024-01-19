package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.InventarioPrecios

class InventarioController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA OBTENER INFORMACION DEL PRODUCTO POR ID
    fun obtenerInformacionProductoPorId(context: Context ,idInventario: Int): Inventario?{
        val base = funciones.getDataBase(context).readableDatabase
        var datos: Inventario? = null
        try {
            val cursor = base.rawQuery("SELECT * FROM inventario WHERE Id=$idInventario", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                datos = Inventario(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getFloat(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getFloat(10),
                    cursor.getFloat(11),
                    cursor.getFloat(12),
                    cursor.getFloat(13),
                    cursor.getFloat(14),
                    cursor.getFloat(15),
                    cursor.getFloat(16),
                    cursor.getString(17),
                    cursor.getString(18),
                    cursor.getInt(19),
                    cursor.getString(20),
                    cursor.getInt(21),
                    cursor.getString(22),
                    cursor.getString(23),
                    cursor.getString(24),
                    cursor.getString(25),
                    cursor.getString(26),
                    cursor.getString(27),
                    cursor.getInt(28),
                    cursor.getString(29),
                    cursor.getFloat(30),
                    cursor.getDouble(31),
                    cursor.getInt(32),
                    cursor.getFloat(33)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR: DETALLE DEL PRODUCTO -> ${e.message}")
        }
        finally {
            base.close()
        }
        return datos
    }

    //FUNCION PARA OBTENER LAS ESCALAS DE PRECIO POR PRODUCTO
    fun obtenerEscalaPrecios(context: Context, idInventario: Int): ArrayList<InventarioPrecios>{
        val base = funciones.getDataBase(context).readableDatabase
        val listaEscalas = ArrayList<InventarioPrecios>()

        try {
            val cursor = base.rawQuery("SELECT * FROM Inventario_precios WHERE id_inventario = '$idInventario'", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val escalas = InventarioPrecios(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getFloat(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10),
                        cursor.getInt(11)
                    )
                    listaEscalas.add(escalas)
                } while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR: OBTENER ESCALAS DE PRECIOS -> ${e.message}")
        }
        finally {
            base.close()
        }
        return listaEscalas
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO O POR NOMBRE
    fun obtenerInformacionProductoPorString(context: Context, busqueda: String): ArrayList<Inventario>{
        val base = funciones.getDataBase(context).readableDatabase
        val lista = ArrayList<Inventario>()
        var query: String = ""

        query = if(busqueda != ""){
            "SELECT * FROM inventario WHERE Id IN (SELECT docid FROM virtualinventario WHERE virtualinventario MATCH '$busqueda') LIMIT 60"
        }else{
            "SELECT * FROM inventario limit 60"
        }

        try {
            val cursor = base.rawQuery(query, null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val arreglo = Inventario(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getFloat(15),
                        cursor.getFloat(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getInt(19),
                        cursor.getString(20),
                        cursor.getInt(21),
                        cursor.getString(22),
                        cursor.getString(23),
                        cursor.getString(24),
                        cursor.getString(25),
                        cursor.getString(26),
                        cursor.getString(27),
                        cursor.getInt(28),
                        cursor.getString(29),
                        cursor.getFloat(30),
                        cursor.getDouble(31),
                        cursor.getInt(32),
                        cursor.getFloat(33)
                    )
                    lista.add(arreglo)
                } while (cursor.moveToNext())
                cursor.close()
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL REALIZAR LA BUSQUEDA EN INVENTARIO -> ${e.message}")
        }finally {
            base.close()
        }
        return lista
    }

    //FUNCION PARA OBTENER LA FECHA DEL INVENTARIO
    fun obtenerFechaInventario(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        var fechaInventario: String = ""

        val base = funciones.getDataBase(context).readableDatabase

        try {
            val consulta = base.rawQuery("SELECT Fecha_inventario FROM inventario LIMIT 1", null)

            fechaInventario = if(consulta.count > 0){
                consulta.moveToFirst()
                consulta.getString(0)
            }else ({
                fechaInventario = ""
            }).toString()

            val editor = preferences.edit()
            editor.putString("fechaInventario", fechaInventario)
            editor.apply()

            consulta.close()
        }catch (e:Exception){
            print("ERROR: ${e.message}")
        }finally {
            base.close()
        }
    }


}