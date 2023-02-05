package com.example.acae30.modelos

data class Config(
    var vistaInventario:Int?
){
    override fun toString(): String {
        return vistaInventario.toString()
    }
}
