package com.dam2.redpro.Modelos

/**
 * Modelo de datos para representar una categoría de productos.
 * @property id Identificador único de la categoría.
 * @property categoria Nombre de la categoría.
 * @property imagenUrl URL de la imagen asociada a la categoría.
 */
class ModeloCategoria() {

    var id: String = ""
    var categoria: String = ""
    var imagenUrl: String = ""

    /**
     * Constructor secundario que inicializa todos los campos.
     */
    constructor(id: String, categoria: String, imagenUrl: String) : this() {
        this.id = id
        this.categoria = categoria
        this.imagenUrl = imagenUrl
    }
}