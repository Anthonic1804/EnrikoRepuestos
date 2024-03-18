package com.example.acae30.modelos.JSONmodels

data class VisitaJSON(
    val Id_app_visita: Int,
    val Fecha_hora_checkin: String,
    val Latitud_checkin: String,
    val Longitud_checkin: String,
    val Id_cliente: Int,
    val Cliente: String,
    val Id_vendedor: Int,
    val Fecha_hora_checkout: String,
    val Latitud_checkout: String,
    val Longitud_checkout: String,
    val Comentarios: String
)