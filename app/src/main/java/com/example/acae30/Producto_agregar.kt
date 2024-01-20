package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.acae30.controllers.InventarioController
import com.example.acae30.database.Database
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.JSONmodels.UpdateTokenDataClassJSON
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.DecimalFormatSymbols


class Producto_agregar : AppCompatActivity() {
    private var btnatras: ImageButton? = null
    private var idproducto: Int? = 0
    private var db: Database? = null
    private var btnagregar: Button? = null
    private var alert: AlertDialogo? = null
    private var txtcodigo: TextView? = null
    private var txtdescripcion: TextView? = null
    private var txtexistencia: TextView? = null
    private var spiner: Spinner? = null
    private var precio: Float = 0.toFloat()
    private var cantidad: Float = 0.toFloat()
    private var txttotal: TextView? = null
    private var idpedido: Int = 0
    private var idcliente: Int? = 0
    private var idpedidodetalle: Int? = 0
    private var lienzo: ConstraintLayout? = null
    private var btneliminar: Button? = null
    private var nombrecliente: String? = ""
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var txtcantidad: EditText? = null
    private var spprecio: Spinner? = null
    private var listPrecios: ArrayList<InventarioPrecios>? = null
    private var unidadActual: String? = null
    private var datosProducto: com.example.acae30.modelos.Inventario? = null
    private var btneditarprecio: ImageButton? = null
    private var proviene: String? = ""
    private var total_param: Float? = null
    private var precioEditado: Float = 0.toFloat()
    private var txttituloproducto: TextView? = null
    private var sinExistencias: Int = 0  // 1 -> Si    0 -> no
    private var existenciaProducto: Float = 0f
    private var getSucursalPosition: Int? = null


    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var precioAutorizado: Float = 0f
    private var codEmpleado: Int = 0
    private var url: String? = null
    private var codigoProducto: String = ""
    private var precioAutorizadoUtilizado: Int = 0

    /*Variable para restriccion
    * de seleccion de escalas de precios
    * y unidades vendidas*/
    private var cantidadEscala: Int? = null
    private var idEscala: Int = 0

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView

    var contexto = this

    private var funciones = Funciones()
    private var inventarioController = InventarioController()
    private var modificarPrecio : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_agregar)
        supportActionBar?.hide()
        lienzo = findViewById(R.id.lienzo)
        btnatras = findViewById(R.id.imgbtnatras)

        idproducto = intent.getIntExtra("idproducto", 0)
        idpedido = intent.getIntExtra("idpedido", 0)
        idcliente = intent.getIntExtra("idcliente", 0)
        idpedidodetalle = intent.getIntExtra("idpedidodetalle", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        codEmpleado = preferencias!!.getInt("Idvendedor", 0)

        sinExistencias = if(preferencias!!.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        modificarPrecio = preferencias!!.getBoolean("modificar_precio_app", false)

        //CAPTURANDO SUCURSAL
        getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)
       // println("posicion enviada desde detalle: $getSucursalPosition")

        db = Database(this)
        alert = AlertDialogo(this)
        txtcodigo = findViewById(R.id.txtcodigo)
        txtdescripcion = findViewById(R.id.txtdescripcion)
        txtexistencia = findViewById(R.id.txtexistencia)
        spiner = findViewById(R.id.spunidad)
        txttotal = findViewById(R.id.txttotal)
        btnagregar = findViewById(R.id.btnagregar)
        btneliminar = findViewById(R.id.btneliminar)
        precioEditado = 0.toFloat()

        txtcantidad = findViewById(R.id.txtcantidad)
        spprecio = findViewById(R.id.spprecio)
        unidadActual = "UNIDAD"
        datosProducto = inventarioController.obtenerInformacionProductoPorId(this@Producto_agregar, idproducto!!)
        btneditarprecio = findViewById(R.id.btneditarprecio)

        proviene = intent.getStringExtra("proviene")
        total_param = intent.getFloatExtra("total_param", 0.toFloat())

        txttituloproducto = findViewById(R.id.txttituloproducto)

        //OPTENIENDO LA IP DEL SERVIDOR
        getApiUrl()

        //TOMANDO LA CANTIDAD DE LAS ESCALA SELECCIONADA.
        //09/01/2024
        cantidadEscala = seleccionarCantidadenEscala(idpedido, idproducto!!)


        // Validar que la cantidad sea con hasta dos decimales
        //ACTUALIZADOS LA VALIDACION QUE SEA HASTA CON 4 DECIMAES
        txtcantidad!!.filters = arrayOf<InputFilter>(object : InputFilter {
            var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence {
                val indexPoint: Int =
                    dest.toString().indexOf(decimalFormatSymbols.decimalSeparator)
                if (indexPoint == -1) return source
                val decimals = dend - (indexPoint + 1)
                return if (decimals < 4) source else ""
            }
        })


        // Actualizar los precios cuando cambie el select de unidad
        //ACTUALIZADOS LOS DECIMALES A 4 ---> 23-08-2022
        //ACTUALIZADO 08/01/2024
        spiner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
//                val toast = Toast.makeText(applicationContext, "Valor: "+parent!!.getItemAtPosition(position).toString(), Toast.LENGTH_LONG)
//                toast.show()

                val unidad = parent!!.getItemAtPosition(position).toString()

                if (unidad != unidadActual) {
                    spprecio!!.adapter = null

                    // Agregar precios a lista

                    val precioss = ArrayList<String>()

                    if (listPrecios!!.size > 0) {
                        if (unidad == "UNIDAD") {

                            precioss.add("${String.format("%.4f", datosProducto!!.Precio_iva)}") //PRECIO AGREGADO DEL PRODUCTO DE LA TABLA INVENTARIO
                            listPrecios!!.forEach {

                                if (it.Unidad == "UNI" || it.Unidad == "") {
                                    var unidad_cantidad = ""
                                    if (it.Cantidad!! > 0.toFloat()) {
                                        unidad_cantidad =
                                            " (" + "${String.format("%.4f", it.Cantidad)}" + ")"
                                    }
                                    precioss.add(
                                        "${
                                            String.format(
                                                "%.4f",
                                                it.Precio_iva
                                            )
                                        }" + " ${it.Nombre}" + unidad_cantidad
                                    )

                                }
                            }
                        }

                        if (unidad == "FRACCIÓN") {
                            listPrecios!!.forEach {

                                if (it.Unidad == "FRA") {
                                    var unidad_cantidad = ""
                                    if (it.Cantidad!! > 0.toFloat()) {
                                        unidad_cantidad =
                                            " (" + "${String.format("%.4f", it.Cantidad)}" + ")"
                                    }
                                    precioss.add(
                                        "${
                                            String.format(
                                                "%.4f",
                                                it.Precio_iva
                                            )
                                        }" + " ${it.Nombre}" + unidad_cantidad
                                    )
                                }
                            }
                        }

                    }

                    // Consultar inventario precios
                    //AGREGA LA LISTA DE PRECIOS EN LA LISTA DESPLEGABLE DE PRECIOS
                    var adapterPrecios = ArrayAdapter(
                        contexto,
                        android.R.layout.simple_spinner_item,
                        precioss
                    )
                    adapterPrecios.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spprecio!!.adapter = adapterPrecios

                    unidadActual = unidad
                }
            }
        }

        // Actualizar el total cuando cambie el precio
        spprecio?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
