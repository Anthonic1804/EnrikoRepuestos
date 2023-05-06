package com.example.acae30.listas

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.Pedidos
import com.example.acae30.modelos.VentasTemp

class VentasTempAdapter(
    private val lista: ArrayList<VentasTemp>, private val context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<VentasTempAdapter.MyViewHolder>() {
    var ani: Funciones? = null
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VentasTempAdapter.MyViewHolder {

        val vista = LayoutInflater.from(p0.context).inflate(R.layout.card_ventastemp, p0, false)
        return MyViewHolder(vista)

    }//devuelve el contenido de cada fila en la vista

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        ani!!.AnimacionCircularReavel(holder.itemView)
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(vista: VentasTempAdapter.MyViewHolder, conta: Int) {
        vista.num_pedido.text = "PE${lista[conta].numero}"
        vista.nom_cliente.text = lista[conta].cliente
        vista.fecha.text = lista[conta].fecha
        vista.sucursal.text = lista[conta].sucursal
        vista.total.text = "$ " + "${String.format("%.4f", lista[conta].total)}"
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var num_pedido: TextView
        internal var nom_cliente: TextView
        internal var sucursal: TextView
        internal var fecha: TextView
        internal var total: TextView

        // internal  var btn:ImageButton
        init {
            num_pedido = itemView.findViewById(R.id.txtPedido)
            nom_cliente = itemView.findViewById(R.id.txtCliente)
            sucursal = itemView.findViewById(R.id.txtSucursal)
            fecha = itemView.findViewById(R.id.txtFecha)
            total = itemView.findViewById(R.id.txtTotal)
            ani = Funciones()
            itemView.setOnClickListener({ itemClick(layoutPosition) })
        }

    }

}