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

class AdapterVagasFull(
    private val mcontext: Context,
    private val VagasTodasList: ArrayList<ItemsVagas>
) :
    RecyclerView.Adapter<AdapterVagasFull.VagasTodasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsVagas> = ArrayList(VagasTodasList)
    private var listaAtual: ArrayList<ItemsVagas> = ArrayList(VagasTodasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VagasTodasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_vagas_todas, parent, false)
        return VagasTodasViewHolder(view)
    }

    override fun onBindViewHolder(holder: VagasTodasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeVaga = currentItem.NomeVaga
        val subtituloVaga = currentItem.Subtitulo
        val intEstadoVaga = currentItem.Estado

        val strEstadoVaga = if (intEstadoVaga == 1) {
            "Disponível"
        } else {
            "Indisponível"
        }

        holder.holderNomeVagaTodas.text = nomeVaga
        holder.holderSubtituloVagaTodas.text = subtituloVaga
        holder.holderEstadoVagaTodas.text = strEstadoVaga
        holder.CardViewVagasTodas.setOnClickListener {
            GlobalVariables.verVagas = false
            GlobalVariables.detalhesNumVaga = currentItem.NVaga
            GlobalVariables.detalhesNomeVaga = currentItem.NomeVaga
            GlobalVariables.detalhesSubtituloVaga = currentItem.Subtitulo
            GlobalVariables.detalhesDescricaoVaga = currentItem.Descricao
            GlobalVariables.detalhesNumLocalidadeVaga = currentItem.NLocalidade
            GlobalVariables.detalhesNumTipoVaga = currentItem.NTipoVaga
            GlobalVariables.detalhesEstadoVaga = currentItem.Estado
            GlobalVariables.detalhesLocalidadeVaga = currentItem.Localidade
            GlobalVariables.detalhesTipoVaga = currentItem.TipoVaga

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesVaga())
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
                listaAtual = results?.values as? ArrayList<ItemsVagas> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class VagasTodasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val CardViewVagasTodas: CardView = itemView.findViewById(R.id.CardViewVagasTodas)
        val holderNomeVagaTodas: TextView = itemView.findViewById(R.id.nomeVagaTodas)
        val holderSubtituloVagaTodas: TextView = itemView.findViewById(R.id.subtituloVagaTodas)
        val holderEstadoVagaTodas: TextView = itemView.findViewById(R.id.estadoVagaTodas)
    }
}