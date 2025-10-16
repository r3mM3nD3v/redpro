package com.dam2.redpro.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam2.redpro.Modelos.ModeloUsuario
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ItemClienteBinding

/**
 * Adapter de clientes.
 * - Muestra imagen, nombre, email, DNI y dirección.
 * - Oculta cualquier UI relacionada a "proveedor" de autenticación.
 */
class AdaptadorCliente(
    private val mContext: Context,
    private var usuarioArrayList: ArrayList<ModeloUsuario>
) : RecyclerView.Adapter<AdaptadorCliente.HolderUsuario>() {

    private lateinit var binding: ItemClienteBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {
        binding = ItemClienteBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderUsuario(binding.root)
    }

    override fun getItemCount(): Int = usuarioArrayList.size

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val u = usuarioArrayList[position]

        holder.nombres.text = u.nombres
        holder.email.text = u.email
        holder.dni.text = u.dni
        holder.ubicacion.text = u.direccion

        // 🔒 Ocultar proveedor (evita exponer phone/password/google, etc.)
        holder.proveedor.visibility = View.GONE
        holder.proveedor.text = ""

        // Imagen con placeholder y fallback
        Glide.with(mContext)
            .load(u.imagen.ifBlank { null })
            .placeholder(R.drawable.img_perfil)
            .error(R.drawable.img_perfil)
            .into(holder.imagen)
    }

    inner class HolderUsuario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen = binding.imagenC
        val nombres = binding.nombresCPerfil
        val email = binding.emailCPerfil
        val dni = binding.dniCPerfil
        val ubicacion = binding.ubicacion
        val proveedor = binding.proveedorCPerfil // Se mantiene en binding pero se oculta siempre
    }
}