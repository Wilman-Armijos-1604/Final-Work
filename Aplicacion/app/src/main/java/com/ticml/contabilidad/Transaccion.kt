package com.ticml.contabilidad

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.Cuenta
import com.ticml.contabilidad.objetos.DocumentoTransaccion
import com.ticml.contabilidad.objetos.TransaccionContable
import com.ticml.contabilidad.subinterfaces.Rcv_Documento
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class Transaccion : AppCompatActivity() {

    val db = Firebase.firestore

    private val arrayAutoCompleteOrganizacion = java.util.ArrayList<Cuenta>()
    private val arrayAutoCompleteCliente = java.util.ArrayList<Cuenta>()

    lateinit var cuentaOrganizacionOpcion: Cuenta
    lateinit var cuentaClienteOpcion: Cuenta

    var documentosTransaccion = ArrayList<DocumentoTransaccion>()

    lateinit var adapterDocumentosTransaccion: Rcv_Documento
    lateinit var rcvDocumentosTransaccion: RecyclerView
    var numTransacciones: Int? = 0
    lateinit var idTransaccion: String
    lateinit var idPeriodo: String

    var banderaDocumentos = false

    var banderaCuentaEmpresa = false
    var banderaCuentaCliente = false
    var banderaCamposSeleccionados = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaccion)

        val btnCrearCuentaOrganizacion =
            findViewById<TextView>(R.id.btn_crearCuentaOrganizacionTransaccion)
        val btnCrearCuentaCliente = findViewById<TextView>(R.id.btn_crearCuentaClienteTransaccion)

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

        btnCrearCuentaCliente.setOnClickListener {
            this.startActivity(Intent(this,InformacionCuentaTercero::class.java))
        }

        btnCrearCuentaOrganizacion.setOnClickListener {
            this.startActivity(Intent(this,InformacionCuentaEmpresa::class.java))
        }

        val totalIngresos = findViewById<TextView>(R.id.txv_ingresosTransaccion)
        val totalEgresos = findViewById<TextView>(R.id.txv_egresosTransaccion)
        val saldoTransaccion = findViewById<TextView>(R.id.txv_saldoTransaccion)

        val intentOrigen = intent.extras
        idPeriodo = intentOrigen!!.get("idPeriodo").toString()

        val cuentaOrganizacion =
            findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaOrganizacion)
        val cuentaCliente = findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaCliente)

        val btnAgregarDocumento =
            findViewById<ConstraintLayout>(R.id.btn_agregarDocumentoTransaccion)
        val btnConfirmarTransaccion = findViewById<Button>(R.id.btn_ConfirmarTransaccion)

        rcvDocumentosTransaccion = findViewById<RecyclerView>(R.id.rcv_documentosTransaccionResumen)
        adapterDocumentosTransaccion =
            Rcv_Documento(this, rcvDocumentosTransaccion, documentosTransaccion)

        db.collection("TransaccionesContables")
            .whereEqualTo("idPeriodo", idPeriodo)
            .get()
            .addOnSuccessListener { transaccionesContablesFirestore ->
                numTransacciones = transaccionesContablesFirestore.size() + 1
            }

        btnAgregarDocumento.setOnClickListener {
            Thread.sleep(1000)

            idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"

            val intent = Intent(this, Documento::class.java)
            intent.putExtra("idTransaccion", idTransaccion)
            startActivity(intent)
        }

        db.collection("CuentasOrganizacion")
            .get()
            .addOnSuccessListener { cuentasOrganizacionFirestore ->
                for (cuentaOrganizacionBase in cuentasOrganizacionFirestore) {
                    arrayAutoCompleteOrganizacion.add(
                        Cuenta(
                            organizacion = cuentaOrganizacionBase.data["organizacion"].toString(),
                            identificacion = cuentaOrganizacionBase.data["identificacion"].toString(),
                            tipo = cuentaOrganizacionBase.data["tipo"].toString(),
                            responsable = cuentaOrganizacionBase.data["responsable"].toString(),
                            idTipo = cuentaOrganizacionBase.data["idTipo"].toString()
                        )
                    )
                }
            }

        cuentaOrganizacion.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteOrganizacion
            )
        )

        cuentaOrganizacion.threshold = 1
        cuentaOrganizacion.setOnItemClickListener { adapterView, view, i, l ->
            cuentaOrganizacionOpcion = adapterView.getItemAtPosition(i) as Cuenta
            cuentaOrganizacion.setText(cuentaOrganizacionOpcion.identificacion)
            banderaCuentaEmpresa = true
        }

        db.collection("CuentasTerceros")
            .get()
            .addOnSuccessListener { cuentasClienteFirestore ->
                for (cuentaClienteBase in cuentasClienteFirestore) {
                    arrayAutoCompleteCliente.add(
                        Cuenta(
                            organizacion = cuentaClienteBase.data["organizacion"].toString(),
                            identificacion = cuentaClienteBase.data["identificacion"].toString(),
                            tipo = cuentaClienteBase.data["tipo"].toString(),
                            responsable = cuentaClienteBase.data["responsable"].toString(),
                            idTipo = cuentaClienteBase.data["idTipo"].toString()
                        )
                    )
                }
            }

        cuentaCliente.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteCliente
            )
        )
        cuentaCliente.threshold = 1
        cuentaCliente.setOnItemClickListener { adapterView, view, i, l ->
            cuentaClienteOpcion = adapterView.getItemAtPosition(i) as Cuenta
            cuentaCliente.setText(cuentaClienteOpcion.identificacion)
            banderaCuentaCliente = true
        }

        idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"
        db.collection("TransaccionesContables").document(idTransaccion)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    totalIngresos.text = String.format("%.2f", it.data!!["totalEgresos"])
                    totalEgresos.text = String.format("%.2f", it.data!!["totalIngresos"])
                    saldoTransaccion.text = String.format("%.2f", it.data!!["saldo"])
                } else {
                    totalIngresos.text = "0.00"
                    totalEgresos.text = "0.00"
                    saldoTransaccion.text = "0.00"
                }
            }

        btnConfirmarTransaccion.setOnClickListener {
            Thread.sleep(1000)
            var cont = 0
            if (cuentaCliente.text.toString() == "" || cuentaOrganizacion.text.toString() == "") {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_LONG).show()
            } else {
                if (!banderaCuentaCliente) {
                    Toast.makeText(
                        this,
                        "Seleccione una opción de la lista de Cuentas del Cliente",
                        Toast.LENGTH_LONG
                    ).show()
                    banderaCamposSeleccionados = false
                } else {
                    cont++
                }
                if (!banderaCuentaEmpresa) {
                    Toast.makeText(
                        this,
                        "Seleccione una opción de la lista de Cuentas de la Empresa",
                        Toast.LENGTH_LONG
                    ).show()
                    banderaCamposSeleccionados = false
                } else {
                    cont++
                }
                if (cont == 2) {
                    banderaCamposSeleccionados = true
                }
                if (banderaCamposSeleccionados) {
                    if (cuentaOrganizacion.text.toString() == cuentaOrganizacionOpcion.identificacion) {
                        if (cuentaCliente.text.toString() == cuentaClienteOpcion.identificacion) {
                            val formatoFechaVista = SimpleDateFormat("dd/MM/yyyy")
                            val formatoFechaFiltro = SimpleDateFormat("yyyyMMdd")
                            idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"
                            val fechaAnotacion =
                                formatoFechaVista.format(Calendar.getInstance().time)
                            val fechaFiltro = formatoFechaFiltro.format(Calendar.getInstance().time)
                            val clienteFiltro =
                                cuentaOrganizacionOpcion.responsable + cuentaOrganizacionOpcion.identificacion
                            val cuentaContableFiltro =
                                cuentaOrganizacionOpcion.tipo + cuentaOrganizacionOpcion.idTipo
                            if (documentosTransaccion.size > 0) {
                                documentosTransaccion.forEachIndexed { index, documentoTransaccion ->
                                    db.collection("Documentos")
                                        .document("${idTransaccion}_D${index + 1}")
                                        .set(documentoTransaccion)
                                }
                                Log.i(
                                    "Transaccion",
                                    "El id de la transaccion ingresada es: ${idTransaccion}"
                                )
                                db.collection("TransaccionesContables").document(idTransaccion)
                                    .set(
                                        TransaccionContable(
                                            idPeriodo = idPeriodo,
                                            fecha = fechaAnotacion,
                                            cuentaOrganizacion = cuentaOrganizacionOpcion.responsable,
                                            cliente = cuentaClienteOpcion.responsable,
                                            totalIngresos = totalIngresos.text.toString()
                                                .toDouble(),
                                            totalEgresos = totalEgresos.text.toString().toDouble(),
                                            saldo = saldoTransaccion.text.toString().toDouble(),
                                            fechaFiltro = fechaFiltro,
                                            clienteFechaFiltro = clienteFiltro + fechaFiltro,
                                            cuentaContableFechaFiltro = cuentaContableFiltro + fechaFiltro,
                                            clienteCuentaContableFechaFiltro = clienteFiltro + cuentaContableFiltro + fechaFiltro,
                                        )
                                    )
                                db.collection("PeriodosContables").document(idPeriodo)
                                    .get()
                                    .addOnSuccessListener {
                                        val debe = it.data!!["debe"].toString().toDouble()
                                        val haber = it.data!!["haber"].toString().toDouble()
                                        val saldo = it.data!!["saldo"].toString().toDouble()
                                        db.collection("PeriodosContables").document(idPeriodo)
                                            .update(
                                                "debe",
                                                debe + totalIngresos.text.toString().toDouble(),
                                                "haber",
                                                haber + totalEgresos.text.toString().toDouble(),
                                                "saldo",
                                                saldo + totalIngresos.text.toString()
                                                    .toDouble() - totalEgresos.text.toString()
                                                    .toDouble()
                                            )
                                    }
                                banderaDocumentos = true
                                this.finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "La Transacción no tiene Documentos.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Seleccione una opción de la lista de Cuentas del Cliente",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Seleccione una opción de la lista de Cuentas de la Empresa",
                            Toast.LENGTH_LONG
                        ).show()
                        if (cuentaCliente.text.toString() != cuentaClienteOpcion.identificacion) {
                            Toast.makeText(
                                this,
                                "Seleccione una opción de la lista de Cuentas del Cliente",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

    }

    override fun finish() {
        super.finish()

        Thread.sleep(1000)

        idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"

        if (!banderaDocumentos) {
            idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"

            db.collection("Documentos")
                .whereEqualTo("idTransaccionDocumento", idTransaccion)
                .get()
                .addOnSuccessListener { documentosFirestore ->
                    if (documentosFirestore.size() > 0) {
                        for (documento in documentosFirestore) {
                            db.collection("Documentos").document(documento.id)
                                .delete()
                        }
                    }
                }
            db.collection("TransaccionesContables")
                .document(idTransaccion).delete()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        Thread.sleep(1000)

        if (!banderaDocumentos) {
            idTransaccion = "P${idPeriodo.takeLast(4)}_T${(numTransacciones)}"

            db.collection("Documentos")
                .whereEqualTo("idTransaccionDocumento", idTransaccion)
                .get()
                .addOnSuccessListener { documentosFirestore ->
                    if (documentosFirestore.size() > 0) {
                        for (documento in documentosFirestore) {
                            db.collection("Documentos").document(documento.id)
                                .delete()
                        }
                    }
                }
            db.collection("TransaccionesContables")
                .document(idTransaccion).delete()
        }

    }

    override fun onPause() {
        super.onPause()

        val cuentaOrganizacion =
            findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaOrganizacion)
        val cuentaCliente = findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaCliente)

        cuentaOrganizacion.setText("")
        cuentaCliente.setText("")

    }

    override fun onResume() {
        super.onResume()

        val cuentaOrganizacion =
            findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaOrganizacion)
        val cuentaCliente = findViewById<AutoCompleteTextView>(R.id.txt_numeroCuentaCliente)

        val totalIngresos = findViewById<TextView>(R.id.txv_ingresosTransaccion)
        val totalEgresos = findViewById<TextView>(R.id.txv_egresosTransaccion)
        val saldoTransaccion = findViewById<TextView>(R.id.txv_saldoTransaccion)

        var auxTotalIngresos = 0.0
        var auxTotalEgresos = 0.0

        val docsVal = getSharedPreferences("documento", Context.MODE_PRIVATE)
        val editorDocs = docsVal.edit()

        val nuevoDocumento = DocumentoTransaccion()

        if (docsVal.all.isNotEmpty()) {
            nuevoDocumento.idDocumentoDetalle =
                docsVal.getString("documento_idDocumentoDetalle", "")
            nuevoDocumento.idTransaccionDocumento =
                docsVal.getString("documento_idTransaccionDocumento", "")
            nuevoDocumento.tipoDocumento = docsVal.getString("documento_tipoDocumento", "")
            nuevoDocumento.identificacionDocumento =
                docsVal.getString("documento_identificacionDocumento", "")
            nuevoDocumento.tipoTransaccion = docsVal.getString("documento_tipoTransaccion", "")
            nuevoDocumento.valor = docsVal.getString("documento_valor", "0.0")!!.toDouble()

            documentosTransaccion.add(nuevoDocumento)

            editorDocs.clear()
            editorDocs.apply()
        }

        if (documentosTransaccion.size > 0) {
            documentosTransaccion.forEach { it ->
                if (it.tipoTransaccion == "Ingreso") {
                    auxTotalIngresos += it.valor!!
                } else {
                    auxTotalEgresos += it.valor!!
                }
            }
        }

        totalIngresos.text = String.format("%.2f", auxTotalIngresos)
        totalEgresos.text = String.format("%.2f", auxTotalEgresos)
        saldoTransaccion.text = String.format("%.2f", (auxTotalIngresos - auxTotalEgresos))

        rcvDocumentosTransaccion.adapter = adapterDocumentosTransaccion
        rcvDocumentosTransaccion.itemAnimator = DefaultItemAnimator()
        rcvDocumentosTransaccion.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapterDocumentosTransaccion.notifyDataSetChanged()

        arrayAutoCompleteOrganizacion.clear()
        db.collection("CuentasOrganizacion")
            .get()
            .addOnSuccessListener { cuentasOrganizacionFirestore ->
                for (cuentaOrganizacionBase in cuentasOrganizacionFirestore) {
                    arrayAutoCompleteOrganizacion.add(
                        Cuenta(
                            organizacion = cuentaOrganizacionBase.data["organizacion"].toString(),
                            identificacion = cuentaOrganizacionBase.data["identificacion"].toString(),
                            tipo = cuentaOrganizacionBase.data["tipo"].toString(),
                            responsable = cuentaOrganizacionBase.data["responsable"].toString(),
                            idTipo = cuentaOrganizacionBase.data["idTipo"].toString()
                        )
                    )
                }
            }

        cuentaOrganizacion.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteOrganizacion
            )
        )

        arrayAutoCompleteCliente.clear()
        db.collection("CuentasTerceros")
            .get()
            .addOnSuccessListener { cuentasClienteFirestore ->
                for (cuentaClienteBase in cuentasClienteFirestore) {
                    arrayAutoCompleteCliente.add(
                        Cuenta(
                            organizacion = cuentaClienteBase.data["organizacion"].toString(),
                            identificacion = cuentaClienteBase.data["identificacion"].toString(),
                            tipo = cuentaClienteBase.data["tipo"].toString(),
                            responsable = cuentaClienteBase.data["responsable"].toString(),
                            idTipo = cuentaClienteBase.data["idTipo"].toString()
                        )
                    )
                }
            }

        cuentaCliente.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteCliente
            )
        )


    }

}