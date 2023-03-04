package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.TransaccionContable

class Rcv_TransaccionContable(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<TransaccionContable>
):RecyclerView.Adapter<Rcv_TransaccionContable.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val fechaTransaccion:TextView
        val clienteTransaccion:TextView
        val valorTransaccion:TextView
        init {
            fechaTransaccion = view.findViewById<TextView>(R.id.txv_fechaTransaccionPeriodo)
            clienteTransaccion = view.findViewById<TextView>(R.id.txv_clienteTransaccion)
            valorTransaccion = view.findViewById<TextView>(R.id.txv_valorTransaccion)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_transacciones_periodo_contable,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.fechaTransaccion.text = opcion.fecha
        holder.clienteTransaccion.text = opcion.cliente
        holder.valorTransaccion.text = String.format("%.2f",opcion.saldo)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}