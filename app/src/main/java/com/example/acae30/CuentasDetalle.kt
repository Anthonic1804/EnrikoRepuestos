package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.CuentasController
import com.example.acae30.databinding.ActivityCuentasDetalleBinding
import com.example.acae30.listas.CuentaAdapter
import com.example.acae30.modelos.Cuenta
import kotlinx.android.synthetic.main.activity_firmar_pagare.view.clear
import kotlinx.coroutines.launch

class CuentasDetalle : AppCompatActivity() {
    private var idcliente = 0
    private var nombrecliente = ""


    private lateinit var binding: ActivityCuentasDetalleBinding
    private val cuentasController = CuentasController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentasDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente").toString()

        binding.txtcliente.text = nombrecliente

        binding.imgatras.setOnClickListener {
            Regresar()
        }

        binding.btnVencidas.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas_vencidas)
            cargarLista("Vencidas")
        }

        binding.btnVigentes.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas_vigentes)
            cargarLista("Vigentes")
        }

        binding.btnTodas.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas)
            cargarLista("Todas")
        }
    }

    override fun onStart() {
        super.onStart()
        cargarLista("Todas")
    }

    //FUNCION PARA CARGAR LA LISTA DE CXC EN EL RECICLERVIEW
    private fun cargarLista(filtro: String){
        this@CuentasDetalle.lifecycleScope.launch {
            try {
                val data = cuentasController.obtenerCxCporIdCliente(idcliente, this@CuentasDetalle, filtro)
                if (data.size > 0) {
                    binding.rvlista.visibility = View.VISIBLE
                    list(data)
                }else{
                    binding.rvlista.visibility = View.GONE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CuentasDetalle, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }

    private fun Regresar() {
        val intento = Intent(this, Cuentas_list::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA ARMAR LA LISTA DE CXC
    private fun list(list: ArrayList<Cuenta>) {
        try {
            if (list.size > 0) {
                val mLayoutManager =
                    LinearLayoutManager(this@CuentasDetalle, LinearLayoutManager.VERTICAL, false)
                binding.rvlista.layoutManager = mLayoutManager
                var t = 0.toFloat()
                for (i in 0 until list.size) {
                    val data = list[i]
                    t += data.Saldo_actual!!
                }
                binding.txttotal.text = "$ " + String.format("%.2f", t)
                val adapter = CuentaAdapter(list, this@CuentasDetalle)
                binding.rvlista.adapter = adapter

            }
        } catch (e: Exception) {
            throw Exception("ERROR: AL ENCONTRAR LAS CXC -> " + e.message)
        }
    }
}