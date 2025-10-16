package com.dam2.redpro.Modelos

/**
 * Modelo que representa una imagen dentro de un carrusel o slider.
 * @property id Identificador único de la imagen en el slider.
 * @property imagenUrl URL pública o interna de la imagen mostrada.
 */
class ModeloImgSlider {

    var id: String = ""
    var imagenUrl: String = ""

    constructor()

    constructor(id: String, imagenUrl: String) {
        this.id = id
        this.imagenUrl = imagenUrl
    }
}