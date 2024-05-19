package com.example.acae30.controllers

import android.content.Context
import android.view.View
import com.example.acae30.Funciones
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos

class PedidosController {

    var funciones = Funciones()
    var clienteController = ClientesController()

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

    //FUNCION PARA ACTUALIZAR EL PAGO Y EL CAMBIO DEL CLIENTE
    fun actualizarPagoCambioPedido(context: Context, idpedido: Int, pago:Float, cambio:Float){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("UPDATE pedidos SET pago=$pago, cambio=$cambio WHERE id=$idpedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }finally {
            bd.close()
        }
    }

    //FUNCION PARA ACTUALIZAR LOS TERMINOS DE ENVIO DEL PEDIDO
    fun actualizarTerminosEnvio(terminos:String, idpedido: Int, context: Context){
        val data = funciones.getDataBase(context).writableDatabase
        try {
            data.execSQL("UPDATE pedidos SET Terminos='$terminos' WHERE id=$idpedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }finally {
            data.close()
        }
    }

    //FUNCION PARA ACTUALIZAR LOS TOTAL SEGUN TIPO DE DOCUMENTO
    fun actualizarTotalesFiscales(context: Context, IdPedido: Int, sumas:Float, iva:Float, iva_perci:Float){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("UPDATE pedidos SET Sumas=$sumas, Iva=$iva, Iva_percibido=$iva_perci" +
                    " WHERE Id=$IdPedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }finally {
            bd.close()
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
                    cursor.getFloat(11),
                    cursor.getFloat(5),
                    cursor.getInt(12),
                    cursor.getString(13),
                    cursor.getInt(14),
                    cursor.getString(15),
                    cursor.getInt(16),
                    cursor.getInt(17),
                    cursor.getString(18)
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
            val cdetalle = base.rawQuery("SELECT *  FROM detalle_producto where Id_pedido=$idPedido", null)
            if (cdetalle.count > 0) {
                cdetalle.moveToFirst()
                do {
                    val detalle = DetallePedido(
                        cdetalle.getInt(0),
                        cdetalle.getInt(1),
                        cdetalle.getInt(2),
                        cdetalle.getString(3),
                        cdetalle.getString(4),
                        cdetalle.getFloat(5),
                        cdetalle.getFloat(6),
                        cdetalle.getFloat(7),
                        cdetalle.getFloat(8),
                        cdetalle.getFloat(9),
                        cdetalle.getFloat(10),
                        cdetalle.getFloat(11),
                        cdetalle.getFloat(12),
                        cdetalle.getFloat(13),
                        cdetalle.getFloat(14),
                        cdetalle.getFloat(15),
                        cdetalle.getString(16),
                        cdetalle.getInt(17),
                        cdetalle.getFloat(18),
                        cdetalle.getString(19),
                        cdetalle.getInt(20)
                    )
                    lista.add(detalle)
                } while (cdetalle.moveToNext())
                cdetalle.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DEL PEDIDO -> ${e.message}")
        }finally {
            base.close()
        }
        return lista
    }

    //FUNCION PARA ELIMINAR PEDIDOS ANTIGUOS
    fun eliminarPedidosAntiguos(context: Context) {
        val fechanow = funciones.obtenerFecha()
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.beginTransaction()
            val cursor = bd.rawQuery("SELECT * FROM pedidos where Enviado=1 AND Fecha_creado != '$fechanow'", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val id = cursor.getInt(0)
                    bd.delete("detalle_pedidos", "Id_pedido=?", arrayOf(id.toString()))
                    bd.delete("pedidos", "Id=?", arrayOf(id.toString()))

                } while (cursor.moveToNext())
                cursor.close()
                bd.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
            bd.close()
        }

    }

}