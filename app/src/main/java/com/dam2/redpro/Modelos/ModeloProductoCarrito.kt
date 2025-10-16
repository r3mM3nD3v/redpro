package com.dam2.redpro.Modelos

/**
 * Modelo que representa un producto añadido al carrito.
 * @property idProducto Identificador único del producto.
 * @property nombre Nombre del producto.
 * @property cantidad Cantidad de unidades del producto en el carrito.
 */
class ModeloProductoCarrito {

    var idProducto: String = ""
    var nombre: String = ""
    var cantidad: Int = 0

    constructor()

    constructor(
        idProducto: String,
        nombre: String,
        cantidad: Int
    ) {
        this.idProducto = idProducto
        this.nombre = nombre
        this.cantidad = cantidad
    }
}