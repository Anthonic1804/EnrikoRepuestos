package com.example.acae30.modelos

import java.util.*

data class Pedidos(

    var Id: Int,
    var Id_cliente: Int?,
    var Nombre_cliente: String?,
    var Total: Float?,
    var Descuento: Float?,
    var Enviado: Boolean,
    var Fecha_enviado: String?,
    var Id_pedido_sistema: Int?,
    var Gps: String?,
    var Cerrado: Int?,
    var Idvisita: Int?,
    var Fecha_creado: String?

)