//                val toast = Toast.makeText(applicationContext, "Valor: "+parent!!.getItemAtPosition(position).toString(), Toast.LENGTH_LONG)
//                toast.show()

                val nuevaCadena = parent!!.getItemAtPosition(position).toString()

                if (nuevaCadena.last() == '*') {
                    precio = precioEditado
                } else {
                    var nuevoValor = precioFromList(nuevaCadena)
                    precio = nuevoValor
                }

                Totalizar(cantidad)

            }
        }

        btneditarprecio!!.setOnClickListener {
            if(modificarPrecio){
                AlertaPrecio(this@Producto_agregar)
            }else{
                verificarPrecioAutorizado(codEmpleado, codigoProducto)
            }
        }//cuando se carga los inventarios

        listPrecios = inventarioController.obtenerEscalaPrecios(this@Producto_agregar, idproducto!!)


        // ACTUALIZAR EL CAMPO TOTAL AL MODIFICAR LA CANTIDAD
        //MODIFICACION PAPELERIA DM
        //23-08-2022
        //AGREGADA VALIDACION PARA QUE LA CANTIDAD SOLO ACEPTE ENTEROS
        //24-082022
        txtcantidad!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val nuevaCantidad = s.toString()
                if(sinExistencias != 0){
                    if (nuevaCantidad != "") {
                        if(isInteger(nuevaCantidad)){
                            cantidad = nuevaCantidad.toFloat()
                            if(cantidad < cantidadEscala!!){ //VALIDADO EL PRECIO SELECCIONADO EN LAS ESCALAS.
                                txtcantidad!!.error = "La cantidad no es válida para el precio seleccionado"
                                btnagregar!!.setBackgroundResource(R.drawable.border_btndisable)
                            }else{
                                Totalizar(cantidad)
                                btnagregar!!.setBackgroundResource(R.drawable.border_btnenviar)
                            }
                        }else{
                            txtcantidad!!.error = "Este campo solo permite datos enteros";
                        }
                    } else {
                        txtcantidad!!.error = "Campo no puede quedar vacio"
                        cantidad = 0.toFloat()
                        Totalizar(cantidad)
                    }
                }else{
                    if (nuevaCantidad != "") {
                        if(isInteger(nuevaCantidad)){
                            cantidad = nuevaCantidad.toFloat()
                            /*VALIDACION DE CANTIDAD DE ITEM
                            * EXISTENCIA Y ESCCARLA*/
                            if(cantidad > existenciaProducto || cantidad == 0f){
                                txtcantidad!!.error = "No puede Agregar una cantidad mayor a las existencias actuales";
                                btnagregar!!.setBackgroundResource(R.drawable.border_btndisable)
                            }else if(cantidad < cantidadEscala!!){ //VALIDADO EL PRECIO SELECCIONADO EN LAS ESCALAS.
                                txtcantidad!!.error = "La cantidad no es válida para el precio seleccionado"
                                btnagregar!!.setBackgroundResource(R.drawable.border_btndisable)
                            }else{
                                Totalizar(cantidad)
                                btnagregar!!.setBackgroundResource(R.drawable.border_btnenviar)
                            }
                        }else{
                            txtcantidad!!.error = "Este campo solo permite datos enteros";
                        }
                    } else {
                        txtcantidad!!.error = "Campo no puede quedar vacio"
                        cantidad = 0.toFloat()
                        Totalizar(cantidad)
                    }
                }
            }
        })

    } //inicializa todas las variables y los objetos del xml

    //YA NO REGRESA HASTA EL DETALLE DEL PEDIDO, REGRESA A LA BUSQUEDA DE PRODUCTOS
    //BTNATRAS Y TEXTO CANTIDAD SETEADO SIN DECIMALES
    //MODIFICACION A LA CANTIDAD DE DECIMALES A 4
    //MODIFICACION PARA LA LIBRERIA DM
    //23-08-2022
    override fun onStart() {
        super.onStart()
//        visor!!.text=cantidad.toString()
        txtcantidad!!.setText(String.format("%.0f", cantidad))
        //txttotal!!.text="0.00"

        var contexto = this


        //YA NO REGRESA HASTA EL DETALLE DEL PEDIDO, REGRESA A LA BUSQUEDA DE PRODUCTOS
        //MODIFICACION PARA LA LIBRERIA DM
        //23-08-2022
        //30-08-2022 CORRECCION AL FUNCIONAMIENTO DE LA NAVEGACION DEL BOTON
        btnatras!!.setOnClickListener {
            if(proviene == "editar"){

                provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)

            }else{
                val intento = Intent(this@Producto_agregar, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombrecliente)
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                startActivity(intento)
            }

        } // boton que lleva atras en el activity

        btnagregar!!.setOnClickListener {
            //VERIFICANDO SI MODIFICAR PRECIO ES TRUE DESDE SQLSERVER
            if(modificarPrecio){
                agregarProducto()
            }else{
                // VERIFICANDO SI MOD PRECIO ES FALSE Y LUEGO COMPROBAR QUE EL TOKEN HAYA SIDO UTILIZADO
                if(precioAutorizadoUtilizado == 1){
                    confirmarToken(codEmpleado, codigoProducto)
                }else{
                    agregarProducto()
                }
            }
        }//AGREGANDO EL PRODUCTO AL PEDIDO

        if (idpedidodetalle!! > 0) {
            btneliminar!!.visibility = View.VISIBLE
        } else {
            btneliminar!!.visibility = View.GONE
        }

        btneliminar!!.setOnClickListener {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    deleteDetalle(idpedidodetalle!!)
                }

                provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)

            }catch (e: Exception){
                funciones.mostrarAlerta("ERROR AL ELIMINAR EL PRODUCTO", this@Producto_agregar, lienzo!!)
            }
        }//boton eliminar

        //SE BUSCA EL DETALLE DEL PEDIDO
        if (idproducto!! > 0) {
            var detalle = getPedidodetalle(idpedidodetalle!!)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val datos = datosProducto
                    val datosInvPrecios = listPrecios!!

                    if (datos != null) {

                        // Mostrar si hay fraccion
                        val arreglo = ArrayList<String>()
                        arreglo.add("UNIDAD")

                        var numeroFraccion = 0.toInt()

                        listPrecios!!.forEach {
                            if (it.Unidad == "FRA") {
                                numeroFraccion++
                            }
                        }

                        if (numeroFraccion > 0.toInt()) {
                            arreglo.add("FRACCIÓN")
                        }

                        // Agregar datos a spiner de unidad

                        var adapter = ArrayAdapter(
                            contexto,
                            android.R.layout.simple_spinner_item,
                            arreglo
                        )
                        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                        spiner!!.adapter = adapter

                        txtcodigo!!.text = datos.Codigo
                        txtdescripcion!!.text = datos.descripcion
                        codigoProducto = datos.Codigo.toString()

                        precio = datos.Precio_iva!!
                        Totalizar(cantidad)
                        txtexistencia!!.text = "${datos.Existencia}"
                        existenciaProducto = datos.Existencia!!.toFloat()
//                       txtprecio!!.text="$"+"${String.format("%.2f", datos!!.Precio_iva)}"

                        // Agregar precios a lista

                        val precioss = ArrayList<String>()

                        var seleccionado = false

                        //CORREGIDO EL PRECIO DE VENTA
                        //30-08-2022

                        // Comprobar si viene de editar y seleccionar ese valor
                        if (proviene == "editar") {
                            btnagregar!!.text = "ACTUALIZAR PRODUCTO";
                            txttituloproducto!!.text = "ACTUALIZAR PRODUCTO";
                            btneditarprecio!!.visibility = View.INVISIBLE;

                            var cantidad_provisional = detalle!!.Cantidad
//                           visor!!.text=cantidad.toString()
                            var precio_provisional = detalle!!.Precio_venta //EDITADO PARA QUE TOME EL VALOR SELECCIONADO PARA LA VENTA
                            precioEditado = precio_provisional!!

                            //var precio_provisional = total_param!! / cantidad_provisional
                            cantidad = cantidad_provisional!!
                            precio = detalle.Precio_venta!! // EDITADO PARA QUE TOME EL VALOR SELECCIONADO PARA LA VENTA

                            txtcantidad!!.setText("${String.format("%.0f", cantidad)}")

                            if ("${String.format("%.4f", precio_provisional)}" == "${String.format("%.4f", precio)}")
                            {
                                if (detalle.Precio_editado == "*") {
                                    // precio = detalle.Precio_venta!!
                                    precioss.add("${String.format("%.4f", precio)}" + "*")
                                } else {
                                    // precio = detalle.Precio_venta!!
                                    precioss.add("${String.format("%.4f", precio)}")
                                }
                                seleccionado = true
                            }

                            if (!seleccionado) {
                                if (detalle.Precio_editado == "*") {
                                    precioss.add("${String.format("%.4f", precio_provisional)}" + "*")
                                } else {
                                    precioss.add("${String.format("%.4f", precio_provisional)}")//EDITADO
                                }

                            }

                            precioss.add("${String.format("%.4f", datos.Precio_iva)}")
                        } else {
                            // precio vi;eta debe ir
                            precioss.add("${String.format("%.4f", datos.Precio_iva)}")
                        }

                        Totalizar(cantidad)

                        if (datosInvPrecios.size > 0) {
                            datosInvPrecios.forEach {
                                if (it.Unidad == "UNI" || it.Unidad == "") {
                                    var unidad_cantidad = ""
                                    if (it.Cantidad!! > 0.toFloat()) {
                                        unidad_cantidad =
                                            " (" + "${String.format("%.4f", it.Cantidad)}" + ")"
                                    }
                                    precioss.add(
                                        "${
                                            String.format(
                                                "%.4f",
                                                it.Precio_iva
                                            )
                                        }" + " ${it.Nombre}" + unidad_cantidad
                                    )
//                                   precioss.add("${String.format("%.2f", it.Precio_iva)}")
                                }
                            }
                        }

                        // Consultar inventario precios

                        var adapterPrecios = ArrayAdapter(
                            contexto,
                            android.R.layout.simple_spinner_item,
                            precioss
                        )

                        adapterPrecios.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                        spprecio!!.adapter = adapterPrecios

                    } else {
                        throw Exception("No se Han encontrado los datos")
                    }
                } catch (e: Exception) {
                    /*  runOnUiThread {
                          alert!!.dismisss()
                          Toast.makeText(this@Producto_agregar, e.message, Toast.LENGTH_LONG).show()
                      }*/
                }

            }

        } else {

        }
        CambioCantidad()
    }


    //MODIFICACION PARA LA PAPELERIA DM
    //EDITAR CANTIDAD DE PRODUCTO SIN BORRAR
    //23-08-2022
    private fun CambioCantidad() {
        txtcantidad!!.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
                txtcantidad!!.setText("${String.format("", cantidad)}");
            }
        })
    }

    //MODIFICANDO LA CANTIDAD DE DECIMALES A 4
    //PAPELERIA DM
    //23-08-2022
    private fun Totalizar(cantidad: Float) {
        var total = precio * cantidad
        txttotal!!.text = "${String.format("%.4f", total)}"
    }

    private fun AddDetallePedido(esPrecioEditado: Boolean): Int {
        val base = db!!.writableDatabase
        try {
            base.beginTransaction()
            val detalle = ContentValues()
            detalle.put("Id_pedido", idpedido)
            detalle.put("Id_producto", idproducto)
            detalle.put("Cantidad", cantidad)

            if (spiner!!.selectedItem.toString() == "UNIDAD") {
                detalle.put("Unidad", "UNI")
            } else {
                detalle.put("Unidad", "FRA")
            }

            detalle.put("Idunidad", 0)
            detalle.put("Precio", precio)
            detalle.put("Precio_oferta", 0.toFloat())
            detalle.put("Subtotal", txttotal!!.text.toString().toFloat())
            detalle.put("Descuento", 0.toFloat())

            if (esPrecioEditado) {
                detalle.put("Precio_editado", "*")
            } else {
                detalle.put("Precio_editado", "")
            }

            detalle.put("Id_Inventario_Precios", idEscala)

            val idpedidodetalle = base.insert("detalle_pedidos", null, detalle)

            val cursor = base.rawQuery(
                "SELECT SUM(Subtotal) FROM detalle_pedidos where Id_pedido=$idpedido",
                null
            )
            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", t, "Id=?", arrayOf(idpedido.toString()))
                //}else{
                //throw Exception("Error en el total")
                //}
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
            base.setTransactionSuccessful()
            return idpedidodetalle.toInt()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()
        }
    } //agrega el producto al pedido y actualiza el total

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun getPedidodetalle(id: Int): DetallePedido? {
        val base = db!!.writableDatabase
        try {
            var vista: DetallePedido? = null
            val cursor = base.rawQuery("SELECT * FROM detalle_producto where Id=$id", null)

            if (cursor.count > 0) {
                cursor.moveToFirst()

                vista = DetallePedido(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getFloat(7),
                    cursor.getFloat(8),
                    cursor.getFloat(9),
                    cursor.getFloat(10),
                    cursor.getFloat(11),
                    cursor.getFloat(12),
                    cursor.getFloat(13),
                    cursor.getFloat(14),
                    cursor.getString(15),
                    cursor.getString(16),
                    cursor.getString(17),
                    cursor.getString(18),
                    cursor.getString(19),
                    cursor.getFloat(20),
                    cursor.getInt(21),
                    cursor.getFloat(22),
                    cursor.getString(23),
                    cursor.getInt(24),
                    cursor.getInt(25)
                )

                cursor.close()
            }
            return vista
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }

    } //obtiene el detalle del pedido

    private fun updateDetalle(iddetalle: Int?, esPrecioEditado: Boolean) {
        val base = db!!.writableDatabase
        try {
            base.beginTransaction()
            val detalle = ContentValues()
            detalle.put("Cantidad", cantidad)
            //detalle.put("Cantidad", spiner!!.selectedItem.toString())
            detalle.put("Subtotal", txttotal!!.text.toString().toFloat())

            if (esPrecioEditado) {
                detalle.put("Precio_editado", "*")
            } else {
                detalle.put("Precio_editado", "")
            }

            if (spiner!!.selectedItem.toString() == "UNIDAD") {
                detalle.put("Unidad", "UNI")
            } else {
                detalle.put("Unidad", "FRA")
            }

            val idpedidodetalle = base.update(
                "detalle_pedidos",
                detalle,
                "Id=?",
                arrayOf(iddetalle.toString())
            )

            val cursor = base.rawQuery(
                "SELECT SUM(Subtotal)  FROM detalle_pedidos where Id_pedido=$idpedido",
                null
            )

            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", t, "Id=?", arrayOf(idpedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()
        }
    } //ACTUALIZA EL DETALLE DEL PRODUCTO

    private fun deleteDetalle(iddetalle: Int?) {
        val base = db!!.writableDatabase
        try {
            base.beginTransaction()
            base.execSQL("DELETE FROM detalle_pedidos where Id=$iddetalle") //elimina
            val cursor = base.rawQuery(
                "SELECT SUM(Subtotal)  FROM detalle_pedidos where Id_pedido=$idpedido",
                null
            )
            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
//                if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", t, "Id=?", arrayOf(idpedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()
        }
    }

    private fun validateProduct(idproducto: Int): Int {
        val base = db!!.writableDatabase
        try {
            val cursor = base.rawQuery(
                "SELECT *  FROM detalle_pedidos where Id_pedido=$idpedido and Id_producto=$idproducto",
                null
            )
            if (cursor.count > 0) {
                var i = 0
                cursor.moveToFirst()
                i = cursor.getInt(0)
                cursor.close()
                return i
            } else {
                return 0
            }

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {

            base.close()
        }
    }//valida si ya existe el producto en el detalle


    //FUNCION PARA VALIDAD SI EL INGRESO ES NUMERICO DECIMAL
    private fun isNumeric(cadena: String): Boolean {
        return try {
            cadena.toFloat()
            return true
        } catch (nfe: NumberFormatException) {
            return false
        }
    }

    //FUNCION PARA VALIDAR SI EL INGRESO ES NUMERO ENTERO
    //PARA PAPELERIA DM
    //24-08-2022
    private fun isInteger(cadena: String): Boolean{
        return try{
            cadena.toInt()
            return  true
        }catch (nfe: NumberFormatException){
            txtcantidad!!.setText("${String.format("", cantidad)}");
            return false
        }
    }

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    //MODIFICADA 08/01/2024
    private fun precioFromList(cadena: String): Float {
        var nuevoValor = 0.toFloat()

        if (isNumeric(cadena)) {
            nuevoValor = cadena.toFloat()
        } else {
            listPrecios!!.forEach {

                var valorPrecio = "${String.format("%.4f", it.Precio_iva)}"
                var unidad_cantidad = ""
                if (it.Cantidad!! > 0.toFloat()) {
                    unidad_cantidad = " (" + "${String.format("%.4f", it.Cantidad)}" + ")"

                }
                if (cadena == valorPrecio + " ${it.Nombre}" + unidad_cantidad) {
                    nuevoValor = valorPrecio.toFloat()

                    /*Asignado la canditada para validar Escala*/
                    cantidadEscala = it.Cantidad!!.toInt()
                    idEscala = it.Id!!.toInt()
                }
            }
        }

        return nuevoValor
    } // Busca en la lista de precios y retorna el precio que se ha encontrado

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    private fun AlertaPrecio(contexto: com.example.acae30.Producto_agregar) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_precio)
        var nuevoprecio = dialogo.findViewById<EditText>(R.id.nuevoprecio)
        nuevoprecio.isEnabled = true

        var cadena_precio = spprecio!!.selectedItem.toString()

        var nuevo_precio = 0.toFloat()

        if (cadena_precio.last() == '*') {
            nuevo_precio =
                cadena_precio.replace(cadena_precio.substring(cadena_precio.length - 1), "")
                    .toFloat()
        } else {
            nuevo_precio = precioFromList(cadena_precio)
        }

        if(!modificarPrecio){
            nuevoprecio.isEnabled = false
            nuevoprecio!!.setText("${String.format("%.4f", precioAutorizado)}")
        }

        // Actualizar el total cuando cambie la cantidad
        nuevoprecio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val nuevaCantidad = s.toString()
                if (nuevaCantidad == "") {
                    nuevoprecio.error = "Campo no puede quedar Vacio"
                }
            }
        })

        // Validar los decimales
        nuevoprecio.filters = arrayOf<InputFilter>(object : InputFilter {
            var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence {
                val indexPoint: Int =
                    dest.toString().indexOf(decimalFormatSymbols.decimalSeparator)
                if (indexPoint == -1) return source
                val decimals = dend - (indexPoint + 1)
                return if (decimals < 4) source else "" //MODIFICADA LA CANTIDAD DE DECIMALES
            }
        })

        // Acccion de click al agregar precio
        dialogo.findViewById<Button>(R.id.btnguardarnuevoprecio).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {

                //VARIABLE PARA DETERMINAR SI EL PRECIO ES MODIFICADO O NO
                precioAutorizadoUtilizado = if(!modificarPrecio) 1 else 0

                try {
                    val unidad = spiner!!.selectedItem.toString()

                    spprecio!!.adapter = null

                    // Agregar precios a lista

                    val precioss = ArrayList<String>()

                    // Agregamos el nuevo precio a la lista
                    var valorNuevoPrecio = nuevoprecio.text.toString()

                    if (valorNuevoPrecio == "" || valorNuevoPrecio == null) {
                        precioss.add("${String.format("%.4f", 0.toFloat())}" + "*")
                        precioEditado = 0.toFloat()
                    } else {
                        precioss.add("${String.format("%.4f", valorNuevoPrecio.toFloat())}" + "*")
                        precioEditado = valorNuevoPrecio.toFloat()
                    }

                    if (listPrecios!!.size > 0) {
                        if (unidad == "UNIDAD") {
                            precioss.add("${String.format("%.4f", datosProducto!!.Precio_iva)}")
                            listPrecios!!.forEach {
                                if (it.Unidad == "UNI" || it.Unidad == "") {
                                    var unidad_cantidad = ""
                                    if (it.Cantidad!! > 0.toFloat()) {
                                        unidad_cantidad =
                                            " (" + "${String.format("%.4f", it.Cantidad)}" + ")"
                                    }
                                    precioss.add(
                                        "${
                                            String.format(
                                                "%.4f",
                                                it.Precio_iva
                                            )
                                        }" + " ${it.Nombre}" + unidad_cantidad
                                    )

                                }
                            }
                        }

                        if (unidad == "FRACCIÓN") {
                            listPrecios!!.forEach {
                                if (it.Unidad == "FRA") {
                                    var unidad_cantidad = ""
                                    if (it.Cantidad!! > 0.toFloat()) {
                                        unidad_cantidad =
                                            " (" + "${String.format("%.4f", it.Cantidad)}" + ")"
                                    }
                                    precioss.add(
                                        "${
                                            String.format(
                                                "%.4f",
                                                it.Precio_iva
                                            )
                                        }" + " ${it.Nombre}" + unidad_cantidad
                                    )
                                }
                            }
                        }

                    }

                    // Consultar inventario precios

                    var adapterPrecios = ArrayAdapter(
                        contexto,
                        android.R.layout.simple_spinner_item,
                        precioss
                    )
                    adapterPrecios.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spprecio!!.adapter = adapterPrecios

                    dialogo.dismiss()
                } catch (e: Exception) {
                    dialogo.dismiss()
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                    alert.show()
                }
            }

        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para agregar precio



    //FUNCION PARA OBTENER LA URL DEL SERVIDOR
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.length > 0 && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }

    //FUNCION PARA OBTENER EL PRECIO AUTORIZADO
    private fun verificarPrecioAutorizado(id_empleado:Int, cod_producto:String){
        try {
            val datos = UpdateTokenDataClassJSON(
                id_empleado,
                cod_producto
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "token/search"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    if (responseCode == 201) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()

                                val res: JSONObject = JSONObject(respuesta.toString())
                                precioAutorizado = res.getString("precio_asig").toString().toFloat();

                                runOnUiThread {
                                    AlertaPrecio(contexto)
                                }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }
                    }else {
                        runOnUiThread {
                            mensajeError()
                        }
                    }
                } catch (e: Exception) {
                    throw  Exception("error: " + e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    //FUNCION PARA CONFIRMAR LA UTILIZACION DEL TOKEN
    private fun confirmarToken(id_empleado:Int, cod_producto:String){
        try {
            val datos = UpdateTokenDataClassJSON(
                id_empleado,
                cod_producto
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "token/update"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto)
                    or.flush()
                    if (responseCode == 201) {
                        agregarProducto()
                    }else {
                        runOnUiThread {
                            mensajeErrorProcesar()
                        }
                    }
                } catch (e: Exception) {
                    throw  Exception("error: " + e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    //MENSAJE DE ERROR
    fun mensajeError(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = getString(R.string.error_titulo)
        tvMensaje.text = "NO ENCONTRÓ PRECIO AUTORIZADO"
        tvUpdate.text = getString(R.string.error_aceptar)

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
        }

        tvCancel.visibility = View.GONE

        updateDialog.show()

    }

    //MENSAJE DE ERROR
    fun mensajeErrorProcesar(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = getString(R.string.error_titulo)
        tvMensaje.text = "ERROR AL AGREGAR EL PRODUCTO AL PEDIDO"
        tvUpdate.text = getString(R.string.error_aceptar)

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
        }

        tvCancel.visibility = View.GONE

        updateDialog.show()

    }

    //FUNCION PARA AGREGAR EL PRODUCTO SELECCIONADO AL PEDIDO
    private fun agregarProducto(){
        // Validar si la cantidad corresponde al precio
        var esCorrecto = true

        var precio_provisional = 0.toFloat()

        var cadena_numero = spprecio!!.selectedItem.toString()

        var longitud = cadena_numero.length

        if (cadena_numero.last() == '*') {
            precio_provisional = precioEditado
        } else {
            precio_provisional = precioFromList(cadena_numero)
        }

        var cantidad_provisional = txtcantidad!!.text.toString().toFloat()

        var cant_superior = 0.toFloat()
        var precio_superior = 0.toFloat()
        var cant_inferior = 0.toFloat()
        var precio_inferior = 0.toFloat()

        listPrecios!!.forEach { element ->
            if (element.Cantidad!! > 0.toFloat()) {
                var precio_iva_element =
                    "${String.format("%.4f", element.Precio_iva!!)}".toFloat()
                // Obtener precio y cantidad superior
                if (precio_iva_element != 0.toFloat()) {

                    // Obtener precio y cantidad superior
                    if (precio_superior > 0.toFloat()) {

                        if (precio_iva_element < precio_superior && precio_iva_element > precio_provisional) {
                            precio_superior = precio_iva_element
                            // determinar si la cantidad es diferente de null o cero
                            if (element.Cantidad!! != 0.toFloat()) {
                                cant_superior = element.Cantidad!!
                            }
                        }

                    } else if (precio_superior == 0.toFloat()) {

                        if (precio_iva_element > precio_provisional) {
                            precio_superior = precio_iva_element
                            // determinar si la cantidad es diferente de null o cero
                            if (element.Cantidad!! != 0.toFloat()) {
                                cant_superior = element.Cantidad!!
                            }
                        }

                    }

                    // Obtener precio y cantidad inferior
                    if (precio_inferior > 0.toFloat()) {

                        if (precio_iva_element > precio_inferior && precio_iva_element < precio_provisional) {
                            precio_inferior = precio_iva_element
                            // determinar si la cantidad es diferente de null o cero
                            if (element.Cantidad != 0.toFloat()) {
                                cant_inferior = element.Cantidad!!
                            }
                        }
                    } else if (precio_inferior == 0.toFloat()) {

                        if (precio_iva_element < precio_provisional) {
                            precio_inferior = precio_iva_element
                            // determinar si la cantidad es diferente de null o cero
                            if (element.Cantidad != 0.toFloat()) {
                                cant_inferior = element.Cantidad!!
                            }
                        }

                    }

                }
            }
        }

        // Evluar si precio y cantidad provisional son cero
        if (precio_provisional == 0.toFloat() || cantidad_provisional == 0.toFloat()) {
            esCorrecto = false
        }

        // Repetidos
        var cant_repetidos = 0.toInt()
        var cant_repetida = 0.toFloat()

        listPrecios!!.forEach {
            if (it.Cantidad!! > 0.toFloat()) {
                if (it.Cantidad!! == cant_repetida) {
                    cant_repetida = it.Cantidad!!
                    cant_repetidos++
                }
                if (cant_repetida == 0.toFloat() || cant_repetidos == 0) {
                    cant_repetida = it.Cantidad!!
                }
            }
        }

        if (cant_repetidos == 0) {
            // Evaluar si el precio es igual a algun item de la lista
            if (esCorrecto) {
                listPrecios!!.forEach { element ->
                    if (element.Cantidad!! > 0.toFloat()) {
                        var precio_iva_element =
                            "${String.format("%.4f", element.Precio_iva!!)}".toFloat()
                        if (precio_iva_element != 0.toFloat()) {
                            if (precio_provisional == precio_iva_element) {

                                // Evaluar si la cantidad es valida
                                if (element.Cantidad!! > 0.toFloat()) {
                                    // evaluar que no se pase de la cantidad superior
                                    //                                            if (cant_superior > 0.toFloat()) {
                                    //                                                if (cantidad_provisional <= cant_superior) {
                                    //                                                    esCorrecto = false
                                    //                                                }
                                    if (cantidad_provisional < element.Cantidad!!) {
                                        esCorrecto = false
                                    }
                                    //                                            }

                                    // Evaluar que no se pase de la cantidad inferior
                                    if (cant_inferior > 0.toFloat()) {
                                        if (cantidad_provisional >= cant_inferior) {
                                            esCorrecto = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Validar en caso de que el precio sea diferente a un elemento de la lista
            if (esCorrecto) {
                // Validar con respecto al precio superior
                if (precio_superior > 0.toFloat()) {
                    // Validar la cantidad superior
                    if (cantidad_provisional <= cant_superior && cant_superior > 0.toFloat()) {
                        esCorrecto = false
                    }

                    // Validar la cantidad inferior
                    if (cantidad_provisional >= cant_inferior && cant_inferior > 0.toFloat()) {
                        esCorrecto = false
                    }

                }

                // Validar con respecto al precio inferior
                if (precio_inferior > 0.toFloat()) {
                    // Validar la cantidad superior
                    if (cantidad_provisional <= cant_superior && cant_superior > 0.toFloat()) {
                        esCorrecto = false
                    }

                    // Validar la cantidad inferior
                    if (cantidad_provisional >= cant_inferior && cant_inferior > 0.toFloat()) {
                        esCorrecto = false
                    }

                }
            }
        }

        var esPrecioEditado = false
        if (cadena_numero.last() == '*') {
            esCorrecto = true
            esPrecioEditado = true
        }

        // Verificar si es correcto la validación en caso de que no, mostrar mensaje de error
        if (esCorrecto) {

            try {

                cantidad = txtcantidad!!.text.toString().toFloat()

                if (idpedido > 0) {
                    if (idpedidodetalle!! > 0) {
                        updateDetalle(idpedidodetalle!!, esPrecioEditado)
                    } else {
                        val id = validateProduct(idproducto!!)
                        if (id > 0) {
                            val data = getPedidodetalle(id)
                            cantidad = cantidad + data!!.Cantidad!!
                            var t =
                                ((txttotal!!.text.toString().toFloat()) + data.Subtotal!!)
                            txttotal!!.text = "${String.format("%.4f", t)}"
                            updateDetalle(id, esPrecioEditado)
                        } else {
                            AddDetallePedido(esPrecioEditado)
                        }
                    }

                } else {
                    //CreatePedido()
                }
                runOnUiThread {
                    provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    funciones.mostrarAlerta("ERROR: ${e.message}", this@Producto_agregar, lienzo!!)
                }
            }
        } else {
            runOnUiThread {
                funciones.mostrarAlerta("PRECIO O CANTIDAD SON VALORES INCORRECTOS", this@Producto_agregar, lienzo!!)
            }
        }
    }

    //SELECCIONANDO ESCALA PARA EDITAR PRODUCTO EN DETALL
    private fun seleccionarCantidadenEscala(idPedido: Int, idProducto: Int): Int{
        val db = db!!.readableDatabase
        var cantidadEscala = 0
        try {
            val cursor = db.rawQuery("SELECT IP.Cantidad FROM detalle_pedidos AS DP " +
                    "INNER JOIN inventario_precios AS IP " +
                    "ON DP.Id_Inventario_Precios = IP.Id " +
                    "WHERE DP.Id_pedido=$idPedido AND DP.Id_producto=$idProducto", null)

            cantidadEscala = if(cursor.count > 0){
                cursor.moveToFirst()
                cursor.getInt(0)
            }else{
                0
            }
            cursor.close()
        }catch (e: Exception){
            println("ERROR: AL SELECCIONAR LA ESCALA -> " + e.message)
        }
        return cantidadEscala
    }

    //FUNCION PARA REGRESAR AL DETALLE DEL PEDIDO
    private fun provieneDetallePedido(idpedido: Int, idcliente: Int?, nombrecliente: String?, idvisita: Int, codigo: String, visita: String,
        idapi: Int,
        sucursalPosition: Int?
    ) {
        val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
        intento.putExtra("idpedido", idpedido)
        intento.putExtra("idcliente", idcliente)
        intento.putExtra("nombrecliente", nombrecliente)
        intento.putExtra("visitaid", idvisita)
        intento.putExtra("codigo", codigo)
        intento.putExtra("from", visita)
        intento.putExtra("idapi", idapi)
        intento.putExtra("sucursalPosition", sucursalPosition)
        startActivity(intento)
        finish()
    }
}