package com.dam2.redpro.Modelos

/**
 * Modelo que representa un producto dentro de una orden.
 * @property nombre Nombre del producto.
 * @property cantidad Cantidad de unidades del producto dentro de la orden.
 */
class ModeloProductoOrden {

    var nombre: String = ""
    var cantidad: Int = 0

    constructor()

    constructor(nombre: String, cantidad: Int) {
        this.nombre = nombre
        this.cantidad = cantidad
    }

    override fun toString(): String {
        return "ModeloProductoOrden(nombre='$nombre', cantidad=$cantidad)"
    }
}