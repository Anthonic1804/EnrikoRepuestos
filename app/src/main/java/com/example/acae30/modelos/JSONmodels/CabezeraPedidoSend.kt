package com.example.acae30.modelos.JSONmodels

import com.example.acae30.modelos.DetallePedido
import java.util.Date


data class CabezeraPedidoSend (
    var Idcliente:Int?,
    var Cliente:String?,
    var Subtotal:Float?,
    var Descuento:Float?,
    var Total:Float?,
    var Enviado:Boolean?,
    var Cerrado:Boolean?,
    var IdSucursal: Int?,
    var CodigoSucursal: String?,
    var NombreSucursal: String?,
    var TipoEnvio: Int?,
    var TipoDocumento:String?,
    var Idvendedor:Int?,
    var Vendedor:String?,
    var fechaCreado: String?, /*AGREGADO PARA ENVIAR LA FECHA Y HORA DE CREACION DEL PEDIDO*/
    var Terminos: String?, //AGREGANDO LOS TERMINOS DEL PEDIDO
    var detalle:ArrayList<DetallePedido>?

)