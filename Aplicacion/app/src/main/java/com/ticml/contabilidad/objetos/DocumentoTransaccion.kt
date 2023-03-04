package com.ticml.contabilidad.objetos

class DocumentoTransaccion(
    var idDocumentoDetalle:String?=null,
    var idTransaccionDocumento:String?=null,
    var tipoDocumento:String?=null,
    var identificacionDocumento:String?=null,
    var tipoTransaccion:String?=null,
    var valor:Double?=null
    ) {

    override fun toString(): String {
        return "${tipoDocumento}: ${identificacionDocumento}"
    }
}