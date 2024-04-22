package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.controllers.ClientesController
import com.example.acae30.database.Database
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class Clientes : AppCompatActivity() {

    private var db: Database? = null
    private var recicle: RecyclerView? = null
    private var alert: AlertDialogo? = null
    private var busqueda: SearchView? = null
    private var busquedaPedido: Boolean = false
    private var lienzo: ConstraintLayout? = null
    private var visita = false
    private var cuentas = false

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvMsj : TextView
    private lateinit var tvTitulo : TextView

    private lateinit var tvListadoClientes: TextView

    private var preferences: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var dSearch : String? = null

    private var clienteHistorio : Boolean = false
    private var pagare: Boolean = false

    private var clienteController = ClientesController()
    private var funciones = Funciones()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)
        supportActionBar?.hide()

        clienteHistorio = intent.getBooleanExtra("Historico", false)
        cuentas = intent.getBooleanExtra("cuentas", false)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        busquedaPedido = preferences!!.getBoolean("busqueda", false)
        visita = preferences!!.getBoolean("visita", false)
        pagare = preferences!!.getBoolean("PagareObligatorio", false)

        db = Database(this)
        alert = AlertDialogo(this)
        busqueda = findViewById(R.id.busquedainv)

        recicle = findViewById(R.id.lista)
        lienzo = findViewById(R.id.lienzo)
        tvListadoClientes = findViewById(R.id.tvListadoClientes)

    }

    override fun onStart() {
        super.onStart()
        //SETEA LA BUSQUEDA DEL SEARCHVIEW
        //SI HAY DATO ALMACENADO EN ESTE

        dSearch = preferences!!.getString("clienteBusqueda", "")
        if(dSearch != ""){
            busqueda!!.setQuery("$dSearch", true)
        }

        if(visita){
            tvListadoClientes.text = getString(R.string.listado_de_clientes_nuevo_pedido)
        }else{
            tvListadoClientes.text = getString(R.string.listado_de_clientes)
        }

        mostrarClientes()
        Busqueda()
    }

    private fun mostrarClientes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val list: ArrayList<Cliente> = clienteController.obtenerListaClientes(this@Clientes, dSearch!!)
                if(list.size > 0){
                    runOnUiThread {
                        MostrarLista(list)
                    }
                }
            }catch (e: Exception){
                runOnUiThread {
                    funciones.mostrarAlerta("ERROR: NO SE LOGRO CARGAR EL LISTADO DE CLIENTES", this@Clientes, lienzo!!)
                }
            }
        }

    }

    //FUNCION PARA ELIMINAR LAS SHARED PREFERENCES CREADAS
    //13/01/2024
    private fun sharedPreferencesFinalizarVisita(){
        val editor = preferences!!.edit()
        editor.remove("visita")
        editor.remove("busqueda")
        editor.apply()
    }

    fun Atras(view: View) {
        if (busquedaPedido) {
            if (visita) {

                eliminarBusqueda()
                sharedPreferencesFinalizarVisita()

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

    private fun Busqueda() {
        busqueda!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            //BUSQUEDA DE CLIENTES DINAMICA
            //MODIFICACION PARA LA LIBRERIA DM
            //22-08-2022

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = clienteController.obtenerListaClientes(this@Clientes ,texto.uppercase())
                this@Clientes.MostrarLista(dSearch)
                return false
            }

        })
    }
    private fun MostrarLista(list: ArrayList<Cliente>?) {
        try {
            if (list!!.size > 0) {
                val mLayoutManager =
                    LinearLayoutManager(this@Clientes, LinearLayoutManager.VERTICAL, false)
                recicle!!.layoutManager = mLayoutManager
                val adapter = ClienteAdapter(list, this@Clientes, this@Clientes, 0) { position ->
                    val cliente = list.get(position)
                    if (busquedaPedido) {

                        val pagareFirmado = clienteController.obtenerInformacionCliente(this@Clientes, cliente.Id!!)?.Firmar_pagare_app!!.toInt()
                        val terminosCliente = clienteController.obtenerInformacionCliente(this@Clientes, cliente.Id!!)?.Terminos_cliente.toString()
                        val idCiente = cliente.Id!!
                        val nomCliente = cliente.Cliente
                        val codCliente = cliente.Codigo

                        if(pagare){
                            if((pagareFirmado == 1 && terminosCliente == "Credito") || (terminosCliente == "Contado")){
                                clienteController.verificarPagareObligatorio(this@Clientes, idCiente, nomCliente!!, codCliente!!,visita)
                            }else{
                                mensajeDialogo(cliente.Id!!)
                            }
                        }else{
                            clienteController.verificarPagareObligatorio(this@Clientes, idCiente, nomCliente!!, codCliente!!,visita)
                        }
                    }else {
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
                //alert!!.dismisss()
            }
        } catch (e: Exception) {
            //alert!!.dismisss()
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

    private fun mensajeDialogo(idCliente: Int){

        val msjDialog = Dialog(this, R.style.Theme_Dialog)
        msjDialog.setCancelable(false)

        msjDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = msjDialog.findViewById(R.id.tvUpdate)
        tvCancel = msjDialog.findViewById(R.id.tvCancel)
        tvMsj = msjDialog.findViewById(R.id.tvMensaje)
        tvTitulo = msjDialog.findViewById(R.id.tvTitulo)

        tvMsj.text = getString(R.string.desea_firmar_el_pagar)
        tvTitulo.text = getString(R.string.firmar_pagar)
        tvUpdate.text = getString(R.string.aceptar)
        tvCancel.text = getString(R.string.cancelar)

        tvUpdate.setOnClickListener {
            val intento = Intent(this@Clientes, ClientesDetalle::class.java)
            intento.putExtra("idcliente", idCliente)
            startActivity(intento)
            finish()

            msjDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            msjDialog.dismiss()
        }

        msjDialog.show()

    }


}