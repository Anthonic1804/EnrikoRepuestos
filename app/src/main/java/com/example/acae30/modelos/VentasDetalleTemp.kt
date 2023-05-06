package com.example.acae30.modelos

data class VentasDetalleTemp(
    val id_venta: Int,
    val id_producto: Int,
    val producto: String,
    val precio_u_iva: Float,
    val cantidad: Int,
    val total_iva: Float
)