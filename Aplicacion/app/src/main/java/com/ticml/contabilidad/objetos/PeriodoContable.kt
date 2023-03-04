package com.ticml.contabilidad.objetos

class PeriodoContable(
    var fechaInicio:String?=null,
    var fechaFin:String?=null,
    var estado:String?=null,
    var anio:Int?=0,
    var debe:Double?=0.0,
    var haber:Double?=0.0,
    var saldo:Double?=0.0,
) {
}