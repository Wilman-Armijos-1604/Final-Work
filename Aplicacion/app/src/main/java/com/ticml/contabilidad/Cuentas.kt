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
import com.ticml.contabilidad.objetos.Cuenta
import com.ticml.contabilidad.subinterfaces.Rcv_CuentaOrganizacion
import com.ticml.contabilidad.subinterfaces.Rcv_CuentaTercero
import kotlin.system.exitProcess

class Cuentas : AppCompatActivity() {

    private val db = Firebase.firestore

    val cuentasOrganizacion = ArrayList<Cuenta>()
    val cuentasTerceros = ArrayList<Cuenta>()

    lateinit var adapterCuentasOrganizacion: Rcv_CuentaOrganizacion
    lateinit var adapterCuentasTerceros: Rcv_CuentaTercero

    lateinit var rcvCuentasOrganizacion: RecyclerView
    lateinit var rcvCuentasTerceros: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuentas)

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

        val btnCrearCuentaOrganizacion =
            findViewById<ConstraintLayout>(R.id.btn_crearNuevaCuentaOrganizacion)
        val btnCrearCuentaTercero =
            findViewById<ConstraintLayout>(R.id.btn_crearNuevaCuentaTerceros)

        rcvCuentasOrganizacion = findViewById<RecyclerView>(R.id.rcv_cuentasOrganizacion)
        adapterCuentasOrganizacion =
            Rcv_CuentaOrganizacion(this, rcvCuentasOrganizacion, cuentasOrganizacion)
        rcvCuentasTerceros = findViewById<RecyclerView>(R.id.rcv_cuentasTerceros)
        adapterCuentasTerceros = Rcv_CuentaTercero(this, rcvCuentasTerceros, cuentasTerceros)

        db.collection("CuentasOrganizacion")
            .get()
            .addOnSuccessListener { cuentasFirestore ->
                for (cuentaOrganizacion in cuentasFirestore) {
                    cuentasOrganizacion.add(
                        Cuenta(
                            organizacion = cuentaOrganizacion.getString("organizacion"),
                            identificacion = cuentaOrganizacion.getString("identificacion"),
                            tipo = cuentaOrganizacion.getString("tipo"),
                            responsable = cuentaOrganizacion.getString("responsable"),
                            idTipo = cuentaOrganizacion.getString("idTipo")
                        )
                    )
                }
                rcvCuentasOrganizacion.adapter = adapterCuentasOrganizacion
                rcvCuentasOrganizacion.itemAnimator = DefaultItemAnimator()
                rcvCuentasOrganizacion.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterCuentasOrganizacion.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Cuentas de Usuario", Toast.LENGTH_LONG).show()
            }



        db.collection("CuentasTerceros")
            .get()
            .addOnSuccessListener { cuentasFirestore ->
                for (cuentaTercero in cuentasFirestore) {
                    cuentasTerceros.add(
                        Cuenta(
                            organizacion = cuentaTercero.getString("organizacion"),
                            identificacion = cuentaTercero.getString("identificacion"),
                            tipo = cuentaTercero.getString("tipo"),
                            responsable = cuentaTercero.getString("responsable"),
                            idTipo = cuentaTercero.getString("idTipo")
                        )
                    )
                }
                rcvCuentasTerceros.adapter = adapterCuentasTerceros
                rcvCuentasTerceros.itemAnimator = DefaultItemAnimator()
                rcvCuentasTerceros.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterCuentasTerceros.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Cuentas de Usuario", Toast.LENGTH_LONG).show()
            }

        btnCrearCuentaOrganizacion.setOnClickListener {
            this.startActivity(Intent(this, InformacionCuentaEmpresa::class.java))
        }

        btnCrearCuentaTercero.setOnClickListener {
            this.startActivity(Intent(this, InformacionCuentaTercero::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        db.collection("CuentasOrganizacion")
            .get()
            .addOnSuccessListener { cuentasFirestore ->
                cuentasOrganizacion.clear()
                for (cuentaOrganizacion in cuentasFirestore) {
                    cuentasOrganizacion.add(
                        Cuenta(
                            organizacion = cuentaOrganizacion.getString("organizacion"),
                            identificacion = cuentaOrganizacion.getString("identificacion"),
                            tipo = cuentaOrganizacion.getString("tipo"),
                            responsable = cuentaOrganizacion.getString("responsable"),
                            idTipo = cuentaOrganizacion.getString("idTipo")
                        )
                    )
                }
                rcvCuentasOrganizacion.adapter = adapterCuentasOrganizacion
                rcvCuentasOrganizacion.itemAnimator = DefaultItemAnimator()
                rcvCuentasOrganizacion.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterCuentasOrganizacion.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Cuentas de Usuario", Toast.LENGTH_LONG).show()
            }

        db.collection("CuentasTerceros")
            .get()
            .addOnSuccessListener { cuentasFirestore ->
                cuentasTerceros.clear()
                for (cuentaTercero in cuentasFirestore) {
                    cuentasTerceros.add(
                        Cuenta(
                            organizacion = cuentaTercero.getString("organizacion"),
                            identificacion = cuentaTercero.getString("identificacion"),
                            tipo = cuentaTercero.getString("tipo"),
                            responsable = cuentaTercero.getString("responsable"),
                            idTipo = cuentaTercero.getString("idTipo")
                        )
                    )
                }
                rcvCuentasTerceros.adapter = adapterCuentasTerceros
                rcvCuentasTerceros.itemAnimator = DefaultItemAnimator()
                rcvCuentasTerceros.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                adapterCuentasTerceros.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Cargar Cuentas de Usuario", Toast.LENGTH_LONG).show()
            }
    }

}