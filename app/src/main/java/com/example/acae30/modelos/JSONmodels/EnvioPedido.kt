package com.example.acae30.modelos.JSONmodels

import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos


data class EnvioPedido (
    var Pedido:Pedidos,
    var Detalle:List<DetallePedido>
)