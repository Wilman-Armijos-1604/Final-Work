package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.DatoDocumento

class Rcv_DatoDocumento(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<DatoDocumento>
):RecyclerView.Adapter<Rcv_DatoDocumento.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val clave:TextView
        val valor:TextView
        init {
            clave = view.findViewById<TextView>(R.id.txv_claveDatoDocumento)
            valor = view.findViewById<TextView>(R.id.txv_valorDatoDocumento)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_datos_detalle_documento,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.clave.text = opcion.clave
        holder.valor.text = opcion.valor
    }

    override fun getItemCount(): Int {
        return list.size
    }
}