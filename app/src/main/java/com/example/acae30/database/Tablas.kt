package com.example.acae30.database

class Tablas {

    fun cliente(): String {
        return "CREATE TABLE clientes (" +
                "Id INTEGER  PRIMARY KEY NOT NULL," +
                "Codigo VARCHAR(25)  NOT NULL," +
                "Cliente varchar(200)  NULL," +
                "Dui varchar(50)  NULL," +
                "Nit VARCHAR(50)  NULL," +
                "Nrc VARCHAR(50)  NULL," +
                "Giro VARCHAR(200)  NULL," +
                "Categoria_cliente VARCHAR(50)  NULL," +
                "Terminos_cliente VARCHAR(100)  NULL," +
                "Plazo_credito INTEGER  NULL," +
                "Limite_credito NUMERIC(20,2)  NULL," +
                "Balance NUMERIC(20,2)  NULL," +
                "Estado_credito VARCHAR(50)  NULL," +
                "Direccion VARCHAR(200)  NULL," +
                "Municipio VARCHAR(70)  NULL," +
                "Departamento VARCHAR(70)  NULL," +
                "Telefono_1 VARCHAR(15)  NULL," +
                "Telefono_2 vARCHAR(15)  NULL," +
                "Correo VARCHAR(200)  NULL," +
                "Contacto VARCHAR(200)  NULL," +
                "Id_ruta INTEGER DEFAULT '''''''0''''''' NULL," +
                "Id_vendedor INTEGER DEFAULT '''''''0''''''' NULL," +
                "Vendedor VARCHAR(50)  NULL," +
                "Status VARCHAR(50)  NULL," +
                "Ultima_venta DATE  NULL," +
                "Aporte_mensual NUMERIC(20,2)  NULL," +
                "Fecha_inventario TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Firmar_pagare_app INTEGER NOT NULL DEFAULT 0," +
                "Persona_juridica VARCHAR(10) NOT NULL DEFAULT 'N'" +
                ");"
    } //tabla cliente

    //CREANDO TABLA CLIENTES_SUCURSALES 25-01-2023
    fun clienteSucursal(): String {

        return "CREATE TABLE cliente_sucursal(" +
                "Id INTEGER NOT NULL," +
                "id_cliente INTEGER NOT NULL," +
                "codigo_sucursal VARCHAR(25) NULL," +
                "nombre_sucursal VARCHAR(45) NULL," +
                "direccion_sucursal VARCHAR(200) NULL," +
                "municipio_sucursal VARCHAR(50) NULL," +
                "depto_sucursal VARCHAR(25) NULL," +
                "telefono_1 VARCHAR(15) NULL," +
                "telefono_2 VARCHAR(15) NULL," +
                "correo_sucursal VARCHAR(100) NULL," +
                "contacto_sucursal VARCHAR(50))"
    }

    //CREANDO TABLA PARA PRECIO PESONALIZADOS POR CLIENTE
    fun clientePrecios(): String {

        return "CREATE TABLE cliente_precios(" +
                "id_cliente INTEGER NOT NULL," +
                "id_inventario INTEGER NOT NULL," +
                "precio_p NUMERIC(18,6) NOT NULL," +
                "precio_p_iva NUMERIC(18,6) NOT NULL," +
                "bonificado NUMERIC(18,6) NOT NULL DEFAULT 0)"
    }

