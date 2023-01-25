package com.example.acae30.modelos

data class VistaPedidos(
    var Id: Int,
    var Id_pedido: Int,
    var Id_producto: Int,
    var Codigo: String,
    var Descripcion: String,
    var Costo: Float,
    var costo_iva: Float,
    var Precio: Float,
    var Precio_iva: Float,
    var Precio_u: Float,
    var Precio_u_iva: Float,
    var Cantidad: Float,
    var Precio_venta: Float,
    var Precio_oferta: Float,
    var Subtotal: Float,
    var Unidad: String,
    var Unidad_medida: String,
    var Nombre_fraccion: String,
    var Cesc: String,
    var Combustible: String,
    var Bonificado: Float,
    var Descuento: Float,
    var Precio_editado: String,
    var Idunidad: Int,
    var Id_talla: Int
)