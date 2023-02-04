package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.database.Database
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.Pedidos
import com.example.acae30.modelos.VistaPedidos
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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


class Detallepedido : AppCompatActivity() {

   // private var btbuscar: ImageButton? = null

    private var btbuscarProducto: ImageButton? = null
    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var txtcliente: TextView? = null
    private var lienzo: ConstraintLayout? = null
    private var idpedido = 0
    private var visita_enviada: Boolean? = null
    private var from: String? = ""
    private var db: Database? = null
    private var funciones: Funciones? = null
    private var recicler: RecyclerView? = null
    private var txtfecha_creacion: TextView? = null
    private var txttotal: TextView? = null
    private var btnatras: ImageButton? = null
    private var btneliminar: Button? = null
    private var cabezera: Pedidos? = null
    private var btnenviar: Button? = null
    private var btnguardar: Button? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var idvendedor = 0
    private var idvisita = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null
    private var codigo = ""
    private var idapi = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detallepedido)
        supportActionBar?.hide()
        btnatras = findViewById(R.id.imbtnatras)
        val intento = intent
        alerta = AlertDialogo(this)
        idcliente = intento.getIntExtra("id", 0)
        nombre = intento.getStringExtra("nombrecliente")
        idpedido = intento.getIntExtra("idpedido", 0)
        idvisita = intento.getIntExtra("visitaid", 0)
        codigo = intento.getStringExtra("codigo").toString()
        idapi = intento.getIntExtra("idapi", 0)
        from = intento.getStringExtra("from").toString()
        txtfecha_creacion = findViewById(R.id.fecha_creacion)
        txtcliente = findViewById(R.id.txtcliente)

       // btbuscar = findViewById(R.id.btnbuscar)

        btbuscarProducto = findViewById(R.id.imgbtnadd)
        lienzo = findViewById(R.id.lienzo)
        funciones = Funciones()
        db = Database(this)
        txttotal = findViewById(R.id.txttotal)
        btneliminar = findViewById(R.id.btncancelar)
        btnenviar = findViewById(R.id.btnenviar)
        btnguardar = findViewById(R.id.btnguardar)
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idvendedor = preferencias!!.getInt("Idvendedor", 0)
        vendedor = preferencias!!.getString("Vendedor", "").toString()
        ip = preferencias!!.getString("ip", "").toString()
        puerto = preferencias!!.getInt("puerto", 0)
        visita_enviada = false

        recicler = findViewById(R.id.reciclerdetalle)

      /*  //obtenemos la direccion del servidor
        btbuscar!!.setOnClickListener {
            val intento = Intent(this, Clientes::class.java)
            intento.putExtra("busqueda", true)

            startActivity(intento)
        } //busca al cliente*/

        // Consultar datos de visita
        if (idpedido > 0) {
            val base = db!!.writableDatabase
            try {
                val cursor = base!!.rawQuery(
                    "select c.codigo as codigo, c.cliente as nombre, c.id as idcliente, v.id as idvisita, v.enviado as visita_enviada, strftime('%d/%m/%Y %H:%M', p.fecha_creado) as fecha_creado from pedidos p inner join clientes c on p.Id_cliente = c.Id inner join visitas v on p.idvisita = v.id where p.id = ${idpedido}",
                    null
                )
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    codigo = cursor.getString(0)
                    nombre = cursor.getString(1)
                    idcliente = cursor.getInt(2)
                    idvisita = cursor.getInt(3)
                    visita_enviada = cursor.getInt(4) == 1
                    txtfecha_creacion!!.text = cursor.getString(5)
                    cursor.close()
                } else {
                    throw Exception("Error al obtener código de cliente")
                }
            } catch (e: Exception) {
                throw Exception(e.message)
            } finally {
                base.close()
            }
        }

        // Si no hay visita, que no se pueda enviar pedido
        if (!visita_enviada!!) {
            btnenviar!!.isEnabled = false
            btnenviar!!.setBackgroundColor(Color.GRAY)
        }

        btnatras!!.setOnClickListener {
            val intento = Intent(this, Pedido::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        btbuscarProducto!!.setOnClickListener {
            if (idcliente >= 0) {
                val intento = Intent(this, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", txtcliente!!.text.toString())
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                startActivity(intento)
            } else {
//                val alert: Snackbar = Snackbar.make(
//                    lienzo!!,
//                    "Debes Ingresar un Nombre o buscar el cliente",
//                    Snackbar.LENGTH_LONG
//                )
//                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
//                alert.show()
            }
        }
        //muestra el listado de los productos
        btneliminar!!.setOnClickListener {
            AlertaEliminar()
        }

        validarDatos()

        btnenviar!!.setOnClickListener {
            if (idpedido > 0) {
                if (funciones!!.isNetworkConneted(this)) {
                    GlobalScope.launch(Dispatchers.IO) {
                        //this@Detallepedido.lifecycleScope.launch {
                        try {
                            runOnUiThread {
                                alerta!!.Cargando()
                                alerta!!.changeText("Enviando Pedido")
                            }
                            var pedido = getPedidoSend(idpedido) //retorna el pedido
                            pedido!!.Idvendedor = idvendedor
                            pedido.Vendedor = vendedor
                            //agregamos los datos del vendedor
                            SendPedido(pedido, idpedido)
                            //envia el pedido y actualiza el estado del pedido en el cel
                            runOnUiThread {
                                alerta!!.dismisss()
                                var visitaAbierta = getEstadoVisita()

                                if (visitaAbierta == 1) {
                                    val intento = Intent(this@Detallepedido, Visita::class.java)
                                    intento.putExtra("id", idcliente)
                                    intento.putExtra("nombrecliente", nombre)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idapi", idapi)
                                    startActivity(intento)
                                    finish()
                                } else {
                                    val intento = Intent(this@Detallepedido, Pedido::class.java)
                                    startActivity(intento)
                                    finish()
                                }

                            }

                        } catch (e: Exception) {
                            runOnUiThread {
                                alerta!!.dismisss()
                                val alert: Snackbar = Snackbar.make(
                                    lienzo!!,
                                    e.message.toString(),
                                    Snackbar.LENGTH_LONG
                                )
                                alert.view.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@Detallepedido,
                                        R.color.moderado
                                    )
                                )
                                alert.show()
                            }
                        }
                    }
                } else {
                    val alert: Snackbar =
                        Snackbar.make(lienzo!!, "Enciende tu wifi", Snackbar.LENGTH_LONG)
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()
                } //valida conexion a internet
            }
        }//boton de enviar pedido

        btnguardar!!.setOnClickListener {
            if (idpedido > 0) {

                if (ConfirmarDetallePedido() > 0.toInt()) {
                    val bd = db!!.writableDatabase
                    try {
                        bd!!.execSQL("UPDATE pedidos set Cerrado=1 WHERE Id=$idpedido")
                    } catch (e: Exception) {
                        throw Exception(e.message)
                    } finally {
                        bd!!.close()
                    }

                    try {
                        runOnUiThread {
                            alerta!!.Cargando()
                            alerta!!.changeText("Guardando Pedido")
                        }

                        runOnUiThread {
                            alerta!!.dismisss()
                            var visitaAbierta = getEstadoVisita()

                            if (visitaAbierta == 1) {
                                val intento = Intent(this@Detallepedido, Visita::class.java)
                                intento.putExtra("id", idcliente)
                                intento.putExtra("nombrecliente", nombre)
                                intento.putExtra("idpedido", idpedido)
                                intento.putExtra("visitaid", idvisita)
                                intento.putExtra("codigo", codigo)
                                intento.putExtra("idapi", idapi)
                                startActivity(intento)
                                finish()
                            } else {
                                val intento = Intent(this@Detallepedido, Pedido::class.java)
                                startActivity(intento)
                                finish()
                            }

                        }

                    } catch (e: Exception) {
                        runOnUiThread {
                            alerta!!.dismisss()
                            val alert: Snackbar =
                                Snackbar.make(lienzo!!, e.message.toString(), Snackbar.LENGTH_LONG)
                            alert.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@Detallepedido,
                                    R.color.moderado
                                )
                            )
                            alert.show()
                        }
                    }
                } else {
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        "Error: PRIMERO No hay productos agregados al pedido.",
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(ContextCompat.getColor(this, R.color.moderado))
                    alert.show()
                }
            }
        }
    }

    fun validarDatos() {
        if (idpedido > 0) {
           // btbuscar!!.visibility = View.GONE
            txtcliente!!.isEnabled = false
            txtcliente!!.text = nombre
            GlobalScope.launch(Dispatchers.IO) {
                //this@Detallepedido.lifecycleScope.launch {

                try {
                    val lista = getPedido(idpedido)
                    if (lista != null && lista.size > 0) {
                        if (cabezera != null) {
                            if (cabezera!!.Cerrado == 1) {
                              //  btbuscar!!.visibility = View.GONE
                                txtcliente!!.isEnabled = false
                                btbuscarProducto!!.visibility = View.GONE
                                btnenviar!!.visibility = View.GONE

                                var visitaAbierta = getEstadoVisita()

                                if (visitaAbierta == 0 && !cabezera!!.Enviado) {
                                    //RUTINA PARA SOLO ENVIAR EL PEDIDO
                                    btnenviar!!.visibility = View.VISIBLE
                                }

                                btnguardar!!.visibility = View.GONE
                                btneliminar!!.visibility = View.GONE
                            } else {

                              //  btbuscar!!.visibility = View.VISIBLE

                               // btbuscar!!.visibility = View.GONE
                              //  btbuscar!!.visibility = View.GONE
                                txtcliente!!.isEnabled = false
                                btbuscarProducto!!.visibility = View.VISIBLE
                                btnenviar!!.visibility = View.VISIBLE
                                btnguardar!!.visibility = View.VISIBLE
                                btneliminar!!.visibility = View.VISIBLE
                            }
                        }
                        ArmarLista(lista)
                    } else if (from == "visita") {

                        //RUTINA PARA AGREGAR NUEVO PEDIDO

                      //  btbuscar!!.visibility = View.VISIBLE

                      //  btbuscar!!.visibility = View.GONE
                      //  btbuscar!!.visibility = View.GONE
                        txtcliente!!.isEnabled = false
                        btbuscarProducto!!.visibility = View.VISIBLE
                        btnenviar!!.visibility = View.VISIBLE
                        btnguardar!!.visibility = View.VISIBLE
                        btneliminar!!.visibility = View.VISIBLE
                    } else {
                        throw Exception("Error al encontrar el pedido")
                    }

                    if (cabezera!!.Cerrado == 1) {
                        btnatras!!.visibility = View.VISIBLE
                    } else {
                        btnatras!!.visibility = View.GONE
                    }
                } catch (e: Exception) {
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
            if (idcliente > 0) {
                txtcliente!!.text = nombre
              //  btbuscar!!.visibility = View.GONE
            } else {
              //  btbuscar!!.visibility = View.VISIBLE
            }
        }
    } //VALIA QUE YA EXISTA EL CLIENTE DATOS

    private fun getPedido(id: Int): ArrayList<DetallePedido> {
        val base = db!!.writableDatabase
        try {
            val pedido = base.rawQuery("SELECT * FROM pedidos where Id=$id", null)
            if (pedido.count > 0) {
                pedido.moveToFirst()
                cabezera = Pedidos(
                    pedido.getInt(0),
                    pedido.getInt(1),
                    pedido.getString(2),
                    pedido.getFloat(3),
                    pedido.getFloat(4),
                    pedido.getInt(5) == 1,
                    pedido.getString(6),
                    pedido.getInt(7),
                    pedido.getString(8),
                    pedido.getInt(9),
                    pedido.getInt(10),
                    pedido.getString(11)
                )
                pedido.close()
            }

            val cursor = base.rawQuery("SELECT *  FROM detalle_producto where Id_pedido=$id", null)
            var lista = ArrayList<DetallePedido>()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    var detalle = DetallePedido(
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
                    if (detalle != null) {
                        lista.add(detalle)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }

    } //obtiene el detalle del pedido


    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun ArmarLista(lista: ArrayList<DetallePedido>) {
        var total = 0.toFloat()
        var mLayoutManager = LinearLayoutManager(
            this@Detallepedido,
            LinearLayoutManager.VERTICAL,
            false
        )
        recicler!!.layoutManager = mLayoutManager
        val adapter = PedidoDetalleAdapter(lista, this@Detallepedido) { i ->
            if (cabezera != null) {
                if (!cabezera!!.Enviado) {
                    val data = lista.get(i)
                    val intento = Intent(this@Detallepedido, Producto_agregar::class.java)
                    intento.putExtra("idpedidodetalle", data.Id)
                    intento.putExtra("idpedido", data.Id_pedido)
                    intento.putExtra("idcliente", idcliente)
                    intento.putExtra("nombrecliente", nombre)
                    intento.putExtra("idproducto", data.Id_producto)
                    intento.putExtra("proviene", "editar")
                    intento.putExtra("total_param", data.Subtotal)
                    startActivity(intento)
                    finish()
                }
            }
        }
        for (i in lista) {
            total = total + i.Subtotal!!
        }
        txttotal!!.text = "$" + "${String.format("%.4f", total)}"
        recicler!!.adapter = adapter

    } //muestra el detalle del pedido

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun AlertaEliminar() {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alert_eliminar)
        dialogo.findViewById<Button>(R.id.btneliminar).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                //this@Detallepedido.lifecycleScope.launch {
                try {
                    EliminarPedido(idpedido)

//                    val intento=Intent(this@Detallepedido,Pedido::class.java)
//                    startActivity(intento)
//                    finish()

                    val intento = Intent(this@Detallepedido, Visita::class.java)
                    intento.putExtra("id", idcliente)
                    intento.putExtra("nombrecliente", nombre)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    startActivity(intento)
                    finish()

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

        dialogo.findViewById<Button>(R.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para eliminar

    private fun EliminarPedido(idpedido: Int) {
        val bd = db!!.writableDatabase
        try {
            val cursor =
                bd!!.rawQuery("SELECT * FROM pedidos where Id=$idpedido and Enviado=1", null)
            if (cursor.count > 0) {
                throw Exception("Este pedido ya fue enviado no se puede eliminar")
            } else {
                bd.execSQL("DELETE FROM pedidos where Id=$idpedido")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    }

    private fun getPedidoSend(idpedido: Int): CabezeraPedidoSend? {
        val base = db!!.readableDatabase
        try {
            var envio: CabezeraPedidoSend? = null
            val pedido = base!!.rawQuery("SELECT * FROM pedidos where Id=$idpedido", null)
            if (pedido.count > 0) {
                pedido.moveToFirst()
                envio = CabezeraPedidoSend(
                    pedido.getInt(1),//id del cliente
                    pedido.getString(2), //nombre del cliente
                    pedido.getFloat(3),
                    pedido.getFloat(4),
                    pedido.getFloat(3),
                    pedido.getInt(5) == 1,
                    pedido.getInt(9) == 1,
                    0,
                    "",
                    null

                )
                pedido.close()
                var cdetalle =
                    base.rawQuery("SELECT * FROM detalle_producto WHERE Id_pedido=$idpedido", null)
                if (cdetalle.count > 0) {
                    var list = ArrayList<DetallePedido>() //lista donde se guardara el pedido
                    cdetalle.moveToFirst()
                    do {
                        var detalle = DetallePedido(
                            cdetalle.getInt(0),
                            cdetalle.getInt(1),
                            cdetalle.getInt(2),
                            cdetalle.getString(3),
                            cdetalle.getString(4),
                            cdetalle.getFloat(5),
                            cdetalle.getFloat(6),
                            cdetalle.getFloat(7),
                            cdetalle.getFloat(8),
                            cdetalle.getFloat(9),
                            cdetalle.getFloat(10),
                            cdetalle.getFloat(11),
                            cdetalle.getFloat(12),
                            cdetalle.getFloat(13),
                            cdetalle.getFloat(14),
                            cdetalle.getString(15),
                            cdetalle.getString(16),
                            cdetalle.getString(17),
                            cdetalle.getString(18),
                            cdetalle.getString(19),
                            cdetalle.getFloat(20),
                            cdetalle.getInt(21),
                            cdetalle.getFloat(22),
                            cdetalle.getString(23),
                            cdetalle.getInt(24),
                            cdetalle.getInt(25)
                        )
                        list.add(detalle)
                    } while (cdetalle.moveToNext())
                    cdetalle.close()
                    envio.detalle = list //se agrega al objecto el detalle del pedido
                }
            }
            return envio
        } catch (e: Exception) {
            throw Exception(e)
        } finally {
            base.close()
        }
    }//obtiene el pedido
    //obtiene el pedido de la base de datos






    private fun SendPedido(pedido: CabezeraPedidoSend, idpedido: Int) {
        try {
            val objecto = convertToJson(pedido, idpedido) //convertimos a json el objecto pedido
            val ruta: String = "http://$ip:$puerto/pedido" //ruta para enviar el pedido
            //val ruta="http://192.168.0.103:53272/pedido"
            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    connectTimeout = 10000
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto.toString()) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                        try {
                            val respuesta = StringBuffer()
                            var inpuline = it.readLine()
                            while (inpuline != null) {
                                respuesta.append(inpuline)
                                inpuline = it.readLine()
                            }
                            it.close()
                            var data: String? = respuesta.toString()
                            if (data != null && data.length > 0) {
                                val datosservidor = JSONObject(data)
                                if (!datosservidor.isNull("error") && !datosservidor.isNull("response")) {
                                    when (responseCode) {
                                        201 -> {
                                            val idpedidoS = datosservidor.getString("error").toInt()
                                            if (idpedidoS > 0) {
                                                ConfirmarPedido(idpedido, idpedidoS)
                                            } else {
                                                throw Exception(datosservidor.getString("response"))
                                            }
                                        }
                                        400 -> {
                                            throw Exception(datosservidor.getString("response"))
                                        }
                                        500 -> {
                                            throw Exception(datosservidor.getString("response"))
                                        }
                                        else -> {
                                            throw Exception("Ocurrio algo Intenta Nuevamente")
                                        }
                                    }
                                } else {
                                    throw Exception("No se recibio ninguna respuesta del servidor")
                                }
                            } else {
                                throw Exception("No se recibio ninguna respuesta del servidor")
                            }
                        } catch (e: Exception) {
                            throw Exception(e.message)
                        }
                    } //se obtiene la respuesta del servidor

                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: SEGUNDO No hay productos agregados al pedido.")
            print(e.message)
        }
    } //funcion que envia el pedido a la bd





    private fun ConfirmarPedido(idpedido: Int, idservidor: Int) {
        val bd = db!!.writableDatabase
        try {
            bd!!.execSQL("UPDATE pedidos set Id_pedido_sistema=$idservidor,Enviado=1,Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
    } //actualiza el pedido y confirma que se envio

    private fun ConfirmarDetallePedido(): Int {
        val bd = db!!.writableDatabase
        var cantidadDetallepedido = 0.toInt()
        try {
            val cursor = bd!!.rawQuery(
                "select count(id_pedido) as cantidad from detalle_pedidos where id_pedido = ${idpedido}",
                null
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                cantidadDetallepedido = cursor.getInt(0)
                cursor.close()
            } else {
                throw Exception("Error al buscar productos del pedidos.")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.close()
        }
        return cantidadDetallepedido
    } //actualiza el pedido y confirma que se envio

    private fun convertToJson(pedido: CabezeraPedidoSend, idpedido_param: Int): JsonObject {

        var idvisita_v = 0.toInt()

        val base = db!!.writableDatabase
        try {
            var cursor = base!!.rawQuery(
                "select v.Idvisita from visitas v inner join pedidos p on v.id = p.idvisita where p.id = ${idpedido_param}",
                null
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                idvisita_v = cursor.getInt(0)
                cursor.close()
            } else {
                throw Exception("Error al obtener código de cliente")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

        var json = JsonObject()
        json.addProperty("Idcliente", pedido.Idcliente)
        json.addProperty("Cliente", pedido.Cliente)
        json.addProperty("Subtotal", pedido.Subtotal)
        json.addProperty("Descuento", pedido.Descuento)
        json.addProperty("Total", pedido.Total)
        json.addProperty("Envidado", false)
        json.addProperty("Cerrado", false)
        json.addProperty("Idvendedor", pedido.Idvendedor)
        json.addProperty("Vendedor", pedido.Vendedor)
        json.addProperty("Idapp", idvisita_v)
        //se ordena la cabezera
        var detalle = JsonArray()
        for (i in 0..(pedido.detalle!!.size - 1)) {
            val data = pedido.detalle!!.get(i)
            var d = JsonObject()
            d.addProperty("Id", data.Id)
            d.addProperty("Id_pedido", data.Id_pedido)
            d.addProperty("Id_producto", data.Id_producto)
            d.addProperty("Codigo", data.Codigo)
            d.addProperty("Descripcion", data.Descripcion)
            d.addProperty("Costo", data.Costo)
            d.addProperty("Costo_iva", data.Costo_iva)
            d.addProperty("Precio", data.Precio)
            d.addProperty("Precio_iva", data.Precio_iva)
            d.addProperty("Precio_u", data.Precio_u)
            d.addProperty("Precio_u_iva", data.Precio_u_iva)
            d.addProperty("Cantidad", data.Cantidad)
            d.addProperty("Precio_venta", data.Precio_venta)
            d.addProperty("Unidad", data.Unidad)
            d.addProperty("Subtotal", data.Subtotal)
            d.addProperty("Total", data.Total)
            d.addProperty("Bonificado", data.Bonificado)
            d.addProperty("Descuento", data.Descuento)
            d.addProperty("Precio_editado", data.Precio_editado)
            d.addProperty("Idunidad", data.Idunidad)
            d.addProperty("Idtalla", data.Id_talla)
            detalle.add(d)
        }
        json.add("detalle", detalle)
        return json

    }
    //convierte el pedido a json

    private fun getEstadoVisita(): Int {
        var visitaAbierta = 1

        val base = db!!.writableDatabase
        try {
            val cursor = base!!.rawQuery("select Abierta from visitas where id = ${idvisita}", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                visitaAbierta = cursor.getInt(0)
                cursor.close()
            } else {
                //throw Exception("Error al obtener código de cliente")
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }

        return visitaAbierta
    }

}