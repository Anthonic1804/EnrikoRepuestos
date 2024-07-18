package com.example.acae30.modelos

data class InformacionSucursal(

    val id : Int,
    val idCliente : Int,
    val codigoSucursal : String,
    val nombreSucursal : String,
    val dteDireccion : String,
    val dteMunicipio : String,
    val dteDepto : String,
    val dteTelefono : String,
    val dteCorreo : String,
    val idRuta : Int,
    val ruta : String,
    val dteCodDepto : String,
    val dteCodMunicipio : String,
    val dteCodPais : String,
    val dtePais : String

)