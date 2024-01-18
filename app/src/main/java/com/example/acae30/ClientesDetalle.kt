package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import kotlin.properties.Delegates

class ClientesDetalle : AppCompatActivity() {

    private lateinit var binding: ActivityClientesDetalleBinding

    private var bd: Database? = null
    private var funciones: Funciones? = null
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""
    private var nitCliente : String = ""
    private var personaJuridica : String = ""
    private var limiteCredito : Float = 0f
    private var plazo : Long = 0
    private var idcliente = 0

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var pagareFirmado : Boolean = false

    private var clienteController = ClientesControllers()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = Database(this)
        idcliente = intent.getIntExtra("idcliente", 0)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        pagareFirmado = preferences.getBoolean("PagareObligatorio", false)

        funciones = Funciones()

        obtenerDatosCliente()

    }

    override fun onStart() {
        super.onStart()

        binding.imgatras.setOnClickListener {
            Regresar()
        }

        binding.btnPagare.setOnClickListener {
            if (personaJuridica != "S"){
                if(duiCliente != "" && direccionCliente != ""){
                    pagare()
                }else{
                    funciones!!.mostrarAlerta("ERROR: DATOS INCOMPLETOS DEL CLIENTE", this@ClientesDetalle, binding.vista)
                }
            }else{
                if(nitCliente != "" && direccionCliente != ""){
                    pagare()
                }else{
                    funciones!!.mostrarAlerta("ERROR: DATOS INCOMPLETOS DEL CLIENTE", this@ClientesDetalle, binding.vista)
                }
            }
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
                        nitCliente = data.Nit.toString()
                        personaJuridica = data.Persona_juridica.toString()

                        //DETERINANDO EN MOSTRAR U OCULTAR EL BOTON PARA FIRMAR EL PAGARE
                        if(pagareFirmado){
                            if(data.Terminos_cliente.toString() == "Contado"){
                                binding.btnPagare.visibility = View.GONE
                            }
                        }else{
                            binding.btnPagare.visibility = View.GONE
                        }//FIN

                        if (personaJuridica != "S"){
                            binding.personaJuridica.text = "PERSONA NATURAL"
                        }else{
                            binding.personaJuridica.text = "PERSONA JURIDICA"
                        }
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
        startActivity(intent)
        finish()
    }
    private fun Regresar() {
        val intento = Intent(this, Clientes::class.java)
        startActivity(intento)
        finish()

    }

}