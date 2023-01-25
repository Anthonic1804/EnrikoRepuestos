package com.example.acae30

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Producto_detalle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventariodetalle)
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){
        //super.onBackPressed();

    }//anula el boton atras
}