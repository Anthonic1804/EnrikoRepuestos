package com.example.acae30.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var tbl: Tablas? = null

    init {
        tbl = Tablas()
    } //inicializamos la tabla  en el constructor

    companion object {
        private const val DATABASE_VERSION = 10 //version de la base
        private const val DATABASE_NAME = "Acae.db" //nombre de la bd
    } //configuracion general de la bd

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(tbl!!.Cliente()) //ejecuta la tabla clientes
        db.execSQL(tbl!!.virtualCliente()) //TABLA FTS4 VIRTUAL CLIENTE
        db.execSQL(tbl!!.Inventario())
        db.execSQL(tbl!!.InventarioPrecios())
        db.execSQL(tbl!!.InventarioUnidades())
        db.execSQL(tbl!!.virtualInventario()) //TABLA FTS4 VIRTUAL INVENTARIO
        db.execSQL(tbl!!.Lineas())
        db.execSQL(tbl!!.Rubros())
        db.execSQL(tbl!!.Pedidos())
        db.execSQL(tbl!!.Cuentas())
        db.execSQL(tbl!!.Visitas())
        db.execSQL(tbl!!.Detalle_pedidos())
        db.execSQL(tbl!!.VistaDetallePedidos())
        db.execSQL(tbl!!.triggerClienteVirtual()) //TRIGGER DE INSERCION DE DATOS EN LA TABLA VIRTUAL CLIENTES
        db.execSQL(tbl!!.triggerInventarioVirtual())//TRIGGER PARA INSERCION DE DATOS EN LA TABLA VIRTUAL INVENTARIO
        db.execSQL(tbl!!.clienteSucursal()) //CREACION DE LA TABLA CLIENTES SUCURSALES -> 25/01/2023

    } //funcion que crea la base de datos y sus tablas

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val tablas = arrayOf(
            "detalle_pedidos", "pedidos",
            "rubros", "lineas", "virtualinventario", "inventario_unidades", "inventario_precios",
            "inventario", "clientes", "cliente_sucursal", "virtualcliente", "cuentas", "visitas"
        )
        tablas.forEach { key ->
            val sql = "DROP TABLE IF EXISTS $key"
            db!!.execSQL(sql)
        }
        db!!.execSQL("DROP VIEW IF EXISTS detalle_producto")
        onCreate(db) //llama la funcion eliminar para crear la bd
    } //elimina los las tablas para actualizar la bd


}