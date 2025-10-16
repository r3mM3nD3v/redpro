package com.dam2.redpro.Modelos

/**
 * Modelo que representa un usuario dentro del sistema.
 *
 * @property uid Identificador único del usuario en Firebase.
 * @property tipoUsuario Define el rol o tipo de usuario (por ejemplo: cliente, vendedor, administrador).
 * @property email Correo electrónico del usuario.
 * @property nombres Nombre completo del usuario.
 * @property dni Documento de identidad o código único de registro.
 * @property proveedor Puede representar el tipo de autenticación (e.g., "password", "google.com") o el rol de proveedor según la lógica de negocio.
 * @property direccion Dirección física o de residencia del usuario.
 * @property imagen URL de la imagen de perfil del usuario.
 */
class ModeloUsuario {

    var uid: String = ""
    var tipoUsuario: String = ""
    var email: String = ""
    var nombres: String = ""
    var dni: String = ""
    var proveedor: String = ""
    var direccion: String = ""
    var imagen: String = ""

    constructor()

    constructor(
        uid: String,
        tipoUsuario: String,
        email: String,
        nombres: String,
        dni: String,
        proveedor: String,
        direccion: String,
        imagen: String
    ) {
        this.uid = uid
        this.tipoUsuario = tipoUsuario
        this.email = email
        this.nombres = nombres
        this.dni = dni
        this.proveedor = proveedor
        this.direccion = direccion
        this.imagen = imagen
    }
}