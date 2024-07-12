package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.DetallePedido

class PedidoDetalleAdapter(
    private var list: ArrayList<DetallePedido>, private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<PedidoDetalleAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PedidoDetalleAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.detalle_pedido, parent, false)
        return MyViewHolder(vista)
    }


    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    override fun onBindViewHolder(vista: PedidoDetalleAdapter.MyViewHolder, position: Int) {
        var data = list[position]
        vista.cantidad.text = "${String.format("%.0f", data.Cantidad?.plus(data.Bonificado!!) ?: data.Cantidad)}"
        vista.descripcion.text = data.Descripcion
        if (data.Precio_editado == "*") {
            vista.total.text = "$" + "${String.format("%.2f".format(data.Total_iva) )}" + "*"
        } else {
            vista.total.text = "$" + "${String.format("%.2f".format(data.Total_iva) )}"
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var cantidad: TextView
        internal var descripcion: TextView
        internal var total: TextView

        init {
            cantidad = itemView.findViewById(R.id.txtcantidad)
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            total = itemView.findViewById(R.id.txtprecio)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}