package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityHistoricoPedidoDetallesBinding
import com.example.acae30.listas.VentaDetalleTempAdapter
import com.example.acae30.modelos.VentasDetalleTemp
import kotlinx.android.synthetic.main.activity_historico_pedido_detalles.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoricoPedidoDetalles : AppCompatActivity() {

    private lateinit var binding: ActivityHistoricoPedidoDetallesBinding
    private var idVentas: Int = 0
    private var correlativo: Int = 0
    private var fecha: String = ""
    private var cliente: String = ""
    private var sucursal: String = ""
    private var total: Float = 0f
    private var vendedor: String = ""

    private var base: Database? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricoPedidoDetallesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        base = Database(this@HistoricoPedidoDetalles)

        idVentas = intent.getIntExtra("id_ventas", 0)
        correlativo = intent.getIntExtra("correlativo", 0)
        fecha = intent.getStringExtra("fecha").toString()
        cliente = intent.getStringExtra("cliente").toString()
        sucursal = intent.getStringExtra("sucursal").toString()
        total = intent.getFloatExtra("total", 0f)
        vendedor = intent.getStringExtra("vendedor").toString()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStart() {
        super.onStart()
        with(binding){
            tvCorrelativo.text = "PE$correlativo"
            tvFecha.text = fecha
            tvCliente.text = cliente
            tvSucursal.text = sucursal
            tvTotal.text = "$ " + "${kotlin.String.format("%.4f", total)}"
            tvVendedor.text = vendedor
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                validarDatos()
            } catch (e: Exception) {
                println("ERROR AL CARGAR EL DETALLE DEL PRODUCTO")
            }

        }

        imgRegresar.setOnClickListener { regresar() }
    }

    //FUNCION PARA OBTENER EL DETALLE DE LA VENTA TEMP
    private fun obtenerDetalle(id: Int): ArrayList<VentasDetalleTemp>{
        val database = base!!.readableDatabase
        val lista = ArrayList<VentasDetalleTemp>()

        try {
            val cursor = database.rawQuery("SELECT Producto, Precio_u_iva, Cantidad FROM ventasDetalleTemp WHERE Id_venta = '$id' ", null)
            if (cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val datos = VentasDetalleTemp(
                        cursor.getString(0),
                        cursor.getFloat(1),
                        cursor.getInt(2)
                    )
                    lista.add(datos)
                }while (cursor.moveToNext())
                cursor.close()
            }else{
                cursor.close()
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            database!!.close()
        }

        return lista
    }

    //FUNCION PARA ARMAR LA LISTA DE DETALLE DE VENTA
    private fun ArmarLista(lista: ArrayList<VentasDetalleTemp>) {

        val mLayoutManager = LinearLayoutManager(
            this@HistoricoPedidoDetalles,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvListadoTemporal.layoutManager = mLayoutManager
        val adapter = VentaDetalleTempAdapter(lista, this@HistoricoPedidoDetalles)
        binding.rvListadoTemporal.adapter = adapter

    }

    //FUNCION PARA CARGAR EN PANTALLA EL DETALLEDE VENTA
    fun validarDatos(){
            try{
                val lista = obtenerDetalle(idVentas)
                if(lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
    }

    //FUNCIONES DE INTERFAZ
    private fun regresar() {
        val intento = Intent(this@HistoricoPedidoDetalles, HistoricoPedidos::class.java)
        startActivity(intento)
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
    //FUNCIONES DE INTERFAZ
}