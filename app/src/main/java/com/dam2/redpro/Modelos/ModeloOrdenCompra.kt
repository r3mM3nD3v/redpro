package com.dam2.redpro.Modelos

/**
 * Modelo que representa una orden de compra realizada por un usuario.
 * @property idOrden Identificador único de la orden.
 * @property ordenadoPor ID del usuario o comprador que generó la orden.
 * @property tiempoOrden Fecha/hora (en formato texto) en la que se realizó la orden.
 * @property estadoOrden Estado actual de la orden (pendiente, enviada, completada, etc.).
 */
class ModeloOrdenCompra {

    var idOrden: String = ""
    var ordenadoPor: String = ""
    var tiempoOrden: String = ""
    var estadoOrden: String = ""

    constructor()

    constructor(
        idOrden: String,
        ordenadoPor: String,
        tiempoOrden: String,
        estadoOrden: String
    ) {
        this.idOrden = idOrden
        this.ordenadoPor = ordenadoPor
        this.tiempoOrden = tiempoOrden
        this.estadoOrden = estadoOrden
    }
}