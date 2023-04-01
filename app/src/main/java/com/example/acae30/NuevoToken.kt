package com.example.acae30

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.acae30.database.Database
import com.example.acae30.modelos.Empleados
import com.example.acae30.modelos.Sucursales

class NuevoToken : AppCompatActivity() {

    private lateinit var btnAtras : Button
    private lateinit var btnProcesar : Button
    private lateinit var btnBuscarProducto : ImageButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var edtProducto : EditText
    private lateinit var edtPrecio : EditText
    private lateinit var edtReferencia : EditText
    private lateinit var edtPrecioOld : EditText
    private lateinit var spEmpleado : Spinner
    private var codigoProducto : String? = ""
    private var nombreProducto : String? = ""
    private var precioProducto : String? = ""
    private var empleadoName: String = ""
    private var db: Database? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_token)
        val intento = intent
        btnAtras = findViewById(R.id.btnatras_token)
        btnProcesar = findViewById(R.id.btnProcesarToken)
        btnBuscarProducto = findViewById(R.id.btnProductoToken)
        edtProducto = findViewById(R.id.edtProducto)
        edtPrecio = findViewById(R.id.edtPrecio)
        edtPrecioOld = findViewById(R.id.edtPrecioOld)
        edtReferencia = findViewById(R.id.edtReferencia)
        codigoProducto = intento.getStringExtra("codigo")
        nombreProducto = intento.getStringExtra("producto")
        precioProducto = intento.getFloatExtra("precio", 0f).toString()
        db = Database(this)

        cargarEmpleado()

        edtProducto.isEnabled = false
        edtReferencia.isEnabled = false
        edtPrecioOld.isEnabled = false

        if(codigoProducto != ""){
            edtProducto.setText(nombreProducto)
            edtReferencia.setText(codigoProducto)
            edtPrecioOld.setText("$ " + precioProducto)
        }

        btnBuscarProducto.setOnClickListener {
            val intent = Intent(this@NuevoToken, Inventario::class.java)
            intent.putExtra("tokenBusqueda", true)
            startActivity(intent)
            finish()
        }

        btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        //IMPLEMENTANDO LOGICA DE EMPLEADO SELECCIONADA EN SPINNER
        spEmpleado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                empleadoName = parent?.getItemAtPosition(position).toString()

                if (empleadoName == "-- SELECCIONE UN EMPLEADO --") {
                    btnProcesar.isEnabled = false
                    btnProcesar.setBackgroundResource(R.drawable.border_btndisable)
                }else{
                    btnProcesar.isEnabled = true
                    btnProcesar.setBackgroundResource(R.drawable.border_btnactualizar)

                   // getSucursalPosition = spSucursal!!.selectedItemPosition
                    //println("Sucursal Seleccioada: $getSucursalPosition")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //NADA IMPLEMENTADO
            }

        }
    }

    private fun atras(){
        val intent = Intent(this@NuevoToken, Tokens::class.java)
        startActivity(intent)
        finish()
    }

    fun mensajeCancelar(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            atras()
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }


    //CARGANDO EL NOMBRE DEL EMPLEADO EN EL SPINNER
    private fun nombreEmpleado(): ArrayList<String> {
        val nombreEmpleado = arrayListOf<String>()
        nombreEmpleado.add("-- SELECCIONE UN EMPLEADO --")
        try {
            val list: ArrayList<com.example.acae30.modelos.Empleados> = getEmpleadoNombre()
            if(list.isNotEmpty()){
                for(data in list){
                    nombreEmpleado.add(data.nombreEmpleado)
                }
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR LA TABLA EMPLEADOS")
        }
        return nombreEmpleado
    }

    //FUNCION PARA CARGAR LOS EMPLEADOS AL SPINNER
    private fun cargarEmpleado() {
        spEmpleado = findViewById(R.id.spVendedor)
        val listSucursal = nombreEmpleado().toMutableList()

        val adaptador = ArrayAdapter(this@NuevoToken, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spEmpleado.adapter = adaptador
    }

    //FUNCION PARA OBTENER LOS EMPLEADOS.
    private fun getEmpleadoNombre(): ArrayList<Empleados> {
        val db = db!!.readableDatabase
        val listaEmpleados = ArrayList<Empleados>()
        try {

            val dataEmpleado = db.rawQuery("SELECT * FROM empleado", null)
            if(dataEmpleado.count > 0){
                dataEmpleado.moveToFirst()
                do{
                    val data = Empleados(
                        dataEmpleado.getString(0),
                        dataEmpleado.getString(1)
                    )
                    listaEmpleados.add(data)
                }while (dataEmpleado.moveToNext())
            }else{
                Toast.makeText(this@NuevoToken, "NO SE ENCONTRARON VENDEDORES REGISTRADOS", Toast.LENGTH_LONG).show()
            }
            dataEmpleado.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaEmpleados
    }
}