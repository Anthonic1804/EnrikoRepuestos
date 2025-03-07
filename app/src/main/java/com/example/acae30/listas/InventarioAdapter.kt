package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.squareup.picasso.Picasso

class InventarioAdapter(
    private val lista: List<com.example.acae30.modelos.Inventario>?,
    private val context: Context,
    private var vistaInventario:Int? = null, //TIPO DE VISTA DEL INVENTARIO
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.MyViewHolder>() {
    var ani: Funciones? = null
    var contador = 0
    //var colores: Array<String>? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): InventarioAdapter.MyViewHolder {
        //VISTAS DE INVENTARIO
        // 1 -> VISTA MINIATURA -> carta_inventario_miniatura
        // 2 -> VISTA LISTA -> carta_inventario
        if(vistaInventario == 1){
            val vistaHolder = LayoutInflater.from(p0.context).inflate(R.layout.carta_inventario_miniatura, p0, false)
            return MyViewHolder(vistaHolder)
        }else{
            val vistaHolder = LayoutInflater.from(p0.context).inflate(R.layout.carta_inventario, p0, false)
            return MyViewHolder(vistaHolder)
        }

    }

    override fun getItemCount(): Int {
        return lista!!.size
    }

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        ani!!.AnimacionCircularReavel(holder.itemView)
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun onBindViewHolder(vista: InventarioAdapter.MyViewHolder, i: Int) {
        if (contador > 6) {
            contador = 0
        }
      //  val color = colores?.get(contador)
       // vista.carta.setBackgroundColor(Color.parseColor("#e0e0e0"))
        val id = lista?.get(i)!!.Id
        vista.titulo.text = lista[i].Codigo
        vista.descripcion.text = lista[i].descripcion
        vista.precio.text = "$" + String.format("%.4f", lista[i].Precio_iva)
        vista.existencia.text = lista[i].Existencia.toString() + " Unidades"
        vista.fraccion.text = lista[i].Fraccion.toString() + " Piezas por Uni."

        if(vistaInventario == 1){
            //CARGANDO LA IMAGEN EL EL MARCO
            var codigo = lista[i].Codigo.toString()
            var url = "https://raw.githubusercontent.com/Anthonic1804/imgFerreteriaRey/master/$codigo.jpg"
            Picasso.get().load(url)
                .placeholder(R.drawable.no_photography)
                .resize(500,500)
                .centerCrop()
                .error(R.drawable.no_photography)
                .into(vista.imagen)
        }else{
            val imgDrawable = R.drawable.ic_car85dp
            vista.imagen.setImageResource(imgDrawable)
        }
        contador++

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var titulo: TextView
        internal var descripcion: TextView
        internal var precio: TextView
        internal var carta: CardView
        internal var existencia : TextView
        internal var fraccion : TextView

        //AGRENADO IMAGEN A LA VISTA MINISTURA
        internal var imagen : ImageView

        init {
            //colores = context.applicationContext.resources.getStringArray(R.array.colors)
            titulo = itemView.findViewById(R.id.txttittulo)
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            precio = itemView.findViewById(R.id.txtprecio)
            carta = itemView.findViewById(R.id.carta)
            existencia = itemView.findViewById(R.id.txtexiste)
            fraccion = itemView.findViewById(R.id.txtFraccion)
            imagen = itemView.findViewById(R.id.imgFoto)
            itemView.setOnClickListener({ itemClick(layoutPosition) })
            ani = Funciones()
        }


    }

}