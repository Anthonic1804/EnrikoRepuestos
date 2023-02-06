package com.example.acae30.modelos

data class Sucursales(
    var codigoSucursal:String,
    var nombreSucursal:String
    ){
    override fun toString(): String {
        return nombreSucursal; codigoSucursal
    }
}