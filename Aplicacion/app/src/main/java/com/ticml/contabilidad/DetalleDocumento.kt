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
import com.ticml.contabilidad.objetos.DatoDocumento
import com.ticml.contabilidad.subinterfaces.Rcv_DatoDocumento
import kotlin.system.exitProcess

class DetalleDocumento : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_documento)

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

        btnInformeContable.setBackgroundColor(getColor(R.color.darkBlueML))
        txtAuxiliar1.setTextColor(getColor(R.color.darkBlueML))
        txtAuxiliar2.setTextColor(getColor(R.color.darkBlueML))
        txtAuxiliar3.setTextColor(getColor(R.color.yellowML))
        DrawableCompat.setTint(imgAuxiliar1.background, getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar2.background, getColor(R.color.darkBlueML))
        DrawableCompat.setTint(imgAuxiliar3.background, getColor(R.color.yellowML))

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
                        val intent = Intent(this, PeriodoContable::class.java)
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
                .setPositiveButton("Sí", DialogInterface.OnClickListener { _, _ ->
                    val intent = Intent(this, InicioSesion::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("terminar", "SALIR")
                    startActivity(intent)
                    finish()
                    exitProcess(0)
                }
                )
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                }
                )
                .create()
                .show()
        }

        val intentOrigen = intent.extras
        val idDocumentoDetalle = intentOrigen!!.get("idDocumentoDetalle").toString()

        val datosDetalleDocumento = ArrayList<DatoDocumento>()
        val valor = findViewById<TextView>(R.id.txv_valorDocumentoDetalle)

        Log.i("DocumentoDetalle", "El idDocumentoDetalle en Documento es $idDocumentoDetalle")
        val rcvDatosDetalleDocumento = findViewById<RecyclerView>(R.id.rcv_datosDocumentoDetalle)
        val adapterDatosDetalleDocumento =
            Rcv_DatoDocumento(this, rcvDatosDetalleDocumento, datosDetalleDocumento)

        db.collection("DocumentosDetalle").document(idDocumentoDetalle)
            .get()
            .addOnSuccessListener {
                var nombreCampo = ""
                var valorCampo = ""
                for (campo in it.data!!) {
                    if (campo.value != null && campo.key != "total") {
                        if (campo.key.toString() == "nombreEmisor") {
                            nombreCampo = "Emisor:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "tipoDocumento") {
                            nombreCampo = "Tipo Documento:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "iva") {
                            nombreCampo = "IVA:"
                            valorCampo = String.format("%.2f", campo.value.toString().toFloat())
                        }
                        if (campo.key.toString() == "fechaEmision") {
                            nombreCampo = "Fecha:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "subtotal") {
                            nombreCampo = "Subtotal:"
                            valorCampo = String.format("%.2f", campo.value.toString().toFloat())
                        }
                        if (campo.key.toString() == "numeroDocumento") {
                            nombreCampo = "Documento:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "identificacionCliente") {
                            nombreCampo = "Cuenta Cliente:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "identificacionEmisor") {
                            nombreCampo = "Cuenta Emisor:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "nombreCliente") {
                            nombreCampo = "Cliente:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "documentoModifica") {
                            nombreCampo = "Modifica:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "empresaDocumentoModifica") {
                            nombreCampo = "Modifica Emisor:"
                            valorCampo = campo.value.toString()
                        }
                        if (campo.key.toString() == "tipoDocumentoModifica") {
                            nombreCampo = "Modifica Tipo:"
                            valorCampo = campo.value.toString()
                        }
                        datosDetalleDocumento.add(
                            DatoDocumento(
                                clave = nombreCampo,
                                valor = valorCampo
                            )
                        )
                    }
                }
                valor.text = String.format("%.2f", it.data!!["total"])
                rcvDatosDetalleDocumento.adapter = adapterDatosDetalleDocumento
                rcvDatosDetalleDocumento.itemAnimator = DefaultItemAnimator()
                rcvDatosDetalleDocumento.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterDatosDetalleDocumento.notifyDataSetChanged()
            }

    }
}