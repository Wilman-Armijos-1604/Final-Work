package com.ticml.contabilidad.objetos

class TransaccionResumen(
    var idTransaccion:String?=null,
    var fecha:String?=null,
    var cuentaOrganizacion:String?=null,
    var cliente:String?=null,
    var totalIngresos:Double?=0.0,
    var totalEgresos:Double?=0.0,
    var saldo:Double?=0.0,
    var tipoTransaccion:String?=null,
){
}