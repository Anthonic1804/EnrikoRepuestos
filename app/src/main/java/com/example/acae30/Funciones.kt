package com.example.acae30

import android.Manifest
import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.acae30.database.Database
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Funciones {

    private var alert: AlertDialogo? = null
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA OBTENER UN TIMESTAMP
    fun getFechaHoraProceso(): String?{
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
            /*TIMESTAMP -> yyyy-MM-dd'T'HH:mm:ss*/
        )
        val date = Date()
        return dateFormat.format(date)
    }

    //OBTENIENDO LA FECHA CON EL FORMATO CORRECTO
    fun obtenerFecha(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    //FUNCION PARA VERIFICAR LA CONEXION A INTERNET
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET])
    fun isInternetAvailable(context: Context): Boolean {
        if (isNetworkAvailable(context)) {
            try {
                val httpConnection: HttpURLConnection = URL("https://clients3.google.com/generate_204")
                    .openConnection() as HttpURLConnection
                httpConnection.setRequestProperty("User-Agent", "Android")
                httpConnection.setRequestProperty("Connection", "close")
                httpConnection.connectTimeout = 1500
                httpConnection.connect()

                return httpConnection.responseCode == 204
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    //FIN FUNCION PARA VERIFICAR LA CONEXION A INTERNET

    //FUNCION PARA OBTENER LA INSTANCIA DE LA BASE DE DATOS
    fun getDataBase(context: Context): Database {
        return Database(context)
    }

    //FUNCION PARA OBTENER EL SERVIDOR
    fun getServidor(ip: String?, puerto: String?): String {
        return "http://${ip}:${puerto}/"
    }

    //FUNCION DE MENSAJE DE ERROR
    fun mostrarAlerta(mensaje: String, context: Context, view: View){
        val alert: Snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
        alert.show()
    }

    //FUNCION DE MENSAJE OK
    fun mostrarMensaje(mensaje: String, context: Context, view: View){
        val alert: Snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.btnVerde))
        alert.show()
    }

    //MENSANJE ASINCRONO
    fun messageAsync(mensaje: String) {
        if (alert != null) {
                alert!!.changeText(mensaje)
        }
    }

    /**
     * FUNCIONES DE VALIDACION DE LAS RESPUESTAS DEL WEBSERVICE
     */
    fun validate(parametro: String?): String {
        if (parametro != null && parametro.length > 0) {
            return parametro.trim()
        } else {
            return ""
        }
    }

    fun validate(parametro: Int?): Int {
        return parametro ?: 0
    }

    fun validate(parametro: Float?): Float {
        return parametro ?: 0.00.toFloat()
    }

    fun validateJsonIsNullInt(json: JSONObject, campo: String): Int {
        return if (json.isNull(campo)) {
            0
        } else {
            json.getInt(campo)
        }
    }

    fun validateJsonIsNullFloat(json: JSONObject, campo: String): Float {
        return if (json.isNull(campo)) {
            0.toFloat()
        } else {
            json.getString(campo).toFloat()
        }
    }

    fun validateJsonIsnullString(json: JSONObject, campo: String): String {
        return if (json.isNull(campo)) {
            ""
        } else {
            json.getString(campo).trim()
        }
    }
    /**
     * FIN DE FUNCIONES DE VALIDACION
     */


    /**
     * FUNCION DE ANIMACION CIRCULAR PARA LOS RECYCLER VIEW
     */
    fun AnimacionCircularReavel(view: View) {
        val centerx = 0
        val centery = 0
        val starRadius = 0.00
        val endRadius = Math.max(view.width, view.height)
        val animacion: Animator? =
            ViewAnimationUtils.createCircularReveal(
                view, centerx, centery, starRadius.toFloat(),
                endRadius.toFloat()
            )
        view.visibility = View.VISIBLE
        animacion!!.start()

    }

    /**
     * FUNCION DE VALIDACION DE CONEXION DEL VENDEDOR
     */
    fun VendedorVerific(context: Context) {
        val instancia = "CONFIG_SERVIDOR"
        val preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        if (!preferencias.contains("ip") || !preferencias.contains("puerto")) {
            val editor = preferencias.edit().clear().commit() //elimina los datos
            val intento = Intent(context, MainActivity::class.java)
            context.startActivity(intento)
        } else if (!preferencias.contains("Idvendedor") || !preferencias.contains("Vendedor")) {
            val intento = Intent(context, Login::class.java)
            context.startActivity(intento)
        } else {

        }
    }


    //FUNCION PARA ELIMINAR INFORMACION DE LAS TABLAS PRINCIPALES AL CERRAR SESSION
    fun eliminarInformacion(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val db = getDataBase(context).writableDatabase
        try {
            db.execSQL("DELETE FROM Inventario")
            db.execSQL("DELETE FROM hoja_carga")
            db.execSQL("DELETE FROM hoja_carga_detalle")
            db.execSQL("DELETE FROM clientes")
            db.execSQL("DELETE FROM cliente_sucursal")
            db.execSQL("DELETE FROM cliente_precios")
            db.execSQL("DELETE FROM inventario_precios")
            //db.execSQL("DELETE FROM pedidos")
            //db.execSQL("DELETE FROM detalle_pedidos")
        }catch (e:Exception){
            throw Exception("ERROR AL ELIMINAR LA INFORMACION AL CERRAR SESSION -> " + e.message)
        }finally {
            db.close()
        }

        val editor = preferences.edit()
        editor.remove("hojaCarga")
        editor.apply()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensaje(context: Context, mensaje: String){
        val dialog = AlertDialog.Builder(context)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

}