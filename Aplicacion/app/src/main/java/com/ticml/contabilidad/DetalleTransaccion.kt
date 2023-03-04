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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.DocumentoTransaccion
import com.ticml.contabilidad.subinterfaces.Rcv_DocumentoDetalle
import kotlin.system.exitProcess

class DetalleTransaccion : AppCompatActivity() {

    private val db = Firebase.firestore
    var sumDebe = 0.0
    var sumHaber = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_transaccion)

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
        val idTransaccion = intentOrigen!!.get("idTransaccion").toString()

        val documentosDetalleTransaccion = ArrayList<DocumentoTransaccion>()

        val rcvDocumentosTransaccion =
            findViewById<RecyclerView>(R.id.rcv_documentosTransaccionResumen)
        val adapterDocumentosTransaccion =
            Rcv_DocumentoDetalle(this, rcvDocumentosTransaccion, documentosDetalleTransaccion)

        val debeDetalleTransaccion = findViewById<TextView>(R.id.txt_debeDetalleTransaccion)
        val haberDetalleTransaccion = findViewById<TextView>(R.id.txt_haberDetalleTransaccion)
        val saldoDetalleTransaccion = findViewById<TextView>(R.id.txt_saldoDetalleTransaccion)

        db.collection("Documentos")
            .whereEqualTo("idTransaccionDocumento", idTransaccion)
            .get()
            .addOnSuccessListener { documentosDetalleFirestore ->
                for (documentosDetalle in documentosDetalleFirestore) {
                    documentosDetalleTransaccion.add(
                        DocumentoTransaccion(
                            idDocumentoDetalle = documentosDetalle.data["idDocumentoDetalle"].toString(),
                            idTransaccionDocumento = documentosDetalle.data["idTransaccionDocumento"].toString(),
                            tipoDocumento = documentosDetalle.data["tipoDocumento"].toString(),
                            identificacionDocumento = documentosDetalle.data["identificacionDocumento"].toString(),
                            tipoTransaccion = documentosDetalle.data["tipoTransaccion"].toString(),
                            valor = documentosDetalle.data["valor"].toString().toDouble()
                        )
                    )
                    if (documentosDetalle.data["tipoTransaccion"].toString() == "Ingreso") {
                        sumHaber += documentosDetalle.data["valor"].toString().toDouble()
                    } else {
                        sumDebe += documentosDetalle.data["valor"].toString().toDouble()
                    }
                }
                debeDetalleTransaccion.text = String.format("%.2f", sumDebe)
                haberDetalleTransaccion.text = String.format("%.2f", sumHaber)
                saldoDetalleTransaccion.text = String.format("%.2f", (sumHaber - sumDebe))
                rcvDocumentosTransaccion.adapter = adapterDocumentosTransaccion
                rcvDocumentosTransaccion.itemAnimator = DefaultItemAnimator()
                rcvDocumentosTransaccion.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterDocumentosTransaccion.notifyDataSetChanged()
            }


    }
}