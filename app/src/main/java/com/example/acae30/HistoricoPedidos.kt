package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.VentasTempAdapter
import com.example.acae30.modelos.JSONmodels.BusquedaPedidoJSON
import com.example.acae30.modelos.VentasTemp
import com.example.acae30.modelos.VentasTempSucursal
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_historico_pedidos.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class HistoricoPedidos : AppCompatActivity() {

    private var idCliente: Int = 0
    private var nombreCliente: String? = ""
    private lateinit var edtCliente: EditText
    private lateinit var edtHasta: EditText
    private lateinit var edtDesde: EditText
    private lateinit var rvVentasTemp: RecyclerView

    private var idVendedor = 0
    private var nombreVendedor: String = ""

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var url: String? = null
    private var alert: AlertDialogo? = null
    private var funciones: Funciones? = null
    private var database: Database? = null

    private lateinit var tvUpdate: TextView
    private lateinit var tvCancel: TextView
    private lateinit var tvMsj: TextView
    private lateinit var tvTitulo: TextView

    private var ventasSucursal : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_pedidos)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        edtCliente = findViewById(R.id.edtCliente)
        edtDesde = findViewById(R.id.etFechaDesde)
        edtHasta = findViewById(R.id.etFechaHasta)
        rvVentasTemp = findViewById(R.id.rvPedidos)
        alert = AlertDialogo(this@HistoricoPedidos)
        funciones = Funciones()
        database = Database(this@HistoricoPedidos)

        nombreVendedor = preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)
        idCliente = intent.getIntExtra("idCliente", 0)
        nombreCliente = intent.getStringExtra("nombreCliente")

        tvNoRegistros.visibility = View.GONE

        if (nombreCliente != "") {
            edtCliente.setText(nombreCliente)
        }

        getApiUrl()
        getSucursalEnHistorial()

    }

    override fun onStart() {
        super.onStart()
        this@HistoricoPedidos.lifecycleScope.launch {
            try {
                if(ventasSucursal > 0){
                    val lista : ArrayList<VentasTemp> = obtenerPedidosAlmacenadosConSucursal()
                    if (lista.size > 0) {
                        armarLista(lista)
                        alert?.dismisss()
                    } else {
                        tvNoRegistros.visibility = View.VISIBLE
                    }
                }else{
                    val lista : ArrayList<VentasTemp> = obtenerPedidosAlmacenadosSinSucursal()
                    if (lista.size > 0) {
                        armarLista(lista)
                        alert?.dismisss()
                    } else {
                        tvNoRegistros.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                println("EXEPCION $e")
               /* runOnUiThread {
                    mensajeError("PEDIDOS")
                }*/
            }
        }

        edtDesde.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                edtDesde.setText(dateStr)
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        edtHasta.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                edtHasta.setText(dateStr)
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        imgRegresar.setOnClickListener { regresarInicio() }

        imgBuscarCliente.setOnClickListener { buscarCliente() }

        edtDesde.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                if(validarFormulario()){
                    generarBusqueda()
                }
            }
        })

        edtHasta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                if(validarFormulario()){
                    generarBusqueda()
                }
            }
        })

    }

    private fun validarFormulario(): Boolean{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val desde = edtDesde.text.toString()
        val hasta = edtHasta.text.toString()
        val cliente = edtCliente.text.toString()

        if (cliente.isEmpty()) {
            mensajeError("ERROR_FORMULARIO_CLIENTE")
            return false
        }

        if((hasta.isNotEmpty() && cliente.isNotEmpty() && desde.isEmpty()) || (hasta.isEmpty() && cliente.isNotEmpty() && desde.isNotEmpty())){
            return false
        }

        if (hasta.isNotEmpty() && desde.isNotEmpty() && cliente.isNotEmpty()) {
            val fechaDesde = dateFormat.parse(desde)
            val fechaHasta = dateFormat.parse(hasta)
            if (fechaDesde > fechaHasta) {
                mensajeError("ERROR_FORMULARIO_FECHA")
                return false
            }
        }
        return true
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun generarBusqueda(){
        if (url != null) {
            if (funciones!!.isNetworkConneted(this)) {
                alert!!.Cargando() //MUESTRA EL MENSAJE DE CARGA
                GlobalScope.launch(Dispatchers.IO) {
                    obtenerPedidos(
                        idCliente,
                        edtDesde.text.toString(),
                        edtHasta.text.toString()
                    )
                } //COURUTINA PARA OBTENER EL HISTORIAL DE PEDIDOS
            } else {
                mensajeError("WIFI")
            }
        } else {
            mensajeError("SERVIDOR")
        }
    }
    //FUNCION PARA VERFICAR SI HAY O NO SUCURSAL EN LOS PEDIDOS
    private fun getSucursalEnHistorial(){
        val db = database!!.readableDatabase
        val lista = ArrayList<VentasTempSucursal>()

        try {
            val verificar = db.rawQuery("SELECT id_sucursal FROM ventasTemp LIMIT 1", null)
            if(verificar.count > 0){
                verificar.moveToFirst()
                val consulta = VentasTempSucursal(
                    verificar.getInt(0)
                )
                lista.add(consulta)

                for(data in lista){
                    ventasSucursal = data.sucursal
                }
                verificar.close()
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    //FUNCION PARA REALIZAR LA BUSQUEDA DE PEDIDOS
    private fun obtenerPedidos(id_cliente: Int, desde: String, hasta: String) {
        try {
            val datos = BusquedaPedidoJSON(
                id_cliente,
                desde,
                hasta
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url!! + "pedido/search"
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
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
                        200 -> {
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        cargarPedidos(res)
                                        //CARGAR LISTADO ENCONTRADO
                                        runOnUiThread {
                                            //val lista = obtenerPedidosAlmacenadosConSucursal()
                                            //armarLista(lista)
                                            getSucursalEnHistorial()

                                            if(ventasSucursal > 0){
                                                val lista : ArrayList<VentasTemp> = obtenerPedidosAlmacenadosConSucursal()
                                                if (lista.size > 0) {
                                                    armarLista(lista)
                                                    alert?.dismisss()
                                                } else {
                                                    tvNoRegistros.visibility = View.VISIBLE
                                                }
                                            }else{
                                                val lista : ArrayList<VentasTemp> = obtenerPedidosAlmacenadosSinSucursal()
                                                if (lista.size > 0) {
                                                    armarLista(lista)
                                                    alert?.dismisss()
                                                } else {
                                                    tvNoRegistros.visibility = View.VISIBLE
                                                }
                                            }
                                        }
                                    } else {
                                        runOnUiThread {
                                            alert!!.dismisss()
                                            mensajeError("NO_ENCONTRADO")
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }

                        400 -> {
                            runOnUiThread { mensajeError("ERROR_CARGAR") }
                        }

                        404 -> {
                            runOnUiThread { mensajeError("NO_ENCONTRADO") }
                        }

                        else -> {
                            runOnUiThread { mensajeError("SERVIDOR") }
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    //FUNCION PARA CARGAR LOS PEDIDOS A SQLITE
    private fun cargarPedidos(json: JSONArray) {
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.delete("ventasTemp", null, null) //LIMPIANDO LA TABLA VENTASTEMP
            bd.delete("ventasDetalleTemp", null, null) //LIMPIANDO LA TABLA VENTASDETALLETEMP

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("fecha", funciones!!.validateJsonIsnullString(dato, "fecha"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("id_sucursal", dato.getInt("id_sucursal"))
                valor.put("id_vendedor", dato.getInt("id_vendedor"))
                valor.put("vendedor", funciones!!.validateJsonIsnullString(dato, "vendedor"))
                valor.put("total", funciones!!.validate(dato.getString("total").toFloat()))
                valor.put("numero", dato.getInt("numero"))

                //ASIGNADO EL JSON INTERNO A UNA VARIABLE
                val detalleVen = dato.getJSONArray("detalleVentas")// EXTRAEMOS EL JSON INTERNO

                //RECORRIENDO EL JSON DETALLEVENTAS
                for (x in 0 until detalleVen.length()) {
                    val detalle = detalleVen.getJSONObject(x)
                    val item = ContentValues()
                    item.put("id_venta", detalle.getInt("id_ventas"))
                    item.put("id_producto", detalle.getInt("id_producto"))
                    item.put("producto", funciones!!.validateJsonIsnullString(detalle, "producto"))
                    item.put(
                        "precio_u_iva",
                        funciones!!.validate(detalle.getString("precio_u_iva").toFloat())
                    )
                    item.put("cantidad", detalle.getInt("cantidad"))
                    item.put(
                        "total_iva",
                        funciones!!.validate(detalle.getString("total_iva").toFloat())
                    )

                    bd.insert("ventasDetalleTemp", null, item) //INSERTANDO EN VENTASDETALLETEMP
                }
                bd.insert("ventasTemp", null, valor) //INSERTANDO EN VENTASDETALLE
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
            alert!!.dismisss()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA LA URL DEL SERVIDOR
    private fun getApiUrl() {
        val ip = preferencias!!.getString("ip", "")
        val puerto = preferencias!!.getInt("puerto", 0)
        if (ip!!.isNotEmpty() && puerto > 0) {
            url = "http://$ip:$puerto/"
        }
    }

    //FUNCION PARA OBTENER TODOS LOS PEDIDOS DE SQLITE
    private fun obtenerPedidosAlmacenadosConSucursal(): ArrayList<VentasTemp> {
        val db = database!!.readableDatabase
        val lista = ArrayList<VentasTemp>()
        try {
            val consulta = db.rawQuery(
                "SELECT VT.id, " +
                        "VT.fecha, " +
                        "C.Cliente, " +
                        "CS.nombre_sucursal, " +
                        "VT.total, " +
                        "VT.numero, " +
                        "VT.Vendedor FROM ventasTemp VT " +
                        "INNER JOIN clientes C ON VT.id_cliente = C.id " +
                        "INNER JOIN cliente_sucursal CS ON VT.id_sucursal = CS.id " +
                        "ORDER BY VT.id DESC", null
            )
            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {
                    val listado = VentasTemp(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getFloat(4),
                        consulta.getInt(5),
                        consulta.getString(6)
                    )
                    lista.add(listado)
                } while (consulta.moveToNext())
                consulta.close()
            } else {
                tvNoRegistros.visibility = View.VISIBLE
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return lista
    }

    private fun obtenerPedidosAlmacenadosSinSucursal(): ArrayList<VentasTemp> {
        val db = database!!.readableDatabase
        val lista = ArrayList<VentasTemp>()
        try {
            val consulta = db.rawQuery(
                "SELECT VT.id, " +
                        "VT.fecha, " +
                        "C.Cliente, " +
                        "'SIN SUCURSAL' AS SUCURSAL, " +
                        "VT.total, " +
                        "VT.numero, " +
                        "VT.Vendedor FROM ventasTemp VT " +
                        "INNER JOIN clientes C ON VT.id_cliente = C.id " +
                        "ORDER BY VT.id DESC", null
            )
            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {
                    val listado = VentasTemp(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getFloat(4),
                        consulta.getInt(5),
                        consulta.getString(6)
                    )
                    lista.add(listado)
                } while (consulta.moveToNext())
                consulta.close()
            } else {
                tvNoRegistros.visibility = View.VISIBLE
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return lista
    }

    //FUNCION PARA MOSTRAR EL LISTADO DE BUSQUEDA
    private fun armarLista(lista: ArrayList<VentasTemp>) {

        if (lista.isNotEmpty()) {
            tvNoRegistros.visibility = View.GONE
        }
        val mLayoutManager =
            LinearLayoutManager(this@HistoricoPedidos, LinearLayoutManager.VERTICAL, false)
        rvVentasTemp.layoutManager = mLayoutManager
        val adapter = VentasTempAdapter(lista, this@HistoricoPedidos) { position ->
            val data = lista[position]

            val intento = Intent(this@HistoricoPedidos, HistoricoPedidoDetalles::class.java)
            intento.putExtra("id_ventas", data.id_venta)
            intento.putExtra("correlativo", data.numero)
            intento.putExtra("fecha", data.fecha)
            intento.putExtra("cliente", data.cliente)
            intento.putExtra("sucursal", data.sucursal)
            intento.putExtra("total", data.total)
            intento.putExtra("vendedor", data.vendedor)
            startActivity(intento)
            finish()
        }
        rvVentasTemp.adapter = adapter
    }

    //FUNCIONES DE INTERFAZ
    private fun regresarInicio() {
        val intent = Intent(this@HistoricoPedidos, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    private fun buscarCliente() {
        val intent = Intent(this@HistoricoPedidos, Clientes::class.java)
        intent.putExtra("Historico", true)
        startActivity(intent)
        finish()
    }

    private fun mensajeError(msj: String) {

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMsj = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        //alert!!.dismisss()

        val mensajeDialogo: String = when (msj) {
            "ERROR_CARGAR" -> {
                "Error al Cargar los Pedidos"
            }

            "NO_ENCONTRADO" -> {
                "No se Encontraron pedidos Registrados"
            }

            "SERVIDOR" -> {
                "Error al intentar conectarse con el Servidor"
            }

            "WIFI" -> {
                "ENCIENDE TUS WIFI/DATOS MÓVILES POR FAVOR"
            }

            "PEDIDOS" -> {
                "ERROR AL CARGAR LOS PEDIDOS ALMACENADOS"
            }

            "ERROR_FORMULARIO_CLIENTE" -> {
                "DEBE DE SELECCIONAR UN CLIENTE"
            }

            "ERROR_FORMULARIO_FECHA" -> {
                "FECHA SELECCIONADAS INCORRECTAMENTE"
            }

            else -> {
                "ERROR AL CONECTARSE CON EL SERVIDOR"
            }
        }

        tvMsj.text = mensajeDialogo
        tvTitulo.text = getString(R.string.titulo_msj_informacion)
        tvCancel.text = getString(R.string.btn_salir)
        tvUpdate.visibility = View.GONE

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
    //FUNCIONES DE INTERFAZ
}