package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.acae30.database.Database
import com.example.acae30.modelos.Config
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.dataPedidos
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_producto_agregar.*
import kotlinx.android.synthetic.main.alerta_precio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.util.*


class Producto_agregar : AppCompatActivity() {
    private var btnatras: ImageButton? = null
    private var idproducto: Int? = 0
    private var funciones: Funciones? = null
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

    private var dataSearch: String? = null

    private var sinExistencias: Int? = null  // 1 -> Si    0 -> no
    private var existenciaProducto: Float = 0f

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

        //DATASEARCH INVENTARIO
        //CAPTURA EL VALOR DE LA BUSQUEDA EN EL SEARCHVIEW
        dataSearch = intent.getStringExtra("dataSearch").toString()



        funciones = Funciones()
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
        datosProducto = GetProducto(idproducto!!)
        btneditarprecio = findViewById(R.id.btneditarprecio)

        proviene = intent.getStringExtra("proviene")
        total_param = intent.getFloatExtra("total_param", 0.toFloat())

        txttituloproducto = findViewById(R.id.txttituloproducto)

        //OBTENIDO DATOS DE LA TABLA CONFIGURACION
        getConfig()

        var contexto = this

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

            AlertaPrecio(contexto)  //muestra la alerta

        }//cuando se carga los inventarios

        listPrecios = GetInvPreciosProducto(idproducto!!)


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
                            Totalizar(cantidad)
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
                            if(cantidad > existenciaProducto || cantidad == 0f){
                                txtcantidad!!.error = "No puede Agregar una cantidad mayor a las existencias actuales";
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

    //FUNCION PARA EXTRAER LOS CAMPOS DE LA TABLA CONFIG
    private fun getConfig(){
        val dataBase = db!!.writableDatabase
        try {
            val getConf = dataBase.rawQuery("SELECT * FROM config", null)
            val getConfData = ArrayList<Config>()
            if(getConf.count > 0){
                getConf.moveToFirst()
                do {
                    val data = Config(
                        getConf.getInt(0),
                        getConf.getInt(1)
                    )
                    getConfData.add(data)
                }while (getConf.moveToNext())
            }

            for(data in getConfData){
                sinExistencias = data.sinExistencias!!.toInt()
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            dataBase!!.close()
        }
    }

    //YA NO REGRESA HASTA EL DETALLE DEL PEDIDO, REGRESA A LA BUSQUEDA DE PRODUCTOS
    //BTNATRAS Y TEXTO CANTIDAD SETEADO SIN DECIMALES
    //MODIFICACION A LA CANTIDAD DE DECIMALES A 4
    //MODIFICACION PARA LA LIBRERIA DM
    //23-08-2022
    override fun onStart() {
        super.onStart()
//        visor!!.text=cantidad.toString()
        txtcantidad!!.setText("${String.format("%.0f", cantidad)}")
        //txttotal!!.text="0.00"

        var contexto = this


        //YA NO REGRESA HASTA EL DETALLE DEL PEDIDO, REGRESA A LA BUSQUEDA DE PRODUCTOS
        //MODIFICACION PARA LA LIBRERIA DM
        //23-08-2022
        //30-08-2022 CORRECCION AL FUNCIONAMIENTO DE LA NAVEGACION DEL BOTON
        btnatras!!.setOnClickListener {
            if(proviene == "editar"){
                val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombrecliente)
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                startActivity(intento)
            }else{
                val intento = Intent(this@Producto_agregar, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombrecliente)
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("dataSearch", dataSearch) //ENVIA LA BUSQUEDA ALMACENADA DEL SEARCHVIEW
                startActivity(intento)
            }

        } // boton que lleva atras en el activity

        btnagregar!!.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                // Validar si la cantidad corresponde al precio

                dataSearch = null

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
                            val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
                            intento.putExtra("idpedido", idpedido)
                            intento.putExtra("id", idcliente)
                            intento.putExtra("nombrecliente", nombrecliente)
                            intento.putExtra("idpedido", idpedido)
                            intento.putExtra("visitaid", idvisita)
                            intento.putExtra("codigo", codigo)
                            intento.putExtra("idapi", idapi)
                            startActivity(intento)
                            finish()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            val alert: Snackbar = Snackbar.make(
                                lienzo!!,
                                e.message.toString(),
                                Snackbar.LENGTH_LONG
                            )
                            alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                            alert.show()
                        }
                    }
                } else {
                    runOnUiThread {
                        val alert: Snackbar = Snackbar.make(
                            lienzo!!,
                            "Precio o cantidad son valores incorrectos.",
                            Snackbar.LENGTH_LONG
                        )
                        alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                        alert.show()
                    }
                }
            }
        }//termina

        if (idpedidodetalle!! > 0) {
            btneliminar!!.visibility = View.VISIBLE
        } else {
            btneliminar!!.visibility = View.GONE
        }

        btneliminar!!.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    deleteDetalle(idpedidodetalle!!)
                    val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
                    intento.putExtra("idpedido", idpedido)
                    intento.putExtra("nombrecliente", nombrecliente)
                    intento.putExtra("idpedido", idpedido)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    intento.putExtra("from", "visita")
                    startActivity(intento)
                    finish()
                } catch (e: Exception) {
                    runOnUiThread {
                        val alert: Snackbar = Snackbar.make(
                            lienzo!!,
                            e.message.toString(),
                            Snackbar.LENGTH_LONG
                        )
                        alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                        alert.show()
                    }
                }
            }
        }//boton eliminar

        //SE BUSCA EL DETALLE DEL PEDIDO
        if (idproducto!! > 0) {
            var detalle = getPedidodetalle(idpedidodetalle!!)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    runOnUiThread {
                        alert!!.Cargando()
                    }
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

//                       if(idpedidodetalle!! > 0){
//                           val detallee=getPedidodetalle(idpedidodetalle!!)
//                           txttotal!!.text="${String.format("%.2f", total_param!!)}"
//                           precio = detalle!!.Precio
//                           cantidad= detallee!!.Cantidad
////                           visor!!.text=cantidad.toString()
//                           txtcantidad!!.setText("${String.format("%.2f", cantidad)}")
//                           //Totalizar(cantidad)
//                       }

                        runOnUiThread {
                            alert!!.dismisss()
                        }
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

    private fun GetProducto(id: Int): com.example.acae30.modelos.Inventario? {
        val base = db!!.writableDatabase
        var dato: com.example.acae30.modelos.Inventario? = null
        try {
            val cursor = base.rawQuery("SELECT * FROM inventario WHERE Id=$id", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    dato = com.example.acae30.modelos.Inventario(
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

                } while (cursor.moveToNext())
                cursor.close()
            }
            return dato
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    } //obtiene los datos del producto

    private fun GetInvPreciosProducto(id_inventario: Int): ArrayList<InventarioPrecios>? {
        val base = db!!.writableDatabase
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
            db!!.close()
        }
        return datos
    } // obtiene los precios de la tabla Inventario precio


  /*  private fun CreatePedido() {
        val base = db!!.writableDatabase
        try {
            base.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Id_cliente", idcliente)
            contenido.put("Nombre_cliente", nombrecliente)
            contenido.put("Total", txttotal!!.text.toString().toFloat())
            contenido.put("Descuento", 0.toFloat())
            contenido.put("Enviado", false)
            val id = base.insert("pedidos", null, contenido)
            //inserta el encabezado del pedido
            idpedido = id.toInt()
            val detalle = ContentValues()
            detalle.put("Id_pedido", id)
            detalle.put("Id_producto", idproducto)
            detalle.put("Cantidad", cantidad)
            detalle.put("Unidad", "UNI")
            detalle.put("Idunidad", 0)
            detalle.put("Precio", precio)
            detalle.put("Precio_oferta", 0.toFloat())
            detalle.put("Subtotal", txttotal!!.text.toString().toFloat())
            detalle.put("Descuento", 0.toFloat())
            base.insert("detalle_pedidos", null, detalle)
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            idpedido = 0
            throw Exception(e.message)
        } finally {
            base.endTransaction()
            base.close()
        }
    } //crea el pedido en caso de que no exista*/



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


        var cadena_precio = spprecio!!.selectedItem.toString()

        var nuevo_precio = 0.toFloat()

        if (cadena_precio.last() == '*') {
            nuevo_precio =
                cadena_precio.replace(cadena_precio.substring(cadena_precio.length - 1), "")
                    .toFloat()
        } else {
            nuevo_precio = precioFromList(cadena_precio)
        }

        nuevoprecio!!.setText("${String.format("%.4f", nuevo_precio)}")

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

}