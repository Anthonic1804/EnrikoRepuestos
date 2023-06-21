package com.example.acae30.modelos.JSONmodels

import com.example.acae30.modelos.DetallePedido


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
    var detalle:ArrayList<DetallePedido>?

)