package com.ticml.contabilidad.subinterfaces

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ticml.contabilidad.Cuentas
import com.ticml.contabilidad.InformeContable
import com.ticml.contabilidad.R
import com.ticml.contabilidad.objetos.OpcionContable

class Rcv_OpcionContable(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val list: List<OpcionContable>
):RecyclerView.Adapter<Rcv_OpcionContable.myViewHolder>() {
    inner class myViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val nombre:TextView
        val imagen:ImageView
        init {
            nombre = view.findViewById<TextView>(R.id.txv_opcion)
            imagen = view.findViewById<ImageView>(R.id.img_Opcion)
            view.setOnClickListener(this)
        }

        override fun onClick(item: View?) {
            when (adapterPosition) {
                0 -> {
                    val intent = Intent(context, Cuentas::class.java)
                    context.startActivity(intent)
                }
                1 -> {
                    val intent = Intent(context, InformeContable::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_opciones, parent, false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val opcion = list[position]
        holder.nombre.text = opcion.nombre
        holder.imagen.setImageResource(opcion.imagen!!)

    }

    override fun getItemCount(): Int {
        return list.size
    }

}