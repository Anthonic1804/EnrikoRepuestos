package com.example.acae30

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_historico_pedidos.*

class HistoricoPedidos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_pedidos)

        etFecha1.setOnClickListener { calendarDialogDesde() }

        etFecha2.setOnClickListener { calendarDialogHasta() }

        imgRegresar.setOnClickListener { regresarInicio() }

    }

    private fun calendarDialogDesde() {
        val datePicker = DatePickerFragment{day, month, year -> onDateSelectedDesde(day, month, year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    private fun onDateSelectedDesde(day: Int, month:Int, year:Int){
        val mes = month + 1
        val date = "$year/$mes/$day"
        etFecha1.setText(date)
    }

    private fun calendarDialogHasta() {
        val datePicker = DatePickerFragment{day, month, year -> onDateSelectedHasta(day, month, year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }
    private fun onDateSelectedHasta(day: Int, month:Int, year:Int){
        val mes = month + 1
        val date = "$year/$mes/$day"
        etFecha2.setText(date)
    }

    private fun regresarInicio(){
        val intent = Intent(this@HistoricoPedidos, Inicio::class.java)
        startActivity(intent)
        finish()
    }
}