package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.launch
import java.util.*

class Clientes : AppCompatActivity() {

    private var db: Database? = null
    private var recicle: RecyclerView? = null
    private var funciones: Funciones? = null
    private var alert: AlertDialogo? = null
    private var busqueda: SearchView? = null
    private var busquedaPedido: Boolean = false
    private var cuentas = false
    private var visita = false

    private var preferences: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var dSearch : String? = null

    private var clienteHistorio : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)
        supportActionBar?.hide()
        busquedaPedido = intent.getBooleanExtra("busqueda", false)
        cuentas = intent.getBooleanExtra("cuentas", false)
        visita = intent.getBooleanExtra("visita", false)
        clienteHistorio = intent.getBooleanExtra("Historico", false)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        db = Database(this)
        alert = AlertDialogo(this)
        funciones = Funciones()
        busqueda = findViewById(R.id.busquedainv)

        recicle = findViewById(R.id.lista)

    }

    override fun onStart() {
        super.onStart()

        //SETEA LA BUSQUEDA DEL SEARCHVIEW
        //SI HAY DATO ALMACENADO EN ESTE
        dSearch = preferences!!.getString("clienteBusqueda", "")
        if(dSearch != ""){
            busqueda!!.setQuery("$dSearch", true)
        }

        MostrarClientes()
        Busqueda()
    }

    private fun MostrarClientes() {
        alert!!.Cargando()
        //GlobalScope.launch(Dispatchers.IO) {
        this@Clientes.lifecycleScope.launch {
            try {
                val list: ArrayList<Cliente> = getClient()
                MostrarLista(list)
            } catch (e: Exception) {
                alert!!.dismisss()
                Toast.makeText(this@Clientes, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun Atras(view: View) {
        if (busquedaPedido) {
            if (visita) {

                eliminarBusqueda()

                val intento = Intent(this, Pedido::class.java)
                startActivity(intento)
                finish()
            } else {

                eliminarBusqueda()

                val intento = Intent(this, Detallepedido::class.java)
                startActivity(intento)
                finish()
            }
        } else {

            eliminarBusqueda()

            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()
        }

        if(clienteHistorio){
            val intento = Intent(this@Clientes, HistoricoPedidos::class.java)
            startActivity(intento)
            finish()
        }
    }

    //DEVUELVE LOS DATOS DE LA TABLA VIRTUAL
    private fun getClient(): ArrayList<Cliente> {
        val base = db!!.readableDatabase
        val lista = ArrayList<Cliente>()
        if(dSearch != ""){

            val query = searchClient(dSearch.toString())
            this@Clientes.MostrarLista(query)

        }else{

            try {
                val consulta = base.rawQuery("SELECT * FROM Clientes LIMIT 50", null)

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
                            consulta.getFloat(25)
                        )
                        lista.add(listado)

                    } while (consulta.moveToNext())
                    consulta.close()
                }
            } catch (e: Exception) {
                throw Exception(e.message)
            } finally {
                db!!.close()
            }
        }
        return lista
    }


    private fun messageAsync(mensaje: String) {
        if (alert != null) {
            runOnUiThread {
                alert!!.changeText(mensaje)
            }
        }
    } //obtiene el listado de clientes

    private fun Busqueda() {
        busqueda!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            //BUSQUEDA DE CLIENTES DINAMICA
            //MODIFICACION PARA LA LIBRERIA DM
            //22-08-2022

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = searchClient(texto.uppercase())
                this@Clientes.MostrarLista(dSearch)
                return false
            }

        })
    }

    private fun searchClient(dato: String): ArrayList<Cliente> {
        val base = db!!.readableDatabase
        val lista = ArrayList<Cliente>()
        try {

            val consulta = base.rawQuery("SELECT * FROM Clientes WHERE Id IN (SELECT docid FROM virtualcliente WHERE virtualcliente MATCH '$dato') LIMIT 50", null)

            var i = 0
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
                        consulta.getFloat(25)
                    )
                    lista.add(listado)

                } while (consulta.moveToNext())
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return lista
    } //obtiene los resultados de la busqueda en la bd

    private fun MostrarLista(list: ArrayList<Cliente>?) {
        try {

            if (list!!.size > 0) {
                val mLayoutManager =
                    LinearLayoutManager(this@Clientes, LinearLayoutManager.VERTICAL, false)
                recicle!!.layoutManager = mLayoutManager
                val adapter = ClienteAdapter(list, this@Clientes, this@Clientes, 0) { position ->
                    val cliente = list.get(position)
                    if (busquedaPedido) {
                        if (visita) {
                            val datos_visitas = funciones!!.GetVisita(cliente.Id!!, db!!)
                            if (datos_visitas != null) {
                                if (datos_visitas.Abierta) {
                                    val intento = Intent(this@Clientes, Visita::class.java)
                                    intento.putExtra("idcliente", cliente.Id!!)
                                    intento.putExtra("nombrecliente", cliente.Cliente)
                                    intento.putExtra("codigo", cliente.Codigo)
                                    intento.putExtra("visitaid", datos_visitas.Id)
                                    intento.putExtra("idapi", datos_visitas.Idvisita)
                                    startActivity(intento)
                                    finish()
                                } else {
                                    val intento = Intent(this@Clientes, Visita::class.java)
                                    intento.putExtra("idcliente", cliente.Id!!)
                                    intento.putExtra("nombrecliente", cliente.Cliente)
                                    intento.putExtra("codigo", cliente.Codigo)
                                    startActivity(intento)
                                    finish()

                                } //valida si la visita esta abierta

                            } else {
                                val intento = Intent(this@Clientes, Visita::class.java)
                                intento.putExtra("idcliente", cliente.Id!!)
                                intento.putExtra("nombrecliente", cliente.Cliente)
                                intento.putExtra("codigo", cliente.Codigo)
                                startActivity(intento)
                                finish()
                            } //valida si existe visita

                        } else {
                            val intento = Intent(this@Clientes, Detallepedido::class.java)
                            intento.putExtra("id", cliente.Id)
                            intento.putExtra("nombrecliente", cliente.Cliente)
                            startActivity(intento)
                            finish()
                        }

                    } else {
                        if(clienteHistorio){
                            val intento = Intent(this@Clientes, HistoricoPedidos::class.java)
                            intento.putExtra("idCliente", cliente.Id!!)
                            intento.putExtra("nombreCliente", cliente.Cliente)
                            startActivity(intento)
                            finish()
                        }else{
                            busquedaCliente(busqueda!!.query.toString())

                            val intento = Intent(this@Clientes, ClientesDetalle::class.java)
                            intento.putExtra("idcliente", cliente.Id)
                            startActivity(intento)
                            finish()
                        }
                    }
                }
                recicle!!.adapter = adapter
                alert!!.dismisss()
            }
        } catch (e: Exception) {
            alert!!.dismisss()
            Toast.makeText(this@Clientes, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun busquedaCliente(busqueda : String){
        //ALAMACENADO EN MEMORIA LA BUSQUEDA DEL CLIENTE
        val clienteBusqueda = preferences!!.edit()
        clienteBusqueda.putString("clienteBusqueda", busqueda)
        clienteBusqueda.apply()
    }

    //FUNCION PARA ELIMINA DE MEMORIA LA BUSQUEDA DEL CLIENTE
    private fun eliminarBusqueda(){
        val dSearch = preferences?.getString("clienteBusqueda", "")
        if(dSearch != null){
            val eliminarBusqueda = preferences?.edit()
            eliminarBusqueda!!.remove("clienteBusqueda")
            eliminarBusqueda.apply()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras


}