package com.example.acae30.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION) {

    private var tbl: Tablas = Tablas()

    companion object {
        private const val DATABASE_VERSION = 1 //version de la base
        private const val DATABASE_NAME = "Acae.db" //nombre de la bd
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(tbl.cliente()) //ejecuta la tabla clientes
        db?.execSQL(tbl.clienteSucursal()) //CREACION DE LA TABLA CLIENTES SUCURSALES -> 25/01/2023
        db?.execSQL(tbl.clientePrecios()) //CREACION DE LA TABLA CLIENTES PRECIOS -> 19/03/2024
        db?.execSQL(tbl.inventario())
        db?.execSQL(tbl.inventarioPrecios())
        db?.execSQL(tbl.inventarioUnidades())
        db?.execSQL(tbl.virtualInventario()) //TABLA FTS4 VIRTUAL INVENTARIO
        db?.execSQL(tbl.hojaCarga())//TBL HOJA DE CARGA MAESTRO
        db?.execSQL(tbl.hojaCargaDetalle())//TBL HOJA DE CARGA DETALLE
        db?.execSQL(tbl.hojaDetalleRecargas())
        db?.execSQL(tbl.pedidos())
        db?.execSQL(tbl.cuentas())
        db?.execSQL(tbl.visitas())
        db?.execSQL(tbl.detallePedidos())
        db?.execSQL(tbl.vistaDetallePedidos())
        db?.execSQL(tbl.triggerInventarioVirtual())//TRIGGER PARA INSERCION DE DATOS EN LA TABLA VIRTUAL INVENTARIO
        db?.execSQL(tbl.empleados()) //CREANDO LA TABLA EMPLEADOS
        db?.execSQL(tbl.preciosAutorizados()) // CREANDO LA TABLA PRECIOS AUTORIZADOS
        db?.execSQL(tbl.ventasTemp())//CREANDO LA TABLA VENTAS TEMP
        db?.execSQL(tbl.ventasDetalleTemp())//CREADNDO LA TABLA VENTAS DETALLE TEMP
        db?.execSQL(tbl.reporteTemp()) //TABLA TEMPORAL PARA EL REPORTE DE VENTAS
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //ESTO SE VERIFICARA CADA VEZ QUE SE ACTUALICE LA APP
        // SE DEBERA CAMBIAR ESTA INFORMACION
        if(oldVersion < newVersion){
            //ACTUALIZADO TBL CLIENTE_SUCURSAL
            /*db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN Id_ruta INTEGER NOT NULL DEFAULT 0")
            db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN Ruta VARCHAR(50) NOT NULL DEFAULT ''")
            db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN DTECodDepto VARCHAR(10) NOT NULL DEFAULT ''")
            db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN DTECodMunicipio VARCHAR(10) NOT NULL DEFAULT ''")
            db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN DTECodPais VARCHAR(10) NOT NULL DEFAULT ''")
            db?.execSQL("ALTER TABLE cliente_sucursal ADD COLUMN DTEPais VARCHAR(50) NOT NULL DEFAULT ''")*/
        }
    }
}