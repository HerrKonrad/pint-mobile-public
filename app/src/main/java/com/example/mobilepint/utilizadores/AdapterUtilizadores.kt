package com.example.mobilepint.utilizadores

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

class AdapterUtilizadores(
    private val mcontext: Context,
    private val UtilizadoresList: ArrayList<ItemsUtilizadores>
) :
    RecyclerView.Adapter<AdapterUtilizadores.UtilizadoresViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsUtilizadores> = ArrayList(UtilizadoresList)
    private var listaAtual: ArrayList<ItemsUtilizadores> = ArrayList(UtilizadoresList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtilizadoresViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_utilizadores, parent, false)
        return UtilizadoresViewHolder(view)
    }

    override fun onBindViewHolder(holder: UtilizadoresViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeUtilizador = currentItem.Nome
        val emailUtilizador = currentItem.Email

        holder.holderNomeUtilizador.text = nomeUtilizador
        holder.holderEmailUtilizador.text = emailUtilizador
        holder.cardViewUtilizadores.setOnClickListener {
            GlobalVariables.detalhesNumUtilizador = currentItem.NUsuario
            GlobalVariables.detalhesNomeUtilizador = currentItem.Nome
            GlobalVariables.detalhesEmailUtilizador = currentItem.Email
            GlobalVariables.detalhesNumCargoUtilizador = currentItem.NCargo
            GlobalVariables.detalhesTelefoneUtilizador = currentItem.Telefone
            GlobalVariables.detalhesLinkedinUtilizador = currentItem.Linkedin
            GlobalVariables.detalhesDataNascimentoUtilizador = currentItem.DataNascimento
            GlobalVariables.detalhesGeneroUtilizador = currentItem.Genero
            GlobalVariables.detalhesLocalidadeUtilizador = currentItem.Localidade
            GlobalVariables.detalhesEstadoUtilizador = currentItem.Estado
            GlobalVariables.detalhesDataCriacaoUtilizador = currentItem.DataHoraRegisto

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesUtilizador())
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
                        if (item.Nome.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsUtilizadores> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class UtilizadoresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewUtilizadores: CardView = itemView.findViewById(R.id.CardViewUtilizadores)
        val holderNomeUtilizador: TextView = itemView.findViewById(R.id.nomeUtilizador)
        val holderEmailUtilizador: TextView = itemView.findViewById(R.id.emailUtilizador)
    }
}