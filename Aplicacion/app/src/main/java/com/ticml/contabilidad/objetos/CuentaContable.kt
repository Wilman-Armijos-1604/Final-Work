package com.ticml.contabilidad.objetos

class CuentaContable(
    var nivel1:String=null!!,
    var nivel2:String=null!!,
    var nivel3:String=null!!,
    var nivel4:String=null!!,
    var nivel5:String=null!!,
    var auxNiveles:String=null!!
) {

    override fun toString(): String {
        return nivel1+" - "+nivel2+" - "+nivel3+" - "+":\n"+nivel4+" - "+nivel5
    }

}