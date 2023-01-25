package com.example.acae30.modelos

data class InventarioPrecios (
    var Id:Int?,
    var Id_inventario:Int?,
    var Codigo_producto:String?,
    var Nombre:String?,
    var Terminos:String?,
    var Plazo:Float?,
    var Unidad:String?,
    var Cantidad:Float?,
    var Porcentaje:Float?,
    var Precio:Float?,
    var Precio_iva:Float?,
    var Id_inventario_unidad:Int?
)