package com.example.acae30.modelos

data class PedidosDetalle (
    var Id:Int,
    var Id_pedido:Int?,
    var Id_producto:Int?,
    var Cantidad:Float?,
    var Unidad:String?,
    var Idunidad:Int?,
    var Precio:Float?,
    var Precio_oferta:Float?,
    var Subtotal:Float?,
    var Bonificado:Int?,
    var Descuento:Float?,
    var Id_talla:Int?
)