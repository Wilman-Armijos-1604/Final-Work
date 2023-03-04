package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.InformacionCuentaTercero
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.Cuenta

class Rcv_CuentaTercero(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<Cuenta>
):RecyclerView.Adapter<Rcv_CuentaTercero.myVewHolder>() {
    inner class myVewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val organizacion:TextView
        val identificacion:TextView
        val tipoCuenta:TextView
        val responsable:TextView
        var idTipoCuenta:String
        init {
            organizacion = view.findViewById<TextView>(R.id.txv_organizacion)
            identificacion = view.findViewById<TextView>(R.id.txv_IdentificacionCuenta)
            tipoCuenta = view.findViewById<TextView>(R.id.txv_tipoCuenta)
            responsable = view.findViewById<TextView>(R.id.txv_responsableCuenta)
            idTipoCuenta = ""
            view.setOnClickListener(this)
        }

        override fun onClick(item: View?) {
            val intent = Intent(context, InformacionCuentaTercero::class.java)
            intent.putExtra("organizacion",organizacion.text.toString())
            intent.putExtra("identificacion",identificacion.text.toString())
            intent.putExtra("responsable",responsable.text.toString())
            intent.putExtra("tipoCuenta",tipoCuenta.text.toString())
            intent.putExtra("idTipoCuenta",idTipoCuenta)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myVewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_cuentas,parent,false)
        return myVewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myVewHolder, position: Int) {
        val opcion = list[position]
        holder.organizacion.text = opcion.organizacion
        holder.identificacion.text = opcion.identificacion
        holder.tipoCuenta.text = opcion.tipo
        holder.responsable.text = opcion.responsable
        holder.idTipoCuenta = opcion.idTipo.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}