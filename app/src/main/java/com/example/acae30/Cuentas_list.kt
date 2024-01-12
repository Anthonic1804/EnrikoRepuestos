package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Cuentas_list : AppCompatActivity() {
    private var funciones: Funciones? = null
    private var bd: Database? = null
    private var lista: RecyclerView? = null
    private var btnatras: ImageButton? = null
    private var search: SearchView? = null
    private var lienzo: ConstraintLayout? = null

    private var preferences : SharedPreferences? = null
    private var instancia = "CONFIG_SERVIDOR"
    private var busquedaCliente : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_cuentas_list)
        funciones = Funciones()
        bd = Database(this)
        btnatras = findViewById(R.id.imgbtnatras)
        search = findViewById(R.id.searchCuenta)
        lienzo = findViewById(R.id.lienzo)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        lista = findViewById(R.id.lista)
        var list_temp : ArrayList<Cliente> = getClient()
        MostrarLista(list_temp)

    }

    override fun onStart() {
        super.onStart()

        busquedaCliente = preferences!!.getString("busquedaCliente", "")
        if(busquedaCliente != ""){
            search!!.setQuery("$busquedaCliente", true)
        }

        btnatras!!.setOnClickListener {

            eliminarBusqueda()

            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()
        }
        //GlobalScope.launch(Dispatchers.IO) {
        this@Cuentas_list.lifecycleScope.launch {
            try {
                val data = getClient()
                if (data.size > 0) {
                    MostrarLista(data)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Cuentas_list, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        } //ejecuta la funcion asyncrona
        Busqueda() //recibe los parametro de busqueda
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
     //   super.onBackPressed()
       // finish()
    }

    private fun getClient(): ArrayList<Cliente> {
        val base = bd!!.readableDatabase
        val lista = ArrayList<Cliente>()

        if(busquedaCliente != ""){

            val query : ArrayList<Cliente> = searchClient(busquedaCliente.toString())
            this@Cuentas_list.MostrarLista(query)

        }else{
            try {

                val consulta = base.rawQuery("SELECT DISTINCT * FROM clientes C " +
                        "INNER JOIN cuentas P " +
                        "ON C.id = P.id_cliente AND P.Status = 'PENDIENTE' " +
                        "GROUP BY C.id " +
                        "LIMIT 30 ", null)

                if (consulta.count > 0) {
                    consulta.moveToFirst()
                    do {

                        val listado = Cliente(
                            consulta.getInt(0),
                            consulta.getString(1),
                            consulta.getString(2),
                            consulta.getString(3),
                            consulta.getString(4),
                            consulta.getString(5),
                            consulta.getString(6),
                            consulta.getString(7),
                            consulta.getString(8),
                            consulta.getInt(9),
                            consulta.getFloat(10),
                            consulta.getFloat(11),
                            consulta.getString(12),
                            consulta.getString(13),
                            consulta.getString(14),
                            consulta.getString(15),
                            consulta.getString(16),
                            consulta.getString(17),
                            consulta.getString(18),
                            consulta.getString(19),
                            consulta.getInt(20),
                            consulta.getInt(21),
                            consulta.getString(22),
                            consulta.getString(23),
                            consulta.getString(24),
                            consulta.getFloat(25),
                            consulta.getInt(26)
                        )
                        lista.add(listado)

                    } while (consulta.moveToNext())
                    consulta.close()
                }
            } catch (e: Exception) {
                throw Exception(e.message)
            } finally {
                bd!!.close()
            }
        }

        return lista
    } //obtiene el listado de clientes

    //FUNCION PARA MANTENER LA BUSQUEDA DEL CLIENTE
    private fun buscarCliente(busqueda : String){
        val clientSearch = preferences!!.edit()
        clientSearch.putString("busquedaCliente", busqueda)
        clientSearch.apply()
    }

    //FUNCION PARA ELIMINAR LA BUSQUEDA PERSISTENTE DEL CLIENTE
    private fun eliminarBusqueda(){
        val clientSearch = preferences!!.getString("busquedaCliente", "")
        if(clientSearch != ""){
            val deleteSearch = preferences!!.edit()
            deleteSearch.remove("busquedaCliente")
            deleteSearch.apply()
        }
    }

    private fun searchClient(dato: String): ArrayList<Cliente> {
        val base = bd!!.readableDatabase
        val lista = ArrayList<Cliente>()
        try {
            /*val consulta = base.rawQuery(
                "SELECT * FROM Clientes WHERE Id IN (SELECT docid FROM virtualcliente WHERE virtualcliente MATCH '$dato') LIMIT 30",
                null
            )*/

            val consulta = base.rawQuery("SELECT * FROM Clientes WHERE cliente LIKE '%$dato%'", null)

            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {
                    val listado = Cliente(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getString(4),
                        consulta.getString(5),
                        consulta.getString(6),
                        consulta.getString(7),
                        consulta.getString(8),
                        consulta.getInt(9),
                        consulta.getFloat(10),
                        consulta.getFloat(11),
                        consulta.getString(12),
                        consulta.getString(13),
                        consulta.getString(14),
                        consulta.getString(15),
                        consulta.getString(16),
                        consulta.getString(17),
                        consulta.getString(18),
                        consulta.getString(19),
                        consulta.getInt(20),
                        consulta.getInt(21),
                        consulta.getString(22),
                        consulta.getString(23),
                        consulta.getString(24),
                        consulta.getFloat(25),
                        consulta.getInt(27)
                    )
                    lista.add(listado)

                } while (consulta.moveToNext())
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }
        return lista
    } //obtiene los resultados de la busqueda en la bd

    //BUSQUEDA DE CLIENTES DINAMICA
    //MODIFICACION PARA LA LIBRERIA DM
    //31-08-2022
    private fun Busqueda() {
        search!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = searchClient(texto)
                MostrarLista(dSearch)
                return false
            }

        })
    } //obtiene los resultados de la busqueda

    private fun CountCuenta(idcliente: Int): Int {
        val bd = bd!!.readableDatabase
        try {
            val cursor =
                bd!!.rawQuery("SELECT COUNT(*) FROM cuentas where Id_cliente=$idcliente AND status LIKE '%PENDIENTE%'", null)
            val cuentas = 0
            if (cursor.count > 0) {
                return cursor.count
            } else {
                return cuentas
            }

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.close()
        }
    } //revisa si tiene cuentas el cliente

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(lienzo!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
        alert.show()
    }

    private fun MostrarLista(list: ArrayList<Cliente>?) {
        try {

            if (list!!.size > 0) {
                val mLayoutManager =
                    LinearLayoutManager(this@Cuentas_list, LinearLayoutManager.VERTICAL, false)
                lista!!.layoutManager = mLayoutManager
                val adapter =
                    ClienteAdapter(list, this@Cuentas_list, this@Cuentas_list, 0) { position ->
                        val cliente = list.get(position)
                        runOnUiThread {
                            //GlobalScope.launch(Dispatchers.IO) {
                            this@Cuentas_list.lifecycleScope.launch {
                                try {
                                    val cuentas = CountCuenta(cliente.Id!!)
                                    if (cuentas > 0) {

                                        buscarCliente(search!!.query.toString())

                                        val intento = Intent(this@Cuentas_list, CuentasDetalle::class.java)
                                        intento.putExtra("idcliente", cliente.Id!!)
                                        intento.putExtra("nombrecliente", cliente.Cliente!!)
                                        startActivity(intento)
                                        finish()
                                    } else {
                                        runOnUiThread {
                                            ShowAlert("Este cliente no Tiene cuentas Pendientes")
                                        }
                                    }
                                } catch (e: Exception) {
                                    runOnUiThread {
                                        ShowAlert(e.message.toString())
                                    }
                                }
                            }
                        }

                    }
                lista!!.adapter = adapter

            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }
}