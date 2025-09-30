package com.dam2.redpro.Cliente.Orden

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dam2.redpro.Adaptadores.AdaptadorProductoOrden
import com.dam2.redpro.Cliente.Pago.PagoActivity
import com.dam2.redpro.Constantes
import com.dam2.redpro.Modelos.ModeloProductoOrden
import com.dam2.redpro.Modelos.ResponseHttp
import com.dam2.redpro.Network.RetrofitClient
import com.dam2.redpro.R
import com.dam2.redpro.data.ApiService
import com.dam2.redpro.databinding.ActivityDetalleOrdenCactivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DetalleOrdenCActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleOrdenCactivityBinding
    private var idOrden = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList : ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdapter : AdaptadorProductoOrden

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: ""

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinuarPago.setOnClickListener {
            enviarProductosAlServidor(productosArrayList)
        }

        datosOrden()
        direccionCliente()
        productosOrden()
    }

    private fun enviarProductosAlServidor(productos : ArrayList<ModeloProductoOrden>) {
        val datosParaEnviar = mapOf(
            "idOrden" to idOrden,
            "costoTotal" to costo,
            "productos" to productos,
            "currentId" to ordenadoPor)

        println("Mapa de datos para convertir a json : ${datosParaEnviar}")

        //Convertir los datos del mapa a una cadena de texto en formato json
        val gson = Gson()
        val jsonDatos = gson.toJson(datosParaEnviar)

        println("Datos de texto en formato json: ${jsonDatos}")

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = jsonDatos.toRequestBody(mediaType)

        val apiService = RetrofitClient.retrofit.create(ApiService::class.java)
        val call = apiService.enviarOrdenDeCompra(requestBody)
        call.enqueue(object : Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                if (response.isSuccessful){
                    println("Orden enviada correctamente")
                    println("Respuesta del servidor: ${response.body()?.message}")
                    println("preferenceId: ${response.body()?.preferenceId}")
                    println("init_point: ${response.body()?.init_point}")

                    /*Obteniendo init_point del servidor y convertimos a string*/
                    val init_point = response.body()?.init_point.toString()

                    /*Lo mandamos como parámetro*/
                    val intent = Intent(this@DetalleOrdenCActivity , PagoActivity::class.java)
                    intent.putExtra("init_point", init_point)
                    startActivity(intent)

                }else if (response.code() == 500){
                    println("Ha ocurrido un error en el servidor: ${response.errorBody()?.toString()}")
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                println("Ha ocurrido un error al hacer la petición: ${t.message}")

            }
        })

    }

    private fun productosOrden(){
        productosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden).child("Productos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProductoOrden = ds.getValue(ModeloProductoOrden::class.java)
                    productosArrayList.add(modeloProductoOrden!!)
                }

                productoOrdenAdapter = AdaptadorProductoOrden(this@DetalleOrdenCActivity, productosArrayList)
                binding.ordenesRv.adapter = productoOrdenAdapter

                binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })





    }

    private fun direccionCliente(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val direccion = "${snapshot.child("direccion").value}"


                    if (direccion.isNotEmpty()){
                        //Si el usuario registró su ubicación
                        binding.direccionOrdenD.text = direccion
                    }else{
                        //Si el usuario no registró su ubicación
                        binding.direccionOrdenD.text = "Registre su ubicación para continuar"
                        binding.btnContinuarPago.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    var costo = ""
    var ordenadoPor = ""
    private fun datosOrden(){
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val idOrden = "${snapshot.child("idOrden").value}"
                costo = "${snapshot.child("costo").value}"
                val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                val estadoOrden = "${snapshot.child("estadoOrden").value}"
                ordenadoPor = "${snapshot.child("ordenadoPor").value}"

                val fecha = Constantes().obtenerFecha(tiempoOrden.toLong())

                binding.idOrdenD.text = idOrden
                binding.fechaOrdenD.text = fecha
                binding.estadoOrdenD.text = estadoOrden
                binding.costoOrdenD.text = costo

                if (estadoOrden.equals("Solicitud recibida")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.azul_marino_oscuro))
                }else if (estadoOrden.equals("En preparación")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.naranja))
                }else if (estadoOrden.equals("Entregado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.verde_oscuro2))
                }else if (estadoOrden.equals("Cancelado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.rojo))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })







    }
}