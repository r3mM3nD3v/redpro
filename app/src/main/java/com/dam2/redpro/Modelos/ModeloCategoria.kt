package com.dam2.redpro.Modelos

class ModeloCategoria {

    var id : String = ""
    var categoria : String = ""
    var imagenUrl : String = ""

    constructor()

    constructor(id: String, categoria: String, imagenUrl: String) {
        this.id = id
        this.categoria = categoria
        this.imagenUrl = imagenUrl
    }


}