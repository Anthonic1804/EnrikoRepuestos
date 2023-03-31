package com.example.acae30.modelos

data class Empleados(
    var idEmpleado:String,
    var nombreEmpleado:String,
    ){
    override fun toString(): String {
        return idEmpleado; nombreEmpleado
    }
}