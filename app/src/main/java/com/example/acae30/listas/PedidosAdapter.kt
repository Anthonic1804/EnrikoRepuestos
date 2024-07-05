package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.modelos.Pedidos

class PedidosAdapter(
    private val lista: ArrayList<Pedidos>, private val context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.MyViewHolder>() {
    private var ani: Funciones? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidosAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.carta_pedidos, parent, false)
        return MyViewHolder(vista)
    }

    override fun onViewAttachedToWindow(holder: PedidosAdapter.MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        ani!!.AnimacionCircularReavel(holder.itemView)
    } //agrega la animacion

    override fun onViewDetachedFromWindow(holder: PedidosAdapter.MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }//quita la animacion

    override fun onBindViewHolder(holder: PedidosAdapter.MyViewHolder, position: Int) {
        val data = lista[position]
        holder.txtPedido.text = "PED${data.Id}"
        holder.txtCliente.text = data.Nombre_cliente
        var total = data.Total
        if(data.Iva_Percibido!! > 0f){
            total = data.Total!! - data.Iva_Percibido!!
        }

        holder.txtTotal.text = "$" + "${String.format("%.2f".format(total))}"

        var estado = "ENVIADO"
        if (data.Enviado == 0) {
            estado = "NO ENVIADO"
            holder.txtEstado.setBackgroundResource(R.drawable.border_status_red)
        }
        holder.txtEstado.text = estado
        holder.txtFecha.text = data.Fecha_creado

        var transmitido = "TRANSMITIDO"
        if(data.pedido_dte == 0){
            transmitido = "NO TRANSMITIDO"
            holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_red)
        }

        if(data.pedido_dte_error == 1 && data.pedido_dte == 0){
            transmitido = "ERROR DE TRANSMISION"
            holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_red)
        }

        holder.txtTransmitido.text = transmitido

    }

    override fun getItemCount(): Int {
        return lista.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var txtPedido: TextView
        internal var txtCliente: TextView
        internal var txtTotal: TextView
        internal var txtEstado: TextView
        internal var txtFecha: TextView
        internal var txtTransmitido: TextView

        init {
            ani = Funciones()
            itemView.setOnClickListener({ itemClick(layoutPosition) })
            txtPedido = itemView.findViewById(R.id.txtPedido)
            txtCliente = itemView.findViewById(R.id.txtCliente)
            txtTotal = itemView.findViewById(R.id.txtTotal)
            txtEstado = itemView.findViewById(R.id.txtEnviado)
            txtFecha = itemView.findViewById(R.id.txtFecha)
            txtTransmitido = itemView.findViewById(R.id.tvTransmitido)
        }
    }

}