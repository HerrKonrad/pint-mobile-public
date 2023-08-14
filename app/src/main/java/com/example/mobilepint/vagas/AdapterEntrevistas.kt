package com.example.mobilepint.vagas

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

class AdapterEntrevistas(
    private val mcontext: Context,
    private val EntrevistasList: ArrayList<ItemsEntrevistas>
) :
    RecyclerView.Adapter<AdapterEntrevistas.EntrevistasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsEntrevistas> = ArrayList(EntrevistasList)
    private var listaAtual: ArrayList<ItemsEntrevistas> = ArrayList(EntrevistasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntrevistasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_entrevistas, parent, false)
        return EntrevistasViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntrevistasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeUtilizador = currentItem.NomeUsuarioCandidatura
        val nomeVaga = currentItem.NomeVagaCandidatura

        holder.holderNomeUtilizador.text = nomeUtilizador
        holder.holderNomeVaga.text = nomeVaga
        holder.holderEstadoEntrevista.text = currentItem.EstadoEntrevista
        holder.cardViewEntrevistas.setOnClickListener {
            GlobalVariables.detalhesNumEntrevista = currentItem.NEntrevista
            GlobalVariables.detalhesDescricaoEntrevista = currentItem.DescricaoEntrevista
            GlobalVariables.detalhesEstadoEntrevista = currentItem.EstadoEntrevista
            GlobalVariables.detalhesNomeUsuarioCandidatura = currentItem.NomeUsuarioCandidatura
            GlobalVariables.detalhesNomeVagaCandidatura = currentItem.NomeVagaCandidatura

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesEntrevista())
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
                        if (item.NomeVagaCandidatura.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsEntrevistas> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class EntrevistasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewEntrevistas: CardView = itemView.findViewById(R.id.CardViewEntrevistas)
        val holderNomeUtilizador: TextView = itemView.findViewById(R.id.nomeUtilizadorEntrevista)
        val holderNomeVaga: TextView = itemView.findViewById(R.id.nomeVagaEntrevista)
        val holderEstadoEntrevista: TextView = itemView.findViewById(R.id.estadoEntrevista)
    }
}