package com.example.acae30

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.controllers.InventarioController
import com.example.acae30.database.Database
import com.example.acae30.listas.InventarioAdapter
import com.example.acae30.modelos.Config
import com.example.acae30.modelos.Inventario
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class Inventario : AppCompatActivity() {
    private var recicle: RecyclerView? = null
    private var funciones: Funciones? = null
    private var busqueda: SearchView? = null
    private var busquedaProducto: Boolean = false
    private var idcliente: Int? = 0
    private var nombrecliente: String? = ""
    private var idpedido = 0
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var scanner: ImageButton? = null

    //VARIABLE MODULO TOKEN
    private var busquedaToken : Boolean = false

    //VARIABLES TABLA CONFIG DE LA APP
    private var vistaInventario: Int = 0 //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var sinExistencias: Int = 0  // 1 -> Si    0 -> no
    private var getSucursalPosition: Int? = null

    //VARIABLES PARA SHAREDPREFERENCES
    //private var preferences : SharedPreferences? = null
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var productSearch : String? = null

    private var inventarioController = InventarioController()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        //supportActionBar?.hide()
        busqueda = findViewById(R.id.busquedainv)

        busquedaProducto = intent.getBooleanExtra("busqueda", false)
        busquedaToken = intent.getBooleanExtra("tokenBusqueda", false)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        sinExistencias = if(preferences.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        vistaInventario = preferences.getInt("vistaInventario", 0)

        recicle = findViewById(R.id.reciInvent)

        funciones = Funciones()

        scanner = findViewById(R.id.btnscanner)

        scanner!!.findFocus()

        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(scanner!!.windowToken, 0)

        // BOTON ESCANER DE CODIGO DE BARRA
        scanner!!.setOnClickListener {
            val integrador = IntentIntegrator(this@Inventario)
            integrador.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrador.setPrompt("LECTOR DE CODIGOS DE BARRA - SISMANTEC")
            integrador.setCameraId(0)
            integrador.setBeepEnabled(true)
            integrador.setBarcodeImageEnabled(true)
            integrador.initiateScan()
        }

        //CAPTURANDO SUCURSAL
        getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)
       // println("posicion enviada desde detalle: $getSucursalPosition")

    }

    override fun onStart() {
        super.onStart()

        //DESHABILITANDO EL BOTON DE SCANNER
        //EN BUSQUEDA DE PEDIDO
        if(idcliente != 0){
            scanner!!.visibility = View.GONE
        }

        //SETEA LA BUSQUEDA DEL SEARCHVIEW
        //SI HAY DATO ALMACENADO EN ESTE
        productSearch = preferences.getString("buscarProducto", "")
        if(productSearch != null){
            busqueda!!.setQuery("$productSearch", true)
        }

        Busqueda()
        //GlobalScope.launch(Dispatchers.IO) {
        this@Inventario.lifecycleScope.launch {
            try {
                val lista = inventarioController.obtenerInformacionProductoPorString(this@Inventario, "")
                MostrarLista(lista)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, e.message, Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    //LECTURA DE CODIGO DE BARRAS
    @OptIn(DelicateCoroutinesApi::class)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, "Lectura Cancelada", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                val busqueda = result.contents.toString().trim()
                val item = inventarioController.obtenerInformacionProductoPorString(this@Inventario, busqueda)

                if(item.size > 0){
                    var id : Int = 0
                    item.forEach {
                        id = it.Id!!
                    }
                    val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                    intento.putExtra("idproducto", id)
                    startActivity(intento)
                    finish()
                }else{
                    Toast.makeText(this@Inventario, "No hay coincidencias", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun Atras(view: View) {
        if (busquedaProducto) {

            eliminarBusqueda()

            val intento = Intent(this, Detallepedido::class.java)
            intento.putExtra("idcliente", idcliente)
            intento.putExtra("nombrecliente", nombrecliente)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisita)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idapi", idapi)
            intento.putExtra("from", "visita")
            intento.putExtra("sucursalPosition", getSucursalPosition)
            startActivity(intento)
        } else {

            if(busquedaToken){
                val intent = Intent(this@Inventario, NuevoToken::class.java)
                startActivity(intent)
                finish()
            }else{
                eliminarBusqueda()

                val intento = Intent(this, Inicio::class.java)
                startActivity(intento)
                finish()
            }

        }

    }

    //IMPLEMENTADA LA FUNCION DE NO AGREGAR PRODUCTOS SIN EXISTENCIAS
    private fun MostrarLista(list: List<Inventario>) {
            try {
                if (list.isNotEmpty()) {
                    //MOSTRANDO INVENTARIO EN VISTA LISTA
                    if(vistaInventario == 2){
                        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        recicle!!.layoutManager = mLayoutManager
                        val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                            if (busquedaProducto) {
                                val existeniasProducto = list.get(position).Existencia!!.toFloat()
                                if(sinExistencias == 0 && existeniasProducto == 0f || existeniasProducto < 0f){
                                    Toast.makeText(this@Inventario, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                                }else{

                                    buscarProducto(busqueda!!.query.toString())

                                    val intento = Intent(this@Inventario, Producto_agregar::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    intento.putExtra("idcliente", idcliente)
                                    intento.putExtra("nombrecliente", nombrecliente)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("from", "visita")
                                    intento.putExtra("proviene", "buscar_producto")
                                    intento.putExtra("total_param", 0.toFloat())
                                    intento.putExtra("sucursalPosition", getSucursalPosition)
                                    startActivity(intento)
                                }
                            } else {

                                if(busquedaToken){
                                    val intent = Intent(this@Inventario, NuevoToken::class.java)
                                    intent.putExtra("codigo", list.get(position).Codigo.toString())
                                    intent.putExtra("producto", list.get(position).descripcion.toString())
                                    intent.putExtra("precio", list.get(position).Precio_iva)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    buscarProducto(busqueda!!.query.toString())

                                    val editor = preferences.edit()
                                    editor.putInt("idProducto", list[position].Id!!)
                                    editor.apply()

                                    val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                                    startActivity(intento)
                                }
                            }
                        }
                        recicle!!.adapter = adapter
                    }else{
                        //MOSTRANDO INVENTARIO EN VISTA MINIATURA
                        val gridLayoutManayer = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                        recicle!!.layoutManager = gridLayoutManayer
                        val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                            if (busquedaProducto) {
                                val existeniasProducto = list.get(position).Existencia!!.toFloat()
                                if(sinExistencias == 0 && existeniasProducto == 0f || existeniasProducto < 0f){
                                    Toast.makeText(this@Inventario, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                                }else{

                                    buscarProducto(busqueda!!.query.toString())

                                    val intento = Intent(this@Inventario, Producto_agregar::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    intento.putExtra("idcliente", idcliente)
                                    intento.putExtra("nombrecliente", nombrecliente)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("from", "visita")
                                    intento.putExtra("proviene", "buscar_producto")
                                    intento.putExtra("total_param", 0.toFloat())
                                    intento.putExtra("sucursalPosition", getSucursalPosition)
                                    startActivity(intento)
                                }
                            } else {

                                if(busquedaToken){
                                    val intent = Intent(this@Inventario, NuevoToken::class.java)
                                    intent.putExtra("codigo", list.get(position).Codigo.toString())
                                    intent.putExtra("producto", list.get(position).descripcion.toString())
                                    intent.putExtra("precio", list.get(position).Precio_iva)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    buscarProducto(busqueda!!.query.toString())

                                    val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    startActivity(intento)
                                }
                            }
                        }
                        recicle!!.adapter = adapter
                    }

                } else {
                    runOnUiThread {
                        //Toast.makeText(this@Inventario, "NO SE ENCONTRARON DATOS", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, e.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    //FUNCION DE BUSQUEDA DE PRODUCTOS DINAMICA
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun Busqueda() {
        busqueda!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = inventarioController.obtenerInformacionProductoPorString(this@Inventario ,texto.uppercase())
                this@Inventario.MostrarLista(dSearch)
                return false
            }

        })
    }
    //filtro para busca en la bd

    //FUNCION PARA ALMACENAR LA BUSQUEDA DEL PRODUCTO EN MEMORIA
    private fun buscarProducto(busqueda : String){
        val dataSearch = preferences.edit()
        dataSearch.putString("buscarProducto", busqueda)
        dataSearch.apply()
    }

    //FUNCION PARA ELIMINAR LA BUSQUEDA DE PRODUCTO EN MEMORIA
    private fun eliminarBusqueda(){
        val dataSearch = preferences.getString("buscarProducto","")
        if(dataSearch != ""){
            val deleteSearch = preferences.edit()
            deleteSearch.remove("buscarProducto")
            deleteSearch.apply()
        }
    }

    // BOTON PARA RETROCEDER
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}

