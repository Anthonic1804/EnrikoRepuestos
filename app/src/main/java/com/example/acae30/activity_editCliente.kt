package com.example.acae30

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.acae30.database.Database
import com.example.acae30.modelos.Cliente
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class activity_editCliente : AppCompatActivity() {

    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var nombre: TextInputEditText? = null
    private var direccion: TextInputEditText? = null
    private var tele_uno: TextInputEditText? = null
    private var tele_dos: TextInputEditText? = null
    private var nrc: TextInputEditText? = null
    private var nit: TextInputEditText? = null
    private var dui: TextInputEditText? = null
    private var codigo: TextView? = null

    private var btnatras: ImageButton? = null

    private var idCliente = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_cliente)

        bd = Database(this)
        idCliente = intent.getIntExtra("idCliente", 0)
        funciones = Funciones()

        codigo = findViewById(R.id.txtcodigo)
        nombre = findViewById<TextInputEditText>(R.id.txtname)
        btnatras = findViewById(R.id.imgatras)

    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch(Dispatchers.IO) {
           try {
               if (idCliente > 0) {
                    val data = getClient(idCliente)
                    if (data != null) {
                        codigo!!.text = data!!.Codigo
                        var n = data!!.Cliente
                        nombre!!.setText("$n")
                        //nombre!!.text = data!!.Cliente
                      //  direccion!!.text =  data.Direccion
                       // txtmuni!!.text = data.Municipio
                       // txtestadocredito!!.text = data.Estado_credito
                      //  txtbalance!!.text = "$ " + data.Balance.toString()
                      ////  txtlimite!!.text = "$ " + data.Limite_credito.toString()
                      //  txtplazo!!.text = data.Plazo_credito.toString() + " días"
                      //  txtnit!!.text = data.Nit
                      //  txtnrc!!.text = data.Nrc
                      //  txtteleuno!!.text = data.Telefono_1
                      //  txtteledos!!.text = data.Telefono_2
                    }
                }

            } catch (e: Exception) {
                /* runOnUiThread {
                      val alert: Snackbar =
                          Snackbar.make(lienzo!!, e.message.toString(), Snackbar.LENGTH_LONG)
                      alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                      alert.show()
                  }*/
            }
        }

        btnatras!!.setOnClickListener {
            regresar()
        }

    }


    private fun regresar(){
        val intento = Intent(this, ClientesDetalle::class.java)
        intento.putExtra("idcliente", idCliente)
        startActivity(intento)
        finish()
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


}