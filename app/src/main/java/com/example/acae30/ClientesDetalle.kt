package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityClientesDetalleBinding
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClientesDetalle : AppCompatActivity() {

    private lateinit var binding: ActivityClientesDetalleBinding

    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""
    private var limiteCredito : Float = 0f
    private var plazo : Long = 0
    private var idcliente = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = Database(this)
        idcliente = intent.getIntExtra("idcliente", 0)
        funciones = Funciones()

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStart() {
        super.onStart()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (idcliente > 0) {
                    val data = getClient(idcliente)
                    if (data != null) {
                        with(binding){
                            txtcodigo.text = data.Codigo
                            txtnombre.text = data.Cliente
                            txtdirec.text = data.Direccion
                            txtdepa.text = data.Departamento
                            txtmunic.text = data.Municipio
                            txtestadocredito.text = data.Estado_credito
                            txtbalance.text = "$ " + data.Balance.toString()
                            txtlimite.text = "$ " + data.Limite_credito.toString()
                            txtplazo.text = data.Plazo_credito.toString() + " días"
                            txtDui.text = data.Dui
                            txtnit.text = data.Nit
                            txtnrc.text = data.Nrc
                            txtteluno.text = data.Telefono_1
                            txtteldos.text = data.Telefono_2
                        }
                        nombreCliente = data.Cliente.toString()
                        direccionCliente = data.Direccion.toString()
                        duiCliente = data.Dui.toString()
                        limiteCredito = data.Limite_credito!!
                        plazo = data.Plazo_credito!!.toLong()

                    }
                }

            } catch (e: Exception) {
               runOnUiThread{
                   Toast.makeText(this@ClientesDetalle, "ERROR AL CARGAR LOS DATOS DEL CLIENTE", Toast.LENGTH_LONG).show()
               }
            }
        }

        binding.imgatras.setOnClickListener {
            Regresar()
        }

        binding.btnPagare.setOnClickListener {
            pagare()
        }
    }

    fun getClient(id: Int): Cliente? {
        val base = bd!!.readableDatabase
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
                consulta.close()
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

    private fun pagare(){
        val intent = Intent(this, verPagare::class.java)
        intent.putExtra("idcliente", idcliente)
        intent.putExtra("nombreCliente", nombreCliente)
        intent.putExtra("direccionCliente", direccionCliente)
        intent.putExtra("duiCliente", duiCliente)
        intent.putExtra("limiteCredito", limiteCredito)
        intent.putExtra("plazoCredito", plazo)
        startActivity(intent)
        finish()
    }
    private fun Regresar() {
        val intento = Intent(this, Clientes::class.java)
        startActivity(intento)
        finish()
    }

}