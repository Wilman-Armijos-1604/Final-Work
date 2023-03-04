package com.ticml.contabilidad

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class InicioSesion : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        if (intent.extras?.isEmpty == false) {
            val intentOrigen = intent.extras
            val terminar = intentOrigen!!.get("terminar").toString()
            if (terminar == "SALIR") {
                finish()
            }
        }

        val txtIngresoUsuario = findViewById<EditText>(R.id.txt_Usuario)
        val txtIngresoClave = findViewById<EditText>(R.id.txt_Clave)
        val btnCredencialesRecuperacion = findViewById<TextView>(R.id.btn_olvidoClave)
        val btnIniciarsesion = findViewById<Button>(R.id.btn_Ingresar)


        btnCredencialesRecuperacion.setOnClickListener {
            Toast.makeText(this, "Comuniquese con Soporte de TI de la empresa", Toast.LENGTH_LONG)
                .show()
        }

        Log.i("Hash", "El valor del hash: ${hash(txtIngresoClave.text.toString())}")

        btnIniciarsesion.setOnClickListener {
            if (txtIngresoUsuario.text.toString() == "" || txtIngresoClave.text.toString() == "") {
                Toast.makeText(this, "Rellene todos los datos", Toast.LENGTH_LONG).show()
            } else {
                db.collection("Usuarios").document(txtIngresoUsuario.text.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            if (it.data?.size!! > 0) {
                                if (it.data?.get("clave") == hash(txtIngresoClave.text.toString())) {
                                    this.startActivity(Intent(this, PaginaPrincipal::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Usuario y/o Clave incorrectos",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            } else {
                                val toast =
                                    Toast.makeText(this, "El usuario no existe", Toast.LENGTH_LONG)
                                toast.show()
                            }
                        } else {
                            val toast =
                                Toast.makeText(this, "El usuario no existe", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Ha ocurrido un error, intente nuevamente",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

    }

    fun hash(string: String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

}
