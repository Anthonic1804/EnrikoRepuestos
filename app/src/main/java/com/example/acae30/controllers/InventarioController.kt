package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import com.example.acae30.Funciones
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.JSONmodels.HojaCargaJSON
import com.google.gson.Gson
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

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

    //FUNCION PARA OBTENER EL INVENTARIO DESDE LA HOJA DE CARGA DE ESCARRSA
    fun obtenerInventarioHojaCarga(id: Int,  numero: Int, id_vendedor: Int, context: Context, view:View) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val datos = HojaCargaJSON(
                id,
                numero,
                id_vendedor
            )
            val objecto =
                Gson().toJson(datos)
            println(objecto)
            val ruta: String = url + "inventario/hojacarga"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
                        200 -> {
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        println(res)
                                        saveInventarioDatabase(res, context, view)
                                    } else {
                                        println("ERROR: ERROR NO SE ENCONTRARON DATOS PARA ALMACENAR 222222")
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {
                            println("ERROR: ERROR AL CARGAR EL INVENTARIO POR HOJA DE CARGA")
                        }

                        404 -> {
                            println("ERROR: ERROR NO SE ENCONTRARON DATOS PARA ALMACENAR")
                        }

                        else -> {
                            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR")
                        }
                    }
                } catch (e: Exception) {
                    throw Exception("ERROR: " + e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception("ERROR EN LA CONEXION CON EL SERVIDOR" + e.message)
        }finally {
            obtenerFechaInventario(context)
        }
    }

    //FUNCION PARA ALMACENAR LOS PRECIOS EN LA BASE DE DATOS
    fun saveInventarioPreciosDatabase(json: JSONArray, context: Context) {
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put(
                    "id_inventario",
                    funciones.validateJsonIsnullString(dato, "id_inventario")
                )
                data.put(
                    "Codigo_producto",
                    funciones.validateJsonIsnullString(dato, "codigo_producto")
                )
                data.put(
                    "Id_inventario_unidad",
                    funciones.validateJsonIsNullInt(dato, "id_inventario_unidad")
                )
                data.put("Unidad", funciones.validateJsonIsnullString(dato, "unidad"))
                data.put("Nombre", funciones.validateJsonIsnullString(dato, "nombre"))
                data.put("Terminos", funciones.validateJsonIsnullString(dato, "terminos"))
                data.put("Plazo", funciones.validateJsonIsNullFloat(dato, "Plazo"))
                data.put("cantidad", funciones.validateJsonIsNullFloat(dato, "cantidad"))
                data.put("porcentaje", funciones.validateJsonIsNullFloat(dato, "porcentaje"))
                data.put("precio", funciones.validateJsonIsNullFloat(dato, "precio"))
                data.put("precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))

                bd.insert("inventario_precios", null, data)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA ALMACENAR EL INVENTARIO EN SQLITE
    fun saveInventarioDatabase(json: JSONArray, context: Context, view:View) {
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("DELETE FROM Inventario")
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                data.put("Tipo", funciones.validateJsonIsnullString(dato, "tipo"))
                data.put("Id_linea", funciones.validateJsonIsNullInt(dato, "id_linea"))
                data.put("Linea", funciones.validateJsonIsnullString(dato, "linea"))
                data.put("Descripcion", funciones.validateJsonIsnullString(dato, "descripcion"))
                data.put(
                    "Unidad_medida",
                    funciones.validateJsonIsnullString(dato, "unidad_medida")
                )
                data.put("Fraccion", funciones.validateJsonIsNullFloat(dato, "fraccion"))
                data.put(
                    "Nombre_fraccion", funciones.validateJsonIsnullString(
                        dato,
                        "nombre_fraccion"
                    )
                )
                data.put("Existencia", funciones.validateJsonIsNullFloat(dato, "existencia"))
                data.put("Costo", funciones.validateJsonIsNullFloat(dato, "costo"))
                data.put("costo_iva", funciones.validateJsonIsNullFloat(dato, "costo_iva"))
                data.put(
                    "Precio_oferta",
                    funciones.validateJsonIsNullFloat(dato, "precio_oferta")
                )
                data.put("Precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))
                data.put("Precio_u", funciones.validateJsonIsNullFloat(dato, "precio_u"))
                data.put("Precio_u_iva", funciones.validateJsonIsNullFloat(dato, "precio_u_iva"))
                data.put("Precio", funciones.validateJsonIsNullFloat(dato, "precio"))
                data.put("Status", funciones.validateJsonIsnullString(dato, "status"))
                data.put("Id_productor", funciones.validateJsonIsNullInt(dato, "id_productor"))
                data.put("Productor", funciones.validateJsonIsnullString(dato, "productor"))
                data.put("Id_proveedor", funciones.validateJsonIsNullInt(dato, "id_proveedor"))
                data.put("Proveedor", funciones.validateJsonIsnullString(dato, "proveedor"))
                data.put("Cesc", "N")
                data.put("Combustible", "N")
                data.put("Imagen", "")
                data.put("Rubro", funciones.validateJsonIsnullString(dato, "rubro"))
                data.put("Marca", funciones.validateJsonIsnullString(dato, "marca"))
                data.put("Sublinea", funciones.validateJsonIsnullString(dato, "sublinea"))
                data.put("Bonificado", funciones.validateJsonIsNullFloat(dato, "bonificado"))
                data.put(
                    "Desc_automatico", funciones.validateJsonIsNullFloat(
                        dato,
                        "desc_automatico"
                    )
                )
                data.put("Id_sublinea", funciones.validateJsonIsNullInt(dato, "id_sublinea"))
                data.put("Id_rubro", funciones.validateJsonIsNullInt(dato, "id_rubro"))
                data.put("Existencia_u", funciones.validateJsonIsNullFloat(dato, "existencia_u"))
                bd.insert("inventario", null, data)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()

            funciones.mostrarMensaje("INVENTARIO CARGADO CORRECTAMENTE", context, view)

        }
    }

    //DESCARGA DE INVENTARIO DE HOJA DE CARGA
    fun descargarProductosInventario(idPedido: Int, context: Context){
        val base = funciones.getDataBase(context).writableDatabase
        try {
            val cursor = base.rawQuery("SELECT ID_PRODUCTO, (CANTIDAD + BONIFICADO) AS CANTIDAD FROM DETALLE_PEDIDOS WHERE ID_PEDIDO=$idPedido",null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    try {
                        base.execSQL("UPDATE Inventario SET Existencia = (Existencia - ${cursor.getInt(1)}) WHERE Id=${cursor.getInt(0)}")
                    }catch (e:Exception){
                        println("ERROR: NO SE ACTUALIZARON LAS EXITENCIAS EN INVENTARIO -> ${e.message}")
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR: NO SE ENCONTRARON REGISTROS EN EL PEDIDO -> ${e.message}")
        }finally {
            base.close()
        }
    }
}