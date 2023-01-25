package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.Cuenta
import java.text.SimpleDateFormat

class CuentaAdapter(private var list: ArrayList<Cuenta>, private val context: Context) :
    RecyclerView.Adapter<CuentaAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuentaAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_detalle_cuentas,
            parent,
            false
        )
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CuentaAdapter.MyViewHolder, position: Int) {
        val data = list.get(position)
        var f = ""
        if (data.Fecha!!.length > 0) {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            val output: String = formatter.format(parser.parse(data.Fecha))
            f = output
        }
        holder.fecha.text = f
        holder.documento.text = data.Documento!!
        holder.total.text = "$ " + String.format("%.4f", data.Saldo_actual)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var fecha: TextView
        internal var documento: TextView
        internal var total: TextView


        init {
            fecha = itemView.findViewById(R.id.txtfecha)
            documento = itemView.findViewById(R.id.txtdocumento)
            total = itemView.findViewById(R.id.txttotal)
        }


    }

}