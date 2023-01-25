package com.example.acae30

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.InventarioDetalleAdapter
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.InventarioPrecios
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.ArrayList

class Inventariodetalle : AppCompatActivity() {

    private var btnatras: ImageButton? = null
    private var bd: Database? = null
    private var idinventario = 0
    private var txtcodigo: TextView? = null
    private var txtdescripcion: TextView? = null
    private var txtexistencia: TextView? = null
    private var txtprecio: TextView? = null
    private var btncosto: ImageButton? = null

    private var listaprecios: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventariodetalle)
        supportActionBar?.hide()
        bd = Database(this)
        idinventario = intent.getIntExtra("idproducto", 0)
        txtcodigo = findViewById(R.id.txtcodigo)
        btnatras = findViewById(R.id.imgbtnatras)
        txtdescripcion = findViewById(R.id.txtdescripcion)
        txtprecio = findViewById(R.id.txtprecio)
        txtexistencia = findViewById(R.id.txtexistencia)
        listaprecios = findViewById(R.id.listaprecios)
        btncosto = findViewById(R.id.imageView7) //IMAGEN DEL SIGNO DE DOLAR


        var contexto = this


        btncosto!!.setOnClickListener {

            AlertaPrecio(contexto)  //muestra la alerta

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras

    override fun onStart() {
        super.onStart()

        btnatras!!.setOnClickListener {
            val intento = Intent(this, com.example.acae30.Inventario::class.java)
            startActivity(intento)
            finish()
        }//BOTON ATRAS

        if (idinventario > 0) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val producto = getProducto(idinventario)

                    txtcodigo!!.text = producto!!.Codigo
                    txtdescripcion!!.text = producto.descripcion
                    txtprecio!!.text = "$" + String.format("%.4f", producto.Precio_iva)
                    txtexistencia!!.text = producto.Existencia!!.toInt().toString() + " UNI"

                    validarDatos();
                } catch (e: Exception) {

                }

            }
        }
    }

    private fun getProducto(id: Int): Inventario? {
        val base = bd!!.writableDatabase
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
        val base =bd!!.writableDatabase
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

        var mLayoutManager = LinearLayoutManager(
            this@Inventariodetalle,
            LinearLayoutManager.VERTICAL,
            false
        )
        listaprecios!!.layoutManager = mLayoutManager
        val adapter = InventarioDetalleAdapter(lista, this@Inventariodetalle)
        listaprecios!!.adapter = adapter

    }



    private fun AlertaPrecio(contexto: com.example.acae30.Inventariodetalle) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_costo)
        var costoProducto = dialogo.findViewById<TextView>(R.id.txtcosto)

        val producto = getProducto(idinventario)

        costoProducto!!.setText("${String.format("%.4f", producto!!.costo_iva)}")



        dialogo.show()

    } //muestra la alerta para MOSTRAR EL COSTO


}