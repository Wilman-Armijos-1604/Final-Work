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
import com.google.firebase.ktx.Firebase
import com.ticml.contabilidad.objetos.Cuenta
import com.ticml.contabilidad.objetos.CuentaContable
import java.util.ArrayList
import kotlin.system.exitProcess

class InformacionCuentaEmpresa : AppCompatActivity() {

    private val db = Firebase.firestore
    private val arrayAutoComplete = ArrayList<CuentaContable>()
    private var tipoCuentaAuxiliar: String = ""
    lateinit var cuentaOpcion: CuentaContable
    var banderaTipoCuenta = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion_cuenta_empresa)

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

        val btnGuardar = findViewById<Button>(R.id.btn_aceptarDatosCuentaOrganizacion)
        val btnEliminar = findViewById<Button>(R.id.btn_eliminarCuentaOrganizacion)

        val txtIdentification = findViewById<EditText>(R.id.txt_identificacionCuentaOrganizacion)
        val txtOrganizacion = findViewById<EditText>(R.id.txt_organizacionCuentaOrganizacion)
        val txtResponsable = findViewById<EditText>(R.id.txt_responsableCuentaOrganizacion)
        val txtTipoCuenta = findViewById<AutoCompleteTextView>(R.id.txt_tipoCuentaOrganizacion)

        db.collection("CuentasContables")
            .get()
            .addOnSuccessListener { cuentasContablesFirestore ->
                for (cuenta in cuentasContablesFirestore) {
                    arrayAutoComplete.add(
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

        txtTipoCuenta.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayAutoComplete
            )
        )
        txtTipoCuenta.threshold = 1
        txtTipoCuenta.setOnItemClickListener { adapterView, view, i, l ->
            cuentaOpcion = adapterView.getItemAtPosition(i) as CuentaContable
            txtTipoCuenta.setText(cuentaOpcion.nivel5)
            tipoCuentaAuxiliar = cuentaOpcion.auxNiveles
            banderaTipoCuenta = true
        }


        if (intentOrigen != null) {
            if (!intentOrigen.isEmpty) {
                val valorOrganizacion = intentOrigen.get("organizacion")
                val valorIdentificacion = intentOrigen.get("identificacion")
                val valorResponsable = intentOrigen.get("responsable")
                val valorTipoCuenta = intentOrigen.get("tipoCuenta")
                db.collection("CuentasContables")
                    .document(intentOrigen.get("idTipoCuenta").toString())
                    .get()
                    .addOnSuccessListener {
                        tipoCuentaAuxiliar = it.data?.get("auxNiveles").toString()
                        cuentaOpcion = CuentaContable(
                            nivel1 = it.data?.get("nivel1").toString(),
                            nivel2 = it.data?.get("nivel2").toString(),
                            nivel3 = it.data?.get("nivel3").toString(),
                            nivel4 = it.data?.get("nivel4").toString(),
                            nivel5 = it.data?.get("nivel5").toString(),
                            auxNiveles = tipoCuentaAuxiliar
                        )
                    }
                txtIdentification.setText(valorIdentificacion.toString())
                txtOrganizacion.setText(valorOrganizacion.toString())
                txtResponsable.setText(valorResponsable.toString())
                txtTipoCuenta.setText(valorTipoCuenta.toString())
                txtOrganizacion.isEnabled = false
                txtOrganizacion.setTextColor(getColor(R.color.darkGreyML))
                txtOrganizacion.setBackgroundColor(getColor(R.color.white))
                txtIdentification.isEnabled = false
                txtIdentification.setTextColor(getColor(R.color.darkGreyML))
                txtIdentification.setBackgroundColor(getColor(R.color.white))
                txtTipoCuenta.isEnabled = false
                txtTipoCuenta.setTextColor(getColor(R.color.darkGreyML))
                txtTipoCuenta.setBackgroundColor(getColor(R.color.white))
                banderaTipoCuenta = true
            }
        }

        btnGuardar.setOnClickListener {
            if (txtIdentification.text.toString() == "" || txtResponsable.text.toString() == "" || txtOrganizacion.text.toString() == "" || txtTipoCuenta.text.toString() == "") {
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_LONG).show()
            } else {
                if (banderaTipoCuenta) {
                    if (txtTipoCuenta.text.toString() != cuentaOpcion.nivel5) {
                        Toast.makeText(
                            this,
                            "Seleccione una opción de la lista de Tipo de Cuenta",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        db.collection("CuentasContables")
                            .whereEqualTo("auxNiveles", tipoCuentaAuxiliar)
                            .get()
                            .addOnSuccessListener {
                                db.collection("CuentasOrganizacion")
                                    .document(txtIdentification.text.toString() + txtOrganizacion.text.toString())
                                    .set(
                                        Cuenta(
                                            organizacion = txtOrganizacion.text.toString(),
                                            identificacion = txtIdentification.text.toString(),
                                            responsable = txtResponsable.text.toString(),
                                            tipo = txtTipoCuenta.text.toString(),
                                            idTipo = it.documents[0].id
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Información guardada exitosamente.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        this.finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "No se pudo guardar la información.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Seleccione una opción de la lista de Tipo de Cuenta",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        btnEliminar.setOnClickListener {
            if (txtIdentification.text.toString() == "" || txtIdentification.isEnabled) {
                Toast.makeText(this, "No se puede eliminar la cuenta", Toast.LENGTH_LONG).show()
            } else {
                val confirmacion = AlertDialog.Builder(this)
                confirmacion
                    .setMessage("¿Seguro que quiere eliminar la cuenta?")
                    .setPositiveButton("Sí", DialogInterface.OnClickListener { dialog, which ->
                        db.collection("CuentasOrganizacion")
                            .document(txtIdentification.text.toString() + txtOrganizacion.text.toString())
                            .delete()
                            .addOnSuccessListener {
                                Log.i(
                                    "Cuentas",
                                    "El nombre del documento a eliminar es: ${txtIdentification.text.toString() + txtOrganizacion.text.toString()}"
                                )
                                Toast.makeText(
                                    this,
                                    "Cuenta eliminada exitosamente.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                this.finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "No se pudo eliminar la cuenta.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                    }
                    )
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        dialog.cancel()
                    }
                    )
                    .create()
                    .show()
            }

        }

    }
}