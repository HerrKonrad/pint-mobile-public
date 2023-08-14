package com.example.mobilepint.oportunidades

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R

class AdapterContactosClientes(
    private val mcontext: Context,
    private val ContactosClientesList: ArrayList<ItemsContactos>
) :
    RecyclerView.Adapter<AdapterContactosClientes.ClientesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_contactos_cliente, parent, false)
        return ClientesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientesViewHolder, position: Int) {
        val currentItem = ContactosClientesList[position]
        val emailCliente = currentItem.Email
        val telefoneCliente = currentItem.Telefone

        holder.holderEmailCliente.text = emailCliente
        holder.holderTelefoneCliente.text = telefoneCliente
        holder.cardViewContactosCliente.setOnClickListener {
            GlobalVariables.detalhesNumContacto = currentItem.NContactos
            GlobalVariables.detalhesEmailContacto = currentItem.Email
            GlobalVariables.detalhesTelefoneContacto = currentItem.Telefone

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesContactoCliente())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return ContactosClientesList.size
    }

    class ClientesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewContactosCliente: CardView = itemView.findViewById(R.id.CardViewContactosCliente)
        val holderEmailCliente: TextView = itemView.findViewById(R.id.contactoEmailCliente)
        val holderTelefoneCliente: TextView = itemView.findViewById(R.id.contactoTelefoneCliente)
    }
}