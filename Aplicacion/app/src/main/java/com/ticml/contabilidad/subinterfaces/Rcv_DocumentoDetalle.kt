package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.DetalleDocumento
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.DocumentoTransaccion

class Rcv_DocumentoDetalle(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<DocumentoTransaccion>
):RecyclerView.Adapter<Rcv_DocumentoDetalle.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val tipoDocumento: TextView
        val identificacionDocumento: TextView
        val valor: TextView
        var idDocumentoDetalle: String
        init {
            tipoDocumento = view.findViewById<TextView>(R.id.txv_tipoDocumento)
            identificacionDocumento = view.findViewById<TextView>(R.id.txv_IdentificacionDocumento)
            valor = view.findViewById<TextView>(R.id.txv_Valor)
            idDocumentoDetalle = ""
            view.setOnClickListener(this)
        }

        override fun onClick(item: View?) {
            val intent = Intent(context, DetalleDocumento::class.java)
            intent.putExtra("idDocumentoDetalle",idDocumentoDetalle)
            Log.i("DocumentoDetalle","idDocumentoDetalle es: ${idDocumentoDetalle}")
            context.startActivity(intent)
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
        holder.idDocumentoDetalle = opcion.idDocumentoDetalle.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }

}