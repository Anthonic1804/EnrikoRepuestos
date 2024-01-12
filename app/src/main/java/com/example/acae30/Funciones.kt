package com.example.acae30

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.acae30.database.Database
import com.example.acae30.modelos.Visitas
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Funciones {

    fun getFechaHoraProceso(): String?{
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            /*TIMESTAMP -> yyyy-MM-dd HH:mm:ss*/
        )
        val date = Date()
        return dateFormat.format(date)
    }

    fun isNetworkConneted(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {

                    return true
                }
            }
        }
        return false
    } //valida la conexion a internet

    fun validate(parametro: String?): String {
        if (parametro != null && parametro.length > 0) {
            return parametro.trim()
        } else {
            return ""
        }
    } //valida campos vacios o nulos dela api

    fun validate(parametro: Int?): Int {
        return parametro ?: 0
    }//devuelve un int correcto

    fun validate(parametro: Float?): Float {
        return parametro ?: 0.00.toFloat()
    }//devuelve un float

    fun validateBoolean(parametro: Boolean?): Boolean {
        return try {
            parametro ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun validateJsonIsNullInt(json: JSONObject, campo: String): Int {
        if (json.isNull(campo)) {
            return 0
        } else {
            return json.getInt(campo)
        }
    }

    fun validateJsonIsNullFloat(json: JSONObject, campo: String): Float {
        if (json.isNull(campo)) {
            return 0.toFloat()
        } else {
            return json.getString(campo).toFloat()
        }
    }

    fun validateJsonIsnullString(json: JSONObject, campo: String): String {
        if (json.isNull(campo)) {
            return ""
        } else {
            return json.getString(campo).trim()
        }
    } //valida si un json de tipo varchar viene nulo

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

    fun VendedorVerific(context: Context) {
        val instancia = "CONFIG_SERVIDOR"
        val preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        if (!preferencias.contains("ip") || !preferencias.contains("puerto")) {
            val editor = preferencias.edit().clear().commit() //elimina todo los datos
            val intento = Intent(context, MainActivity::class.java)
            context.startActivity(intento)
        } else if (!preferencias.contains("Idvendedor") || !preferencias.contains("Vendedor")) {
            val intento = Intent(context, Login::class.java)
            context.startActivity(intento)
        } else {

        }
    } //revisa si la app ya tiene la configuracion de conexion y si hay sesion iniciada

    fun GetVisita(idcliente: Int, bd: Database): Visitas? {
        val base = bd.writableDatabase
        try {
            var visita: Visitas? = null
            val cursor = base.rawQuery("SELECT * FROM visitas where Id_cliente=$idcliente", null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                visita = Visitas(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getInt(11) == 1,
                    cursor.getInt(12) == 1,
                    cursor.getInt(13) == 1
                )
                cursor.close()
            }
            return visita
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base!!.close()
        }
    }

    //FUNCION PARA OBTENER LA INSTANCIA DE LA BASE DE DATOS
    fun getDataBase(context: Context): Database {
        return Database(context)
    }

    //FUNCION DE MENSAJE DE ERROR
    fun mostrarAlerta(mensaje: String, context: Context, view: View){
        val alert: Snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
        alert.show()
    }



}