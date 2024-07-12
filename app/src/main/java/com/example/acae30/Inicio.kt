package com.example.acae30

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.acae30.controllers.InventarioController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityInicioBinding
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
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

@Suppress("DEPRECATION")
class Inicio : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityInicioBinding

    private var funciones: Funciones? = null
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var database: Database? = null

    //VARIABLES PARA UN SLIDE MENU
    private lateinit var  drawerLayout: DrawerLayout
    private lateinit var toogle: ActionBarDrawerToggle
    private var generaToken: Int = 0

    private var ip = ""
    private var puerto = 0

    private var inventarioController = InventarioController()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
       /* if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(this, "Vuelve a presionar para salir", Toast.LENGTH_SHORT).show()
        }
        tiempoPrimerClick = System.currentTimeMillis()*/
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        funciones = Funciones()
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        funciones!!.VendedorVerific(this) //valida que haya sesion y que haya configuracion
        database = Database(this)

        ip = preferencias!!.getString("ip", "").toString()
        puerto = preferencias!!.getInt("puerto", 0)
        generaToken = preferencias!!.getInt("generaToken", 0)

        //FUNCION PARA OBTENER LA FECHA DEL INVENTARIO
        /*CoroutineScope(Dispatchers.IO).launch {
            inventarioController.obtenerFechaInventario(this@Inicio)
        }*/

        val fechaInventario: String? = preferencias!!.getString("fechaInventario", "NULL")
        binding.includeBar.lblupdate.text = fechaInventario

        //PRUEBA DE COMPARACION DE FECHA
        if(inventarioController.verificarFechaInventario(this@Inicio)){
            println("LA FECHA ES IGUAL")
        }else{
            println("LA FECHA ES DIFERENTE")
        }

        val nombre_vendedor =  preferencias!!.getString("Vendedor", "")
        binding.includeBar.txtvendedor.text = "BIENVENIDO ${nombre_vendedor?.trim()}"

        //IMPLEMENTANDO MENU SLIDE
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toogle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toogle)
        supportActionBar?.hide()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //OCULTANDO EL MENU TOKEN
        if(generaToken == 0){
            navigationView.menu.setGroupVisible(R.id.group_admin, false)
        }

        //FIN DE LA IMPLEAMENTACION DEL MENU

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_PHONE_STATE
                ), 101
            )
        }

        // COMPROBAR SI AUN ES VALIDO EL INICIO DE SESION
        if (isConnected()) {
            GlobalScope.launch(Dispatchers.IO) {
                comprobarSesion()
            }
        }


    } //relacianomos los widgets y inicializamos la variables

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        menu() //llama los botones de menu
        funciones!!.VendedorVerific(this) //valida que haya sesion
    }

    private fun menu() {
        with(binding.includeBar){
            cvData.setOnClickListener {
                val intento = Intent(this@Inicio, carga_datos::class.java)
                startActivity(intento)
                finish()
            }

            cvconfig.setOnClickListener {
                val intento = Intent(this@Inicio, Configuracion::class.java)
                startActivity(intento)
                finish()
            }
            cvcliente.setOnClickListener {
                val intento = Intent(this@Inicio, Clientes::class.java)
                startActivity(intento)
                finish()
            }
            cvinventario.setOnClickListener {
                val intento = Intent(this@Inicio, Inventario::class.java)
                startActivity(intento)
                finish()
            }
            cvpedido.setOnClickListener {
                val intento = Intent(this@Inicio, Pedido::class.java)
                intento.putExtra("proviene", "inicio")
                startActivity(intento)
                finish()
            }
            cvcuentas.setOnClickListener {
                val intento = Intent(this@Inicio, Cuentas_list::class.java)
                intento.putExtra("cuentas", true)
                startActivity(intento)
                finish()
            }
        }
    }//acciones de los botones del menu

    private fun salir(){
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alert_cerrar_sesion_usuario)
        dialogo.findViewById<Button>(R.id.btncerrar).setOnClickListener {
            //FUNCION PARA ALIMINAR LA INFORMACION DE TABLAS MAESTRAS
            /*CoroutineScope(Dispatchers.IO).launch {
                funciones!!.eliminarInformacion(this@Inicio)
            }

            val editor = preferencias!!.edit()
            editor.putString("fechaInventario", "NULL")
            editor.apply()*/

            updateSesionServer()
            cerrarSesion()
            dialogo.dismiss()
        }
        dialogo.findViewById<Button>(R.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.show()
    }

    private fun comprobarSesion() {
        val usuario = preferencias!!.getString("Usuario", "")
        val identidad = preferencias!!.getString("Identidad", "")

        val comprobarEstado = comprobarEstado(usuario!!, identidad!!)

        if (comprobarEstado == "Invalido") {
            // BORRAR TODOS LOS DATOS
            cerrarSesion()
        }

    } // COMPROBAR ESTADO DE LA SESION Y REALIZAR ACCIONES NECESARIAS

    private fun comprobarEstado(usuario: String, identidad: String): String {
        var respuestaVal = ""
        try {
            val credenciales = com.example.acae30.modelos.JSONmodels.LoginComprobar(
                usuario.uppercase()
            ) //se crea el modelo con los datos    que se enviaran

            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login/comprobar"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 5000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()
                                val res: JSONObject =
                                    JSONObject(respuesta.toString()) //obtenemos la respuesta del servidor y la pasamos a json
                                if (res.length() > 0) {
                                    if (!res.isNull("error") && !res.isNull("response")) {
                                        val coderror: Int = res.getInt("error")
                                        val response: String = res.getString("response")
                                        val identidad_param: String = res.getString("identidad")
                                        val estado: String = res.getString("estado").trim()

                                        when (response) {
                                            "Credenciales validas" -> respuestaVal =
                                                "Valido" //Estado para ingresar a la primera
                                            "Clave o Usuario Invalidos" -> respuestaVal =
                                                "Invalido" // Usuario y contraseña no validos
                                        }

                                        if (respuestaVal == "Valido") {
                                            if (estado == "INACTIVO") {
                                                respuestaVal = "Invalido"
                                            }

                                            if (estado == "ACTIVO") {
                                                if (identidad != identidad_param) {
                                                    respuestaVal = "Invalido"
                                                }
                                            }
                                        }
                                    } else {
                                        throw Exception("Error al procesar la solicitud")
                                    }//valido si el json contiene esas variables
                                } else {
                                    throw Exception("error de comunicacion")
                                }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }//buffer donde se obtiene la respuesta del servidor
                    } else if (responseCode == 204) {
                        throw Exception("No se han recibido parametros")
                    } else {
                        throw Exception("Error de comunicacion con el servidor")
                    } //valido que la respuesta sea la correcta

                } catch (e: Exception) {
                    if (e.message.toString() == "Host unreachable") {
                        runOnUiThread {
                            //AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            respuestaVal = "Error"
            //throw Exception(e.message)
        }
        return respuestaVal

    } // CONSULTAR ESTADO EN EL SERVIDOR DE LA SESION

    private fun cerrarSesion() {
        //SE ELIMINO LA FUNCION DE ELIMINADO DE DATOS DE LA TABLA PEDIDOS Y PEDIDOS_DETALLE
        //22/04/2023
        // BORRAR DATOS DE SESION Y SALIR A LA PANTALLA DE LOGIN
        val editor = preferencias!!.edit()
        editor.putInt("Idvendedor", 0)
        editor.putString("Vendedor", "")
        editor.putString("Usuario", "")
        editor.putString("Identidad", "")
        editor.putInt("generaToken", 0)
        editor.putBoolean("sesion", false)
        editor.apply()

        val intento = Intent(this, Login::class.java)
        startActivity(intento)
        finish()
    } // ELIMINAR TODOS LOS DATOS Y CIERRA SESION

    private fun updateSesionServer() {
        val usuario = preferencias!!.getString("Usuario", "")
        var identidad = preferencias!!.getString("Identidad", "")
        try {
            val credenciales = com.example.acae30.modelos.JSONmodels.Logout(
                usuario!!.uppercase(),
                identidad!!
            ) //se crea el modelo con los datos    que se enviaran

            val objecto =
                Gson().toJson(credenciales) //Transformo la data clas a un objecto json para enviarlo
            val ruta: String = "http://$ip:$puerto/login/logout"
            val url = URL(ruta) //creacion del objecto url.
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 5000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()
                                val res: JSONObject =
                                    JSONObject(respuesta.toString()) //obtenemos la respuesta del servidor y la pasamos a json
                                if (res.length() > 0) {
                                    if (!res.isNull("error") && !res.isNull("response")) {
                                        val coderror: Int = res.getInt("error")
                                        val response: String = res.getString("response")
                                    } else {
                                        throw Exception("Error al procesar la solicitud")
                                    }//valido si el json contiene esas variables
                                } else {
                                    throw Exception("error de comunicacion")
                                }
                            } catch (e: Exception) {
                                throw Exception(e.message)
                            }
                        }//buffer donde se obtiene la respuesta del servidor
                    } else if (responseCode == 204) {
                        throw Exception("No se han recibido parametros")
                    } else {
                        throw Exception("Error de comunicacion con el servidor")
                    } //valido que la respuesta sea la correcta

                } catch (e: Exception) {
                    if (e.message.toString() == "Host unreachable") {
                        runOnUiThread {
                            //AlertaEliminar()
                        }
                    } else {
                        throw Exception(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    } // CAMBIAR ESTADO EN EL SERVIDOR PARA CERRAR SESION

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        var isConnected = true

        isConnected = networkInfo != null && networkInfo.isConnected
        return isConnected
    } // VERIFICA LA CONEXION A WIFI O REDES


    //FUNCIONES PARA EL MENU DESPLEGABLE
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_pedido -> historicoPedidos()
            R.id.nav_token -> crearTokens()
            R.id.nav_salir -> salir()
        }
        //drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toogle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toogle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toogle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)

    }

    //REDIRECCIONES DEL MENU SLIDE
    private fun historicoPedidos(){
        val intent = Intent(this@Inicio, HistoricoPedidos::class.java)
        startActivity(intent)
        finish()
    }
    private fun crearTokens(){
        val intent = Intent(this@Inicio, PreciosAutorizados::class.java)
        startActivity(intent)
        finish()
    }

}