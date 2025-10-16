package com.dam2.redpro.Modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo genérico para representar respuestas HTTP del servidor.
 * @property message Mensaje de respuesta del servidor.
 * @property isSuccess Indica si la operación fue exitosa.
 * @property error Mensaje de error devuelto, si aplica.
 */
class ResponseHttp(

    @SerializedName("message") val message: String,
    @SerializedName("success") val isSuccess: Boolean,
    @SerializedName("error") val error: String

) {
    override fun toString(): String {
        return "ResponseHttp(message='$message', isSuccess=$isSuccess, error='$error')"
    }
}