    fun inventario(): String {
        return "CREATE TABLE inventario (" +
                "Id INTEGER  PRIMARY KEY NOT NULL," +
                "Codigo VARCHAR(100)  NULL," +
                "Tipo VARCHAR(50)  NULL," +
                "Id_linea INTEGER  NULL," +
                "Linea VARCHAR(100)  NULL," +
                "Descripcion VaRCHAR(150)  NULL," +
                "Unidad_medida VARCHAR(25) NULL," +
                "Fraccion NUMERIC(20,6) NULL," +
                "Nombre_fraccion VARCHAR(50) NULL," +
                "Existencia NUMERIC(20,6)  NULL," +
                "Costo NUMERIC(20,6) NOT NULL," +
                "costo_iva NUMERIC(20,6) NOT NULL," +
                "Precio_oferta NUMERIC(12,2)  NULL," +
                "Precio_iva NUMERIC(20,6)  NULL," +
                "Precio NUMERIC(20,6)," +
                "Precio_u NUMERIC(20,6)," +
                "Precio_u_iva NUMERIC(20,6)," +
                "Status VARCHAR(50)  NULL," +
                "Fecha_inventario DATE DEFAULT CURRENT_DATE NOT NULL," +
                "Id_productor INTEGER  NULL," +
                "Productor VARCHAR(200)  NULL," +
                "Id_proveedor INTEGER DEFAULT '0' NULL," +
                "Proveedor VARCHAR(200)  NULL," +
                "Cesc varchar(1) not null," +
                "Combustible varchar(1) not null," +
                "Imagen TEXT NULL," +
                "Rubro VARCHAR(50)," +
                "Marca VARCHAR(50)," +
                "Id_sublinea INTEGER," +
                "Sublinea VARCHAR(50)," +
                "Desc_automatico NUMERIC(20,6)," +
                "Bonificado NUMERIC(20,6)," +
                "Id_rubro INTEGER," +
                "Existencia_u NUMERIC(20,6))"
    } //tabla inventario

    fun inventarioPrecios(): String {

        return "CREATE TABLE inventario_precios(" +
                "Id INTEGER NOT NULL," +
                "Id_inventario NOT NULL," +
                "Codigo_producto VARCHAR(25)DEFAULT '' ," +
                "Nombre VARCHAR(50) ," +
                "Terminos VARCHAR(20)," +
                "Plazo NUMERIC(18,0)," +
                "Unidad VARCHAR(25)NOT NULL," +
                "Cantidad NUMERIC(18,0) NOT NULL," +
                "Porcentaje NUMERIC(18,2)NOT NULL," +
                "Precio NUMERIC(18,6) NOT NULL," +
                "Precio_iva NUMERIC(18,6) NOT NULL," +
                "Id_inventario_unidad INTEGER NOT NULL DEFAULT 0)"
    } //tabla inventario precios

    fun inventarioUnidades(): String {

        return "CREATE TABLE inventario_unidades(" +
                "Id INTEGER NOT NULL," +
                "Id_inventario INTEGER NOT NULL," +
                "Nombre_unidad VARCHAR(25) NOT NULL," +
                "Equivale NUMERIC(18,2) NOT NULL," +
                "Unidades VARCHAR(3) NOT NULL)"
    } //tabla inventario unidades

    //CREANDO LA TABLA VIRTUAL INVENTARIO
    fun virtualInventario(): String {
        return "CREATE VIRTUAL TABLE virtualinventario USING FTS4 (" +
                "CONTENT='inventario'," +
                "Codigo," +
                "Descripcion" +
                ") "
    }

    //CREANDO TABLA HOJA DE CARGA
    fun hojaCarga():String{
        return "CREATE TABLE hoja_carga(" +
                "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numeroHoja INTEGER NOT NULL DEFAULT O," +
                "Fecha_registro DATE DEFAULT CURRENT_DATE)"
    }

    //CREADO TABLA HOJA DE CARGA DETALLE
    fun hojaCargaDetalle():String{
        return "" +
                "CREATE TABLE hoja_carga_detalle(" +
                "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Id_hojaCarga INTEGER NOT NULL DEFAULT 0," +
                "Id_inventario INTEGER NOT NULL DEFACULT O," +
                "Codigo_inventario VARCHAR(25) NOT NULL," +
                "Cantidad NUMERIC(20,6) NOT NULL DEFAULT O)"
    }

    //CREANDO LA TABLA EMPLEADOS
    fun empleados(): String {
        return "CREATE TABLE empleado(" +
                "id_empleado INTEGER NOT NULL," +
                "nombre_empleado VARCHAR(50) NOT NULL);"
    }

