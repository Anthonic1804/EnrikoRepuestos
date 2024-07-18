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


    //VARIABLES PARA ALMACENAR LOS VALORES
    var pagoEfectivo : Float?,
    var pagoCheque : Float?,
    var pagoTarjeta : Float?,
    var pagoDeposito : Float?,
    var numeroOrden : String?,
    var bancoCheque : String?,
    var numCuentaCheque : String?,
    var numCheque : String?,
    var bancoTarjeta : String?,
    var nombreTarjeta: String?,
    var numTarjeta : String?,
    var bancoDeposito : String?,
    var numCuentaDeposito : String?,
    var numDeposito : String?,
    var formaPago: String?,

    //DATOS DTE Y RUTA
    var idRuta: Int?,
    var ruta : String?,
    var dteDireccion : String?,
    var dteCodDepto : String?,
    var dteCodMunicipio : String?,
    var dteCodPais : String?,
    var dtePais : String?,
    var dteCorreo : String?,
    var dteTelefono : String?,

    var detalle:ArrayList<DetallePedido>?
)