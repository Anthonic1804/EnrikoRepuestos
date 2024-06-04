package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.modelos.Cliente

class CuentasController {

    val funciones = Funciones()

    //FUNCION PARA OBTENER LAS CXC POR CLIENTE
    fun obtenerCuentasPorCliente(cliente: String, context: Context): ArrayList<Cliente> {
        val base = funciones.getDataBase(context).readableDatabase
        val lista = ArrayList<Cliente>()
        try {
            val consulta = base.rawQuery("SELECT * FROM Clientes WHERE cliente LIKE '%$cliente%'", null)

            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {
                    val listado = Cliente(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getString(4),
                        consulta.getString(5),
                        consulta.getString(6),
                        consulta.getString(7),
                        consulta.getString(8),
                        consulta.getInt(9),
                        consulta.getFloat(10),
                        consulta.getFloat(11),
                        consulta.getString(12),
                        consulta.getString(13),
                        consulta.getString(14),
                        consulta.getString(15),
                        consulta.getString(16),
                        consulta.getString(17),
                        consulta.getString(18),
                        consulta.getString(19),
                        consulta.getInt(20),
                        consulta.getInt(21),
                        consulta.getString(22),
                        consulta.getString(23),
                        consulta.getString(24),
                        consulta.getFloat(25),
                        consulta.getInt(27),
                        consulta.getString(28)
                    )
                    lista.add(listado)

                } while (consulta.moveToNext())
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception("ERROR: NO SE ENCONTRARON CUENTAS -> " + e.message)
        } finally {
            base!!.close()
        }
        return lista
    }

    //FUNCION PARA OBTENER TODAS LAS CUENTAS EN LISTA
    fun obtenerTodaslasCxC(context: Context): ArrayList<Cliente>{
        val base = funciones.getDataBase(context).readableDatabase
        val lista = ArrayList<Cliente>()

        try {

            val consulta = base.rawQuery("SELECT DISTINCT * FROM clientes C " +
                    "INNER JOIN cuentas P " +
                    "ON C.id = P.id_cliente AND P.Status = 'PENDIENTE' " +
                    "GROUP BY C.id " +
                    "LIMIT 30 ", null)

            if (consulta.count > 0) {
                consulta.moveToFirst()
                do {

                    val listado = Cliente(
                        consulta.getInt(0),
                        consulta.getString(1),
                        consulta.getString(2),
                        consulta.getString(3),
                        consulta.getString(4),
                        consulta.getString(5),
                        consulta.getString(6),
                        consulta.getString(7),
                        consulta.getString(8),
                        consulta.getInt(9),
                        consulta.getFloat(10),
                        consulta.getFloat(11),
                        consulta.getString(12),
                        consulta.getString(13),
                        consulta.getString(14),
                        consulta.getString(15),
                        consulta.getString(16),
                        consulta.getString(17),
                        consulta.getString(18),
                        consulta.getString(19),
                        consulta.getInt(20),
                        consulta.getInt(21),
                        consulta.getString(22),
                        consulta.getString(23),
                        consulta.getString(24),
                        consulta.getFloat(25),
                        consulta.getInt(26),
                        consulta.getString(28)
                    )
                    lista.add(listado)

                } while (consulta.moveToNext())
                consulta.close()
            }
        } catch (e: Exception) {
            throw Exception("ERROR: AL OBTENER TODAS LAS CXC ->" + e.message)
        } finally {
            base.close()
        }
        return lista
    }

}