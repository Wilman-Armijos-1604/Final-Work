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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.DocumentoDetalle
import com.ticml.contabilidad.objetos.DocumentoTransaccion
import kotlin.system.exitProcess

class Documento : AppCompatActivity() {

    val db = Firebase.firestore

    private val arrayAutoCompleteTipoDocumento = ArrayList<String>()
    private val arrayAutoCompleteNumeroDocumentoTransaccion = ArrayList<DocumentoDetalle>()
    private val arrayAutoCompleteTipoTransaccion = ArrayList<String>()

    lateinit var tipoDocumentoOpcion: String
    lateinit var numeroDocumentoTransaccionOpcion: DocumentoDetalle
    lateinit var tipoTransaccionOpcion: String

    var banderaTipoDocumento = false
    var banderaNumeroDocumento = false
    var banderaTipoTransaccion = false
    var banderaCamposSeleccionados = true

    var numDocumentos: Int? = 0
    lateinit var idTransaccion: String
    lateinit var idDocumento: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documento)

        val pattern = Regex("^([0-9]+(.[0-9]{1,2})?)\$")

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

        val tipoDocumento = findViewById<AutoCompleteTextView>(R.id.txt_tipoDocumento)
        val numeroDocumento = findViewById<AutoCompleteTextView>(R.id.txt_numeroDocumento)
        val tipoTransaccion = findViewById<AutoCompleteTextView>(R.id.txt_tipoTransaccionDocumento)
        val valorDocumento = findViewById<EditText>(R.id.txv_valorRegistroDocumento)

        val btnAgregarDocumento = findViewById<Button>(R.id.btn_guardarDocumento)

        val intentOrigen = intent.extras
        idTransaccion = intentOrigen!!.get("idTransaccion").toString()

        arrayAutoCompleteTipoDocumento.add("Factura")
        arrayAutoCompleteTipoDocumento.add("Nota de Credito")
        arrayAutoCompleteTipoDocumento.add("Nota de Debito")

        arrayAutoCompleteTipoTransaccion.add("Ingreso")
        arrayAutoCompleteTipoTransaccion.add("Egreso")


        db.collection("Documentos")
            .whereEqualTo("idTransaccionDocumento", idTransaccion)
            .get()
            .addOnSuccessListener { transaccionesContablesFirestore ->
                numDocumentos = transaccionesContablesFirestore.size() + 1
            }

        tipoDocumento.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteTipoDocumento
            )
        )
        tipoDocumento.threshold = 1
        tipoDocumento.setOnItemClickListener { adapterView, view, i, l ->
            tipoDocumentoOpcion = adapterView.getItemAtPosition(i) as String
            db.collection("DocumentosDetalle")
                .whereEqualTo("tipoDocumento", tipoDocumentoOpcion)
                .get()
                .addOnSuccessListener { documentosSegunTipoFirestore ->
                    for (documentosSegunTipo in documentosSegunTipoFirestore) {
                        arrayAutoCompleteNumeroDocumentoTransaccion.add(documentosSegunTipo.toObject<DocumentoDetalle>())
                    }
                }
            banderaTipoDocumento = true
        }

        numeroDocumento.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteNumeroDocumentoTransaccion
            )
        )
        numeroDocumento.threshold = 1
        numeroDocumento.setOnItemClickListener { adapterView, view, i, l ->
            numeroDocumentoTransaccionOpcion = adapterView.getItemAtPosition(i) as DocumentoDetalle
            numeroDocumento.setText(numeroDocumentoTransaccionOpcion.numeroDocumento.toString())
            banderaNumeroDocumento = true
        }

        tipoTransaccion.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoCompleteTipoTransaccion
            )
        )
        tipoTransaccion.threshold = 1
        tipoTransaccion.setOnItemClickListener { adapterView, view, i, l ->
            tipoTransaccionOpcion = adapterView.getItemAtPosition(i) as String
            banderaTipoTransaccion = true
        }

        btnAgregarDocumento.setOnClickListener {
            Thread.sleep(1000)
            var cont = 0
            if (tipoDocumento.text.toString() == "" || numeroDocumento.text.toString() == "" || tipoTransaccion.text.toString() == "" || valorDocumento.text.toString() == "") {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_LONG).show()
            } else {
                if (!pattern.matches(valorDocumento.text.toString())) {
                    Toast.makeText(this, "El valor debe ser un número", Toast.LENGTH_LONG).show()
                } else {
                    if (!banderaTipoDocumento || !arrayAutoCompleteTipoDocumento.contains(
                            tipoDocumento.text.toString()
                        )
                    ) {
                        Toast.makeText(
                            this,
                            "Seleccione una opción de la lista de Tipo de Tipos de Documento",
                            Toast.LENGTH_LONG
                        ).show()
                        banderaCamposSeleccionados = false
                    } else {
                        cont++
                    }
                    if (!banderaNumeroDocumento || numeroDocumento.text.toString() != numeroDocumentoTransaccionOpcion.numeroDocumento) {
                        Toast.makeText(
                            this,
                            "Seleccione una opción de la lista de Números de Documento",
                            Toast.LENGTH_LONG
                        ).show()
                        banderaCamposSeleccionados = false
                    } else {
                        cont++
                    }
                    if (!banderaTipoTransaccion || !arrayAutoCompleteTipoTransaccion.contains(
                            tipoTransaccion.text.toString()
                        )
                    ) {
                        Toast.makeText(
                            this,
                            "Seleccione una opción de la lista de Tipo de Tipos de Transaccion",
                            Toast.LENGTH_LONG
                        ).show()
                        banderaCamposSeleccionados = false
                    } else {
                        cont++
                    }
                    if (cont==3) {
                        banderaCamposSeleccionados=true
                    }
                    if (banderaCamposSeleccionados) {
                        val nuevoDocumento = DocumentoTransaccion(
                            idDocumentoDetalle = numeroDocumentoTransaccionOpcion.tipoDocumento + numeroDocumentoTransaccionOpcion.numeroDocumento + numeroDocumentoTransaccionOpcion.nombreEmisor,
                            idTransaccionDocumento = idTransaccion,
                            tipoDocumento = tipoDocumento.text.toString(),
                            identificacionDocumento = numeroDocumentoTransaccionOpcion.numeroDocumento,
                            tipoTransaccion = tipoTransaccion.text.toString(),
                            valor = valorDocumento.text.toString().toDouble(),
                        )
                        val docsVal = getSharedPreferences("documento", Context.MODE_PRIVATE)
                        val editor = docsVal.edit()
                        editor.putString("documento_idDocumentoDetalle",nuevoDocumento.idDocumentoDetalle)
                        editor.putString("documento_idTransaccionDocumento",nuevoDocumento.idTransaccionDocumento)
                        editor.putString("documento_tipoDocumento",nuevoDocumento.tipoDocumento)
                        editor.putString("documento_identificacionDocumento",nuevoDocumento.identificacionDocumento)
                        editor.putString("documento_tipoTransaccion",nuevoDocumento.tipoTransaccion)
                        editor.putString("documento_valor",nuevoDocumento.valor.toString())
                        editor.commit()
                        this.finish()
                    }
                }
            }

        }

    }

}