package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.controllers.EmpleadosController
import com.example.acae30.controllers.PreciosAutorizadosController
import com.example.acae30.database.Database
import com.example.acae30.listas.TokenAdapter
import com.example.acae30.modelos.PrecioPersonalizado
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreciosAutorizados : AppCompatActivity() {

    private lateinit var btnEmpleado : FloatingActionButton
    private lateinit var btnNuevo : FloatingActionButton
    private lateinit var btnAtras : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var rvTokenResgistrados : RecyclerView
    private lateinit var lblNoData : TextView

    private var database: Database? = null
    private var alert: AlertDialogo? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var vista: ConstraintLayout? = null


    private var funciones = Funciones()
    private var preciosController = PreciosAutorizadosController()
    private var empleadosController = EmpleadosController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tokens)

        btnEmpleado = findViewById(R.id.btn_floatEmpleados)
        btnNuevo = findViewById(R.id.btn_floatNuevo)
        btnAtras = findViewById(R.id.imgbtnatras)
        rvTokenResgistrados = findViewById(R.id.rvTokenRegistrados)
        lblNoData = findViewById(R.id.lblNoData)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        database = Database(this)
        alert = AlertDialogo(this)

        lblNoData.visibility = View.GONE

        mostrarDatos()

    }

    override fun onStart() {
        super.onStart()

        btnAtras.setOnClickListener {
            atras()
        }

        btnNuevo.setOnClickListener{
            nuevoToken()
        }

        btnEmpleado.setOnClickListener {
            mensajeEmpleados()
        }
    }

    private fun mostrarDatos(){
        try{
            val lista = preciosController.obtenerPrecioAutorizadoPorFecha(this@PreciosAutorizados)
            if(lista.size > 0){
                armarLista(lista)
            }else{
                lblNoData.visibility = View.VISIBLE
                lblNoData.text = "NO SE ENCONTRARON DATOS REGISTRADOS"
            }
        }catch (e: Exception){
                throw Exception(e.message)
        }
    }
    private fun armarLista(lista: java.util.ArrayList<PrecioPersonalizado>) {

        val mLayoutManager = LinearLayoutManager(
            this@PreciosAutorizados,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvTokenResgistrados.layoutManager = mLayoutManager
        val adapter = TokenAdapter(lista, this@PreciosAutorizados)
        rvTokenResgistrados.adapter = adapter

    }

    //MOSTRANDO DIALOGO DE CARGA DE INFORMACION
    private fun mensajeEmpleados(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cargar_empleados)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            cargarEmpleados()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    //VALIDANDO LA CARGA DE LOS EMPLEADOS
    private fun cargarEmpleados(){
        if (funciones.isInternetAvailable(this)) {
            alert!!.Cargando() //muestra la alerta

            CoroutineScope(Dispatchers.IO).launch {
                empleadosController.obtenerEmpleados(this@PreciosAutorizados, vista!!)
            }

        } else {
            mostrarAlerta("ERROR: NO TIENES CONEXION A INTERNET")
        }
    }

    private fun mostrarAlerta(mensaje: String) {
        val alert: Snackbar = Snackbar.make(vista!!, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@PreciosAutorizados, R.color.moderado))
        alert.show()
    }

    //FUNCIONES DE REDIRECCION
    private fun nuevoToken(){
        val intent = Intent(this@PreciosAutorizados, NuevoPrecioAutorizado::class.java)
        startActivity(intent)
        finish()
    }
    private fun atras(){
        val intent = Intent(this@PreciosAutorizados, Inicio::class.java)
        startActivity(intent)
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }



}