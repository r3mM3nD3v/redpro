package com.dam2.redpro.Modelos

import android.net.Uri

/**
 * Modelo que representa una imagen seleccionada por el usuario o cargada desde Internet.
 * @property id Identificador único de la imagen (puede usarse como clave).
 * @property imagenUri URI local de la imagen (cuando se selecciona desde el dispositivo).
 * @property imagenUrl URL de la imagen en la nube o base de datos (cuando se carga desde Internet).
 * @property deInternet Indica si la imagen proviene de Internet (true) o del almacenamiento local (false).
 */
class ModeloImagenSeleccionada {

    var id: String = ""
    var imagenUri: Uri? = null
    var imagenUrl: String? = null
    var deInternet: Boolean = false

    constructor()

    constructor(id: String, imagenUri: Uri?, imagenUrl: String?, deInternet: Boolean) {
        this.id = id
        this.imagenUri = imagenUri
        this.imagenUrl = imagenUrl
        this.deInternet = deInternet
    }
}
