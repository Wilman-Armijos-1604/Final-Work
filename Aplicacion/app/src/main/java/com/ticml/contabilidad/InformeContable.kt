package com.ticml.contabilidad

import android.app.DatePickerDialog
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
import com.ticml.contabilidad.objetos.CuentaContable
import com.ticml.contabilidad.objetos.TransaccionResumen
import com.ticml.contabilidad.subinterfaces.Rcv_TransaccionResumen
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class InformeContable : AppCompatActivity() {

    val db = Firebase.firestore

    lateinit var adapterTransaccionesInforme: Rcv_TransaccionResumen
    lateinit var rcvTransaccionesInforme: RecyclerView

    private val arrayAutoCompleteCuentaContable = java.util.ArrayList<CuentaContable>()
    private val arrayAutoCompleteCliente = java.util.ArrayList<Cuenta>()

    private val transaccionesInforme = ArrayList<TransaccionResumen>()
    lateinit var cuentaContableOpcion: CuentaContable
    lateinit var clienteOpcion: Cuenta

    val calendarioAuxiliarHasta: Calendar = Calendar.getInstance()
    val calendarioAuxiliarDesde: Calendar = Calendar.getInstance()

    var estadoSubmenu: String = "C"
    var banderaCuenta: String = "N"
    var banderaCliente: String = "N"
    var banderaBusqueda: String = "F"

    var banderaCuentaOpcion = false
    var banderaClienteOpcion = false

    var banderaCuentaSeleccion = false
    var banderaClienteSeleccion = false

    var banderaCamposSeleccionados = false

    var debeVal = 0.0
    var haberVal = 0.0

    var scale: Double? = 1.0
    var idTipoCuentaAuxiliar = ""
    var tipoCuentaAuxiliar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informe_contable)

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

        estadoSubmenu = "C"
        banderaCuenta = "N"
        banderaCliente = "N"
        banderaBusqueda = "F"

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

        scale = resources.displayMetrics.density.toDouble()

        val debeInforme = findViewById<TextView>(R.id.txv_debeInforme)
        val haberInforme = findViewById<TextView>(R.id.txv_haberInforme)
        val saldoInforme = findViewById<TextView>(R.id.txv_saldoInforme)

        val filtroFechaDesde = findViewById<TextView>(R.id.txt_desdeFecha)
        val filtroFechaHasta = findViewById<TextView>(R.id.txt_hastaFecha)

        val btnSubMenuFiltro = findViewById<TextView>(R.id.btn_FiltrarInforme)
        val btnAplicarFiltro = findViewById<Button>(R.id.btn_AplicarFiltro)

        val subMenuLayout = findViewById<ConstraintLayout>(R.id.layout_submenuFiltro)

        val btnCuentaContableFiltro = findViewById<TextView>(R.id.btn_FiltroCuentaContable)
        val btnClienteFiltro = findViewById<TextView>(R.id.btn_FiltroCuentaCliente)

        val cuentaContableFiltro = findViewById<AutoCompleteTextView>(R.id.txt_CuentaFiltro)
        val clienteFiltro = findViewById<AutoCompleteTextView>(R.id.txt_clienteFiltro)


        db.collection("CuentasContables")
            .get()
            .addOnSuccessListener { cuentasContablesFirestore ->
                for (cuenta in cuentasContablesFirestore) {
                    arrayAutoCompleteCuentaContable.add(
                        CuentaContable(
                            cuenta.data["nivel1"].toString(),
                            cuenta.data["nivel2"].toString(),
                            cuenta.data["nivel3"].toString(),
                            cuenta.data["nivel4"].toString(),
                            cuenta.data["nivel5"].toString(),
                            cuenta.data["auxNiveles"].toString()
                        )
                    )
                }
            }

        cuentaContableFiltro.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteCuentaContable
            )
        )
        cuentaContableFiltro.threshold = 1
        cuentaContableFiltro.setOnItemClickListener { adapterView, view, i, l ->
            cuentaContableOpcion = adapterView.getItemAtPosition(i) as CuentaContable
            cuentaContableFiltro.setText(cuentaContableOpcion.nivel5)
            tipoCuentaAuxiliar = cuentaContableOpcion.auxNiveles
            banderaCuentaSeleccion = true
        }

        db.collection("CuentasTerceros")
            .get()
            .addOnSuccessListener { cuentasContablesFirestore ->
                for (cuenta in cuentasContablesFirestore) {
                    arrayAutoCompleteCliente.add(
                        Cuenta(
                            cuenta.data["organizacion"].toString(),
                            cuenta.data["responsable"].toString(),
                            cuenta.data["identificacion"].toString(),
                            cuenta.data["tipo"].toString(),
                            cuenta.data["idTipo"].toString()
                        )
                    )
                }
            }

        clienteFiltro.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteCliente
            )
        )
        clienteFiltro.threshold = 1
        clienteFiltro.setOnItemClickListener { adapterView, view, i, l ->
            clienteOpcion = adapterView.getItemAtPosition(i) as Cuenta
            clienteFiltro.setText(clienteOpcion.identificacion)
            banderaClienteSeleccion = true
        }


        rcvTransaccionesInforme = findViewById<RecyclerView>(R.id.rcv_transaccionesInforme)
        adapterTransaccionesInforme =
            Rcv_TransaccionResumen(this, rcvTransaccionesInforme, transaccionesInforme)

        val anioDefectoHasta = calendarioAuxiliarHasta.get(Calendar.YEAR)
        calendarioAuxiliarHasta.set(Calendar.YEAR, anioDefectoHasta)
        val mesDefectoHasta = calendarioAuxiliarHasta.get(Calendar.MONTH)
        calendarioAuxiliarHasta.set(Calendar.MONTH, mesDefectoHasta)
        val diaDefectoHasta = calendarioAuxiliarHasta.get(Calendar.DAY_OF_MONTH)
        calendarioAuxiliarHasta.set(Calendar.DAY_OF_MONTH, diaDefectoHasta)

        val anioDefectoDesde = anioDefectoHasta - 2
        calendarioAuxiliarDesde.set(Calendar.YEAR, anioDefectoDesde)
        val mesDefectoDesde = mesDefectoHasta
        calendarioAuxiliarDesde.set(Calendar.MONTH, mesDefectoDesde)
        val diaDefectoDesde = diaDefectoHasta
        calendarioAuxiliarDesde.set(Calendar.DAY_OF_MONTH, diaDefectoDesde)

        val formato = SimpleDateFormat("dd/MM/yyyy")
        val formatoFiltro = SimpleDateFormat("yyyyMMdd")

        filtroFechaDesde.text = formato.format(calendarioAuxiliarDesde.time)
        filtroFechaHasta.text = formato.format(calendarioAuxiliarHasta.time)

        val desdeFechaSeleccion = DatePickerDialog.OnDateSetListener { view, anio, mes, dia ->
            calendarioAuxiliarDesde.set(Calendar.YEAR, anio)
            calendarioAuxiliarDesde.set(Calendar.MONTH, mes)
            calendarioAuxiliarDesde.set(Calendar.DAY_OF_MONTH, dia)
            filtroFechaDesde.text = formato.format(calendarioAuxiliarDesde.time)
        }

        val hastaFechaSeleccion = DatePickerDialog.OnDateSetListener { view, anio, mes, dia ->
            calendarioAuxiliarHasta.set(Calendar.YEAR, anio)
            calendarioAuxiliarHasta.set(Calendar.MONTH, mes)
            calendarioAuxiliarHasta.set(Calendar.DAY_OF_MONTH, dia)
            filtroFechaHasta.text = formato.format(calendarioAuxiliarHasta.time)
        }

        filtroFechaDesde.setOnClickListener {
            DatePickerDialog(
                this,
                desdeFechaSeleccion,
                calendarioAuxiliarDesde.get(Calendar.YEAR),
                calendarioAuxiliarDesde.get(Calendar.MONTH),
                calendarioAuxiliarDesde.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        filtroFechaHasta.setOnClickListener {
            DatePickerDialog(
                this,
                hastaFechaSeleccion,
                calendarioAuxiliarHasta.get(Calendar.YEAR),
                calendarioAuxiliarHasta.get(Calendar.MONTH),
                calendarioAuxiliarHasta.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        db.collection("TransaccionesContables")
            .whereGreaterThanOrEqualTo(
                "fechaFiltro",
                formatoFiltro.format(calendarioAuxiliarDesde.time)
            )
            .whereLessThanOrEqualTo(
                "fechaFiltro",
                formatoFiltro.format(calendarioAuxiliarHasta.time)
            )
            .get()
            .addOnSuccessListener { transaccionesInformeFirestore ->
                Log.i(
                    "Transacciones Informe",
                    "Las transacciones traidas son: ${transaccionesInformeFirestore.size()}"
                )

                for (transaccionInforme in transaccionesInformeFirestore) {
                    var auxiliarTipoTransaccion = "Ingreso"
                    if (transaccionInforme.data["saldo"].toString().toDouble() < 0) {
                        auxiliarTipoTransaccion = "Egreso"
                    }
                    debeVal += transaccionInforme.data["totalEgresos"].toString().toDouble()
                    haberVal += transaccionInforme.data["totalIngresos"].toString().toDouble()
                    transaccionesInforme.add(
                        TransaccionResumen(
                            idTransaccion = transaccionInforme.id,
                            tipoTransaccion = auxiliarTipoTransaccion,
                            fecha = transaccionInforme.data["fecha"].toString(),
                            cuentaOrganizacion = transaccionInforme.data["cuentaOrganizacion"].toString(),
                            cliente = transaccionInforme.data["cliente"].toString(),
                            totalIngresos = transaccionInforme.data["totalIngresos"].toString()
                                .toDouble(),
                            totalEgresos = transaccionInforme.data["totalEgresos"].toString()
                                .toDouble(),
                            saldo = transaccionInforme.data["saldo"].toString().toDouble(),
                        )
                    )
                }
                Log.i(
                    "Transacciones Informe",
                    "Los valores debe y haber el bucle son: (${debeVal},${haberVal})"
                )
                debeInforme.text = String.format("%.2f", debeVal)
                haberInforme.text = String.format("%.2f", haberVal)
                saldoInforme.text = String.format("%.2f", (haberVal - debeVal))
                rcvTransaccionesInforme.adapter = adapterTransaccionesInforme
                rcvTransaccionesInforme.itemAnimator = DefaultItemAnimator()
                rcvTransaccionesInforme.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterTransaccionesInforme.notifyDataSetChanged()
            }

        btnSubMenuFiltro.setOnClickListener {
            val layout = subMenuLayout.layoutParams
            if (estadoSubmenu == "C") {
                layout.height = (140 * scale!! + 0.5f).toInt()
                subMenuLayout.layoutParams = layout
                estadoSubmenu = "D"
            } else if (estadoSubmenu == "D") {
                layout.height = (20 * scale!! + 0.5f).toInt()
                subMenuLayout.layoutParams = layout
                estadoSubmenu = "C"
            }
        }

        btnCuentaContableFiltro.setOnClickListener {
            if (banderaCuenta == "S") {
                banderaCuenta = "N"
                btnCuentaContableFiltro.background =
                    resources.getDrawable(R.drawable.rounded_textfield, theme)
                btnCuentaContableFiltro.setTextColor(resources.getColor(R.color.darkBlueML, theme))
            } else if (banderaCuenta == "N") {
                banderaCuenta = "S"
                btnCuentaContableFiltro.background =
                    resources.getDrawable(R.drawable.rounded_button_inicio_sesion, theme)
                btnCuentaContableFiltro.setTextColor(resources.getColor(R.color.white, theme))

            }
        }

        btnClienteFiltro.setOnClickListener {
            if (banderaCliente == "S") {
                banderaCliente = "N"
                btnClienteFiltro.background =
                    resources.getDrawable(R.drawable.rounded_textfield, theme)
                btnClienteFiltro.setTextColor(resources.getColor(R.color.darkBlueML, theme))
            } else if (banderaCliente == "N") {
                banderaCliente = "S"
                btnClienteFiltro.background =
                    resources.getDrawable(R.drawable.rounded_button_inicio_sesion, theme)
                btnClienteFiltro.setTextColor(resources.getColor(R.color.white, theme))
            }
        }

        btnAplicarFiltro.setOnClickListener {
            if (banderaCliente == "S") {
                if (banderaCuenta == "S") {
                    banderaBusqueda = "FCCC"
                    banderaClienteOpcion = true
                    banderaCuentaOpcion = true
                } else if (banderaCuenta == "N") {
                    banderaBusqueda = "FC"
                    banderaClienteOpcion = true
                    banderaCuentaOpcion = false
                }
            } else if (banderaCliente == "N") {
                if (banderaCuenta == "S") {
                    banderaBusqueda = "FCC"
                    banderaClienteOpcion = false
                    banderaCuentaOpcion = true
                } else if (banderaCuenta == "N") {
                    banderaBusqueda = "F"
                    banderaClienteOpcion = false
                    banderaCuentaOpcion = false
                }
            }

            transaccionesInforme.clear()
            when (banderaBusqueda) {
                "F" -> {
                    db.collection("TransaccionesContables")
                        .whereGreaterThanOrEqualTo(
                            "fechaFiltro",
                            formatoFiltro.format(calendarioAuxiliarDesde.time)
                        )
                        .whereLessThanOrEqualTo(
                            "fechaFiltro",
                            formatoFiltro.format(calendarioAuxiliarHasta.time)
                        )
                        .get()
                        .addOnSuccessListener { transaccionesInformeFirestore ->
                            var debeVal = 0.0
                            var haberVal = 0.0
                            if (transaccionesInformeFirestore.size() > 0) {
                                for (transaccionInforme in transaccionesInformeFirestore) {
                                    var auxiliarTipoTransaccion = "Ingreso"
                                    if (transaccionInforme.data["saldo"].toString()
                                            .toDouble() < 0
                                    ) {
                                        auxiliarTipoTransaccion = "Egreso"
                                    }
                                    debeVal +=
                                        transaccionInforme.data["totalEgresos"].toString()
                                            .toDouble()

                                    haberVal +=
                                        transaccionInforme.data["totalIngresos"].toString()
                                            .toDouble()
                                    transaccionesInforme.add(
                                        TransaccionResumen(
                                            idTransaccion = transaccionInforme.id,
                                            tipoTransaccion = auxiliarTipoTransaccion,
                                            fecha = transaccionInforme.data["fecha"].toString(),
                                            cuentaOrganizacion = transaccionInforme.data["cuentaOrganizacion"].toString(),
                                            cliente = transaccionInforme.data["cliente"].toString(),
                                            totalIngresos = transaccionInforme.data["totalIngresos"].toString()
                                                .toDouble(),
                                            totalEgresos = transaccionInforme.data["totalEgresos"].toString()
                                                .toDouble(),
                                            saldo = transaccionInforme.data["saldo"].toString()
                                                .toDouble(),
                                        )
                                    )
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "No se encontraron resultados",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            debeInforme.text = String.format("%.2f", debeVal)
                            haberInforme.text = String.format("%.2f", haberVal)
                            saldoInforme.text = String.format("%.2f", (haberVal - debeVal))
                            rcvTransaccionesInforme.adapter = adapterTransaccionesInforme
                            rcvTransaccionesInforme.itemAnimator = DefaultItemAnimator()
                            rcvTransaccionesInforme.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            adapterTransaccionesInforme.notifyDataSetChanged()

                            cuentaContableFiltro.setText("")
                            clienteFiltro.setText("")

                            val layout = subMenuLayout.layoutParams
                            layout.height = (20 * scale!! + 0.5f).toInt()
                            subMenuLayout.layoutParams = layout
                            estadoSubmenu = "C"
                        }
                }
                "FC" -> {
                    if (banderaClienteOpcion && banderaClienteSeleccion) {
                        if (clienteFiltro.text.toString() != "" || clienteOpcion.identificacion == clienteFiltro.text.toString()) {
                            db.collection("TransaccionesContables")
                                .whereGreaterThanOrEqualTo(
                                    "clienteFechaFiltro",
                                    clienteOpcion.responsable + clienteOpcion.identificacion + formatoFiltro.format(
                                        calendarioAuxiliarDesde.time
                                    )
                                )
                                .whereLessThanOrEqualTo(
                                    "clienteFechaFiltro",
                                    clienteOpcion.responsable + clienteOpcion.identificacion + formatoFiltro.format(
                                        calendarioAuxiliarHasta.time
                                    )
                                )
                                .get()
                                .addOnSuccessListener { transaccionesInformeFirestore ->
                                    var debeVal = 0.0
                                    var haberVal = 0.0
                                    if (transaccionesInformeFirestore.size() > 0) {
                                        for (transaccionInforme in transaccionesInformeFirestore) {
                                            var auxiliarTipoTransaccion = "Ingreso"
                                            if (transaccionInforme.data["saldo"].toString()
                                                    .toDouble() < 0
                                            ) {
                                                auxiliarTipoTransaccion = "Egreso"
                                            }
                                            debeVal +=
                                                transaccionInforme.data["totalEgresos"].toString()
                                                    .toDouble()
                                            haberVal +=
                                                transaccionInforme.data["totalIngresos"].toString()
                                                    .toDouble()
                                            transaccionesInforme.add(
                                                TransaccionResumen(
                                                    idTransaccion = transaccionInforme.id,
                                                    tipoTransaccion = auxiliarTipoTransaccion,
                                                    fecha = transaccionInforme.data["fecha"].toString(),
                                                    cuentaOrganizacion = transaccionInforme.data["cuentaOrganizacion"].toString(),
                                                    cliente = transaccionInforme.data["cliente"].toString(),
                                                    totalIngresos = transaccionInforme.data["totalIngresos"].toString()
                                                        .toDouble(),
                                                    totalEgresos = transaccionInforme.data["totalEgresos"].toString()
                                                        .toDouble(),
                                                    saldo = transaccionInforme.data["saldo"].toString()
                                                        .toDouble(),
                                                )
                                            )
                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "No se encontraron ",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    debeInforme.text = String.format("%.2f", debeVal)
                                    haberInforme.text = String.format("%.2f", haberVal)
                                    saldoInforme.text = String.format("%.2f", (haberVal - debeVal))
                                    rcvTransaccionesInforme.adapter = adapterTransaccionesInforme
                                    rcvTransaccionesInforme.itemAnimator = DefaultItemAnimator()
                                    rcvTransaccionesInforme.layoutManager =
                                        LinearLayoutManager(
                                            this,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    adapterTransaccionesInforme.notifyDataSetChanged()

                                    cuentaContableFiltro.setText("")

                                    val layout = subMenuLayout.layoutParams
                                    layout.height = (20 * scale!! + 0.5f).toInt()
                                    subMenuLayout.layoutParams = layout
                                    estadoSubmenu = "C"
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Seleccione un Cliente de la lista",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Seleccione un Cliente de la lista",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                "FCC" -> {
                    if (banderaCuentaOpcion && banderaCuentaSeleccion) {
                        if (cuentaContableFiltro.text.toString() != "" || cuentaContableOpcion.nivel5 == cuentaContableFiltro.text.toString()) {
                            db.collection("CuentasContables")
                                .whereEqualTo("auxNiveles", tipoCuentaAuxiliar)
                                .get()
                                .addOnSuccessListener {
                                    if (!it.isEmpty) {
                                        idTipoCuentaAuxiliar = it.documents[0].id
                                        db.collection("TransaccionesContables")
                                            .whereGreaterThanOrEqualTo(
                                                "cuentaContableFechaFiltro",
                                                cuentaContableOpcion.nivel5 + idTipoCuentaAuxiliar + formatoFiltro.format(
                                                    calendarioAuxiliarDesde.time
                                                )
                                            )
                                            .whereLessThanOrEqualTo(
                                                "cuentaContableFechaFiltro",
                                                cuentaContableOpcion.nivel5 + idTipoCuentaAuxiliar + formatoFiltro.format(
                                                    calendarioAuxiliarHasta.time
                                                )
                                            )
                                            .get()
                                            .addOnSuccessListener { transaccionesInformeFirestore ->
                                                var debeVal = 0.0
                                                var haberVal = 0.0
                                                if (transaccionesInformeFirestore.size() > 0) {
                                                    for (transaccionInforme in transaccionesInformeFirestore) {
                                                        var auxiliarTipoTransaccion = "Ingreso"
                                                        if (transaccionInforme.data["saldo"].toString()
                                                                .toDouble() < 0
                                                        ) {
                                                            auxiliarTipoTransaccion = "Egreso"
                                                        }
                                                        debeVal +=
                                                            transaccionInforme.data["totalEgresos"].toString()
                                                                .toDouble()
                                                        haberVal +=
                                                            transaccionInforme.data["totalIngresos"].toString()
                                                                .toDouble()
                                                        transaccionesInforme.add(
                                                            TransaccionResumen(
                                                                idTransaccion = transaccionInforme.id,
                                                                tipoTransaccion = auxiliarTipoTransaccion,
                                                                fecha = transaccionInforme.data["fecha"].toString(),
                                                                cuentaOrganizacion = transaccionInforme.data["cuentaOrganizacion"].toString(),
                                                                cliente = transaccionInforme.data["cliente"].toString(),
                                                                totalIngresos = transaccionInforme.data["totalIngresos"].toString()
                                                                    .toDouble(),
                                                                totalEgresos = transaccionInforme.data["totalEgresos"].toString()
                                                                    .toDouble(),
                                                                saldo = transaccionInforme.data["saldo"].toString()
                                                                    .toDouble(),
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "No se encontraron resultados",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                debeInforme.text = String.format("%.2f", debeVal)
                                                haberInforme.text = String.format("%.2f", haberVal)
                                                saldoInforme.text =
                                                    String.format("%.2f", (haberVal - debeVal))
                                                rcvTransaccionesInforme.adapter =
                                                    adapterTransaccionesInforme
                                                rcvTransaccionesInforme.itemAnimator =
                                                    DefaultItemAnimator()
                                                rcvTransaccionesInforme.layoutManager =
                                                    LinearLayoutManager(
                                                        this,
                                                        LinearLayoutManager.VERTICAL,
                                                        false
                                                    )
                                                adapterTransaccionesInforme.notifyDataSetChanged()

                                                clienteFiltro.setText("")

                                                val layout = subMenuLayout.layoutParams
                                                layout.height = (20 * scale!! + 0.5f).toInt()
                                                subMenuLayout.layoutParams = layout
                                                estadoSubmenu = "C"
                                            }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "No se encontraron resultados",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        transaccionesInforme.clear()
                                        rcvTransaccionesInforme.adapter =
                                            adapterTransaccionesInforme
                                        rcvTransaccionesInforme.itemAnimator = DefaultItemAnimator()
                                        rcvTransaccionesInforme.layoutManager =
                                            LinearLayoutManager(
                                                this,
                                                LinearLayoutManager.VERTICAL,
                                                false
                                            )
                                        adapterTransaccionesInforme.notifyDataSetChanged()

                                        val layout = subMenuLayout.layoutParams
                                        layout.height = (20 * scale!! + 0.5f).toInt()
                                        subMenuLayout.layoutParams = layout
                                        estadoSubmenu = "C"
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Seleccione una Cuenta Contable de la lista",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Seleccione una Cuenta Contable de la lista",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                "FCCC" -> {
                    if (banderaClienteOpcion && banderaClienteSeleccion) {
                        if (clienteFiltro.text.toString() != "" || clienteOpcion.identificacion == clienteFiltro.text.toString()) {
                            if (banderaCuentaOpcion && banderaCuentaSeleccion) {
                                if (cuentaContableFiltro.text.toString() != "" || cuentaContableOpcion.nivel5 == cuentaContableFiltro.text.toString()) {
                                    db.collection("CuentasContables")
                                        .whereEqualTo("auxNiveles", tipoCuentaAuxiliar)
                                        .get()
                                        .addOnSuccessListener {
                                            if (!it.isEmpty) {
                                                idTipoCuentaAuxiliar = it.documents[0].id
                                                db.collection("TransaccionesContables")
                                                    .whereGreaterThanOrEqualTo(
                                                        "clienteCuentaContableFechaFiltro",
                                                        clienteOpcion.responsable + clienteOpcion.identificacion + cuentaContableOpcion.nivel5 + idTipoCuentaAuxiliar + formatoFiltro.format(
                                                            calendarioAuxiliarDesde.time
                                                        )
                                                    )
                                                    .whereLessThanOrEqualTo(
                                                        "clienteCuentaContableFechaFiltro",
                                                        clienteOpcion.responsable + clienteOpcion.identificacion + cuentaContableOpcion.nivel5 + idTipoCuentaAuxiliar + formatoFiltro.format(
                                                            calendarioAuxiliarHasta.time
                                                        )
                                                    )
                                                    .get()
                                                    .addOnSuccessListener { transaccionesInformeFirestore ->
                                                        var debeVal = 0.0
                                                        var haberVal = 0.0
                                                        if (transaccionesInformeFirestore.size() > 0) {
                                                            for (transaccionInforme in transaccionesInformeFirestore) {
                                                                var auxiliarTipoTransaccion = "Ingreso"
                                                                if (transaccionInforme.data["saldo"].toString()
                                                                        .toDouble() < 0
                                                                ) {
                                                                    auxiliarTipoTransaccion = "Egreso"
                                                                }
                                                                debeVal +=
                                                                    transaccionInforme.data["totalEgresos"].toString()
                                                                        .toDouble()
                                                                haberVal +=
                                                                    transaccionInforme.data["totalIngresos"].toString()
                                                                        .toDouble()
                                                                transaccionesInforme.add(
                                                                    TransaccionResumen(
                                                                        idTransaccion = transaccionInforme.id,
                                                                        tipoTransaccion = auxiliarTipoTransaccion,
                                                                        fecha = transaccionInforme.data["fecha"].toString(),
                                                                        cuentaOrganizacion = transaccionInforme.data["cuentaOrganizacion"].toString(),
                                                                        cliente = transaccionInforme.data["cliente"].toString(),
                                                                        totalIngresos = transaccionInforme.data["totalIngresos"].toString()
                                                                            .toDouble(),
                                                                        totalEgresos = transaccionInforme.data["totalEgresos"].toString()
                                                                            .toDouble(),
                                                                        saldo = transaccionInforme.data["saldo"].toString()
                                                                            .toDouble(),
                                                                    )
                                                                )
                                                            }
                                                        } else {
                                                            Toast.makeText(
                                                                this,
                                                                "No se encontraron resultados",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                        debeInforme.text =
                                                            String.format("%.2f", debeVal)
                                                        haberInforme.text =
                                                            String.format("%.2f", haberVal)
                                                        saldoInforme.text =
                                                            String.format("%.2f", (haberVal - debeVal))
                                                        rcvTransaccionesInforme.adapter =
                                                            adapterTransaccionesInforme
                                                        rcvTransaccionesInforme.itemAnimator =
                                                            DefaultItemAnimator()
                                                        rcvTransaccionesInforme.layoutManager =
                                                            LinearLayoutManager(
                                                                this,
                                                                LinearLayoutManager.VERTICAL,
                                                                false
                                                            )
                                                        adapterTransaccionesInforme.notifyDataSetChanged()

                                                        val layout = subMenuLayout.layoutParams
                                                        layout.height = (20 * scale!! + 0.5f).toInt()
                                                        subMenuLayout.layoutParams = layout
                                                        estadoSubmenu = "C"
                                                    }

                                                transaccionesInforme.clear()
                                                rcvTransaccionesInforme.adapter =
                                                    adapterTransaccionesInforme
                                                rcvTransaccionesInforme.itemAnimator =
                                                    DefaultItemAnimator()
                                                rcvTransaccionesInforme.layoutManager =
                                                    LinearLayoutManager(
                                                        this,
                                                        LinearLayoutManager.VERTICAL,
                                                        false
                                                    )
                                                adapterTransaccionesInforme.notifyDataSetChanged()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "No se encontraron resultados",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                transaccionesInforme.clear()
                                                rcvTransaccionesInforme.adapter =
                                                    adapterTransaccionesInforme
                                                rcvTransaccionesInforme.itemAnimator =
                                                    DefaultItemAnimator()
                                                rcvTransaccionesInforme.layoutManager =
                                                    LinearLayoutManager(
                                                        this,
                                                        LinearLayoutManager.VERTICAL,
                                                        false
                                                    )
                                                adapterTransaccionesInforme.notifyDataSetChanged()

                                                val layout = subMenuLayout.layoutParams
                                                layout.height = (20 * scale!! + 0.5f).toInt()
                                                subMenuLayout.layoutParams = layout
                                                estadoSubmenu = "C"
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Seleccione una Cuenta Contable de la lista",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Seleccione una Cuenta Contable de la lista",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Seleccione un Cliente de la lista",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Seleccione un Cliente de la lista",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }

}