package com.ticml.contabilidad.objetos

class Cuenta(
    var organizacion: String?=null,
    var responsable: String?=null,
    var identificacion: String?=null,
    var tipo: String?=null,
    var idTipo: String?=null,
) {

    override fun toString(): String {
        return "${organizacion}: ${identificacion}"
    }
}