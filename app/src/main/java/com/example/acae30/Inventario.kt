package com.example.acae30

import android.app.Activity
import android.content.Intent
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
import com.example.acae30.database.Database
import com.example.acae30.listas.InventarioAdapter
import com.example.acae30.modelos.Config
import com.example.acae30.modelos.Inventario
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.carta_inventario_miniatura.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class Inventario : AppCompatActivity() {

    private var db: Database? = null
    private var recicle: RecyclerView? = null
    private var funciones: Funciones? = null
    private var alert: AlertDialogo? = null
    private var busqueda: SearchView? = null
    private var busquedaProducto: Boolean = false
    private var idcliente: Int? = 0
    private var nombrecliente: String? = ""
    private var idpedido = 0
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var scanner: ImageButton? = null
    private var dataSearch: String? = null

    //VARIABLES TABLA CONFIG DE LA APP
    private var vistaInventario: Int? = null //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var sinExistencias: Int? = null  // 1 -> Si    0 -> no


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        //supportActionBar?.hide()
        busqueda = findViewById(R.id.busquedainv)

        busquedaProducto = intent.getBooleanExtra("busqueda", false)
        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)
        db = Database(this)

        dataSearch = intent.getStringExtra("dataSearch") //OBTIENE LA BUSQUEDA ALMACENADA DEL SEARCHVIEW
        recicle = findViewById(R.id.reciInvent)

        funciones = Funciones()
        alert = AlertDialogo(this)

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

        //OPTENIENDO LA INFORMACION DE LA TABLA CONFIG
        getConfigApp()

    }

    //FUNCION PARA SELECCION LOS DATOS DE LA TABLA CONFIG
    private fun getConfigTable(): ArrayList<Config> {
        val data = db!!.writableDatabase
        val list = ArrayList<Config>()

        try {
            val cursor = data.rawQuery("SELECT * FROM config", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do{
                    val arreglo = Config(
                        cursor.getInt(0),
                        cursor.getInt(1)
                    )
                    list.add(arreglo)
                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            data.close()
        }
        return list
    }

    //FUNCION PARA EXTRAER LA CONFIGURACION DE LA APP
    private fun getConfigApp(){
        try {
            val list: ArrayList<com.example.acae30.modelos.Config> = getConfigTable()
            if(list.isNotEmpty()){
                for(data in list){
                    vistaInventario = data.vistaInventario!!.toInt()
                    sinExistencias = data.sinExistencias!!.toInt()
                }
            }
        } catch (e: Exception) {
            println("ERROR AL CARGAR LA TABLA CONFIG")
        }
    }

    //LECTURA DE CODIGO DE BARRAS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                alert!!.dismisss()
                runOnUiThread {
                    Toast.makeText(this@Inventario, "Lectura Cancelada", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                val base = db!!.writableDatabase
                val dato = result.contents.toString().trim()
                    try {

                        GlobalScope.launch(Dispatchers.IO) {
                            val cursor =
                                base.rawQuery(
                                    "SELECT * FROM inventario WHERE Codigo LIKE '%$dato%'",
                                    null
                                )

                            if (cursor.count > 0) {
                                cursor.moveToFirst()
                                do {
                                    val arreglo = Inventario(
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

                                    val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                                    intento.putExtra("idproducto", arreglo.Id)
                                    startActivity(intento)
                                    cursor.close()
                                    finish()
                                } while (cursor.moveToNext())
                            }else{
                                alert!!.dismisss()
                                runOnUiThread {
                                    Toast.makeText(this@Inventario, "No hay coincidencias", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    }
                } catch (e: Exception) {
                    println("ERROR: ${e.message}")

                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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
        if(dataSearch != null){
            busqueda!!.setQuery("$dataSearch", true)
        }

        Busqueda()
        alert!!.Cargando()
        //GlobalScope.launch(Dispatchers.IO) {
        this@Inventario.lifecycleScope.launch {
            try {
                val lista = getInventario()
                MostrarLista(lista)
            } catch (e: Exception) {
                alert!!.dismisss()
                runOnUiThread {
                    Toast.makeText(this@Inventario, e.message, Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    fun Atras(view: View) {
        if (busquedaProducto) {
            val intento = Intent(this, Detallepedido::class.java)
            intento.putExtra("idcliente", idcliente)
            intento.putExtra("nombrecliente", nombrecliente)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisita)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idapi", idapi)
            intento.putExtra("from", "visita")
            startActivity(intento)
        } else {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            dataSearch = null
            finish()
        }

    }


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
                                if(sinExistencias == 0 && existeniasProducto == 0f){
                                    Toast.makeText(this@Inventario, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                                }else{
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
                                    intento.putExtra("dataSearch", busqueda!!.query.toString()) //PRUEBA DE ENVIO HISTORIAL BSUQUEDA
                                    startActivity(intento)
                                }
                            } else {
                                val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                                intento.putExtra("idproducto", list.get(position).Id)
                                startActivity(intento)
                            }
                        }
                        recicle!!.adapter = adapter
                        alert!!.dismisss()
                    }else{
                        //MOSTRANDO INVENTARIO EN VISTA MINIATURA
                        val gridLayoutManayer = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                        recicle!!.layoutManager = gridLayoutManayer
                        val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                            if (busquedaProducto) {
                                val existeniasProducto = list.get(position).Existencia!!.toFloat()
                                if(sinExistencias == 0 && existeniasProducto == 0f){
                                    Toast.makeText(this@Inventario, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                                }else{
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
                                    intento.putExtra("dataSearch", busqueda!!.query.toString()) //PRUEBA DE ENVIO HISTORIAL BSUQUEDA
                                    startActivity(intento)
                                }
                            } else {
                                val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                                intento.putExtra("idproducto", list.get(position).Id)
                                startActivity(intento)
                            }
                        }
                        recicle!!.adapter = adapter
                        alert!!.dismisss()
                    }

                } else {
                    alert!!.dismisss()
                }
            } catch (e: Exception) {
                alert!!.dismisss()
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
                val dSearch = SearchInventario(texto)
                this@Inventario.MostrarLista(dSearch)
                return false
            }

        })
    }
    //filtro para busca en la bd


    private fun getInventario(): List<Inventario> {
        val base = db!!.writableDatabase
        val lista = ArrayList<Inventario>()
        try {

            if(dataSearch != null){
                //REALIZA LA BUSQUEDA ALMACENA DEL SEARCHVIEW
                //val cursor = base.rawQuery("SELECT * FROM inventario WHERE Descripcion LIKE '%$dataSearch%' limit 30", null)

                val cursor = base.rawQuery("SELECT * FROM inventario WHERE Id IN (SELECT docid FROM virtualinventario WHERE virtualinventario MATCH '$dataSearch') LIMIT 30", null)

                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val arreglo = com.example.acae30.modelos.Inventario(
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

                        lista.add(arreglo)


                    } while (cursor.moveToNext())

                    cursor.close()
                }
            }else{
                val cursor = base.rawQuery("SELECT * FROM inventario limit 30", null)

                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val arreglo = Inventario(
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

                        lista.add(arreglo)


                    } while (cursor.moveToNext())

                    cursor.close()
                }
            }

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
        return lista
    } //SE OBTIENE EL INVENTARIO Y SE FILTRA LA INFORMACION SEGUN EL SEARCHVIEW


    private fun SearchInventario(dato: String): List<Inventario> {
        val base = db!!.writableDatabase
        val lista = ArrayList<Inventario>()
        try {
            if(dato.isNotEmpty()){
                val cursor = base.rawQuery("SELECT * FROM inventario WHERE Id IN (SELECT docid FROM virtualinventario WHERE virtualinventario MATCH '$dato') LIMIT 30", null)

                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val arreglo = Inventario(
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

                        lista.add(arreglo)


                    } while (cursor.moveToNext())

                    cursor.close()
                }else{
                    val cursor = base.rawQuery("SELECT * FROM inventario LIMIT 30", null)

                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        do {
                            val arreglo = Inventario(
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

                            lista.add(arreglo)


                        } while (cursor.moveToNext())

                        cursor.close()
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
        return lista
    } //busaca el

    // BOTON PARA RETROCEDER
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}

