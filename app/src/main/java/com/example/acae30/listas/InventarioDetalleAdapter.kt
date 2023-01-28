package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.InventarioPrecios

class InventarioDetalleAdapter(private var list: ArrayList<InventarioPrecios>, private var context: Context) : RecyclerView.Adapter<InventarioDetalleAdapter.MyViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioDetalleAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.escalas, parent, false)
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(vista: InventarioDetalleAdapter.MyViewHolder, position: Int) {
        var data = list[position]

        vista.descipcionEscala.text = data.Nombre
        vista.precioEscala.text = "$ ${String.format("%.4f", data.Precio_iva)}"
        vista.cantidad.text = data.Cantidad.toString() + " UNI"

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(View: View) : RecyclerView.ViewHolder(View) {

        internal var descipcionEscala: TextView
        internal var precioEscala: TextView
        internal var cantidad: TextView

        init {

            descipcionEscala = View.findViewById(R.id.txtdescripcion)
            precioEscala = View.findViewById(R.id.txtprecio)
            cantidad = View.findViewById(R.id.txtcantidad)

        }

    }

}