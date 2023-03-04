package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.DocumentoTransaccion

class Rcv_Documento(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<DocumentoTransaccion>
):RecyclerView.Adapter<Rcv_Documento.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tipoDocumento:TextView
        val identificacionDocumento:TextView
        val valor:TextView
        var idDocumentoTransaccion: String
        init {
            tipoDocumento = view.findViewById<TextView>(R.id.txv_tipoDocumento)
            identificacionDocumento = view.findViewById<TextView>(R.id.txv_IdentificacionDocumento)
            valor = view.findViewById<TextView>(R.id.txv_Valor)
            idDocumentoTransaccion = ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_documentos,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.tipoDocumento.text = opcion.tipoDocumento
        holder.identificacionDocumento.text = opcion.identificacionDocumento
        holder.valor.text = String.format("%.2f",opcion.valor)
        holder.idDocumentoTransaccion = opcion.idDocumentoDetalle.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }


}