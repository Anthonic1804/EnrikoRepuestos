package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.TokenData

class TokenAdapter(private var list: ArrayList<TokenData>, private val context: Context) :
    RecyclerView.Adapter<TokenAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_token,
            parent,
            false
        )
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: TokenAdapter.MyViewHolder, position: Int) {
        val data = list[position]

        holder.id.text = "ID" + data.id.toString()
        holder.referencia.text = data.cod_producto
        holder.vendedor.text = data.nombre_empleado
        holder.producto.text = data.descripcion
        holder.precio.text = "$ " + String.format("%.4f", data.precio_asig)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var id : TextView
        internal var referencia: TextView
        internal var producto:TextView
        internal var vendedor: TextView
        internal var precio: TextView

        init {
            id = itemView.findViewById(R.id.tvId)
            referencia = itemView.findViewById(R.id.tvReferencia)
            producto = itemView.findViewById(R.id.tvProducto)
            vendedor = itemView.findViewById(R.id.tvVendedor)
            precio = itemView.findViewById(R.id.tvNuevoPrecio)
        }
    }

}