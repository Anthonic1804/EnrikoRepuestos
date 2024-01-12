package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.controllers.ClientesControllers
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityClientesDetalleBinding
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private var clienteController = ClientesControllers()

    private var busquedaPedido: Boolean = false
    private var visita = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = Database(this)
        idcliente = intent.getIntExtra("idcliente", 0)
        busquedaPedido = intent.getBooleanExtra("busqueda", false)
        visita = intent.getBooleanExtra("visita", false)

        funciones = Funciones()

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStart() {
        super.onStart()

        obtenerDatosCliente()

        binding.imgatras.setOnClickListener {
            Regresar()
        }

        binding.btnPagare.setOnClickListener {
            pagare()
        }
    }

    //FUNCION PARA OBTENER LOS DATOS DEL CLIENTE
    private fun obtenerDatosCliente(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (idcliente > 0) {
                    val data = clienteController.obtenerInformacionCliente(this@ClientesDetalle ,idcliente)
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
                withContext(Dispatchers.Main){
                    Toast.makeText(this@ClientesDetalle, "ERROR AL CARGAR LOS DATOS DEL CLIENTE", Toast.LENGTH_LONG).show()
                }
            }
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

        intent.putExtra("busqueda", true)
        intent.putExtra("visita", true)

        startActivity(intent)
        finish()
    }
    private fun Regresar() {
        if(visita){
            val intento = Intent(this, Clientes::class.java)
            intento.putExtra("busqueda", true)
            intento.putExtra("visita", true)
            startActivity(intento)
            finish()
        }else{
            val intento = Intent(this, Clientes::class.java)
            startActivity(intento)
            finish()
        }

    }

}