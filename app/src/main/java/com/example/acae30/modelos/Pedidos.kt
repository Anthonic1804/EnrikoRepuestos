package com.example.acae30.modelos

data class Pedidos(

    var Id: Int,
    var Id_cliente: Int?,
    var Nombre_cliente: String?,
    var Total: Float?,
    var Descuento: Float?,
    var Enviado: Int,
    var Fecha_enviado: String?,
    var Id_pedido_sistema: Int?,
    var Gps: String?,
    var Cerrado: Int?,
    var Idvisita: Int?,
    var Fecha_creado: String?,
    var Suma: Float?,
    var Iva: Float?,
    var Iva_Percibido: Float?,
    var pedido_dte: Int?,
    var pedido_dte_error: Int?,
    var dteAmbiente:String?,
    var dteCodigoGeneracion: String?,
    var dteSelloRecibido: String?,
    var dteNumeroControl: String?,
    var Tipo_documento: String?,
    var Terminos: String?
)