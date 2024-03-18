package com.example.acae30.controllers

import android.content.Context
import android.view.View
import com.example.acae30.Funciones
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos

class PedidosController {

    var funciones = Funciones()

    //FUNCION PARA ACTUALIZAR EL TIPO DE ENVIO SELECCIONADO
    fun updateTipoPedido(tipoPedido:Int, idpedido:Int, context: Context){
        val data = funciones.getDataBase(context).writableDatabase
        try {
            data!!.execSQL("UPDATE pedidos set tipo_envio=$tipoPedido WHERE id=$idpedido")
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
    }

    //FUNCION PARA ACTUALIZAR EL TIPO DE DOCUMENTO SELECCIONADO
    fun updateTipoDocumento(tipoDocumento:String, idpedido: Int, context: Context){
        val data = funciones.getDataBase(context).writableDatabase
        try {
            data.execSQL("UPDATE pedidos SET tipo_documento='$tipoDocumento' WHERE id=$idpedido")
        }catch (e: Exception){
            throw Exception(e.message)
        }finally {
            data.close()
        }
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO DEL PEDIDO AL GUARDARLO
    fun actualizarEstadoAlGuardar(idpedido: Int, context: Context, view: View){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.execSQL("UPDATE pedidos set Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            funciones.mostrarAlerta("ERROR: NO SE ACTUALIZO EL ESTADO DEL PEDIDO AL GUARDARLO", context, view)
        } finally {
            bd!!.close()
        }
    }

    //FUNCION PARA OBTENER INFORMACION DEL PEDIDO
    fun obtenerInformacionPedido(idPedido: Int, context: Context): Pedidos?{
        val base  = funciones.getDataBase(context).readableDatabase
        var infoPedido : Pedidos? = null
        try{
            val cursor = base.rawQuery("SELECT * FROM pedidos WHERE Id=$idPedido", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                infoPedido = Pedidos(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getFloat(3),
                    cursor.getFloat(4),
                    cursor.getInt(5) == 1,
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getString(11)
                )
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LA INFORMACION DEL PEDIDO -> ${e.message}")
        }finally {
            base.close()
        }
        return infoPedido
    }

    //FUNCION PARA OBTENER EL DETALLE DEL PEDIDO
    fun obtenerDetallePedido(idPedido: Int, context: Context) : ArrayList<DetallePedido> {
        val base = funciones.getDataBase(context).readableDatabase
        val lista = ArrayList<DetallePedido>()
        try{
            val cursor = base.rawQuery("SELECT *  FROM detalle_producto where Id_pedido=$idPedido", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val detalle = DetallePedido(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getFloat(5),
                        cursor.getFloat(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getString(15),
                        cursor.getString(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getString(19),
                        cursor.getFloat(20),
                        cursor.getInt(21),
                        cursor.getFloat(22),
                        cursor.getString(23),
                        cursor.getInt(24),
                        cursor.getInt(25)
                    )
                    lista.add(detalle)
                } while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DEL PEDIDO -> ${e.message}")
        }finally {
            base.close()
        }
        return lista
    }

}