    //CREANDO LA TABLA TOKENAPP
    fun preciosAutorizados(): String {
        return "CREATE TABLE preciosAutorizados(" +
                "Id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "Id_vendedor INTEGER NOT NULL," +
                "Id_admin INTEGER NOT NULL," +
                "cod_producto VARCHAR(25) NOT NULL," +
                "precio_asig NUMERIC(18,4) NOT NULL," +
                "fecha_registrado TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "id_server INTEGER NOT NULL);"
    }

    /*ESCARRSA 15/05/2024
    * CAMBIOS EN LA TBL PEDIDOS -> SE AGREGARON LOS SIGUIENTES CAMPOS
    * PAGO, CAMBIO, SUMAS, IVA, SUBTOTAL, IVA_RETENIDO, IVA_PERCIBIDO
    * */
    fun pedidos(): String {
        return "CREATE TABLE [pedidos] (" +
                "Id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "Id_cliente INTEGER   NOT NULL DEFAULT 0," +
                "Nombre_cliente VARCHAR(100) NOT NULL," +
                "Pago NUMERIC(20,6) NULL DEFAULT 0," +
                "Cambio NUMERIC(20,6) NULL DEFAULT 0," +
                "Descuento NUMERIC(20,6) NULL DEFAULT 0," +
                "Sumas NUMERIC(20,6) NULL DEFAULT 0," +
                "Iva NUMERIC(20,6) NULL DEFAULT 0," +
                "SubTotal NUMERIC(20,6) NULL DEFAULT 0," +
                "Iva_retenido NUMERIC(20,6) NULL DEFAULT 0," +
                "Iva_percibido NUMERIC(20,6) NULL DEFAULT 0," +
                "Total NUMERIC(20,6)  NOT NULL," +
                "Enviado BOOLEAN DEFAULT 'FALSE' NOT NULL," +
                "Fecha_enviado TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Id_pedido_sistema INTEGER NOT NULL DEFAULT 0," +
                "Gps TEXT  NULL," +
                "Cerrado INTEGER NOT NUll default 0," +
                "Idvisita INTEGER NOT NULL DEFAULT 0," +
                "[Fecha_creado] TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Id_sucursal INTEGER NOT NULL DEFAULT 0," +
                "Codigo_sucursal VARCHAR(25) NOT NULL DEFAULT ''," +
                "Nombre_sucursal VARCHAR(45) NOT NULL DEFAULT ''," +
                "Tipo_documento VARCHAR(2) NULL DEFAULT 'FC'," +
                "Tipo_envio INTEGER NOT NULL DEFAULT 0," +
                "Terminos VARCHAR(25) NOT NULL);"
    }

    fun detallePedidos(): String {
        return "CREATE TABLE [detalle_pedidos] (" +
                "[Id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "[Id_pedido] INTEGER   NOT NULL," +
                "[Id_producto] INTEGER   NOT NULL," +
                "[Cantidad] NUMERIC(18,2) DEFAULT '0' NOT NULL," +
                "Unidad VARCHAR(10)," +
                "Idunidad INTEGER not null default 0," +
                "Precio NUMERIC(18,2) NOT NULL DEFAULT 0," +
                "Precio_iva NUMERIC(18,2)  NOT NULL DEFAULT 0," +
                "Total NUMERIC(18,2)  NOT NULL DEFAULT 0," +
                "Total_iva NUMERIC(18,2) NOT NULL DEFAULT 0," +
                "[Precio_oferta] NUMERIC(18,2)  NOT NULL," +
                "Bonificado Integer not null DEFAULT 0," +
                "Descuento Numeric(18,2)not null default 0," +
                "Precio_editado VARCHAR(10) DEFAULT '' NOT NULL," +
                "Id_talla INTEGER NOT NULL DEFAULT 0," +
                "Id_Inventario_Precios INTEGER NOT NULL DEFAULT 0," +
                "FOREIGN KEY(Id_pedido) REFERENCES pedidos(Id_pedido)" +
                ")"
    } //tabla detalle pedidos

    fun vistaDetallePedidos(): String {
        return "CREATE VIEW detalle_producto AS " +
                "SELECT detalle_pedidos.id," +
                    "detalle_pedidos.id_pedido," +
                    "detalle_pedidos.id_producto," +
                    "inventario.codigo," +
                    "inventario.descripcion," +
                    "inventario.costo," +
                    "inventario.costo_iva," +
                    "inventario.precio," +
                    "inventario.precio_iva," +
                    "inventario.precio_u," +
                    "inventario.precio_u_iva," +
                    "detalle_pedidos.cantidad," +
                    "detalle_pedidos.precio AS Precio_venta_siva," +
                    "detalle_pedidos.precio_iva AS precio_venta," +
                    "detalle_pedidos.total," +
                    "detalle_pedidos.total_iva," +
                    "detalle_pedidos.unidad," +
                    "detalle_pedidos.bonificado," +
                    "detalle_pedidos.descuento," +
                    "detalle_pedidos.precio_editado," +
                    "detalle_pedidos.idunidad " +
                "FROM detalle_pedidos " +
                "INNER JOIN inventario " +
                "ON inventario.Id = detalle_pedidos.Id_producto;"

    }

    //TABLA REPORTE TEMP
    fun reporteTemp(): String {
        return "CREATE TABLE reporteTemp(" +
                "Cliente VARCHAR(50) NOT NULL," +
                "Sucursal VARCHAR(50) NOT NULL," +
                "Total NUMERIC(20,6) NOT NULL)"
    }

    //TABLA VENTAS PARA HISTORICO DE PEDIDOS
    fun ventasTemp(): String {
        return "CREATE TABLE ventasTemp (" +
                "Id INTEGER NOT NULL," +
                "Fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Id_cliente INTEGER NOT NULL," +
                "Id_Sucursal INTEGER NOT NULL," +
                "Id_vendedor INTEGER NOT NULL," +
                "Vendedor VARCHAR(25) NOT NULL," +
                "Total NUMERIC(20,6) NOT NULL," +
                "Numero INTEGER NOT NULL);"
    }

    //DETALLE DE HISTORICO
    fun ventasDetalleTemp(): String {
        return "CREATE TABLE ventasDetalleTemp (" +
                "Id_venta INTEGER NOT NULL," +
                "Id_producto INTEGER NOT NULL," +
                "Producto VARCHAR(50) NOT NULL," +
                "Precio_u_iva NUMERIC(20,6) NOT NULL," +
                "Cantidad INTEGER NOT NULL," +
                "Total_iva NUMERIC(20,6) NOT NULL)"
    }

    fun cuentas(): String {
        return "CREATE TABLE cuentas(" +
                "Id INTEGER NOT NULL," +
                "Id_cliente INTEGER NOT NULL," +
                "Codigo_cliente VARCHAR(25) NULL," +
                "Documento VARCHAR(25) NOT NULL," +
                "Fecha DATE NOT NULL," +
                "Valor NUMERIC(18,2)," +
                "Abono_inicial NUMERIC(18,2)," +
                "Saldo_inicial NUMERIC(18,2)," +
                "Plazo NUMERIC(4,0)," +
                "Fecha_vencimiento DATE," +
                "Saldo_actual NUMERIC(18,2)," +
                "Fecha_ult_pago DATE," +
                "Valor_pago NUMERIC(18,2)," +
                "Relacionado VARCHAR(1)," +
                "Status VARCHAR(10)," +
                "Fecha_cancelado DATE)"

    } //tabla de cuentas

    fun visitas(): String {
        return "CREATE TABLE visitas (" +
                "Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "Id_cliente INTEGER NOT NULL DEFAULT 0," +
                "Nombre_cliente VARCHAR(100) NOT NULL," +
                "Gps_in varchar(250) null," +
                "Fecha_inicial TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Gps_out varchar(250) null," +
                "Fecha_final TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Idvisita INTEGER NOT NULL DEFAULT 0," +
                "Comentario VARCHAR(250) NOT NULL DEFAULT ''," +
                "Imagen_url VARCHAR(250) NOT NULL DEFAULT ''," +
                "Imagen VARCHAR(250) NOT NULL DEFAULT ''," +
                "Abierta BOOLEAN DEFAULT 'FALSE' NOT NULL," +
                "Enviado BOOLEAN DEFAULT 'FALSE' NOT NULL," +
                "Enviado_final BOOLEAN DEFAULT 'FALSE' NOT NULL" +
                ");"
    } //tabla de las visitas

    //TRIGGER PARA LA INSERCION DE DATOS EN TABLA FTS4 VIRTUAL INVENTARIO
    fun triggerInventarioVirtual(): String {
        return "CREATE TRIGGER triggerInventarioVirtual AFTER INSERT ON inventario BEGIN" +
                " INSERT INTO virtualinventario(virtualinventario) VALUES ('rebuild');" +
                "END;"

    }


}