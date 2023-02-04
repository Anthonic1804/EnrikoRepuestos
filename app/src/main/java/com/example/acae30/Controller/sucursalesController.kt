package com.example.acae30.Controller

import com.example.acae30.database.Database
import com.example.acae30.modelos.Sucursales

class sucursalesController {

    private var dataBase: Database? = null

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE.
    //03-02-2023
    private fun getSucursales(idCliente:Int): ArrayList<Sucursales> {
        val dataBase = dataBase!!.writableDatabase
        val listaSucursales = ArrayList<Sucursales>()

        try {

            val dataSucursal = dataBase.rawQuery("SELECT * FROM cliente_sucursal WHERE id_cliente='$idCliente'", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                do{
                    val data = Sucursales(
                        dataSucursal.getInt(0),
                        dataSucursal.getInt(1),
                        dataSucursal.getString(2),
                        dataSucursal.getString(3),
                        dataSucursal.getString(4),
                        dataSucursal.getString(5),
                        dataSucursal.getString(6),
                        dataSucursal.getString(7),
                        dataSucursal.getString(8),
                        dataSucursal.getString(9),
                        dataSucursal.getString(10)
                    )
                    listaSucursales.add(data)
                }while (dataSucursal.moveToNext())
            }
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            dataBase!!.close()
        }
        return listaSucursales
    }

}