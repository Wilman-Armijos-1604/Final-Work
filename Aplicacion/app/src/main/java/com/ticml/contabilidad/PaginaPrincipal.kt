package com.ticml.contabilidad

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.OpcionContable
import com.ticml.contabilidad.objetos.PeriodoContable
import com.ticml.contabilidad.subinterfaces.Rcv_OpcionContable
import com.ticml.contabilidad.subinterfaces.Rcv_PeriodoContable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class PaginaPrincipal : AppCompatActivity() {

    private val db = Firebase.firestore

    val periodosContables = ArrayList<PeriodoContable>()

    lateinit var adapterPeriodos: Rcv_PeriodoContable
    lateinit var rcvPeriodos: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_principal)

        val btnInicio = findViewById<ConstraintLayout>(R.id.btn_menuInicioSesion)
        val btnPeriodoActual = findViewById<ConstraintLayout>(R.id.btn_menuPeriodoActual)
        val btnInformeContable = findViewById<ConstraintLayout>(R.id.btn_menuInformeContable)
        val btnSalir = findViewById<ConstraintLayout>(R.id.btn_menuSalir)
        val txtAuxiliar1 = findViewById<TextView>(R.id.txt_MenuPaginaPrincipal)
        val txtAuxiliar2 = findViewById<TextView>(R.id.txt_menuInformeContable)
        val txtAuxiliar3 = findViewById<TextView>(R.id.txt_menuPeriodoActual)
        val imgAuxiliar1 = findViewById<ImageView>(R.id.img_op1)
        val imgAuxiliar2 = findViewById<ImageView>(R.id.img_op2)
        val imgAuxiliar3 = findViewById<ImageView>(R.id.img_op3)

        btnInicio.setBackgroundColor(getColor(R.color.darkBlueML))
        txtAuxiliar1.setTextColor(getColor(R.color.yellowML))
        txtAuxiliar2.setTextColor(getColor(R.color.darkBlueML))
        txtAuxiliar3.setTextColor(getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar1.background, getColor(R.color.yellowML))
        DrawableCompat.setTint(imgAuxiliar2.background, getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar3.background, getColor(R.color.darkBlueML))


        btnInicio.setOnClickListener {
            val intent = Intent(this, PaginaPrincipal::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        btnPeriodoActual.setOnClickListener {
            db.collection("PeriodosContables")
                .whereEqualTo("estado", "Activo")
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        val intent =
                            Intent(this, com.ticml.contabilidad.PeriodoContable::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        intent.putExtra("idPeriodo", "${it.documents[0].id}")
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "No existen periodos activos actualmente",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnInformeContable.setOnClickListener {
            val intent = Intent(this, InformeContable::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        btnSalir.setOnClickListener {
            val confirmacion = AlertDialog.Builder(this)
            confirmacion
                .setMessage("¿Seguro que quiere salir?")
                .setPositiveButton("Sí", DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent(this, InicioSesion::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("terminar", "SALIR")
                    startActivity(intent)
                    finish()
                    exitProcess(0)
                }
                )
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                }
                )
                .create()
                .show()
        }

        val opciones = ArrayList<OpcionContable>()
        opciones.add(OpcionContable("Cuentas", R.drawable.ic_cuentas))
        opciones.add(OpcionContable("Informe Contable", R.drawable.ic_informe_contable))

        val btnCrearPeriodoContable =
            findViewById<ConstraintLayout>(R.id.btn_crearNuevoPeriodoContable)

        val rcvOpciones = findViewById<RecyclerView>(R.id.rcv_opciones)
        val adapterOpciones = Rcv_OpcionContable(this, rcvOpciones, opciones)

        rcvOpciones.adapter = adapterOpciones
        rcvOpciones.itemAnimator = DefaultItemAnimator()
        rcvOpciones.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterOpciones.notifyDataSetChanged()


        rcvPeriodos = findViewById<RecyclerView>(R.id.rcv_periodos)
        adapterPeriodos = Rcv_PeriodoContable(this, rcvPeriodos, periodosContables)

        db.collection("PeriodosContables")
            .orderBy("anio", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { periodosContablesFirestore ->
                for (periodoContable in periodosContablesFirestore) {
                    periodosContables.add(
                        PeriodoContable(
                            fechaInicio = periodoContable.data["fechaInicio"].toString(),
                            fechaFin = periodoContable.data["fechaFin"].toString(),
                            anio = periodoContable.data["anio"].toString().toInt(),
                            debe = periodoContable.data["debe"].toString().toDouble(),
                            haber = periodoContable.data["haber"].toString().toDouble(),
                            saldo = periodoContable.data["saldo"].toString().toDouble(),
                            estado = periodoContable.data["estado"].toString()
                        )
                    )
                }
                rcvPeriodos.adapter = adapterPeriodos
                rcvPeriodos.itemAnimator = DefaultItemAnimator()
                rcvPeriodos.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterPeriodos.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Periodos Contables", Toast.LENGTH_LONG).show()
            }

        btnCrearPeriodoContable.setOnClickListener {
            val anioActual = Calendar.getInstance().get(Calendar.YEAR)
            db.collection("PeriodosContables").document("Periodo${anioActual}")
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Toast.makeText(this, "Ya existe el Periodo Contable", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        db.collection("PeriodosContables").document("Periodo${anioActual}")
                            .set(
                                PeriodoContable(
                                    anio = anioActual,
                                    fechaInicio = "01/01/${anioActual}",
                                    fechaFin = "31/12/${anioActual}",
                                    debe = 0.0,
                                    haber = 0.0,
                                    saldo = 0.0,
                                    estado = "Activo"
                                )
                            )
                    }
                }
            onResume()
        }

    }

    override fun onResume() {
        super.onResume()

        Thread.sleep(1000)

        db.collection("PeriodosContables")
            .orderBy("anio",Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { periodosContablesFirestore ->
                periodosContables.clear()
                for (periodoContable in periodosContablesFirestore) {
                    periodosContables.add(
                        PeriodoContable(
                            fechaInicio = periodoContable.data["fechaInicio"].toString(),
                            fechaFin = periodoContable.data["fechaFin"].toString(),
                            anio = periodoContable.data["anio"].toString().toInt(),
                            debe = periodoContable.data["debe"].toString().toDouble(),
                            haber = periodoContable.data["haber"].toString().toDouble(),
                            saldo = periodoContable.data["saldo"].toString().toDouble(),
                            estado = periodoContable.data["estado"].toString()
                        )
                    )
                }
                rcvPeriodos.adapter = adapterPeriodos
                rcvPeriodos.itemAnimator = DefaultItemAnimator()
                rcvPeriodos.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterPeriodos.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Periodos Contables", Toast.LENGTH_LONG).show()
            }
    }

}