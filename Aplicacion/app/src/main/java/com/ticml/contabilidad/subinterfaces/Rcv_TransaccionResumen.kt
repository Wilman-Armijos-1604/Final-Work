package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.DetalleTransaccion
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.TransaccionResumen

class Rcv_TransaccionResumen(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<TransaccionResumen>
):RecyclerView.Adapter<Rcv_TransaccionResumen.myViewHolder>() {
    inner class myViewHolder(view: View):RecyclerView.ViewHolder(view), View.OnClickListener {
        val fechaTransaccion:TextView
        val tipoTransaccion:TextView
        val valorTransaccion:TextView
        var idTransaccion:String
        init {
            fechaTransaccion = view.findViewById<TextView>(R.id.txv_fechaTransaccionInforme)
            tipoTransaccion = view.findViewById<TextView>(R.id.txv_tipoTransaccionInforme)
            valorTransaccion = view.findViewById<TextView>(R.id.txv_valorTransaccionInforme)
            idTransaccion=""
            view.setOnClickListener(this)
        }

        override fun onClick(item: View?) {
            val intent = Intent(context,DetalleTransaccion::class.java)
            intent.putExtra("idTransaccion",idTransaccion)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_transacciones_informe_contable,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.fechaTransaccion.text = opcion.fecha
        if (opcion.saldo!!<0) {
            holder.tipoTransaccion.text = "Egreso"
        } else {
            holder.tipoTransaccion.text = "Ingreso"
        }
        holder.valorTransaccion.text = String.format("%.2f",opcion.saldo)
        holder.idTransaccion = opcion.idTransaccion.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}