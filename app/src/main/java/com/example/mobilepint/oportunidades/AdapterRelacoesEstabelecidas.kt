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

class AdapterRelacoesEstabelecidas(
    private val mcontext: Context,
    private val RelacoesEstabelecidasList: ArrayList<ItemsRelacoesEstabelecidas>
) :
    RecyclerView.Adapter<AdapterRelacoesEstabelecidas.RelacoesEstabelecidasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsRelacoesEstabelecidas> = ArrayList(RelacoesEstabelecidasList)
    private var listaAtual: ArrayList<ItemsRelacoesEstabelecidas> = ArrayList(RelacoesEstabelecidasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelacoesEstabelecidasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_relacoes_estabelecidas, parent, false)
        return RelacoesEstabelecidasViewHolder(view)
    }

    override fun onBindViewHolder(holder: RelacoesEstabelecidasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val titulo = currentItem.Titulo
        val descricao = currentItem.Descricao

        holder.holderTituloRelacaoEstabelecida.text = titulo
        holder.holderDescricaoRelacaoEstabelecida.text = descricao
        holder.cardViewRelacoesEstabelecidas.setOnClickListener {
            GlobalVariables.detalhesNumStatus = currentItem.NStatus
            GlobalVariables.detalhesTituloStatus = currentItem.Titulo
            GlobalVariables.detalhesDescricaoStatus = currentItem.Descricao
            GlobalVariables.detalhesEnderecoAnexoStatus = currentItem.EnderecoAnexo
            GlobalVariables.detalhesDataHoraCriacaoStatus = currentItem.DataHoraCriacao

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesRelacaoCliente())
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
                        if (item.Descricao.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsRelacoesEstabelecidas> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class RelacoesEstabelecidasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewRelacoesEstabelecidas: CardView = itemView.findViewById(R.id.CardViewRelacoesEstabelecidas)
        val holderTituloRelacaoEstabelecida: TextView = itemView.findViewById(R.id.tituloRelacaoEstabelecida)
        val holderDescricaoRelacaoEstabelecida: TextView = itemView.findViewById(R.id.descricaoRelacaoEstabelecida)
    }
}