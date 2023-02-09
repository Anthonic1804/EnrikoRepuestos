package com.example.acae30.modelos

data class Sucursales(
    var idSucursa:String,
    var codigoSucursal:String,
    var nombreSucursal:String
    ){
    override fun toString(): String {
        return idSucursa; nombreSucursal; codigoSucursal
    }
}