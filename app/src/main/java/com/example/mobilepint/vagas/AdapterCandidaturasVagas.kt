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

class AdapterCandidaturasVagas(
    private val mcontext: Context,
    private val CandidaturasVagasList: ArrayList<ItemsCandidaturas>
) :
    RecyclerView.Adapter<AdapterCandidaturasVagas.CandidaturasVagasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsCandidaturas> = ArrayList(CandidaturasVagasList)
    private var listaAtual: ArrayList<ItemsCandidaturas> = ArrayList(CandidaturasVagasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidaturasVagasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_candidaturas_vagas, parent, false)
        return CandidaturasVagasViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidaturasVagasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val globalVariables = GlobalVariables()
        val numCargo = GlobalVariables.idCargoUtilizadorAutenticado

        val nomeVaga = currentItem.NomeVaga
        val nomeUtilizador = currentItem.NomeUsuario
        val estagioCandidatura = currentItem.Estagio

        holder.holderCandidaturaNomeVaga.text = nomeVaga
        holder.holderCandidaturaNomeUtilizador.text = nomeUtilizador
        holder.holderCandidaturaEstagio.text = estagioCandidatura
        holder.CardViewCandidaturasVagas.setOnClickListener {
            GlobalVariables.criarCandidatura = false
            GlobalVariables.detalhesNumCandidatura = currentItem.NCandidatura
            GlobalVariables.detalhesNumVagaCandidatura = currentItem.NVaga
            GlobalVariables.detalhesNumUsuarioCandidatura = currentItem.NUsuario
            GlobalVariables.detalhesDataCandidatura = currentItem.DataCandidatura
            GlobalVariables.detalhesPretencaoSalarial = currentItem.PretencaoSalarial
            GlobalVariables.detalhesMensagemCandidatura = currentItem.Mensagem
            GlobalVariables.detalhesEstadoCandidatura = currentItem.Estado
            GlobalVariables.detalhesEstagioCandidatura = currentItem.Estagio
            GlobalVariables.detalhesNomeUsuarioCandidatura = currentItem.NomeUsuario
            GlobalVariables.detalhesNomeVagaCandidatura = currentItem.NomeVaga
            GlobalVariables.detalhesSubtituloVagaCandidatura = currentItem.SubtituloVaga
            GlobalVariables.detalhesEmailUsuarioCandidatura = currentItem.EmailUsuario

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentCriarCandidaturaVaga())
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
                        if (item.NomeVaga.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsCandidaturas> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class CandidaturasVagasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val CardViewCandidaturasVagas: CardView = itemView.findViewById(R.id.CardViewCandidaturasVagas)
        val holderCandidaturaNomeVaga: TextView = itemView.findViewById(R.id.candidaturaNomeVaga)
        val holderCandidaturaNomeUtilizador: TextView = itemView.findViewById(R.id.candidaturaNomeUtilizador)
        val holderCandidaturaEstagio: TextView = itemView.findViewById(R.id.candidaturaEstagio)
    }
}