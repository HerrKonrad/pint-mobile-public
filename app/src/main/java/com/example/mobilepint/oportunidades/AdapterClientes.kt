package com.example.mobilepint.oportunidades

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.util.Locale

class AdapterClientes(
    private val mcontext: Context,
    private val ClientesList: ArrayList<ItemsClientes>
) :
    RecyclerView.Adapter<AdapterClientes.ClientesViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsClientes> = ArrayList(ClientesList)
    private var listaAtual: ArrayList<ItemsClientes> = ArrayList(ClientesList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_clientes, parent, false)
        return ClientesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientesViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeCliente = currentItem.NomeEmp
        val emailCliente = currentItem.EmailEmp
        val telefoneCliente = currentItem.TelefoneEmp

        holder.holderNomeCliente.text = nomeCliente
        holder.holderEmailCliente.text = emailCliente
        holder.holderTelefoneCliente.text = telefoneCliente
        holder.cardViewClientes.setOnClickListener {
            GlobalVariables.detalhesNumCliente = currentItem.NCliente
            GlobalVariables.detalhesNomeCliente = currentItem.NomeEmp
            GlobalVariables.detalhesEmailCliente = currentItem.EmailEmp
            GlobalVariables.detalhesTelefoneCliente = currentItem.TelefoneEmp
            GlobalVariables.detalhesDescricaoCliente = currentItem.Descricao
            GlobalVariables.detalhesNumUsuarioCriadorCliente = currentItem.NUsuarioCriador
            GlobalVariables.detalhesNomeUsuarioCriadorCliente = currentItem.NomeUsuarioCriador
            GlobalVariables.detalhesDataCriacaoCliente = currentItem.DataHoraCriacao

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesCliente())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return listaAtual.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence.isNullOrEmpty()) {
                    listaAtual = ArrayList(listaOriginal)
                } else {
                    val filterPattern = charSequence.toString().toLowerCase(Locale.ROOT).trim()
                    listaAtual.clear()
                    for (item in listaOriginal) {
                        if (item.NomeEmp.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsClientes> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class ClientesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewClientes: CardView = itemView.findViewById(R.id.CardViewClientes)
        val holderNomeCliente: TextView = itemView.findViewById(R.id.nomeCliente)
        val holderEmailCliente: TextView = itemView.findViewById(R.id.emailCliente)
        val holderTelefoneCliente: TextView = itemView.findViewById(R.id.telefoneCliente)
    }
}