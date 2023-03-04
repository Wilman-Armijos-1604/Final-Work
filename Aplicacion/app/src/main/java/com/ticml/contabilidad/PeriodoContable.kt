package com.ticml.contabilidad

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.TransaccionContable
import com.ticml.contabilidad.subinterfaces.Rcv_TransaccionContable
import kotlin.system.exitProcess

class PeriodoContable : AppCompatActivity() {

    private val db = Firebase.firestore

    val transacciones = ArrayList<TransaccionContable>()

    lateinit var adapterTransacciones: Rcv_TransaccionContable
    lateinit var rcvTransacciones: RecyclerView

    lateinit var idPeriodo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_periodo_contable)

        val btnInicio = findViewById<ConstraintLayout>(R.id.btn_menuInicioSesion)
        val btnPeriodoActual = findViewById<ConstraintLayout>(R.id.btn_menuPeriodoActual)
        val btnInformeContable = findViewById<ConstraintLayout>(R.id.btn_menuInformeContable)
        val btnSalir = findViewById<ConstraintLayout>(R.id.btn_menuSalir)
        val txtAuxiliar1 = findViewById<TextView>(R.id.txt_MenuPaginaPrincipal)
        val txtAuxiliar2 = findViewById<TextView>(R.id.txt_menuPeriodoActual)
        val txtAuxiliar3 = findViewById<TextView>(R.id.txt_menuInformeContable)
        val imgAuxiliar1 = findViewById<ImageView>(R.id.img_op1)
        val imgAuxiliar2 = findViewById<ImageView>(R.id.img_op2)
        val imgAuxiliar3 = findViewById<ImageView>(R.id.img_op3)

        btnPeriodoActual.setBackgroundColor(getColor(R.color.darkBlueML))
        txtAuxiliar1.setTextColor(getColor(R.color.darkBlueML))
        txtAuxiliar2.setTextColor(getColor(R.color.yellowML))
        txtAuxiliar3.setTextColor(getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar1.background, getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar2.background, getColor(R.color.yellowML))
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
                            Intent(this, PeriodoContable::class.java)
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

        val intentOrigen = intent.extras
        idPeriodo = intentOrigen!!.get("idPeriodo").toString()

        val periodoFechas = findViewById<TextView>(R.id.txt_intervaloTiempoPeriodo)
        val anioPeriodo = findViewById<TextView>(R.id.txt_anioPeriodoContable)
        val estadoPeriodo = findViewById<TextView>(R.id.txt_estadoPeriodo)

        val btnAgregarTransaccion =
            findViewById<ConstraintLayout>(R.id.btn_agregarTransaccionPeriodo)

        rcvTransacciones = findViewById<RecyclerView>(R.id.rcv_transaccionesPeriodoContable)
        adapterTransacciones = Rcv_TransaccionContable(this, rcvTransacciones, transacciones)

        Log.i("PeriodoContable","El periodo contable es $idPeriodo")
        db.collection("PeriodosContables").document(idPeriodo)
            .get()
            .addOnSuccessListener {
                periodoFechas.text =
                    it.data!!["fechaInicio"].toString() + " - " + it.data!!["fechaFin"].toString()
                anioPeriodo.text = it.data!!["anio"].toString()
                estadoPeriodo.text = it.data!!["estado"].toString()
            }

        db.collection("TransaccionesContables")
            .whereEqualTo("idPeriodo", anioPeriodo.text)
            .get()
            .addOnSuccessListener { transaccionesContablesFirestore ->
                for (transaccionContable in transaccionesContablesFirestore) {
                    transacciones.add(
                        TransaccionContable(
                            idPeriodo = transaccionContable.data["idPeriodo"].toString(),
                            fecha = transaccionContable.data["fecha"].toString(),
                            cliente = transaccionContable.data["cliente"].toString(),
                            saldo = transaccionContable.data["saldo"].toString().toDouble(),
                        )
                    )
                    rcvTransacciones.adapter = adapterTransacciones
                    rcvTransacciones.itemAnimator = DefaultItemAnimator()
                    rcvTransacciones.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    adapterTransacciones.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Transacciones Contables", Toast.LENGTH_LONG)
                    .show()
            }

        btnAgregarTransaccion.setOnClickListener {
            val intent = Intent(this, Transaccion::class.java)
            intent.putExtra("idPeriodo", "Periodo${anioPeriodo.text}")
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()

        val periodoFechas = findViewById<TextView>(R.id.txt_intervaloTiempoPeriodo)
        val anioPeriodo = findViewById<TextView>(R.id.txt_anioPeriodoContable)
        val estadoPeriodo = findViewById<TextView>(R.id.txt_estadoPeriodo)

        db.collection("PeriodosContables").document(idPeriodo)
            .get()
            .addOnSuccessListener {
                periodoFechas.text =
                    it.data!!["fechaInicio"].toString() + " - " + it.data!!["fechaFin"].toString()
                anioPeriodo.text = it.data!!["anio"].toString()
                estadoPeriodo.text = it.data!!["estado"].toString()
            }

        db.collection("TransaccionesContables")
            .get()
            .addOnSuccessListener { transaccionesContablesFirestore ->
                transacciones.clear()
                for (transaccionContable in transaccionesContablesFirestore) {
                    transacciones.add(
                        TransaccionContable(
                            idPeriodo = transaccionContable.data["idPeriodo"].toString(),
                            fecha = transaccionContable.data["fecha"].toString(),
                            cliente = transaccionContable.data["cliente"].toString(),
                            saldo = transaccionContable.data["saldo"].toString().toDouble(),
                        )
                    )
                    rcvTransacciones.adapter = adapterTransacciones
                    rcvTransacciones.itemAnimator = DefaultItemAnimator()
                    rcvTransacciones.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    adapterTransacciones.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Transacciones Contables", Toast.LENGTH_LONG)
                    .show()
            }
    }

}