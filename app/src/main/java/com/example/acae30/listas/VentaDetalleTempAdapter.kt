package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.VentasDetalleTemp

class VentaDetalleTempAdapter(private var list: ArrayList<VentasDetalleTemp>, private var context: Context) : RecyclerView.Adapter<VentaDetalleTempAdapter.MyViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaDetalleTempAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.detalle_pedido_temp, parent, false)
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(vista: VentaDetalleTempAdapter.MyViewHolder, position: Int) {
        val data = list[position]
        vista.cantidad.text = data.Cantidad.toString()
        vista.descripcion.text = data.Producto
        vista.total.text = "$ ${String.format("%.4f", data.Precio_u_iva)}"
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(View: View) : RecyclerView.ViewHolder(View) {

        internal var cantidad: TextView
        internal var descripcion: TextView
        internal var total: TextView

        init {
            cantidad = View.findViewById(R.id.tvCantidadTemp)
            descripcion = View.findViewById(R.id.tvDescripcionTemp)
            total = View.findViewById(R.id.tvPrecioTemp)
        }
    }

}