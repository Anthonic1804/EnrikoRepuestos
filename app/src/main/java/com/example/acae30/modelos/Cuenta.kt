package com.example.acae30.modelos

data class Cuenta (
    var Id:Int?,
    var Id_cliente:Int?,
    var Codigo_cliente:String?,
    var Documento:String?,
    var Fecha:String?,
    var Valor:Float?,
    var Abono_inicial:Float?,
    var Saldo_inicial:Float?,
    var Plazo:Float?,
    var Fecha_vencimiento:String?,
    var Saldo_actual:Float?,
    var Fecha_ult_pago:String?,
    var Valor_pago:Float?,
    var Relacionado:String?,
    var Status:String?,
    var Fecha_cancelado:String?
)