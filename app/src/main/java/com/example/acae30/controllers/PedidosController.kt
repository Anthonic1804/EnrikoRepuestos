package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import com.example.acae30.Funciones
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import com.example.acae30.modelos.JSONmodels.PedidoDTE
import com.example.acae30.modelos.Pedidos
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class PedidosController {

    var funciones = Funciones()
    var clienteController = ClientesController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


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
    fun actualizarPagoCambioPedido(context: Context, idpedido: Int, pago:Float, cambio:Float, pagoEfectivo:Float,
                                   pagoCheque:Float,pagoTarjeta:Float,pagoDeposito:Float, numeroOrden:String,
                                   bancoCheque:String,numCuentaCheque:String,numCheque:String, bancoTarjeta:String,
                                   nombreTarjeta:String,numTarjeta:String,bancoDeposito:String,numCuentaDeposito:String,
                                   numDeposito:String, formaPago:String){
        val bd = funciones.getDataBase(context).writableDatabase
        var orden = ""
        orden = if(numeroOrden==""){
            "0"
        }else{
            numeroOrden
        }

        try {
            bd.execSQL("UPDATE pedidos SET pago=$pago, " +
                    "cambio=$cambio," +
                    "pagoEfectivo=$pagoEfectivo," +
                    "pagoCheque=$pagoCheque," +
                    "pagoTarjeta=$pagoTarjeta," +
                    "pagoDeposito=$pagoDeposito," +
                    "bancoCheque='$bancoCheque'," +
                    "numCuentaCheque='$numCuentaCheque'," +
                    "numCheque='$numCheque'," +
                    "bancoTarjeta='$bancoTarjeta'," +
                    "nombreTarjeta='$nombreTarjeta'," +
                    "numTarjeta='$numTarjeta'," +
                    "bancoDeposito='$bancoDeposito'," +
                    "numCuentaDeposito='$numCuentaDeposito'," +
                    "numDeposito='$numDeposito'," +
                    "numero_orden='$orden'," +
                    "formaPago='$formaPago' WHERE id=$idpedido")
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

    //FUNCION PARA OBTENER PEDIDOS NO TRANSMITIDOS EN SQLITE
    fun obtenerPedidosNoTransmitidos(context: Context) :Pedidos?{
        val db = funciones.getDataBase(context).readableDatabase
        var pedido : Pedidos? = null
        try {
            val cursor = db.rawQuery(
                "SELECT Id," +
                        " Id_cliente," +
                        " Nombre_cliente," +
                        " Total," +
                        " Descuento," +
                        " Enviado," +
                        " Fecha_enviado," +
                        " Id_pedido_sistema," +
                        " Gps," +
                        " Cerrado," +
                        " Idvisita," +
                        " strftime('%d/%m/%Y %H:%M'," +
                        " fecha_creado) as fecha_creado," +
                        "Sumas," +
                        "Iva," +
                        "Iva_percibido, " +
                        "pedido_dte, " +
                        "pedido_dte_error FROM pedidos WHERE Enviado=1 AND pedido_dte=0" +
                        " order by id desc limit 1",
                null
            )
            if(cursor.count > 0){
                cursor.moveToFirst()
                pedido = Pedidos(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getFloat(3),
                    cursor.getFloat(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getString(11),
                    cursor.getFloat(12),
                    cursor.getFloat(13),
                    cursor.getFloat(14),
                    cursor.getInt(15),
                    cursor.getInt(16),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                )
            }

            cursor.close()
            return pedido
        }catch (e:Exception){
            throw Exception("ERROR NO SE ENCONTRARON PEDIDOS -> " + e.message)
        }finally {
            db.close()
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
                    cursor.getString(18),
                    cursor.getFloat(6),
                    cursor.getFloat(7),
                    cursor.getFloat(10),
                    cursor.getInt(40),
                    cursor.getInt(41),
                    cursor.getString(42),
                    cursor.getString(43),
                    cursor.getString(44),
                    cursor.getString(45),
                    cursor.getString(22),
                    cursor.getString(24),
                    cursor.getString(21),
                    cursor.getString(49)
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
                        cdetalle.getInt(20),
                        cdetalle.getString(21)
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

    //FUNCION PARA ACTUALIZAR EL ESTADO DE TRANSMISION
    fun actualizarEstadoTransmisionPedido(context: Context, idPedidoServidor:Int, pedido_dte : Int, pedido_dte_error:Int,
                                          dteAmbiente:String, dteCodigoGeneracion:String, dteSelloRecibido:String, dteNumeroControl:String,
                                          idDocTransmitido: Int){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("UPDATE pedidos SET pedido_dte=$pedido_dte, pedido_dte_error=$pedido_dte_error, " +
                    "dteAmbiente='$dteAmbiente', dteCodigoGeneracion='$dteCodigoGeneracion', dteSelloRecibido='$dteSelloRecibido'," +
                    "dteNumeroControl='$dteNumeroControl', idDocTransmitido=$idDocTransmitido " +
                    "WHERE Id_pedido_sistema = $idPedidoServidor")
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR EL PEDIDO")
        }finally {
            bd.close()
        }
    }


    //ACTUALIZAR TOTALES SEGUN DOCUMENTO
    fun actualizarTotalesPedido(context: Context, idPedido: Int, precioConIVASeleccionado:Boolean){
        val bd = funciones.getDataBase(context).writableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val precioConIvaShared = preferences.getBoolean("precioConIva", false)
        try {

            val cursor = bd.rawQuery("SELECT * FROM detalle_pedidos where id_pedido=$idPedido LIMIT 1", null)
            if (cursor.count > 0) {
                if(precioConIvaShared != precioConIVASeleccionado){
                    if(!precioConIVASeleccionado){
                        //actualizar quitando iva
                        bd.execSQL("UPDATE detalle_pedidos SET precio=(precio/1.13), precio_iva=(precio_iva/1.13), " +
                                "total=cantidad*(precio/1.13), total_iva=cantidad*(precio_iva/1.13) WHERE Id_pedido=$idPedido")

                        actualizarTotalPedido(context, idPedido)
                        println("SE QUITO IVA")
                    }else{
                        //actualiar agregando iva
                        bd.execSQL("UPDATE detalle_pedidos SET precio=(precio*1.13), precio_iva=(precio_iva*1.13), " +
                                "total=cantidad*(precio*1.13), total_iva=cantidad*(precio_iva*1.13) WHERE Id_pedido=$idPedido")

                        actualizarTotalPedido(context, idPedido)
                        println("SE AGREGO IVA")
                    }
                    val editor = preferences.edit()
                    editor.remove("precioConIva")
                    editor.putBoolean("precioConIva",precioConIVASeleccionado)
                    editor.apply()
                }
            }
            cursor.close()
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR LOS PRECIOS CON IVA O SIN IVA -> " + e.message)
        }finally {
            bd.close()
        }
    }

    //ACTUALIZAR TOTAL DEL PEDIDO
    private fun actualizarTotalPedido(context: Context, idPedido: Int){
        val bd = funciones.getDataBase(context).writableDatabase
        try{
            val cursor = bd.rawQuery(
                "SELECT SUM(Total_iva)  FROM detalle_pedidos where Id_pedido=$idPedido",
                null
            )

            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                bd.update("pedidos", t, "Id=?", arrayOf(idPedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR TOTAL EN PEDIDO -> " + e.message)
        }finally {
            bd.close()
        }
    }

}