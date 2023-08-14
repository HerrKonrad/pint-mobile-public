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

class AdapterNotasEntrevistas(
    private val mcontext: Context,
    private val NotasEntrevistasList: ArrayList<ItemsNotasEntrevistas>
) :
    RecyclerView.Adapter<AdapterNotasEntrevistas.NotasEntrevistasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsNotasEntrevistas> = ArrayList(NotasEntrevistasList)
    private var listaAtual: ArrayList<ItemsNotasEntrevistas> = ArrayList(NotasEntrevistasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotasEntrevistasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_notas_entrevistas, parent, false)
        return NotasEntrevistasViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotasEntrevistasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeUtilizadorRH = currentItem.NomeRH
        val textoNota = currentItem.Texto

        holder.holderNomeUtilizadorNotaEntrevista.text = nomeUtilizadorRH
        holder.holderTextoNotaEntrevista.text = textoNota
        holder.cardViewNotasEntrevistas.setOnClickListener {
            GlobalVariables.detalhesNumEntrevista = currentItem.NEntrevista
            GlobalVariables.detalhesEstadoEntrevista = currentItem.EstadoEntrevista
            GlobalVariables.detalhesNomeUsuarioCandidatura = currentItem.NomeUsuarioCandidatura
            GlobalVariables.detalhesNomeVagaCandidatura = currentItem.NomeVagaCandidatura
            GlobalVariables.detalhesNumNota = currentItem.NNota
            GlobalVariables.detalhesTextoNota = currentItem.Texto
            GlobalVariables.detalhesAutorNota = currentItem.NomeRH
            GlobalVariables.detalhesDataNota = currentItem.DataHora

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesNotaEntrevista())
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
                        if (item.Texto.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsNotasEntrevistas> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class NotasEntrevistasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewNotasEntrevistas: CardView = itemView.findViewById(R.id.CardViewNotasEntrevistas)
        val holderNomeUtilizadorNotaEntrevista: TextView = itemView.findViewById(R.id.nomeUtilizadorNotaEntrevista)
        val holderTextoNotaEntrevista: TextView = itemView.findViewById(R.id.textoNotaEntrevista)
    }
}