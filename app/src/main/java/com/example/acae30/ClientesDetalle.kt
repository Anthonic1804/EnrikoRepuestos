package com.example.acae30

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.acae30.database.Database
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClientesDetalle : AppCompatActivity() {
    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var txtcodigo: TextView? = null
    private var txtnombre: TextView? = null
    private var txtdirec: TextView? = null
    private var txtdepa: TextView? = null
    private var txtmuni: TextView? = null
    private var txtestadocredito: TextView? = null
    private var txtbalance: TextView? = null
    private var txtlimite: TextView? = null
    private var txtplazo: TextView? = null
    private var txtnit: TextView? = null
    private var txtnrc: TextView? = null
    private var txtteleuno: TextView? = null
    private var txtteledos: TextView? = null
    private var btnatras: ImageButton? = null
    private var btneditar: ImageButton? = null

    private var idcliente = 0

  //  private var lienzo: ConstraintLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes_detalle)
       // supportActionBar!!.title = "DETALLE DE CLIENTE"
        bd = Database(this)
        idcliente = intent.getIntExtra("idcliente", 0)
        funciones = Funciones()

        txtcodigo = findViewById(R.id.txtcodigo)
        txtnombre = findViewById(R.id.txtnombre)
        txtdirec = findViewById(R.id.txtdirec)
        txtdepa = findViewById(R.id.txtdepa)
        txtmuni = findViewById(R.id.txtmunic)
        txtestadocredito = findViewById(R.id.txtestadocredito)
        txtbalance = findViewById(R.id.txtbalance)
        txtlimite = findViewById(R.id.txtlimite)
        txtplazo = findViewById(R.id.txtplazo)
        txtnit = findViewById(R.id.txtnit)
        txtnrc = findViewById(R.id.txtnrc)
        txtteleuno = findViewById(R.id.txtteluno)
        txtteledos = findViewById(R.id.txtteldos)

        btnatras = findViewById(R.id.imgatras)
        btneditar = findViewById(R.id.imgeditar)

        btneditar!!.visibility = View.GONE // DESHABILITANDO EL BOTON EDITAR DETALLE DEL CLIENTE

      //  lienzo = findViewById(R.id.lienzo)
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (idcliente > 0) {
                    val data = getClient(idcliente)
                    if (data != null) {
                        txtcodigo!!.text = data.Codigo
                        txtnombre!!.text = data.Cliente
                        txtdirec!!.text =  data.Direccion
                        txtdepa!!.text = data.Departamento
                        txtmuni!!.text = data.Municipio
                        txtestadocredito!!.text = data.Estado_credito
                        txtbalance!!.text = "$ " + data.Balance.toString()
                        txtlimite!!.text = "$ " + data.Limite_credito.toString()
                        txtplazo!!.text = data.Plazo_credito.toString() + " días"
                        txtnit!!.text = data.Nit
                        txtnrc!!.text = data.Nrc
                        txtteleuno!!.text = data.Telefono_1
                        txtteledos!!.text = data.Telefono_2
                    }
                }

            } catch (e: Exception) {
              /*  runOnUiThread {
                    val alert: Snackbar =
                        Snackbar.make(lienzo!!, e.message.toString(), Snackbar.LENGTH_LONG)
                    alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                    alert.show()
                }*/
            }
        }

        btnatras!!.setOnClickListener {
            Regresar()
        }

        btneditar!!.setOnClickListener{
            editClient()
        }

    }

    fun getClient(id: Int): Cliente? {
        val base = bd!!.writableDatabase
        try {
            val consulta = base!!.rawQuery("SELECT * FROM clientes where Id=$id", null)
            var cliente: Cliente? = null
            if (consulta.count > 0) {
                consulta.moveToFirst()
                cliente = Cliente(
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
            }
            return cliente
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.close()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }
    private fun Regresar() {
        val intento = Intent(this, Clientes::class.java)
        startActivity(intento)
        finish()
    }

    private fun editClient(){
        val intento = Intent(this, activity_editCliente::class.java)
        intento.putExtra("idproducto", idcliente)
        startActivity(intento)
        finish()
    }

}