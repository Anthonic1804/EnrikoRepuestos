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
        //BASE ACTUAL DE FERRETERIA EL REY : 11
        private const val DATABASE_VERSION = 1 //version de la base
        private const val DATABASE_NAME = "Acae.db" //nombre de la bd
    } //configuracion general de la bd

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(tbl!!.Cliente()) //ejecuta la tabla clientes
        //db.execSQL(tbl!!.virtualCliente()) //TABLA FTS4 VIRTUAL CLIENTE
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
       // db.execSQL(tbl!!.triggerClienteVirtual()) //TRIGGER DE INSERCION DE DATOS EN LA TABLA VIRTUAL CLIENTES
        db.execSQL(tbl!!.triggerInventarioVirtual())//TRIGGER PARA INSERCION DE DATOS EN LA TABLA VIRTUAL INVENTARIO
        db.execSQL(tbl!!.clienteSucursal()) //CREACION DE LA TABLA CLIENTES SUCURSALES -> 25/01/2023
        db.execSQL(tbl!!.configApp())//CREACION DE LA TABLA CONDIFURACION
        db.execSQL(tbl!!.insertConfig())//INSERTANDO LA VISTA POR DEFECTO DEL INVENTARIO
        db.execSQL(tbl!!.Empleados()) //CREANDO LA TABLA EMPLEADOS
        db.execSQL(tbl!!.Token()) // CREANDO LA TABLA TOKEN
        db.execSQL(tbl!!.VentasTemp())//CREANDO LA TABLA VENTAS TEMP
        db.execSQL(tbl!!.VentasDetalleTemp())//CREADNDO LA TABLA VENTAS DETALLE TEMP
        db.execSQL(tbl!!.ReporteTemp()) //TABLA TEMPORAL PARA EL REPORTE DE VENTAS

    } //funcion que crea la base de datos y sus tablas

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //ESTO SE VERIFICARA CADA VEZ QUE SE ACTUALICE LA APP
        // SE DEBERA CAMBIAR ESTA INFORMACION
        if(oldVersion < newVersion){
            //db?.execSQL(tbl!!.Empleados()) //CREANDO LA TABLA EMPLEADOS
            //db?.execSQL(tbl!!.Token()) //CREANDO LA TABLA TOKEN
            //db?.execSQL(tbl!!.VentasTemp())
            //db?.execSQL(tbl!!.VentasDetalleTemp())
        }
    }
}