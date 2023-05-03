package com.example.acae30.modelos.JSONmodels

data class BusquedaPedidoJSON(
    val id_cliente : Int,
    val desde : String,
    val hasta : String,
    val id_vendedor : Int,
    val nombre_vendedor: String
)
