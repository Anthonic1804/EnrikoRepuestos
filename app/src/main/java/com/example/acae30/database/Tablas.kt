package com.example.acae30.database

class Tablas {

    fun Cliente(): String {
        val cliente = "CREATE TABLE clientes (" +
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
                "Fecha_inventario TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                ");"
        return cliente
    } //tabla cliente

    //CREANDO TABLA CLIENTES_SUCURSALES 25-01-2023
    fun clienteSucursal(): String{
        val clienteSucursal = "CREATE TABLE cliente_sucursal(" +
                "Id INTEGER NOT NULL," +
                "id_cliente INTEGER NOT NULL," +
                "codigo_sucursal VARCHAR(25) NULL," +
                "nombre_sucursal VARCHAR(25) NULL," +
                "direccion_sucursal VARCHAR(200) NULL," +
                "municipio_sucursal VARCHAR(50) NULL," +
                "depto_sucursal VARCHAR(25) NULL," +
                "telefono_1 VARCHAR(15) NULL," +
                "telefono_2 VARCHAR(15) NULL," +
                "correo_sucursal VARCHAR(100) NULL," +
                "contacto_sucursal VARCHAR(50))"

        return clienteSucursal
    }
    //CREANDO LA TABLA CONFIG DE LA APP
    fun configApp(): String{
        val configApp = "CREATE TABLE config(" +
                "vistaInventario INTEGER NOT NULL)"
        return  configApp
    }
    //INSERTANDO LA VISTA POR DEFECTO DEL INVENTARIO
    fun insertConfig():String{
        val insertConfig = "INSERT INTO config values(" +
                "2)"
        return insertConfig
    }

    //CREANDO TABLA VIRTUAL CLIENTES
    fun virtualCliente(): String{
        val virtualCliente = "CREATE VIRTUAL TABLE virtualcliente USING FTS4 (" +
                "CONTENT='clientes'," +
                "Cliente" +
        ") ";

        return virtualCliente
    }

    fun Inventario(): String {
        val inventario = "CREATE TABLE inventario (" +
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
        return inventario
    } //tabla inventario

    fun InventarioPrecios(): String {
        val tabla = "CREATE TABLE inventario_precios(" +
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

        return tabla
    } //tabla inventario precios

    fun InventarioUnidades(): String {

        val tabla = "CREATE TABLE inventario_unidades(" +
                "Id INTEGER NOT NULL," +
                "Id_inventario INTEGER NOT NULL," +
                "Nombre_unidad VARCHAR(25) NOT NULL," +
                "Equivale NUMERIC(18,2) NOT NULL," +
                "Unidades VARCHAR(3) NOT NULL)"
        return tabla
    } //tabla inventario unidades

    //CREANDO LA TABLA VIRTUAL INVENTARIO
    fun virtualInventario(): String{
        val virtualInventario = "CREATE VIRTUAL TABLE virtualinventario USING FTS4 (" +
                "CONTENT='inventario'," +
                "Descripcion" +
                ") ";

        return virtualInventario
    }

    fun Lineas(): String {
        val tabla = "CREATE TABLE lineas(" +
                "Id INTEGER NOT NULL," +
                "Nombre VARCHAR(50)," +
                "Mayoreo_detalle VARCHAR(1));"
        return tabla
    } //tabla lineas

    fun Rubros(): String {
        val tabla = "CREATE TABLE rubros(" +
                "Id INTEGER NOT NULL," +
                "Rubro VARCHAR(50)," +
                "Tipo VARCHAR(20));"
        return tabla
    } //tabla rubros

    fun Pedidos(): String {
        val pedido = "CREATE TABLE [pedidos] (" +
                "[Id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "[Id_cliente] INTEGER   NOT NULL DEFAULT 0," +
                "[Nombre_cliente] VARCHAR(100) NOT NULL," +
                "[Total] NUMERIC(20,6)  NOT NULL," +
                "Descuento NUMERIC(20,2)not null," +
                "[Enviado] BOOLEAN DEFAULT 'FALSE' NOT NULL," +
                "[Fecha_enviado] TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "Id_pedido_sistema INTEGER NOT NULL DEFAULT 0," +
                "[Gps] TEXT  NULL," +
                "Cerrado INTEGER NOT NUll default 0," +
                "[Idvisita] INTEGER NOT NULL DEFAULT 0," +
                "[Fecha_creado] TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                ");"
        return pedido
    } //tabla pedidos

    fun Cuentas(): String {
        val cuenta = "CREATE TABLE cuentas(" +
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
        return cuenta

    } //tabla de cuentas

    fun Visitas(): String {
        val tbl = "CREATE TABLE visitas (" +
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
        return tbl
    } //tabla de las visitas

    fun Detalle_pedidos(): String {
        val detalle = "CREATE TABLE [detalle_pedidos] (" +
                "[Id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "[Id_pedido] INTEGER   NOT NULL," +
                "[Id_producto] INTEGER   NOT NULL," +
                "[Cantidad] NUMERIC(18,2) DEFAULT '0' NOT NULL," +
                "Unidad VARCHAR(10)," +
                "Idunidad INTEGER not null default 0," +
                "[Precio] NUMERIC(18,2)  NOT NULL," +
                "[Precio_oferta] NUMERIC(18,2)  NOT NULL," +
                "[Subtotal] NUMERIC(18,2)  NOT NULL," +
                "Bonificado Integer not null DEFAULT 0," +
                "Descuento Numeric(18,2)not null default 0," +
                "Precio_editado VARCHAR(10) DEFAULT '' NOT NULL," +
                "Id_talla INTEGER NOT NULL DEFAULT 0," +
                "FOREIGN KEY(Id_pedido) REFERENCES pedidos(Id_pedido)" +
                ")"
        return detalle
    } //tabla detalle pedidos

    fun VistaDetallePedidos(): String {
        val vista = "CREATE VIEW detalle_producto AS " +
                "SELECT  detalle_pedidos.Id, " +
                "detalle_pedidos.Id_pedido," +
                "detalle_pedidos.Id_producto," +
                "inventario.Codigo," +
                "inventario.Descripcion," +
                "inventario.Costo," +
                "inventario.costo_iva," +
                "inventario.Precio," +
                "inventario.Precio_iva," +
                "inventario.Precio_u," +
                "inventario.Precio_u_iva," +
                "detalle_pedidos.Cantidad," +
                "detalle_pedidos.Precio as precio_venta," +
                "detalle_pedidos.Precio_oferta," +
                "detalle_pedidos.Subtotal," +
                "detalle_pedidos.Unidad," +
                "inventario.Unidad_medida," +
                "inventario.Nombre_fraccion," +
                "inventario.Cesc," +
                "inventario.Combustible," +
                "detalle_pedidos.Subtotal as Total," +
                "detalle_pedidos.Bonificado," +
                "detalle_pedidos.Descuento," +
                "detalle_pedidos.Precio_editado," +
                "detalle_pedidos.Idunidad," +
                "detalle_pedidos.Id_talla" +
                " FROM detalle_pedidos " +
                "INNER JOIN inventario  on inventario.Id=detalle_pedidos.Id_producto;"
        return vista
    }

    //TRIGGER PARA LA INSERCION DE DATOS EN TABLA FTS4 VIRTUAL CLIENTE
    fun triggerClienteVirtual(): String{

        val triggerCliente = "CREATE TRIGGER triggerClienteVirtual AFTER INSERT ON clientes BEGIN" +
                "  INSERT INTO virtualcliente(virtualcliente) VALUES ('rebuild');" +
                "END;";

        return triggerCliente
    }

    //TRIGGER PARA LA INSERCION DE DATOS EN TABLA FTS4 VIRTUAL INVENTARIO
    fun triggerInventarioVirtual(): String{

        val triggerInventario = "CREATE TRIGGER triggerInventarioVirtual AFTER INSERT ON inventario BEGIN" +
                " INSERT INTO virtualinventario(virtualinventario) VALUES ('rebuild');" +
                "END;";

        return triggerInventario

    }


}