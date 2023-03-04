package com.ticml.contabilidad.objetos

class TransaccionContable(
    var idPeriodo:String?=null,
    var fecha:String?=null,
    var cuentaOrganizacion:String?=null,
    var cliente:String?=null,
    var totalIngresos:Double?=0.0,
    var totalEgresos:Double?=0.0,
    var saldo:Double?=0.0,
    var fechaFiltro:String?=null,
    var clienteFechaFiltro:String?=null,
    var cuentaContableFechaFiltro:String?=null,
    var clienteCuentaContableFechaFiltro:String?=null
) {
}