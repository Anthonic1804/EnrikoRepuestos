package com.example.acae30.listas

import android.app.Activity
import android.content.Context
<<<<<<< HEAD
import android.content.Intent
import android.graphics.Color
=======
>>>>>>> 7afa4de (LOCAL DE LA OFICINA)
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.modelos.Cliente
<<<<<<< HEAD
import com.example.acae30.modelos.JSONmodels.virtualCliente
=======
>>>>>>> 7afa4de (LOCAL DE LA OFICINA)

class ClienteAdapter(
    private val lista: ArrayList<Cliente>?, private val context: Context,
    private val actividad: Activity?, private val tipo: Int,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.MyViewHolder>() {
    var ani: Funciones? = null
    var contador = 0
    var colores: Array<String>? = null
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ClienteAdapter.MyViewHolder {

        val vista = LayoutInflater.from(p0.context).inflate(R.layout.cartaproducto, p0, false)
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
        return lista!!.size
    }

    override fun onBindViewHolder(vista: ClienteAdapter.MyViewHolder, conta: Int) {
        if (contador > 6) {
            contador = 0
        }
        //val color = colores?.get(contador)
        //vista.carta.setBackgroundColor(Color.parseColor("#e0e0e0"))
        val id = lista!![conta].Id
        val nombre = lista[conta].Cliente
        var idpedido = 0
        vista.titulo.text = lista[conta].Cliente
        vista.descripcion.text = lista[conta].Direccion
        vista.codigo.text = lista[conta].Codigo
        contador++
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var titulo: TextView
        internal var descripcion: TextView
        internal var codigo: TextView
        internal var carta: CardView

        // internal  var btn:ImageButton
        init {
            colores = context.applicationContext.resources.getStringArray(R.array.colors)
            titulo = itemView.findViewById(R.id.titulo)
            descripcion = itemView.findViewById(R.id.descripcion)
            // btn=itemView.findViewById(R.id.btnaccion)
            carta = itemView.findViewById(R.id.card_view)
            ani = Funciones()
            codigo = itemView.findViewById(R.id.txtcodigo)
            itemView.setOnClickListener({ itemClick(layoutPosition) })
        }

    }

}