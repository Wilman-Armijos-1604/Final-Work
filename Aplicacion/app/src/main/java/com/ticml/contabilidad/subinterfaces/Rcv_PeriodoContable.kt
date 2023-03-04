package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.PeriodoContable

class Rcv_PeriodoContable(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<PeriodoContable>
):RecyclerView.Adapter<Rcv_PeriodoContable.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val anio:TextView
        val periodoTiempo:TextView
        val debe:TextView
        val haber:TextView
        val saldo:TextView
        var estado:String

        init {
            anio = view.findViewById<TextView>(R.id.txv_anioContablePeriodo)
            periodoTiempo = view.findViewById<TextView>(R.id.txv_periodoTiempoPeriodo)
            debe = view.findViewById<TextView>(R.id.txv_debePeriodo)
            haber = view.findViewById<TextView>(R.id.txv_haberPeriodo)
            saldo = view.findViewById<TextView>(R.id.txv_saldoPeriodo)
            estado = ""
            view.setOnClickListener(this)
        }

        override fun onClick(item: View?) {
            if (estado=="Activo") {
                val intent = Intent(context, com.ticml.contabilidad.PeriodoContable::class.java)
                intent.putExtra("idPeriodo","Periodo${anio.text}")
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Solo se puede trabajar sobre Periodos Activos", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_periodos_contables,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.anio.text = opcion.anio.toString()
        holder.periodoTiempo.text = opcion.fechaInicio+" - "+opcion.fechaFin
        holder.debe.text = String.format("%.2f",opcion.debe)
        holder.haber.text = String.format("%.2f",opcion.haber)
        holder.saldo.text = String.format("%.2f",opcion.saldo)
        holder.estado = opcion.estado.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}