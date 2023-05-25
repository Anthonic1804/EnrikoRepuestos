package com.example.acae30

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityInventariodetalleBinding
import com.example.acae30.listas.InventarioDetalleAdapter
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.InventarioPrecios
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Inventariodetalle : AppCompatActivity() {

    private lateinit var binding: ActivityInventariodetalleBinding
    private var bd: Database? = null
    private var idinventario = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventariodetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = Database(this@Inventariodetalle)
        idinventario = intent.getIntExtra("idproducto", 0)
        val contexto = this@Inventariodetalle

        binding.imageView7.setOnClickListener {
            AlertaPrecio(contexto)  //muestra la alerta
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            val intento = Intent(this, com.example.acae30.Inventario::class.java)
            startActivity(intento)
            finish()
        }//BOTON ATRAS

        if (idinventario > 0) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val producto = getProducto(idinventario)
                    with(binding){
                        txtcodigo.text = producto!!.Codigo
                        txtdescripcion.text = producto.descripcion
                        txtprecio.text = "$" + String.format("%.4f", producto.Precio_iva)
                        txtexistencia.text = producto.Existencia!!.toInt().toString() + " UNI"
                    }

                    validarDatos();
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@Inventariodetalle, "ERROR AL CARGAR EL DETALLE DEL PRODUCTO", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }

    private fun getProducto(id: Int): Inventario? {
        val base = bd!!.readableDatabase
        try {
            val cursor = base!!.rawQuery("SELECT * FROM inventario where Id=$id", null)
            var datos: Inventario? = null
            if (cursor.count > 0) {
                cursor.moveToFirst()
                datos = Inventario(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getFloat(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getFloat(10),
                    cursor.getFloat(11),
                    cursor.getFloat(12),
                    cursor.getFloat(13),
                    cursor.getFloat(14),
                    cursor.getFloat(15),
                    cursor.getFloat(16),
                    cursor.getString(17),
                    cursor.getString(18),
                    cursor.getInt(19),
                    cursor.getString(20),
                    cursor.getInt(21),
                    cursor.getString(22),
                    cursor.getString(23),
                    cursor.getString(24),
                    cursor.getString(25),
                    cursor.getString(26),
                    cursor.getString(27),
                    cursor.getInt(28),
                    cursor.getString(29),
                    cursor.getFloat(30),
                    cursor.getDouble(31),
                    cursor.getInt(32),
                    cursor.getFloat(33)
                )
            }
            return datos
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }
    }


    private fun GetInvPreciosProducto(id_inventario: Int): ArrayList<InventarioPrecios>? {
        val base =bd!!.readableDatabase
        var datos = ArrayList<InventarioPrecios>()
        try {
            val cursor = base.rawQuery(
                "SELECT * FROM Inventario_precios WHERE id_inventario = '$id_inventario'",
                null
            )
            var i = 0
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    var inv_precio = InventarioPrecios(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getFloat(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10),
                        cursor.getInt(11)
                    )

//                    print("Valor: "+inv_precio!!.Codigo_producto)

//                    if (inv_precio != null) {
                    datos.add(inv_precio)
//                    }

                } while (cursor.moveToNext())
                cursor.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
        return datos
    } // obtiene los precios de la tabla Inventario precio

    fun validarDatos(){
        if(idinventario > 0){
            try{
                val lista = GetInvPreciosProducto(idinventario)
                if(lista != null && lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }

        }
    }

    private fun ArmarLista(lista: ArrayList<InventarioPrecios>) {

        val mLayoutManager = LinearLayoutManager(
            this@Inventariodetalle,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaprecios.layoutManager = mLayoutManager
        val adapter = InventarioDetalleAdapter(lista, this@Inventariodetalle)
        binding.listaprecios.adapter = adapter

    }



    private fun AlertaPrecio(contexto: com.example.acae30.Inventariodetalle) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_costo)
        val costoProducto = dialogo.findViewById<TextView>(R.id.txtcosto)

        val producto = getProducto(idinventario)

        costoProducto!!.text = String.format("%.4f", producto!!.costo_iva)



        dialogo.show()

    } //muestra la alerta para MOSTRAR EL COSTO


}