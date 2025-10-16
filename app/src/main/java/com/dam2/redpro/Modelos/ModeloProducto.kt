package com.dam2.redpro.Modelos

/**
 * Modelo que representa un producto dentro del sistema.
 * @property id Identificador único del producto.
 * @property nombre Nombre del producto.
 * @property descripcion Descripción breve del producto.
 * @property categoria Categoría a la que pertenece el producto.
 */
class ModeloProducto {

    var id: String = ""
    var nombre: String = ""
    var descripcion: String = ""
    var categoria: String = ""

    constructor()

    constructor(
        id: String,
        nombre: String,
        descripcion: String,
        categoria: String,
    ) {
        this.id = id
        this.nombre = nombre
        this.descripcion = descripcion
        this.categoria = categoria
    }
}