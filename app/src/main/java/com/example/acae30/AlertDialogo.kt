package com.example.acae30

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.LayoutInflater
import android.widget.TextView

class AlertDialogo(act: Activity) {

    lateinit var actividad: Activity
    lateinit var dialogo: Dialog

    //lateinit var textocarga:TextView
    init {
        actividad = act

    }

    fun Cargando() {
        val ale: AlertDialog.Builder = AlertDialog.Builder(actividad)
        val ly: LayoutInflater = actividad.layoutInflater
        ale.setView(ly.inflate(R.layout.alerta_carga, null))
        ale.setCancelable(true)
        dialogo = ale.create()

        dialogo.show()
    }

    fun dismisss() {
        dialogo.dismiss()
    }

    fun changeText(mensaje: String) {
        val textocarga = dialogo.findViewById<TextView>(R.id.txtcargando)
        if (textocarga != null) {
            textocarga.text = mensaje
        }
    }


}