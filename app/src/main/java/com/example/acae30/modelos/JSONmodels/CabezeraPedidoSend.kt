package com.example.acae30.modelos.JSONmodels
import com.example.acae30.modelos.DetallePedido
data class CabezeraPedidoSend (
    var Idcliente:Int?,
    var Cliente:String?,
    var Subtotal:Float?,
    var Descuento:Float?,
    var Total:Float?,
    var Enviado:Int?,
    var Cerrado:Int?,
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