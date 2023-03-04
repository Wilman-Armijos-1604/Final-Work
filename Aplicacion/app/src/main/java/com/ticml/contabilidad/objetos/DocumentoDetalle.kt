package com.ticml.contabilidad.objetos

class DocumentoDetalle(
    var tipoDocumento: String?=null,
    var numeroDocumento: String?=null,
    var fechaEmision: String?=null,
    var identificacionEmisor: String?=null,
    var nombreEmisor: String?=null,
    var identificacionCliente: String?=null,
    var nombreCliente: String?=null,
    var subtotal: Double?=0.0,
    var iva: Double?=0.0,
    var total: Double?=0.0,
    var tipoDocumentoModifica: String?=null,
    var documentoModifica: String?=null,
    var empresaDocumentoModifica: String?=null
) {

    override fun toString(): String {
        return "${nombreEmisor} - ${tipoDocumento}: ${numeroDocumento}"
    }
}