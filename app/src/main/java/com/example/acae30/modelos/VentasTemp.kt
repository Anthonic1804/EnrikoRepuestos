package com.example.acae30.modelos

data class VentasTemp(
    val id_venta: Int,
    val fecha: String,
    val cliente: String,
    val sucursal: String,
    val total: Float,
    val numero: Int
)