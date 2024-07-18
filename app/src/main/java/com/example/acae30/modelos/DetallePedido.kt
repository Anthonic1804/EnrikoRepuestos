package com.example.acae30.modelos

data class DetallePedido(
    var Id: Int,
    var Id_pedido: Int?,
    var Id_producto: Int?,
    var Codigo: String?,
    var Descripcion: String?,
    var Costo: Float?,
    var Costo_iva: Float?,
    var Precio: Float?,
    var Precio_iva: Float?,
    var Precio_u: Float?,
    var Precio_u_iva: Float?,
    var Cantidad: Float?,
    var Precio_venta_siva: Float?,
    var Precio_venta: Float?,
    var Total: Float?,
    var Total_iva: Float?,
    var Unidad: String?,
    var Bonificado: Int?,
    var Descuento: Float?,
    var Precio_editado: String?,
    var Idunidad: Int?,
    var Codigo_de_barra: String?
)
