package com.example.acae30.modelos

data class Config(
    var vistaInventario:Int?,
    var sinExistencias: Int?,
    var versionApp: String?
){
    override fun toString(): String {
        return vistaInventario.toString(); sinExistencias.toString(); versionApp.toString()
    }
}
