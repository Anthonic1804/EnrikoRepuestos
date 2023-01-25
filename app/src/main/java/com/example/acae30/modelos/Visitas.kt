package com.example.acae30.modelos

import java.util.*

data class Visitas(
    var Id:Int,
    var Id_cliente:Int,
    var Nombre_cliente:String,
    var Gps_in:String,
    var Fecha_inicial:String,
    var Gps_out:String,
    var Fecha_final:String,
    var Idvisita:Int,
    var Comentario:String,
    var Imagen_url:String,
    var Imagen:String,
    var Abierta:Boolean,
    var Enviado:Boolean,
    var Enviado_final:Boolean
)